package com.example.applishopify;

import com.shopify.graphql.support.ID;

import java.io.Serializable;

public class Product implements Serializable {
    private String handle;
    private ID id;
    private String description;
    private String imageURL;
    private String price;
    private int quantity=0;

    public Product(String title, ID id, String description, String url, String price){
        this.handle = title;
        this.id = id;
        this.description = description;
        this.imageURL = url;
        this.price = price;
    }

    public String getHandle(){
        return handle;
    }

    public ID getId(){
        return  id;
    }

    public  String getDescription(){
        return description;
    }

    public String getUrl(){
        return imageURL;
    }

    public String getPrice() { return price; }

    public int getQuantity(){ return quantity; }

    public void setQuantity(int quantity){
        this.quantity = quantity;
    }

    public void addQuantity (int quantity){
        this.quantity+= quantity;
    }
}