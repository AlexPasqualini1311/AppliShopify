package com.example.applishopify;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class ProductView extends AppCompatActivity {

    protected Product product;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_view);
        Intent intent = getIntent();
        product = (Product) intent.getSerializableExtra("PRODUCT");

        ImageView image = (ImageView) findViewById(R.id.product_view_image);
        Picasso.get()
                .load(product.getUrl())
                .resize(450, 450)
                .centerCrop()
                .into(image);

        TextView title = (TextView) findViewById(R.id.product_view_title);
        title.setText(product.getHandle());

        TextView price = (TextView) findViewById(R.id.product_view_price);
        String priceValue = "Price : "+product.getPrice()+" €";
        price.setText(priceValue);

        TextView description = (TextView) findViewById(R.id.product_view_description);
        description.setText(product.getDescription());

        Button addToCart = (Button) findViewById(R.id.product_view_addToCart);
        addToCart.setTag(product);

    }

    public void addToCart_function(View v){
        Bag.addProduct((Product) v.getTag(), 1);
        TextView productAdded = (TextView) findViewById(R.id.product_view_productAdded);
        productAdded.setVisibility(View.VISIBLE);
    }
}