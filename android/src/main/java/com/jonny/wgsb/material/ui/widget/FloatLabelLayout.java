package com.jonny.wgsb.material.ui.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.jonny.wgsb.material.R;
import com.jonny.wgsb.material.util.CompatUtils;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.view.ViewHelper;
import com.nineoldandroids.view.ViewPropertyAnimator;

@SuppressLint("NewApi")
@SuppressWarnings("unused")
public final class FloatLabelLayout extends FrameLayout {
    private static final long ANIMATION_DURATION = 150;
    private static final float DEFAULT_PADDING_LEFT_RIGHT_DP = 4f;
    private EditText mEditText;
    private TextView mLabel;

    public FloatLabelLayout(Context context) {
        this(context, null);
    }

    public FloatLabelLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FloatLabelLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.FloatLabelLayout);
        final int sidePadding = a.getDimensionPixelSize(R.styleable.FloatLabelLayout_floatLabelSidePadding, dipsToPix(DEFAULT_PADDING_LEFT_RIGHT_DP));
        mLabel = new TextView(context);
        mLabel.setPadding(sidePadding, 0, sidePadding, 0);
        mLabel.setVisibility(INVISIBLE);
        mLabel.setTextAppearance(context, a.getResourceId(R.styleable.FloatLabelLayout_floatLabelTextAppearance, android.R.style.TextAppearance_Small));
        addView(mLabel, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        a.recycle();
    }

    @Override
    public final void addView(@NonNull View child, int index, ViewGroup.LayoutParams params) {
        if (child instanceof EditText) {
            if (mEditText != null)
                throw new IllegalArgumentException("We already have an EditText, can only have one");
            final LayoutParams lp = new LayoutParams(params);
            lp.gravity = Gravity.BOTTOM;
            lp.topMargin = (int) mLabel.getTextSize();
            params = lp;
            setEditText((EditText) child);
        }
        super.addView(child, index, params);
    }

    public EditText getEditText() {
        return mEditText;
    }

    private void setEditText(EditText editText) {
        mEditText = editText;
        mEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                if (TextUtils.isEmpty(s)) {
                    if (mLabel.getVisibility() == View.VISIBLE) hideLabel();
                } else {
                    if (mLabel.getVisibility() != View.VISIBLE) showLabel();
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });
        mEditText.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean focused) {
                if (CompatUtils.isNotLegacyHoneyComb()) mLabel.setActivated(focused);
            }
        });
        mLabel.setText(mEditText.getHint());
    }

    public TextView getLabel() {
        return mLabel;
    }

    private void showLabel() {
        mLabel.setVisibility(View.VISIBLE);
        ViewHelper.setAlpha(mLabel, 0f);
        ViewHelper.setTranslationY(mLabel, mLabel.getHeight());
        ViewPropertyAnimator.animate(mLabel).alpha(1f).translationY(0f).setDuration(ANIMATION_DURATION).setListener(null).start();
    }

    private void hideLabel() {
        ViewHelper.setAlpha(mLabel, 1f);
        ViewHelper.setTranslationY(mLabel, 0f);
        ViewPropertyAnimator.animate(mLabel).alpha(0f).translationY(mLabel.getHeight()).setDuration(ANIMATION_DURATION)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mLabel.setVisibility(View.GONE);
                    }
                }).start();
    }

    private int dipsToPix(float dps) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dps, getResources().getDisplayMetrics());
    }
}