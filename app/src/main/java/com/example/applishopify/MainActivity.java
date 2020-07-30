package com.example.applishopify;


import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import com.shopify.buy3.GraphCall;
import com.shopify.buy3.GraphCallResult;
import com.shopify.buy3.GraphClient;
import com.shopify.buy3.GraphResponse;
import com.shopify.buy3.QueryGraphCall;
import com.shopify.buy3.Storefront;

import java.util.ArrayList;
import java.util.List;

import kotlin.Unit;
import kotlin.jvm.internal.PropertyReference1;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

public class MainActivity extends AppCompatActivity {

    private static final String SHOP_DOMAIN = "testngrok.myshopify.com";
    private static final String API_KEY = "f988d1db7cbb4fe008520d661238fb92";
    private final OkHttpClient httpClient = new OkHttpClient();

    public GraphClient client;
    protected List<Product>productList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        client = GraphClient.Companion.build(MainActivity.this, SHOP_DOMAIN, API_KEY,
                builder -> {
                    builder.setHttpClient(httpClient);
                    return Unit.INSTANCE;
                });

        products();

        MyApp.setContext(this);

    }

    public void accessToCart(View view){
        Intent intent = new Intent(this, CartView.class);
        startActivity(intent);
    }

    public void products(){
        Storefront.QueryRootQuery productConnection = Storefront.query(queryRoot ->
                queryRoot.products(arg -> arg.first(10), productEdges ->
                        productEdges.edges(productNode ->
                                productNode.node(product ->
                                        product.title()
                                        .description()
                                        .variants(arg -> arg.first(1), variantEdges ->
                                                variantEdges.edges(variantNode ->
                                                        variantNode.node(variant ->
                                                                variant.title()
                                                        )
                                                )
                                        )
                                        .images(arg -> arg.first(1), imageEdges ->
                                                imageEdges.edges(imageNode ->
                                                        imageNode.node(image ->
                                                                image.transformedSrc()
                                                        )
                                                )
                                        )
                                        .priceRange(priceRange ->
                                                priceRange.maxVariantPrice(maxPrice ->
                                                        maxPrice.amount()))
                                )
                        )
                )
        );

        QueryGraphCall call = client.queryGraph(productConnection)
                .enqueue(new Handler(), result -> {
                    if (result instanceof GraphCallResult.Success){
                        Storefront.ProductConnection products = ((GraphCallResult.Success<Storefront.QueryRoot>) result).getResponse().getData().getProducts();
                        for (Storefront.ProductEdge productEdge : products.getEdges()) {
                            Storefront.Product product = productEdge.getNode();
                            List<Storefront.ImageEdge> imageEdges = product.getImages().getEdges();
                            productList.add(new Product(
                                    product.getTitle(),
                                    product.getVariants().getEdges().get(0).getNode().getId(),
                                    product.getDescription(),
                                    imageEdges.get(0).getNode().getTransformedSrc(),
                                    product.getPriceRange().getMaxVariantPrice().getAmount()
                            ));
                        }
                        ListView list = (ListView) findViewById(R.id.listProducts);
                        ProductAdapter adapter = new ProductAdapter(MainActivity.this, productList);
                        list.setAdapter(adapter);
                    }
                    if (result instanceof GraphCallResult.Failure){
                        Log.i("First_call", ((GraphCallResult.Failure) result).getError().toString());
                    }
                    return Unit.INSTANCE;
                });
        return;
    }
}