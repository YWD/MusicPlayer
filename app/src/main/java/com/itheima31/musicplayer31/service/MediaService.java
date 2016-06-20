package com.itheima31.musicplayer31.service;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

import com.itheima31.musicplayer31.MainActivity;
import com.itheima31.musicplayer31.util.ConstantValue;
import com.itheima31.musicplayer31.util.MediaUtil;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.widget.Toast;

public class MediaService extends Service implements OnErrorListener, OnCompletionListener {
	private MediaPlayer mediaPlayer;
	private Timer timer;
	@Override
	public void onCreate() {
		mediaPlayer = new MediaPlayer();
		mediaPlayer.setOnErrorListener(this);
		mediaPlayer.setOnCompletionListener(this);
	}
	@Override
	public void onStart(Intent intent, int startId) {
		int option = intent.getIntExtra("option", -1);
		switch (option) {
		case ConstantValue.OPTION_PLAY://等于播放
			String path = intent.getStringExtra("path");
			play(path);
			MediaUtil.CURRENTOPTION = ConstantValue.OPTION_PLAY;
			break;
		case ConstantValue.OPTION_PAUSE:
			pause();
			MediaUtil.CURRENTOPTION = ConstantValue.OPTION_PAUSE;
			break;
		case ConstantValue.OPTION_CONTINUE:
			continueplay();
			MediaUtil.CURRENTOPTION = ConstantValue.OPTION_CONTINUE;
			break;
		case ConstantValue.PULL_PROGRESS:
			int position = intent.getIntExtra("position", -1);
			seektoposition(position);
			break;
		}
	}
	@Override
	public void onDestroy() {
		super.onDestroy();
		stop();
	}
	private void stop() {
		if(mediaPlayer!=null){
			mediaPlayer.stop();
			mediaPlayer.release();
			mediaPlayer = null;
			if(timer!=null){
				timer.cancel();
			}
		}
	}
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	//播放音乐的方法，需要传递播放歌曲路径
	public void play(String path){
		try {
			if(mediaPlayer!=null){
				mediaPlayer.reset();
				mediaPlayer.setDataSource(path);
				mediaPlayer.prepare();
				mediaPlayer.start();
			}
			sendTimerTask();
			
			//调用方法解析歌词文件
			String musicpath = MediaUtil.getInstance().getAllMusic().get(MediaUtil.CURRENTMUSIC).getPath();
			String temp = musicpath.substring(0, path.lastIndexOf("."));
			
			File file = new File(temp+".lrc");
			if(file==null || !file.exists()){
				file = new File(temp+".txt");
			}
			MainActivity.lyrcUtil.ReadLRC(file);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	private void sendTimerTask() {
		if(timer == null){
			timer = new Timer();
			timer.schedule(new TimerTask() {
				
				@Override
				public void run() {
					//获取当前正在播放音乐时长
					int total = mediaPlayer.getDuration();
					//获取当前音乐播放到的位置
					int position = mediaPlayer.getCurrentPosition();
					Message message = Message.obtain(); 
					Bundle bundle = new Bundle();
					bundle.putInt("total", total);
					bundle.putInt("position", position);
					message.what = ConstantValue.UPDATE_PROGRESS_MSG;
					message.setData(bundle);
					if(position<total){
						MainActivity.handler.sendMessage(message);
					}
				}
			}, 5, 500);
		}
	}
	//暂停音乐
	public void pause(){
		if(mediaPlayer!=null && mediaPlayer.isPlaying()){
			mediaPlayer.pause();
			if(timer!=null){
				timer.cancel();
				timer = null;
			}
		}
	}
	//继续播放的方法
	public void continueplay(){
		if(mediaPlayer!=null){
			mediaPlayer.start();
		}
		sendTimerTask();
	}
	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		Toast.makeText(getApplicationContext(), "音频文件损坏", 0).show();
		return false;
	}
	public void seektoposition(int position){
		if(mediaPlayer!=null){
			mediaPlayer.seekTo(position);
		}
		sendTimerTask();
	}
	@Override
	public void onCompletion(MediaPlayer arg0) {
		Message message = Message.obtain();
		message.what = ConstantValue.FINISH;
		MainActivity.handler.sendMessage(message);
	}
}
