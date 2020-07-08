package com.example.applishopify;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.shopify.buy3.GraphCallResult;
import com.shopify.buy3.GraphClient;
import com.shopify.buy3.Storefront;
import com.shopify.graphql.support.ID;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import kotlin.Unit;
import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ShippingView extends AppCompatActivity {

    protected Address defaultAddress;
    protected ID checkoutId;
    protected GraphClient client;
    protected OkHttpClient httpClient = new OkHttpClient();
    private static final String SHOP_DOMAIN = "testngrok.myshopify.com";
    private static final String API_KEY = "f988d1db7cbb4fe008520d661238fb92";

    protected Storefront.MailingAddressInput addressInput = new Storefront.MailingAddressInput();
    protected MutableLiveData<String> vaultId = new MutableLiveData<>();
    protected MutableLiveData<String> paymentSuccess = new MutableLiveData<>();
    protected String totalAmount;
    protected Storefront.CurrencyCode currencyCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shipping_view);

        client = GraphClient.Companion.build(this, SHOP_DOMAIN, API_KEY,
                builder -> {
                    builder.setHttpClient(httpClient);
                    return Unit.INSTANCE;
                });

        Intent intent = getIntent();
        defaultAddress = (Address) intent.getSerializableExtra("DEFAULT_ADDRESS");
        checkoutId = (ID) intent.getSerializableExtra("CHECKOUT_ID");

        if (defaultAddress != null) {
            TextView defaultFname = (TextView) findViewById(R.id.address_defaultFname);
            TextView defaultLname = (TextView) findViewById(R.id.address_defaultLname);
            TextView defaultCountry = (TextView) findViewById(R.id.address_defaultCountry);
            TextView defaultCity = (TextView) findViewById(R.id.address_defaultCity);
            TextView defaultLine1 = (TextView) findViewById(R.id.address_defaultLine1);
            TextView defaultLine2 = (TextView) findViewById(R.id.address_defaultLine2);

            defaultFname.setText(defaultAddress.getFname());
            defaultLname.setText(defaultAddress.getLname());
            defaultCountry.setText(defaultAddress.getCountry());
            defaultCity.setText(defaultAddress.getCity());
            defaultLine1.setText(defaultAddress.getAddress1());
            defaultLine2.setText(defaultAddress.getAddress2());
        }

        vaultId.observe(this, new Observer<String>() {
            @Override
            public void onChanged(String id) {
                completeCheckout(id);
            }
        });

        paymentSuccess.observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                TextView success = (TextView) findViewById(R.id.address_addressAddedText);
                success.setText(s);
            }
        });
    }

    public void AddressValidate(View v){
        switch (v.getId()){
            case R.id.address_DefaultAddressLayout :
                addressInput.setAddress1(defaultAddress.getAddress1());
                addressInput.setAddress2(defaultAddress.getAddress2());
                addressInput.setCity(defaultAddress.getCity());
                addressInput.setCountry(defaultAddress.getCountry());
                addressInput.setFirstName(defaultAddress.getFname());
                addressInput.setLastName(defaultAddress.getLname());
                break;
            case R.id.address_submitNewAddress :
                EditText newFname = (EditText) findViewById(R.id.address_newFname);
                EditText newLname = (EditText) findViewById(R.id.address_newLname);
                EditText newCountry = (EditText) findViewById(R.id.address_newCountry);
                EditText newCity = (EditText) findViewById(R.id.address_newCity);
                EditText newLine1 = (EditText) findViewById(R.id.address_newLine1);
                EditText newLine2 = (EditText) findViewById(R.id.address_newLine2);

                addressInput.setAddress1(newLine1.getText().toString());
                addressInput.setAddress2(newLine2.getText().toString());
                addressInput.setCity(newCity.getText().toString());
                addressInput.setCountry(newCountry.getText().toString());
                addressInput.setFirstName(newFname.getText().toString());
                addressInput.setLastName(newLname.getText().toString());
                break;
        }
        queryVault(addressInput);
    }

    protected void queryVault(Storefront.MailingAddressInput addressInput){
        Storefront.MutationQuery setAddress = Storefront.mutation(mutationQuery ->
                mutationQuery.checkoutShippingAddressUpdateV2(addressInput, checkoutId, shippingUpdate ->
                        shippingUpdate.checkout(checkout ->
                                checkout.email()
                                .totalPriceV2(total ->
                                        total.amount()
                                        .currencyCode()
                                )
                        )
                )
        );

        client.mutateGraph(setAddress)
                .enqueue(result -> {
                    if (result instanceof GraphCallResult.Success){
                        currencyCode = ((GraphCallResult.Success<Storefront.Mutation>) result).getResponse().getData().getCheckoutShippingAddressUpdateV2().getCheckout().getTotalPriceV2().getCurrencyCode();
                        totalAmount = ((GraphCallResult.Success<Storefront.Mutation>) result).getResponse().getData().getCheckoutShippingAddressUpdateV2().getCheckout().getTotalPriceV2().getAmount();
                        String requestBody = "{ \n" +
                                "\"credit_card\": { \n" +
                                "\"number\": \"1\",\n" +
                                "\"first_name\": \"John\",\n" +
                                "\"last_name\": \"Smith\",\n" +
                                "\"month\": \"5\",\n" +
                                "\"year\": \"15\",\n" +
                                "\"verification_value\": \"123\"\n" +
                                "}\n" +
                                "}";
                        Request request =new Request.Builder()
                                .url("https://elb.deposit.shopifycs.com/sessions")
                                .post(RequestBody.create(MediaType.parse("application/json; charset=utf-8"), requestBody))
                                .build();
                        Call call = httpClient.newCall(request);
                        Response response = null;
                        try {
                            response = call.execute();
                            String responseStr = response.body().string();
                            try{
                                vaultId.postValue(new JSONObject(responseStr).getString("id"));

                            } catch (JSONException e){
                                e.printStackTrace();
                            }
                        } catch (IOException e){
                            e.printStackTrace();
                        }

                    }
                    return Unit.INSTANCE;
                });
    }

    protected void vault(View v){
        queryVault(null);
    }

    protected void completeCheckout(String id){
        Storefront.MoneyInput amount = new Storefront.MoneyInput(totalAmount, currencyCode);
        Storefront.CreditCardPaymentInputV2 creditCardPaymentInput= new Storefront.CreditCardPaymentInputV2(amount, "aRandomString", addressInput, id);

        Storefront.MutationQuery completeCheckout = Storefront.mutation(mutationQuery ->
                mutationQuery.checkoutCompleteWithCreditCardV2(checkoutId, creditCardPaymentInput, creditCard ->
                        creditCard.payment(payment ->
                                payment.ready()
                        )
                )
        );

        client.mutateGraph(completeCheckout)
                .enqueue(result -> {
                   if (result instanceof GraphCallResult.Success){
                       if (((GraphCallResult.Success<Storefront.Mutation>) result).getResponse().getErrors().get(0).message() != null) {
                         paymentSuccess.postValue(((GraphCallResult.Success<Storefront.Mutation>) result).getResponse().getErrors().get(0).message());
                       } else {
                           ID paymentId = ((GraphCallResult.Success<Storefront.Mutation>) result).getResponse().getData().getCheckoutCompleteWithCreditCardV2().getPayment().getId();
                           boolean paymentReady = ((GraphCallResult.Success<Storefront.Mutation>) result).getResponse().getData().getCheckoutCompleteWithCreditCardV2().getPayment().getReady();
                           paymentSuccess.postValue("paymentaccepted ! \n" +
                                   "payment id : "+ paymentId + "\n" +
                                   "payment is ready ? : "+ paymentReady);
                       }
                   } else{
                       paymentSuccess.postValue(((GraphCallResult.Failure) result).getError().getMessage());
                   }
                   return Unit.INSTANCE;
                });
    }
}