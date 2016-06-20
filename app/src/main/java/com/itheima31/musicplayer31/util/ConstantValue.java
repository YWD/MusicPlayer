package com.itheima31.musicplayer31.util;

public class ConstantValue {
	//停止
	public static final int OPTION_STOP = 1;
	//播放
	public static final int OPTION_PLAY = 2;
	//暂停
	public static final int OPTION_PAUSE = 3;
	//继续播放
	public static final int OPTION_CONTINUE = 4;
	//在正常播放情况下更新时间的状态码
	public static final int UPDATE_PROGRESS_MSG = 5;
	//在拖拽的情况下去更新进度条，时间,跳转到指定的地方去播放
	public static final int PULL_PROGRESS = 6;
	//播放完毕
	public static final int FINISH = 7;
	
	//列表循环
	public static final int MODE_CIRCLE = 8;
	//列表播放
	public static final int MODE_ORDER = 9;
	//单曲循环
	public static final int MODE_SINGLE = 10;
	//随机播放
	public static final int MODE_RANDOM = 11;
}
