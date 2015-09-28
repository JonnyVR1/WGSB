package com.jonny.wgsb.material.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jonny.wgsb.material.R;

import java.util.HashMap;
import java.util.List;

public class TopicalRecyclerViewAdapter extends RecyclerView.Adapter<TopicalRecyclerViewAdapter.ViewHolder> {
    private final List<HashMap<String, String>> items;
    private final int itemLayout;
    private OnItemClickListener mItemClickListener;

    public TopicalRecyclerViewAdapter(List<HashMap<String, String>> items, int itemLayout) {
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
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public final TextView id, title;

        public ViewHolder(View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.titleTopical);
            id = (TextView) view.findViewById(R.id.topicalId);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (mItemClickListener != null) mItemClickListener.onItemClick(v, getAdapterPosition());
        }
    }
}