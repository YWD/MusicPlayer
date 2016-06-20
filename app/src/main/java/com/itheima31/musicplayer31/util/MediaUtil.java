package com.itheima31.musicplayer31.util;

import java.util.ArrayList;
import java.util.List;

import com.itheima31.musicplayer31.domain.Music;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.webkit.WebView.FindListener;

public class MediaUtil {
	// 默认进入应用的时候是停止状态
	public static int CURRENTOPTION = ConstantValue.OPTION_STOP;
	public static int CURRENTMUSIC = 0;
	public static int CURRENT_MODE = ConstantValue.MODE_CIRCLE;
	
	private List<Music> musicList = new ArrayList<Music>();
	// 单例模式
	private static MediaUtil mediaUtil = new MediaUtil();
	private Cursor cursor;

	private MediaUtil() {
	};

	public static MediaUtil getInstance() {
		return mediaUtil;
	}

	public List<Music> getAllMusic() {
		return musicList;
	}

	public void initMusic(Context context) {

		try {
			musicList.clear();
			// 内容接收者获取数据
			ContentResolver resolver = context.getContentResolver();
			// 查询sd卡中音频
			cursor = resolver.query(
					MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, new String[] {
							// 歌曲名称
							MediaStore.Audio.Media.TITLE,
							// 演唱者
							MediaStore.Audio.Media.ARTIST,
							// 歌曲id
							MediaStore.Audio.Media._ID,
							// 歌曲路径
							MediaStore.Audio.Media.DATA,
							// 歌曲持续时长
							MediaStore.Audio.Media.DURATION, }, null, null,
					null);
			while (cursor.moveToNext()) {
				String title = cursor.getString(0);
				String artist = cursor.getString(1);
				String id = cursor.getString(2);
				String path = cursor.getString(3);
				String duration = cursor.getString(4);

				Music music = new Music(title, artist, id, path, duration);
				musicList.add(music);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
	}

	public static String durationToString(String duration) {
		//接收到一个时间的毫秒值
		String reVal = "";
		int j = Integer.valueOf(duration);
		int i = j / 1000;//秒
		int min = (int) i / 60;//分
		int sec = i % 60;//单出来的秒
		
		if (min > 9) {//分钟为两位数
			if (sec > 9) {//秒为两位数
				reVal = min + ":" + sec;
			}
			if (sec <= 9) {//秒补0
				reVal = min + ":0" + sec;
			}
		} else {
			if (sec > 9) {
				reVal = "0" + min + ":" + sec;
			}
			if (sec <= 9) {
				reVal = "0" + min + ":0" + sec;
			}
		}
		return reVal;
	}

}
