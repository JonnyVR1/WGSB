package com.jonny.wgsb;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.AbsListView;

import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.view.ViewHelper;

class FloatingActionButton extends View {
    private final Interpolator mInterpolator = new AccelerateDecelerateInterpolator();
    private final Paint mButtonPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Drawable mIconDrawable;
    private Rect rect;
    private int mRadius, mShadowRadius;
    private int mShadowOffsetX, mShadowOffsetY;
    private int mColor, mPressedColor;
    private boolean mHidden = false;
    private int mLeftDisplayed = -1, mRightDisplayed = -1, mTopDisplayed = -1, mBottomDisplayed = -1;
    private float mYDisplayed = -1, mYHidden = -1;

    public FloatingActionButton(Context context) {
        this(context, null);
    }

    public FloatingActionButton(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public FloatingActionButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.FloatingActionButton);
        mColor = a.getColor(R.styleable.FloatingActionButton_fabColor, Color.WHITE);
        mButtonPaint.setStyle(Paint.Style.FILL);
        mButtonPaint.setColor(mColor);
        float radius, dx, dy;
        radius = a.getDimension(R.styleable.FloatingActionButton_shadowRadius, getResources().getDimension(R.dimen.fab_default_shadow_radius));
        dx = a.getDimension(R.styleable.FloatingActionButton_shadowDx, 0.0f);
        dy = a.getDimension(R.styleable.FloatingActionButton_shadowDy, getResources().getDimension(R.dimen.fab_default_shadow_dy));
        int color = a.getInteger(R.styleable.FloatingActionButton_shadowColor, Color.argb(100, 0, 0, 0));
        mPressedColor = a.getColor(R.styleable.FloatingActionButton_pressedColor, darkenColor(mColor));
        mButtonPaint.setShadowLayer(radius, dx, dy, color);
        setDrawable(a.getDrawable(R.styleable.FloatingActionButton_drawable));
        mRadius = a.getDimensionPixelSize(R.styleable.FloatingActionButton_fabRadius, getResources().getDimensionPixelSize(R.dimen.fab_radius));
        a.recycle();
        mShadowRadius = (int) Math.ceil(radius);
        mShadowOffsetX = Math.round(dx);
        mShadowOffsetY = Math.round(dy);
        setWillNotDraw(false);
        if (CompatUtils.isNotLegacyHoneyComb()) setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        WindowManager mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = mWindowManager.getDefaultDisplay();
        Point size = new Point();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            display.getSize(size);
            mYHidden = size.y;
        } else mYHidden = display.getHeight();
    }

    public static int darkenColor(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] *= 0.8f;
        return Color.HSVToColor(hsv);
    }

    public void setColor(int color) {
        mColor = color;
        mButtonPaint.setColor(mColor);
        invalidate();
    }

    public void setPressedColor(int color) {
        mPressedColor = color;
        invalidate();
    }

    public void setDrawable(Drawable drawable) {
        mIconDrawable = drawable;
        if (drawable != null)  drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int size = 2 * mRadius + 2 * mShadowRadius;
        setMeasuredDimension(size + getPaddingLeft() + getPaddingRight(), size + getPaddingTop() + getPaddingBottom());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float centerX = getWidth() / 2 - mShadowOffsetX - (getPaddingRight() - getPaddingLeft()) / 2;
        float centerY = getHeight() / 2 - mShadowOffsetY - (getPaddingBottom() - getPaddingTop()) / 2;
        canvas.drawCircle(centerX, centerY, mRadius, mButtonPaint);
        if (mIconDrawable != null) {
            canvas.save();
            canvas.translate(centerX - mIconDrawable.getIntrinsicWidth() / 2, centerY - mIconDrawable.getIntrinsicHeight() / 2);
            mIconDrawable.draw(canvas);
            canvas.restore();
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (mLeftDisplayed == -1) {
            mLeftDisplayed = left;
            mRightDisplayed = right;
            mTopDisplayed = top;
            mBottomDisplayed = bottom;
        }
        if (mYDisplayed == -1) mYDisplayed = ViewHelper.getY(this);
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        int color;
        if (event.getAction() == MotionEvent.ACTION_UP) color = mColor;
        else {
            color = mPressedColor;
            rect = new Rect(mLeftDisplayed, mTopDisplayed, mRightDisplayed, mBottomDisplayed);
        }
        if (event.getAction() == MotionEvent.ACTION_MOVE)
            if (!rect.contains(mLeftDisplayed + (int) event.getX(), mTopDisplayed + (int) event.getY()))
                color = mColor;
        mButtonPaint.setColor(color);
        invalidate();
        return super.onTouchEvent(event);
    }

    public void hide(boolean hide) {
        if (mHidden != hide) {
            mHidden = hide;
            ObjectAnimator animator = ObjectAnimator.ofFloat(this, "y", mHidden ? mYHidden : mYDisplayed).setDuration(500);
            animator.setInterpolator(mInterpolator);
            animator.start();
        }
    }

    public void listenTo(AbsListView listView) {
        if (null != listView) listView.setOnScrollListener(new DirectionScrollListener(this));
    }
}