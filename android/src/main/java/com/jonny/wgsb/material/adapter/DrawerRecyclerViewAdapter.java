package com.jonny.wgsb.material.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.jonny.wgsb.material.R;
import com.jonny.wgsb.material.ui.helper.Icons;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class DrawerRecyclerViewAdapter extends RecyclerView.Adapter<DrawerRecyclerViewAdapter.ViewHolder> {

    private ArrayList<Icons> items;
    private int itemLayout;

    public DrawerRecyclerViewAdapter(ArrayList<Icons> items, int itemLayout) {
        this.items = items;
        this.itemLayout = itemLayout;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(itemLayout, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final Icons item = items.get(position);
        holder.itemView.setTag(item);
        holder.text.setText(item.title);
        Picasso.with(holder.image.getContext()).load(item.icon).into(holder.image);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView image;
        public TextView text;

        public ViewHolder(View itemView) {
            super(itemView);
            image = (ImageView) itemView.findViewById(R.id.drawerIcon);
            text = (TextView) itemView.findViewById(R.id.drawerText);
        }
    }
}
