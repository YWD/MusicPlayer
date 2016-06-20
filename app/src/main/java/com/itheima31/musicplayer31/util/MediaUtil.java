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
	// Ĭ�Ͻ���Ӧ�õ�ʱ����ֹͣ״̬
	public static int CURRENTOPTION = ConstantValue.OPTION_STOP;
	public static int CURRENTMUSIC = 0;
	public static int CURRENT_MODE = ConstantValue.MODE_CIRCLE;
	
	private List<Music> musicList = new ArrayList<Music>();
	// ����ģʽ
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
			// ���ݽ����߻�ȡ����
			ContentResolver resolver = context.getContentResolver();
			// ��ѯsd������Ƶ
			cursor = resolver.query(
					MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, new String[] {
							// ��������
							MediaStore.Audio.Media.TITLE,
							// �ݳ���
							MediaStore.Audio.Media.ARTIST,
							// ����id
							MediaStore.Audio.Media._ID,
							// ����·��
							MediaStore.Audio.Media.DATA,
							// ��������ʱ��
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
		//���յ�һ��ʱ��ĺ���ֵ
		String reVal = "";
		int j = Integer.valueOf(duration);
		int i = j / 1000;//��
		int min = (int) i / 60;//��
		int sec = i % 60;//����������
		
		if (min > 9) {//����Ϊ��λ��
			if (sec > 9) {//��Ϊ��λ��
				reVal = min + ":" + sec;
			}
			if (sec <= 9) {//�벹0
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
