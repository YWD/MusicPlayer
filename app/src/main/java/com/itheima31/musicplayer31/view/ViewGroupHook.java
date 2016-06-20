package com.itheima31.musicplayer31.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;
//歌词界面对于帧布局
public class ViewGroupHook extends FrameLayout
{
  private Context mContext = null;

  public ViewGroupHook(Context paramContext)
  {
    super(paramContext);
    mContext = paramContext;
  }

  public ViewGroupHook(Context paramContext, AttributeSet paramAttributeSet)
  {
    super(paramContext, paramAttributeSet);
    mContext = paramContext;
  }

  public ViewGroupHook(Context paramContext, AttributeSet paramAttributeSet, int paramInt)
  {
    super(paramContext, paramAttributeSet, paramInt);
    mContext = paramContext;
  }
  //FrameLayout默认不处理触摸事件，返回true是为了让其响应触摸事件，如果是false则会导致不能左右滑动
  public boolean onTouchEvent(MotionEvent paramMotionEvent)
  {
    return true;
  }
}
