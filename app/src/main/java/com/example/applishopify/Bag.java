package com.example.applishopify;

import com.shopify.buy3.Storefront;
import com.shopify.graphql.support.ID;

import java.util.ArrayList;
import java.util.List;

public class Bag {

    private static List<Storefront.CheckoutLineItemInput> items = new ArrayList<>();
    private static List<Product> productsInTheBag = new ArrayList<>();

    public static void addProduct(Product product, int quantity){
        for (Storefront.CheckoutLineItemInput item : items){
            if (item.getVariantId().equals(product.getId())){
                productsInTheBag.get(items.indexOf(item)).addQuantity(quantity);
                item.setQuantity(item.getQuantity()+quantity);
                return;
            }
        }
        product.addQuantity(quantity);
        productsInTheBag.add(product);
        items.add(new Storefront.CheckoutLineItemInput(quantity, product.getId()));
    }

    public static List<Storefront.CheckoutLineItemInput> getBagForCheckout(){
        return items;
    }

    public static List<Product> getBag(){
        return productsInTheBag;
    }

    public static void erase(String handle){
        for (Product item : productsInTheBag){
            if (item.getHandle().equals(handle)){
                items.remove(productsInTheBag.indexOf(item));
                productsInTheBag.remove(item);
                return;
            }
        }
    }
}
