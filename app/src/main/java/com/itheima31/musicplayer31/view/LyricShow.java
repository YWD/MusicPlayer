package com.itheima31.musicplayer31.view;


import android.content.Context;
import java.util.Vector;

import com.itheima31.musicplayer31.util.LyrcUtil.Timelrc;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;

/**
 * 
 * @author Administrator
 * ��ʾ���
 *
 */
public class LyricShow extends TextView {
	private static final String TAG = "LyricView";

	private Paint NotCurrentPaint; // �ǵ�ǰ��ʻ���
	private Paint CurrentPaint; // ��ǰ��ʻ���
	
	private int notCurrentPaintColor = Color.WHITE;// �ǵ�ǰ��ʻ��� ��ɫ
	private int CurrentPaintColor = Color.GREEN; // ��ǰ��ʻ��� ��ɫ
	private Typeface Texttypeface = Typeface.SERIF;
	private Typeface CurrentTexttypeface = Typeface.DEFAULT_BOLD;
	private float width;

	// private int brackgroundcolor = 0xff00ff00; // ������ɫ
	private float lrcTextSize = 40; // ��ʴ�С
	private float CurrentTextSize = 60;//��ǰ��ʵĴ�С
	// private Align = Paint.Align.CENTER��
	public float mTouchHistoryY;
	private int height;
	private long currentDunringTime; // ��ǰ�и�ʳ�����ʱ�䣬�ø�ʱ����sleep

	private int TextHeight = 35; // ÿһ�еļ��
	private boolean lrcInitDone = false;// �Ƿ��ʼ�������
	public int index = 0;

	private static Vector<Timelrc> lrclist;
	
	private long currentTime;//��ǰ���ŵ���ʱ��
	private long sentenctTime;//��һ���ʵ�ʱ��

	public void SetTimeLrc(Vector<Timelrc> list) {
		lrclist = list;
	}

	public Paint getNotCurrentPaint() {
		return NotCurrentPaint;
	}

	public void setNotCurrentPaint(Paint notCurrentPaint) {
		NotCurrentPaint = notCurrentPaint;
	}

	public boolean isLrcInitDone() {
		return lrcInitDone;
	}

	public Typeface getCurrentTexttypeface() {
		return CurrentTexttypeface;
	}

	public void setCurrentTexttypeface(Typeface currrentTexttypeface) {
		CurrentTexttypeface = currrentTexttypeface;
	}

	public void setLrcInitDone(boolean lrcInitDone) {
		this.lrcInitDone = lrcInitDone;
	}

	public float getLrcTextSize() {
		return lrcTextSize;
	}

	public void setLrcTextSize(float lrcTextSize) {
		this.lrcTextSize = lrcTextSize;
	}

	public float getCurrentTextSize() {
		return CurrentTextSize;
	}

	public void setCurrentTextSize(float currentTextSize) {
		CurrentTextSize = currentTextSize;
	}

	public Paint getCurrentPaint() {
		return CurrentPaint;
	}

	public void setCurrentPaint(Paint currentPaint) {
		CurrentPaint = currentPaint;
	}

	public int getNotCurrentPaintColor() {
		return notCurrentPaintColor;
	}

	public void setNotCurrentPaintColor(int notCurrentPaintColor) {
		this.notCurrentPaintColor = notCurrentPaintColor;
	}

	public int getCurrentPaintColor() {
		return CurrentPaintColor;
	}

	public void setCurrentPaintColor(int currrentPaintColor) {
		CurrentPaintColor = currrentPaintColor;
	}

	public Typeface getTexttypeface() {
		return Texttypeface;
	}

	public void setTexttypeface(Typeface texttypeface) {
		Texttypeface = texttypeface;
	}

	// public int getBrackgroundcolor() {
	// return brackgroundcolor;
	// }
	// public void setBrackgroundcolor(int brackgroundcolor) {
	// this.brackgroundcolor = brackgroundcolor;
	// }
	public int getTextHeight() {
		return TextHeight;
	}

	public void setTextHeight(int textHeight) {
		TextHeight = textHeight;
	}

	public LyricShow(Context context) {
		super(context);
		init();
	}

	public LyricShow(Context context, AttributeSet attr) {
		super(context, attr);
		init();
	}

	public LyricShow(Context context, AttributeSet attr, int i) {
		super(context, attr, i);
		init();
	}

	private void init() {
		setFocusable(true);

		// �Ǹ�������
		NotCurrentPaint = new Paint();
		NotCurrentPaint.setAntiAlias(true);
		NotCurrentPaint.setTextAlign(Paint.Align.CENTER);
		// �������� ��ǰ���
		CurrentPaint = new Paint();
		CurrentPaint.setAntiAlias(true);
		CurrentPaint.setColor(CurrentPaintColor);
		CurrentPaint.setTextAlign(Paint.Align.CENTER);

	}

	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		Log.e(TAG, "onDraw---");
		// canvas.drawColor(brackgroundcolor);
		NotCurrentPaint.setTextSize(lrcTextSize);
		NotCurrentPaint.setColor(notCurrentPaintColor);
		NotCurrentPaint.setTypeface(Texttypeface);
		
		
		CurrentPaint.setColor(CurrentPaintColor);
		CurrentPaint.setTextSize(lrcTextSize);
		CurrentPaint.setTypeface(CurrentTexttypeface);
		
		if (index == -1)
			return;

		// float plus = 5;
		//
//		float plus = currentDunringTime == 0 ? 20
//				: 20
//						+ (((float) currentTime - (float) sentenctTime) / (float) currentDunringTime)
//						* (float) 20;
//		// ���Ϲ��� ����Ǹ��ݸ�ʵ�ʱ�䳤������������������
//		//ÿ�ν��յ���Ϣ���������20������+��ʱ������20�ȷݣ���ǰʱ��������ݶ�*20)
//		while(plus -5 > 0){
//			canvas.translate(0, -5);
//			plus = plus -5;
//		}
		
		float plus = currentDunringTime == 0 ? 0
				: (((float) currentTime - (float) sentenctTime) / (float) currentDunringTime)
						* (float) 20;
		// ���Ϲ��� ����Ǹ��ݸ�ʵ�ʱ�䳤������������������
		canvas.translate(0, -plus);
		// �Ȼ���ǰ�У�֮���ٻ�����ǰ��ͺ��棬�����ͱ��ֵ�ǰ�����м��λ��
		
		if(lrclist!=null && lrclist.size() > 0){
			try {
				//����ǰ�����
				canvas.drawText(lrclist.get(index).getLrcString(), width / 2,
						height / 2, CurrentPaint);
				// canvas.translate(0, plus);
				
				float tempY = height / 2;
				// ��������֮ǰ�ľ���
				
				Log.i(TAG, "onDraw--��������֮ǰ�ľ���-");
				for (int i = index - 1; i >= 0; i--) {
					// Sentence sen = list.get(i);
					// ��������
					tempY = tempY - TextHeight;
					if (tempY < 0) {
						break;
					}
					Log.i(TAG, "onDraw--����-"+lrclist.get(i).getLrcString());
					canvas.drawText(lrclist.get(i).getLrcString(), width / 2,
							tempY, NotCurrentPaint);
					// canvas.translate(0, TextHeight);
				}
				tempY = height / 2;
				
				
				
				// ��������֮��ľ���
				Log.e(TAG, "onDraw--��������֮��ľ���-");
				for (int i = index + 1; i < lrclist.size(); i++) {
					// ��������
					tempY = tempY + TextHeight;
					if (tempY > height) {
						break;
					}
					canvas.drawText(lrclist.get(i).getLrcString(), width / 2,
							tempY, NotCurrentPaint);
					// canvas.translate(0, TextHeight);
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}else{
			
			canvas.drawText("û�ҵ���ʣ�", width / 2,
					height / 2, CurrentPaint);
			currentDunringTime = 0 ;
		}
	}

	protected void onSizeChanged(int w, int h, int ow, int oh) {
		super.onSizeChanged(w, h, ow, oh);
		width = w; // remember the center of the screen
		height = h;
		// middleY = h * 0.5f;
	}

	//   
	/**
	 * @param current
	 *            ��ǰ��ʵ�ʱ����
	 * 
	 * @return null
	 */
	public void SetNowPlayIndex(int current) {
		this.currentTime = current;
		// // ������
//		if (index != -1) {
//			sentenctTime = lrclist.get(index).getTimePoint();
//			currentDunringTime = lrclist.get(index).getSleepTime();
//			// Log.d(TAG,"sentenctTime = "+sentenctTime+",  currentDunringTime = "+currentDunringTime);
//		}
		if(lrclist!=null){
			for (int i = 1; i < lrclist.size(); i++) {
				if (current < lrclist.get(i).getTimePoint())
					if (i == 1 || current >= lrclist.get(i - 1).getTimePoint()) {
						index = i-1;
						sentenctTime = lrclist.get(i - 1).getTimePoint();
						currentDunringTime = lrclist.get(i - 1).getSleepTime();
				}
			}
		}
		
		this.invalidate();
//		this.postInvalidate();
	}
	
}
