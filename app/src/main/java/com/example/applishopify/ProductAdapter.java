package com.example.applishopify;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.Serializable;
import java.util.List;

public class ProductAdapter extends ArrayAdapter<Product> {

    public ProductAdapter(Context context, List<Product> products) {
        super(context, 0, products);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if(convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.row_product,parent, false);
        }

        TweetViewHolder viewHolder = (TweetViewHolder) convertView.getTag();
        if(viewHolder == null){
            viewHolder = new TweetViewHolder();
            viewHolder.pseudo = (TextView) convertView.findViewById(R.id.title);
            viewHolder.avatar = (ImageView) convertView.findViewById(R.id.productImage);
            convertView.setTag(viewHolder);
        }

        //getItem(position) va récupérer l'item [position] de la List<Tweet> tweets
        Product product = getItem(position);

        //il ne reste plus qu'à remplir notre vue
        viewHolder.pseudo.setText(product.getHandle());
        Picasso.get()
                .load(product.getUrl())
                .resize(450, 450)
                .centerCrop()
                .into(viewHolder.avatar);
        viewHolder.avatar.setTag(product);
        viewHolder.avatar.setOnClickListener(new  View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MyApp.getContext(), ProductView.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("PRODUCT", (Serializable) v.getTag());
                MyApp.getContext().startActivity(intent);
            }
        });
        return convertView;
    }

    private class TweetViewHolder{
        public TextView pseudo;
        public ImageView avatar;
    }
}
