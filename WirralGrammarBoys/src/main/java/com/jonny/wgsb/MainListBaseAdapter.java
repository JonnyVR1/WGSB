package com.jonny.wgsb;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

class MainListBaseAdapter extends BaseAdapter {
    private static ArrayList<MainDetails> mainDetailArrayList;
    final private Integer[] imgId;
    final private LayoutInflater l_Inflater;

    MainListBaseAdapter(Context paramContext, ArrayList<MainDetails> paramArrayList) {
        Integer[] arrayOfInteger = new Integer[5];
        arrayOfInteger[0] = R.drawable.news;
        arrayOfInteger[1] = R.drawable.topical_information;
        arrayOfInteger[2] = R.drawable.timetable;
        arrayOfInteger[3] = R.drawable.calendar;
        imgId = arrayOfInteger;
        mainDetailArrayList = paramArrayList;
        l_Inflater = LayoutInflater.from(paramContext);
    }

    public int getCount() {
        return mainDetailArrayList.size();
    }

    public Object getItem(int paramInt) {
        return mainDetailArrayList.get(paramInt);
    }

    public long getItemId(int paramInt) {
        return paramInt;
    }

    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder viewholder;
        if (view != null) {
            viewholder = (ViewHolder) view.getTag();
        } else {
            view = l_Inflater.inflate(R.layout.list_main, viewGroup, false);
            viewholder = new ViewHolder();
            viewholder.itemImage = ((ImageView) view.findViewById(R.id.list_item_img));
            viewholder.txt_itemName = ((TextView) view.findViewById(R.id.list_item));
            view.setTag(viewholder);
        }
        viewholder.itemImage.setImageResource(imgId[(-1 + (mainDetailArrayList.get(i)).getImageNumber())]);
        viewholder.txt_itemName.setText((mainDetailArrayList.get(i)).getName());
        return view;
    }

    static class ViewHolder {
        ImageView itemImage;
        TextView txt_itemName;
    }
}