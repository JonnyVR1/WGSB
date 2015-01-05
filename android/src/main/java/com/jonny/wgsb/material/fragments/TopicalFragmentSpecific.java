package com.jonny.wgsb.material.fragments;

import android.annotation.SuppressLint;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.github.ksoichiro.android.observablescrollview.ObservableScrollView;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
import com.github.ksoichiro.android.observablescrollview.ScrollState;
import com.jonny.wgsb.material.R;
import com.jonny.wgsb.material.activities.MainActivity;
import com.jonny.wgsb.material.db.DatabaseHandler;
import com.jonny.wgsb.material.ui.helper.Topical;
import com.jonny.wgsb.material.util.CompatUtils;
import com.nineoldandroids.animation.ValueAnimator;
import com.nineoldandroids.view.ViewHelper;
import com.nineoldandroids.view.ViewPropertyAnimator;

@SuppressLint("NewApi")
@SuppressWarnings("deprecation")
public class TopicalFragmentSpecific extends Fragment implements ObservableScrollViewCallbacks {
    private static TopicalFragmentSpecific instance = null;
    private MainActivity mActivity;
    private FrameLayout.LayoutParams originalParams;
    private FrameLayout frame;
    private View mImageHolder, mHeader, mHeaderBar, mHeaderBackground;
    private ObservableScrollView mScrollView;
    private int mActionBarSize, mFlexibleSpaceImageHeight, mIntersectionHeight, mPrevScrollY;
    private boolean mGapIsChanging, mGapHidden;

    public TopicalFragmentSpecific() {
    }

    public static TopicalFragmentSpecific getInstance() {
        if (instance == null) instance = new TopicalFragmentSpecific();
        return instance;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_topical_specific, container, false);
        setRetainInstance(true);
        int topicalId = getArguments().getInt("id");
        mActivity = ((MainActivity) getActivity());
        mActivity.mToolbar.setVisibility(Toolbar.GONE);
        mActivity.getSupportActionBar().hide();
        frame = (FrameLayout) getActivity().findViewById(R.id.fragment_container);
        originalParams = (FrameLayout.LayoutParams) frame.getLayoutParams();
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        frame.setLayoutParams(params);
        ((MainActivity) getActivity()).setSupportActionBar((Toolbar) view.findViewById(R.id.toolbar));
        TextView titleTextView = (TextView) view.findViewById(R.id.titleArticleTopical);
        TextView storyTextView = (TextView) view.findViewById(R.id.storyArticleTopical);
        DatabaseHandler dbhandler = DatabaseHandler.getInstance(getActivity());
        Topical articleTopical = dbhandler.getTopical(topicalId);
        String articleTitle = articleTopical.title;
        String articleStory = articleTopical.story;
        Spanned htmlSpan;
        mFlexibleSpaceImageHeight = getResources().getDimensionPixelSize(R.dimen.flexible_space_image_height);
        mIntersectionHeight = getResources().getDimensionPixelSize(R.dimen.intersection_height);
        mActionBarSize = getActionBarSize();
        mImageHolder = view.findViewById(R.id.image_holder);
        mHeader = view.findViewById(R.id.header);
        mHeaderBar = view.findViewById(R.id.header_bar);
        mHeaderBackground = view.findViewById(R.id.header_background);
        mScrollView = (ObservableScrollView) view.findViewById(R.id.scroll);
        mScrollView.setScrollViewCallbacks(this);
        mActivity.mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        mActivity.mDrawerToggle.setDrawerIndicatorEnabled(false);
        mActivity.getSupportActionBar().setHomeButtonEnabled(true);
        mActivity.getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_HOME_AS_UP);
        mActivity.mDrawerToggle.setToolbarNavigationClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mActivity.getSupportFragmentManager().popBackStack();
                mActivity.mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                mActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                mActivity.getSupportActionBar().setDisplayShowTitleEnabled(true);
                mActivity.mDrawerToggle.setDrawerIndicatorEnabled(true);
            }
        });
        titleTextView.setText(articleTitle);
        htmlSpan = Html.fromHtml(articleStory);
        storyTextView.setText(htmlSpan);
        storyTextView.setMovementMethod(LinkMovementMethod.getInstance());
        ViewTreeObserver vto = mScrollView.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (CompatUtils.isNotLegacyJellyBean()) {
                    mScrollView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                } else {
                    mScrollView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
                onScrollChanged(0, false, false);
            }
        });
        return view;
    }

    @Override
    public void onDestroyView() {
        mActivity.mToolbar.setVisibility(Toolbar.VISIBLE);
        frame.setLayoutParams(originalParams);
        mActivity.mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        mActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        mActivity.getSupportActionBar().setDisplayShowTitleEnabled(true);
        mActivity.mDrawerToggle.setDrawerIndicatorEnabled(true);
        super.onDestroyView();
    }

    @Override
    public void onScrollChanged(int scrollY, boolean firstScroll, boolean dragging) {
        ViewHelper.setTranslationY(mImageHolder, -scrollY / 2);
        ViewHelper.setTranslationY(mHeader, getHeaderTranslationY(scrollY));
        final int headerHeight = mHeaderBar.getHeight();
        boolean scrollUp = mPrevScrollY < scrollY;
        if (scrollUp) {
            if (mFlexibleSpaceImageHeight - headerHeight - mActionBarSize <= scrollY) {
                changeHeaderBackgroundHeight(false);
            }
        } else {
            if (scrollY <= mFlexibleSpaceImageHeight - headerHeight - mActionBarSize) {
                changeHeaderBackgroundHeight(true);
            }
        }
        mPrevScrollY = scrollY;
    }

    @Override
    public void onDownMotionEvent() {
    }

    @Override
    public void onUpOrCancelMotionEvent(ScrollState scrollState) {
    }

    private float getHeaderTranslationY(int scrollY) {
        final int headerHeight = mHeaderBar.getHeight();
        int headerTranslationY = mActionBarSize - mIntersectionHeight;
        if (0 <= -scrollY + mFlexibleSpaceImageHeight - headerHeight - mActionBarSize + mIntersectionHeight) {
            headerTranslationY = -scrollY + mFlexibleSpaceImageHeight - headerHeight;
        }
        return headerTranslationY;
    }

    private void changeHeaderBackgroundHeight(boolean shouldShowGap) {
        if (mGapIsChanging) return;
        final int heightOnGapShown = mHeaderBar.getHeight();
        final int heightOnGapHidden = mHeaderBar.getHeight() + mActionBarSize;
        final float from = mHeaderBackground.getLayoutParams().height;
        final float to;
        if (shouldShowGap) {
            if (!mGapHidden) return;
            to = heightOnGapShown;
        } else {
            if (mGapHidden) return;
            to = heightOnGapHidden;
        }
        ViewPropertyAnimator.animate(mHeaderBackground).cancel();
        ValueAnimator a = ValueAnimator.ofFloat(from, to);
        a.setDuration(100);
        a.setInterpolator(new AccelerateDecelerateInterpolator());
        a.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float height = (float) animation.getAnimatedValue();
                FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) mHeaderBackground.getLayoutParams();
                lp.height = (int) height;
                lp.topMargin = (int) (mHeaderBar.getHeight() - height);
                mHeaderBackground.requestLayout();
                mGapIsChanging = (height != to);
                if (!mGapIsChanging) mGapHidden = (height == heightOnGapHidden);
            }
        });
        a.start();
    }

    private int getActionBarSize() {
        TypedValue typedValue = new TypedValue();
        int[] textSizeAttr = new int[]{R.attr.actionBarSize};
        int indexOfAttrTextSize = 0;
        TypedArray a = mActivity.obtainStyledAttributes(typedValue.data, textSizeAttr);
        int actionBarSize = a.getDimensionPixelSize(indexOfAttrTextSize, -1);
        a.recycle();
        return actionBarSize;
    }
}