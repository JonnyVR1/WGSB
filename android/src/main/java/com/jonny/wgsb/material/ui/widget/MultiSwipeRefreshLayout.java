package com.jonny.wgsb.material.ui.widget;

import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AbsListView;

public class MultiSwipeRefreshLayout extends SwipeRefreshLayout {
    private View mMyScrollableView = null;

    public MultiSwipeRefreshLayout(Context context) {
        super(context);
    }

    public MultiSwipeRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setScrollableView(View view) {
        mMyScrollableView = view;
    }

    @Override
    public boolean canChildScrollUp() {
        if (mMyScrollableView == null) return false;
        if (android.os.Build.VERSION.SDK_INT < 14) {
            if (mMyScrollableView instanceof AbsListView) {
                final AbsListView absListView = (AbsListView) mMyScrollableView;
                return absListView.getChildCount() > 0
                        && (absListView.getFirstVisiblePosition() > 0 || absListView.getChildAt(0)
                        .getTop() < absListView.getPaddingTop());
            } else return mMyScrollableView.getScrollY() > 0;
        } else return ViewCompat.canScrollVertically(mMyScrollableView, -1);
    }
}