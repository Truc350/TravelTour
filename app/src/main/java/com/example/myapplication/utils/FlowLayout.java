package com.example.myapplication.utils;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

public class FlowLayout extends ViewGroup {
    private int horizontalSpacing = 16;
    private int verticalSpacing = 16;

    public FlowLayout(Context context) {
        super(context);
        init(context);
    }

    public FlowLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public FlowLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        float density = context.getResources().getDisplayMetrics().density;
        horizontalSpacing = (int) (8 * density);
        verticalSpacing = (int) (8 * density);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        
        int paddingLeft = getPaddingLeft();
        int paddingRight = getPaddingRight();
        int paddingTop = getPaddingTop();
        int paddingBottom = getPaddingBottom();
        
        int currentX = paddingLeft;
        int currentY = paddingTop;
        int maxRowHeight = 0;
        
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            if (child.getVisibility() == View.GONE) continue;
            
            child.measure(
                MeasureSpec.makeMeasureSpec(widthSize - paddingLeft - paddingRight, MeasureSpec.AT_MOST),
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
            );
            
            int childWidth = child.getMeasuredWidth();
            int childHeight = child.getMeasuredHeight();
            
            if (currentX + childWidth > widthSize - paddingRight) {
                currentX = paddingLeft;
                currentY += maxRowHeight + verticalSpacing;
                maxRowHeight = 0;
            }
            
            currentX += childWidth + horizontalSpacing;
            maxRowHeight = Math.max(maxRowHeight, childHeight);
        }
        
        int totalHeight = currentY + maxRowHeight + paddingBottom;
        if (childCount == 0) {
            totalHeight = paddingTop + paddingBottom;
        }
        
        setMeasuredDimension(widthSize, resolveSize(totalHeight, heightMeasureSpec));
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int width = r - l;
        int paddingLeft = getPaddingLeft();
        int paddingRight = getPaddingRight();
        int paddingTop = getPaddingTop();
        
        int currentX = paddingLeft;
        int currentY = paddingTop;
        int maxRowHeight = 0;
        
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            if (child.getVisibility() == View.GONE) continue;
            
            int childWidth = child.getMeasuredWidth();
            int childHeight = child.getMeasuredHeight();
            
            if (currentX + childWidth > width - paddingRight) {
                currentX = paddingLeft;
                currentY += maxRowHeight + verticalSpacing;
                maxRowHeight = 0;
            }
            
            child.layout(currentX, currentY, currentX + childWidth, currentY + childHeight);
            currentX += childWidth + horizontalSpacing;
            maxRowHeight = Math.max(maxRowHeight, childHeight);
        }
    }
}
