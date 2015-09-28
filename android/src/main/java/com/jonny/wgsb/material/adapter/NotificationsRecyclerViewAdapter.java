package com.jonny.wgsb.material.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jonny.wgsb.material.R;

import java.util.HashMap;
import java.util.List;

public class NotificationsRecyclerViewAdapter extends RecyclerView.Adapter<NotificationsRecyclerViewAdapter.ViewHolder> {
    private final List<HashMap<String, String>> items;
    private final int itemLayout;
    private OnItemClickListener mItemClickListener;
    private OnItemLongClickListener mItemLongClickListener;

    public NotificationsRecyclerViewAdapter(List<HashMap<String, String>> items, int itemLayout) {
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
        holder.readText.setText(item.get("listRead"));
        holder.title.setText(item.get("listTitle"));
        holder.date.setText(item.get("listDate"));
        Integer read = Integer.parseInt(holder.readText.getText().toString());
        if (read == 0)
            holder.title.setTextColor(holder.title.getContext().getResources().getColor(R.color.colorAccent));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }

    public void setOnItemLongClickListener(final OnItemLongClickListener mItemLongClickListener) {
        this.mItemLongClickListener = mItemLongClickListener;
    }

    public interface OnItemClickListener {
        public void onItemClick(View view, int position);
    }

    public interface OnItemLongClickListener {
        public void onItemLongClick(View view, int position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        public final TextView id, readText, title, date;

        public ViewHolder(View view) {
            super(view);
            readText = (TextView) view.findViewById(R.id.readNotification);
            title = (TextView) view.findViewById(R.id.titleNotification);
            date = (TextView) view.findViewById(R.id.dateNotification);
            id = (TextView) view.findViewById(R.id.notificationId);
            view.setOnClickListener(this);
            view.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (mItemClickListener != null) mItemClickListener.onItemClick(v, getAdapterPosition());
        }

        @Override
        public boolean onLongClick(View v) {
            if (mItemLongClickListener != null)
                mItemLongClickListener.onItemLongClick(v, getAdapterPosition());
            return true;
        }
    }
}