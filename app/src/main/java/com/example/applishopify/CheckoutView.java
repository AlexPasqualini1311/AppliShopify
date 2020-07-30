package com.example.applishopify;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Browser;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.shopify.buy3.GraphCallResult;
import com.shopify.buy3.GraphClient;
import com.shopify.buy3.Storefront;
import com.shopify.graphql.support.ID;
import com.squareup.picasso.Picasso;

import java.util.List;

import kotlin.Unit;
import okhttp3.OkHttpClient;

public class CheckoutView extends AppCompatActivity {

    protected String customerAccessToken;
    protected ID checkoutId;
    protected String totalPrice;
    protected MutableLiveData<String> shippingPassed = new MutableLiveData<>();
    protected MutableLiveData<String> webUrl = new MutableLiveData<>();
    public Address customerDefaultAddress;

    protected GraphClient client;
    private static final String SHOP_DOMAIN = "testngrok.myshopify.com";
    private static final String API_KEY = "f988d1db7cbb4fe008520d661238fb92";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout_view);

        Intent intent = getIntent();
        customerAccessToken = intent.getStringExtra("CUSTOMER_ACCESS_TOKEN");
        checkoutId = (ID) intent.getSerializableExtra("CHECKOUT_ID");
        totalPrice = intent.getStringExtra("TOTAL_PRICE");

        client = GraphClient.Companion.build(this, SHOP_DOMAIN, API_KEY,
                builder -> {
                    builder.setHttpClient(new OkHttpClient());
                    return Unit.INSTANCE;
                });
        List<Product> items = Bag.getBag();

        LinearLayout itemsLayout = (LinearLayout) findViewById(R.id.checkout_view_itemsLayout);
        for (Product item : items) {
            LinearLayout product = new LinearLayout(this);
            product.setOrientation(LinearLayout.VERTICAL);


            TextView title = new TextView(this);
            title.setText(item.getHandle());
            title.setTextSize(24);
            product.addView(title);

            TextView quantity = new TextView(this);
            String quantityText = "Quantity : " + item.getQuantity();
            quantity.setText(quantityText);


            product.addView(quantity);
            itemsLayout.addView(product);
        }
        TextView totalPriceView = (TextView) findViewById(R.id.checkout_view_totalPrice);
        String totalPriceText = "Total price : "+ totalPrice + " €";
        totalPriceView.setText(totalPriceText);

        shippingPassed.observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                if (s==null){
                    return;
                }
                TextView shippingAddress = (TextView) findViewById(R.id.checkout_view_shippingAddress);
                shippingAddress.setText(s);
            }
        });

        webUrl.observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                if (s==null){
                    return;
                }
                Button webUrlCheckoutButton = (Button) findViewById(R.id.checkout_view_webCheckout);
                webUrlCheckoutButton.setTag(s);
            }
        });

        webUrl.postValue(intent.getStringExtra("WEB_URL"));
    }

    public void associateCustomer(View v){
        v.setTag(true);
        Storefront.MutationQuery associateCustomer = Storefront.mutation(mutationQuery ->
                mutationQuery.checkoutCustomerAssociateV2(checkoutId, customerAccessToken, checkoutCustomerAssociate ->
                    checkoutCustomerAssociate
                        .checkout(checkout ->
                            checkout
                                .webUrl()
                                .shippingAddress(shippingAddress ->
                                    shippingAddress
                                        .country()
                                        .city()
                                        .address1()
                                        .address2()
                                        .firstName()
                                        .lastName()
                                )
                        )
                        .customer(customer ->
                            customer.defaultAddress(defaultAddress ->
                                defaultAddress
                                    .country()
                                    .city()
                                    .address1()
                                    .address2()
                                    .firstName()
                                    .lastName()
                            )
                        )
                )
        );

        client.mutateGraph(associateCustomer)
                .enqueue(result -> {
                   if (result instanceof GraphCallResult.Success){
                       Storefront.Checkout checkout = ((GraphCallResult.Success<Storefront.Mutation>) result).getResponse().getData().getCheckoutCustomerAssociateV2().getCheckout();
                       webUrl.postValue(checkout.getWebUrl());

                       Storefront.MailingAddress customerAddress = ((GraphCallResult.Success<Storefront.Mutation>) result).getResponse().getData().getCheckoutCustomerAssociateV2().getCustomer().getDefaultAddress();
                       customerDefaultAddress = new Address(
                               customerAddress.getFirstName(),
                               customerAddress.getLastName(),
                               customerAddress.getCountry(),
                               customerAddress.getCity(),
                               customerAddress.getAddress1(),
                               customerAddress.getAddress2()
                       );
                       shippingPassed.postValue(customerDefaultAddress.toString());
                   }

                   return Unit.INSTANCE;
                });
    }

    public void go_to_shipping(View view){
        switch (view.getId()) {
            case R.id.checkout_view_nativeCheckout:
                Intent nativeIntent = new Intent(CheckoutView.this, ShippingView.class);
                nativeIntent.putExtra("DEFAULT_ADDRESS", customerDefaultAddress);
                nativeIntent.putExtra("CHECKOUT_ID", checkoutId);
                startActivity(nativeIntent);
                break;

            case R.id.checkout_view_webCheckout:
                Intent webIntent = new Intent(Intent.ACTION_VIEW,Uri.parse(view.getTag().toString()));
                Bundle bundle = new Bundle();
                bundle.putString("X-Shopify-Customer-Access-Token", customerAccessToken);
                webIntent.putExtra(Browser.EXTRA_HEADERS, bundle);
                startActivity(webIntent);
                break;
        }
    }
}