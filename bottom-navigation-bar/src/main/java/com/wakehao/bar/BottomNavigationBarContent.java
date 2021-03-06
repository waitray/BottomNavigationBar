package com.wakehao.bar;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.List;

/**
 * Created by WakeHao on 2017/1/10.
 */

public class BottomNavigationBarContent extends LinearLayout {

    private final int mBottomNavigationBarHeight;

    private final int mActiveItemMaxWidth;
    private final int mActiveItemMinWidth;

    private final int mInactiveItemMaxWidth;
    private final int mInactiveItemMinWidth;


    private int mActivePosition=0;
    private OnClickListener mOnClickListener;
    private int[] widthSpec;
    private int mSwitchMode;
    private int counts;


    public BottomNavigationBarContent(Context context) {
        this(context,null);
    }


    private BottomNavigationBarContent(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }
    private long LIMIT_OF_CLICK;
    private BottomNavigationBarContent(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        final Resources res=getResources();

        mActiveItemMaxWidth=res.getDimensionPixelSize(
                R.dimen.bar_active_item_max_width
        );
        mActiveItemMinWidth=res.getDimensionPixelSize(
                R.dimen.bar_active_item_min_width
        );
        mBottomNavigationBarHeight=res.getDimensionPixelSize(
                R.dimen.bar_height);
        mInactiveItemMinWidth = res.getDimensionPixelSize(
                R.dimen.bar_inactive_item_min_width);
        mInactiveItemMaxWidth=res.getDimensionPixelSize(
                R.dimen.bar_inactive_item_max_width
        );

        mOnClickListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
//                BottomNavigationItem item = (BottomNavigationItem) ((BottomNavigationItemWithDot)v).getChildAt(0);
                BottomNavigationItem item = (BottomNavigationItem) v;
                if(mListener==null||(mListener!=null&&mListener.onNavigationItemSelected(item,item.getPosition()))){
                    if(System.currentTimeMillis()-LIMIT_OF_CLICK>=150L) {
                        //when is sliding,it can not been clicked
                        if(viewPager==null){
                            setItemSelected(item.getPosition(),true);
                            LIMIT_OF_CLICK=System.currentTimeMillis();
                        }
                        else {
                            if(((BottomNavigationBar) getParent()).getCanClick()){
                                viewPager.setCurrentItem(item.getPosition(), false);
                                setItemSelected(item.getPosition(),true);
                                LIMIT_OF_CLICK=System.currentTimeMillis();
                            }
                        }
                    }
                }
            }
        };
        setId(R.id.bar_content_private);

    }

//    private BottomNavigationItem getBottomNavigationItem(){
//        return
//    }


    public void setItemSelected(int position,boolean isAnim) {
        if(mActivePosition==position)
        {
            if(mListener!=null)mListener.onNavigationItemSelectedAgain(getBottomItem(position),position);
            return;
        }
        int shiftedColor = ((BottomNavigationItem) ((BottomNavigationItemWithDot) getChildAt(position)).getChildAt(0)).getShiftedColor();
        if(shiftedColor!=0){
            ((BottomNavigationBar) getParent()).drawBackgroundCircle(shiftedColor,downX,downY);
        }
        mActivePosition=position;
        for(int i=0;i<getChildCount();i++)
        {
            final BottomNavigationItem item = (BottomNavigationItem) ((BottomNavigationItemWithDot) getChildAt(i)).getChildAt(0);
            item.setSelected(i==position,isAnim);
        }
    }

    void updatePosition(int mActivePosition){
        this.mActivePosition=mActivePosition;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if(ev.getAction()==MotionEvent.ACTION_DOWN){
            downX=ev.getRawX();
            downY=ev.getY();
        }
        return super.onInterceptTouchEvent(ev);
    }

    private float downX;
    private float downY;

    //onTouchEvent 除非被拦截或者子view不处理才会调用
//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//        if(event.getAction()==MotionEvent.ACTION_DOWN){
//            downX=event.getRawX();
//            downY=event.getRawY();
//        }
//        return super.onTouchEvent(event);
//    }


    public void setItems(List<BottomNavigationItem> bottomNavigationItems){
        setOrientation(HORIZONTAL);
        setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,mBottomNavigationBarHeight));
        counts = bottomNavigationItems.size();
        widthSpec = new int[counts];
        int screenWidth=BarUtils.getDeviceWidth(getContext());
//        int heightSpec=MeasureSpec.makeMeasureSpec(mBottomNavigationBarHeight,MeasureSpec.EXACTLY);

        int remain_activeMax;
        int activeItem;
        int inActiveItem;
        int remain;
        //shift mode
        if(mSwitchMode==1){
             remain_activeMax=screenWidth-(counts -1)*mInactiveItemMinWidth;
             activeItem=Math.min(mActiveItemMaxWidth,remain_activeMax);

            if(activeItem<mActiveItemMinWidth)activeItem=mActiveItemMinWidth;
            int remain_inActiveMax=(screenWidth - activeItem) / (counts - 1);
            inActiveItem=Math.min(mInactiveItemMaxWidth,remain_inActiveMax);

            remain=screenWidth-activeItem-(counts -1)*inActiveItem;
        }
        else {
             remain_activeMax=screenWidth/ counts;
             activeItem=Math.min(mActiveItemMaxWidth,remain_activeMax);
             inActiveItem=activeItem;
             remain=screenWidth-activeItem* counts;
        }

        for (int i = 0; i< counts; i++)
        {
            widthSpec[i]=mActivePosition==i?activeItem:inActiveItem;
            if(remain>0){
                widthSpec[i]++;
                remain--;
            }
            final BottomNavigationItem item = bottomNavigationItems.get(i);
            item.setClickable(true);
            item.setPosition(i);
            item.setOnClickListener(mOnClickListener);
                item.setActiveItemWidth(activeItem);
                item.setInActiveItemWidth(inActiveItem);
            LinearLayout.LayoutParams layoutParams=new LinearLayout.LayoutParams(
                    widthSpec[i],mBottomNavigationBarHeight);
            item.setLayoutParams(layoutParams);
            addView(new BottomNavigationItemWithDot(getContext(),item));
//            if(i==mActivePosition)


        }
//        setItemSelected(0,true);
    }
    private ViewPager viewPager;

    public void setViewPager(ViewPager viewPager){
        this.viewPager=viewPager;
    }
    private BottomNavigationBar.OnNavigationItemSelectedListener mListener;
    public void injectListener(BottomNavigationBar.OnNavigationItemSelectedListener mListener) {
        this.mListener=mListener;
    }

    public void finishInit(List<BottomNavigationItem> bottomNavigationItems,boolean isViewpager) {
        for(BottomNavigationItem item: bottomNavigationItems){
            item.setIsViewPager(isViewpager);
            item.finishInit();
        }
    }

    public BottomNavigationBarContent setSwitchMode(int mSwitchMode) {
        this.mSwitchMode = mSwitchMode;
        return this;
    }

    public void startAlphaAnim(int position, float positionOffset, boolean isMoving) {
       if(isMoving){
           getBottomItem(position).setHasCorrect(false).alphaAnim(positionOffset);
           getBottomItem(position+1).setHasCorrect(false).alphaAnim(1-positionOffset);
       }
        else{
           getBottomItem(position).alphaAnim(positionOffset);
           getBottomItem(position+1).alphaAnim(1-positionOffset);
       }
    }

    public BottomNavigationItem getBottomItem(int position){
        return ((BottomNavigationItem) ((BottomNavigationItemWithDot) getChildAt(position)).getChildAt(0));
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle bundle=new Bundle();
        bundle.putInt("mActivePosition",mActivePosition);
        bundle.putParcelable("superState",super.onSaveInstanceState());
        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if(state instanceof  Bundle){
            Bundle bundle= (Bundle) state;
            int mRestoreActivePosition = bundle.getInt("mActivePosition");
            setItemSelected(mRestoreActivePosition,true);
            state=bundle.getParcelable("superState");
        }
        super.onRestoreInstanceState(state);
    }
}
