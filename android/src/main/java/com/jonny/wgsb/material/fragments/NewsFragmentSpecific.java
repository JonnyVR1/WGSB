package com.jonny.wgsb.material.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.jonny.wgsb.material.MainActivity;
import com.jonny.wgsb.material.R;
import com.jonny.wgsb.material.db.DatabaseHandler;
import com.jonny.wgsb.material.ui.helper.News;
import com.squareup.picasso.Picasso;

public class NewsFragmentSpecific extends Fragment {
    DatabaseHandler dbhandler;
    TextView titleTextView, storyTextView, dateTextView;
    ImageView storyImageView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_news_specific, container, false);
        int newsId = getArguments().getInt("id");
        titleTextView = (TextView) view.findViewById(R.id.titleArticleNews);
        storyImageView = (ImageView) view.findViewById(R.id.storyNewsImage);
        storyTextView = (TextView) view.findViewById(R.id.storyArticleNews);
        dateTextView = (TextView) view.findViewById(R.id.dateArticleNews);
        dbhandler = DatabaseHandler.getInstance(getActivity());
        News articleNews = dbhandler.getNews(newsId);
        String articleTitle = articleNews.title;
        String articleStory = articleNews.story;
        String articleDate = articleNews.date;
        String imageUrl = articleNews.imageSrc;
        Spanned htmlSpan;
        MainActivity mActivity = ((MainActivity) getActivity());
        mActivity.mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        mActivity.getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_HOME_AS_UP);
        mActivity.getSupportActionBar().setTitle(articleTitle);
        titleTextView.setText(articleTitle);
        dateTextView.setText(articleDate);
        htmlSpan = Html.fromHtml(articleStory);
        storyTextView.setText(htmlSpan);
        storyTextView.setMovementMethod(LinkMovementMethod.getInstance());
        Picasso.with(getActivity()).load(imageUrl).resize(770, 550).into(storyImageView);
        return view;
    }

    @Override
    public void onDetach() {
        ((MainActivity) getActivity()).mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        super.onDetach();
    }
}
