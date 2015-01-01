package com.jonny.wgsb.material.adapter;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.jonny.wgsb.material.R;
import com.jonny.wgsb.material.ui.PaletteTransformation;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;

public class NewsRecyclerViewAdapter extends RecyclerView.Adapter<NewsRecyclerViewAdapter.ViewHolder> {
    private final List<HashMap<String, String>> items;
    private final int itemLayout;
    private OnItemClickListener mItemClickListener;

    public NewsRecyclerViewAdapter(List<HashMap<String, String>> items, int itemLayout) {
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
        final HashMap<String, String> item = items.get(position);
        holder.itemView.setTag(item);
        holder.id.setText(item.get("listID"));
        holder.title.setText(item.get("listTitle"));
        holder.date.setText(item.get("listDate"));
        Picasso.with(holder.image.getContext())
                .load(item.get("listURL"))
                .transform(PaletteTransformation.instance())
                .into(holder.image, new Callback.EmptyCallback() {
                    @Override
                    public void onSuccess() {
                        Bitmap bitmap = ((BitmapDrawable) holder.image.getDrawable()).getBitmap();
                        Palette palette = PaletteTransformation.getPalette(bitmap);
                        Palette.Swatch vibrantSwatch = palette.getVibrantSwatch();
                        if (vibrantSwatch != null) {
                            holder.card.setCardBackgroundColor(vibrantSwatch.getRgb());
                            holder.title.setTextColor(vibrantSwatch.getTitleTextColor());
                            holder.date.setTextColor(vibrantSwatch.getBodyTextColor());
                        }
                    }
                });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }

    public interface OnItemClickListener {
        public void onItemClick(View view, int position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public final CardView card;
        public final ImageView image;
        public final TextView id, title, date;

        public ViewHolder(View view) {
            super(view);
            card = (CardView) view.findViewById(R.id.card_view);
            image = (ImageView) view.findViewById(R.id.imageNews);
            title = (TextView) view.findViewById(R.id.titleNews);
            id = (TextView) view.findViewById(R.id.newsId);
            date = (TextView) view.findViewById(R.id.dateNews);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (mItemClickListener != null) mItemClickListener.onItemClick(v, getPosition());
        }
    }
}