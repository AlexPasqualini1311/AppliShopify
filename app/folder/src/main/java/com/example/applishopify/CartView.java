package com.example.applishopify;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.shopify.buy3.GraphCall;
import com.shopify.buy3.GraphCallResult;
import com.shopify.buy3.GraphClient;
import com.shopify.buy3.Storefront;
import com.shopify.graphql.support.ID;
import com.shopify.graphql.support.Input;
import com.squareup.picasso.Picasso;

import java.util.List;

import kotlin.Unit;
import okhttp3.OkHttpClient;

public class CartView extends AppCompatActivity {

    protected GraphClient client;
    protected String customerAccessToken;
    private static final String SHOP_DOMAIN = "testngrok.myshopify.com";
    private static final String API_KEY = "f988d1db7cbb4fe008520d661238fb92";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart_view);
        client = GraphClient.Companion.build(this, SHOP_DOMAIN, API_KEY,
                builder -> {
                    builder.setHttpClient(new OkHttpClient());
                    return Unit.INSTANCE;
                });

        Intent intent = getIntent();

        List<Product> items = Bag.getBag();
        LinearLayout globalLayout = (LinearLayout) findViewById(R.id.cart_view_globalLayout);
        LinearLayout buttonLayout = (LinearLayout) findViewById(R.id.cart_view_buttonLayout);
        LinearLayout productLayout = (LinearLayout) findViewById(R.id.cart_view_productLayout);
        if (items.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText("Your cart is empty :'(");
            globalLayout.addView(empty);
            buttonLayout.setVisibility(View.GONE);
            productLayout.setVisibility(View.GONE);

        } else {
            for (Product item : items) {
                LinearLayout product = new LinearLayout(this);
                product.setOrientation(LinearLayout.VERTICAL);

                ImageView image = new ImageView(this);
                Picasso.get()
                        .load(item.getUrl())
                        .resize(250, 250)
                        .centerCrop()
                        .into(image);
                product.addView(image);

                TextView title = new TextView(this);
                title.setText(item.getHandle());
                product.addView(title);

                TextView quantity = new TextView(this);
                String quantityText = "Quantity : " + item.getQuantity();
                quantity.setText(quantityText);

                LinearLayout line = new LinearLayout(this);
                line.setOrientation(LinearLayout.HORIZONTAL);
                line.addView(quantity);

                Button erase = new Button(this);
                erase.setText("delete product");
                erase.setTag(item.getHandle());
                erase.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        Bag.erase((String) v.getTag());
                        finish();
                        overridePendingTransition(0, 0);
                        startActivity(getIntent());
                        overridePendingTransition(0, 0);
                    }
                });

                line.addView(erase);
                product.addView(line);
                productLayout.addView(product);
            }
        }
    }

    public void login(View view){
        Storefront.CustomerAccessTokenCreateInput input = new Storefront.CustomerAccessTokenCreateInput("exemple2@email.com", "password");

        Storefront.MutationQuery accessToken = Storefront.mutation(mutationQuery ->
                mutationQuery.customerAccessTokenCreate(input, customerAccessTokenCreate ->
                        customerAccessTokenCreate.customerAccessToken(token ->
                                token.accessToken()
                        )
                )
        );

        client.mutateGraph(accessToken)
                .enqueue(result -> {
                    if (result instanceof GraphCallResult.Success){
                        customerAccessToken = ((GraphCallResult.Success<Storefront.Mutation>) result).getResponse().getData().getCustomerAccessTokenCreate().getCustomerAccessToken().getAccessToken();

                        TextView tokenView = (TextView) findViewById(R.id.cart_view_accessToken);
                        String tokenText = "Your access token is : "+customerAccessToken;
                        tokenView.setText(tokenText);
                    }
                    return Unit.INSTANCE;
                });

    }

    public void go_to_checkout(View view){
        Storefront.CheckoutCreateInput input = new Storefront.CheckoutCreateInput()
                .setLineItemsInput(Input.value(Bag.getBagForCheckout()));

        Storefront.MutationQuery checkoutQuery = Storefront.mutation(mutationQuery -> mutationQuery
                .checkoutCreate(input, createPayloadQuery -> createPayloadQuery
                        .checkout(checkoutquery -> checkoutquery
                                .webUrl()
                                .totalPriceV2(totalPrice ->
                                        totalPrice.amount())
                        )
                        .userErrors(userErrorQuery -> userErrorQuery
                                .field()
                                .message()
                        )
                )
        );

        client.mutateGraph(checkoutQuery)
                .enqueue(result -> {
                   if (result instanceof GraphCallResult.Success){
                       ID checkoutId = ((GraphCallResult.Success<Storefront.Mutation>) result).getResponse().getData().getCheckoutCreate().getCheckout().getId();
                       String totalPrice = ((GraphCallResult.Success<Storefront.Mutation>) result).getResponse().getData().getCheckoutCreate().getCheckout().getTotalPriceV2().getAmount();
                       String webUrl = ((GraphCallResult.Success<Storefront.Mutation>) result).getResponse().getData().getCheckoutCreate().getCheckout().getWebUrl();
                       checkout(checkoutId, totalPrice, webUrl);
                   }
                   return Unit.INSTANCE;
                });
    }

    public void checkout(ID checkoutId, String totalPrice, String webUrl){
        Intent intent = new Intent(CartView.this, CheckoutView.class);
        intent.putExtra("CHECKOUT_ID", checkoutId);
        intent.putExtra("TOTAL_PRICE", totalPrice);
        intent.putExtra("WEB_URL", webUrl);
        intent.putExtra("CUSTOMER_ACCESS_TOKEN", customerAccessToken);
        startActivity(intent);
    }
}