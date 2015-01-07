package com.jonny.wgsb.material.ui.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListenerAdapter;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jonny.wgsb.material.R;
import com.jonny.wgsb.material.util.CompatUtils;

@SuppressLint("InlinedApi")
@SuppressWarnings("unused")
public class FloatLabelLayout extends LinearLayout {
    private static final long ANIMATION_DURATION = 150;
    private static final float DEFAULT_LABEL_PADDING_LEFT = 3f;
    private static final float DEFAULT_LABEL_PADDING_TOP = 4f;
    private static final float DEFAULT_LABEL_PADDING_RIGHT = 3f;
    private static final float DEFAULT_LABEL_PADDING_BOTTOM = 4f;
    private EditText mEditText;
    private TextView mLabel;
    private CharSequence mHint;
    private Interpolator mInterpolator;

    public FloatLabelLayout(Context context) {
        this(context, null);
    }

    public FloatLabelLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOrientation(VERTICAL);
        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.FloatLabelLayout);
        int leftPadding = a.getDimensionPixelSize(
                R.styleable.FloatLabelLayout_floatLabelPaddingLeft, dipsToPix(DEFAULT_LABEL_PADDING_LEFT));
        int topPadding = a.getDimensionPixelSize(
                R.styleable.FloatLabelLayout_floatLabelPaddingTop, dipsToPix(DEFAULT_LABEL_PADDING_TOP));
        int rightPadding = a.getDimensionPixelSize(
                R.styleable.FloatLabelLayout_floatLabelPaddingRight, dipsToPix(DEFAULT_LABEL_PADDING_RIGHT));
        int bottomPadding = a.getDimensionPixelSize(
                R.styleable.FloatLabelLayout_floatLabelPaddingBottom, dipsToPix(DEFAULT_LABEL_PADDING_BOTTOM));
        mHint = a.getText(R.styleable.FloatLabelLayout_floatLabelHint);
        mLabel = new TextView(context);
        mLabel.setPadding(leftPadding, topPadding, rightPadding, bottomPadding);
        mLabel.setVisibility(INVISIBLE);
        mLabel.setText(mHint);
        ViewCompat.setPivotX(mLabel, 0f);
        ViewCompat.setPivotY(mLabel, 0f);
        mLabel.setTextAppearance(context, a.getResourceId(R.styleable.FloatLabelLayout_floatLabelTextAppearance,
                android.R.style.TextAppearance_Small));
        a.recycle();
        addView(mLabel, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        mInterpolator = AnimationUtils.loadInterpolator(context, CompatUtils.isNotLegacyLollipop()
                ? android.R.interpolator.fast_out_slow_in : android.R.anim.decelerate_interpolator);
    }

    @Override
    public final void addView(@NonNull View child, int index, ViewGroup.LayoutParams params) {
        if (child instanceof EditText) setEditText((EditText) child);
        super.addView(child, index, params);
    }

    private void updateLabelVisibility(boolean animate) {
        boolean hasText = !TextUtils.isEmpty(mEditText.getText());
        boolean isFocused = mEditText.isFocused();
        if (CompatUtils.isNotLegacyApi11()) mLabel.setActivated(isFocused);
        else mLabel.setSelected(isFocused);
        if (hasText || isFocused) {
            if (mLabel.getVisibility() != VISIBLE) showLabel(animate);
        } else {
            if (mLabel.getVisibility() == VISIBLE) hideLabel(animate);
        }
    }

    public EditText getEditText() {
        return mEditText;
    }

    private void setEditText(EditText editText) {
        if (mEditText != null)
            throw new IllegalArgumentException("We already have an EditText, can only have one");
        mEditText = editText;
        updateLabelVisibility(false);
        mEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                updateLabelVisibility(true);
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
                updateLabelVisibility(true);
            }
        });
        if (TextUtils.isEmpty(mHint)) setHint(mEditText.getHint());
    }

    public TextView getLabel() {
        return mLabel;
    }

    public void setHint(CharSequence hint) {
        mHint = hint;
        mLabel.setText(hint);
    }

    private void showLabel(boolean animate) {
        if (animate) {
            mLabel.setVisibility(View.VISIBLE);
            ViewCompat.setTranslationY(mLabel, mLabel.getHeight());
            float scale = mEditText.getTextSize() / mLabel.getTextSize();
            ViewCompat.setScaleX(mLabel, scale);
            ViewCompat.setScaleY(mLabel, scale);
            ViewCompat.animate(mLabel)
                    .translationY(0f)
                    .scaleY(1f)
                    .scaleX(1f)
                    .setDuration(ANIMATION_DURATION)
                    .setListener(null)
                    .setInterpolator(mInterpolator).start();
        } else {
            mLabel.setVisibility(VISIBLE);
        }
        mEditText.setHint(null);
    }

    private void hideLabel(boolean animate) {
        if (animate) {
            float scale = mEditText.getTextSize() / mLabel.getTextSize();
            ViewCompat.setScaleX(mLabel, 1f);
            ViewCompat.setScaleY(mLabel, 1f);
            ViewCompat.setTranslationY(mLabel, 0f);
            ViewCompat.animate(mLabel)
                    .translationY(mLabel.getHeight())
                    .setDuration(ANIMATION_DURATION)
                    .scaleX(scale)
                    .scaleY(scale)
                    .setListener(new ViewPropertyAnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(View view) {
                            mLabel.setVisibility(INVISIBLE);
                            mEditText.setHint(mHint);
                        }
                    })
                    .setInterpolator(mInterpolator).start();
        } else {
            mLabel.setVisibility(INVISIBLE);
            mEditText.setHint(mHint);
        }
    }

    private int dipsToPix(float dps) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dps, getResources().getDisplayMetrics());
    }
}