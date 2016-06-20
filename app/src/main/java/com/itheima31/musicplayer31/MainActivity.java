package com.itheima31.musicplayer31;

import java.io.DataInputStream;
import java.util.List;
import java.util.Random;

import com.itheima31.musicplayer31.adapter.MusicListAdapter;
import com.itheima31.musicplayer31.domain.Music;
import com.itheima31.musicplayer31.service.MediaService;
import com.itheima31.musicplayer31.util.ConstantValue;
import com.itheima31.musicplayer31.util.LyrcUtil;
import com.itheima31.musicplayer31.util.MediaUtil;
import com.itheima31.musicplayer31.view.LyricShow;
import com.itheima31.musicplayer31.view.ScrollableViewGroup;
import com.itheima31.musicplayer31.view.ScrollableViewGroup.OnCurrentViewChangedListener;

import android.media.AudioManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Toast;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class MainActivity extends Activity implements OnClickListener {

	public static final String tag = "MainActivity";
	private static ListView playList;
	private MusicListAdapter adapter;
	private ImageButton indPlayMode;
	private ImageButton btnPrev;
	private ImageButton btnPlay;
	private ImageButton btnNext;
	private ImageButton indMenu;
	private static ImageView imgPlay;
	public static LyrcUtil lyrcUtil;
	private static MainActivity activityActivity;
	
	public static Handler handler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			//处理消息
			switch (msg.what) {
			case ConstantValue.UPDATE_PROGRESS_MSG:
				//更新时间，还有进度条
				Bundle bundle = msg.getData();
				//当前播放歌曲总长度
				int total = bundle.getInt("total", -1);
				//当前播放到的位置
				int position = bundle.getInt("position",-1);
				
				//获取指定格式总时间
				String totalstr = MediaUtil.durationToString(total+"");
				//获取当所处时间的字符串形式
				String positionstr = MediaUtil.durationToString(position+"");
				
				txtLapse.setText(positionstr);
				txtDuration.setText(totalstr);
				
				skbGuage.setMax(total);
				skbGuage.setProgress(position);
				
				lyrcUtil.RefreshLRC(position);
				
				lyricShow.SetTimeLrc(lyrcUtil.getLrcList());
				lyricShow.SetNowPlayIndex(position);
				break;
			case ConstantValue.FINISH:
				switch (MediaUtil.CURRENT_MODE) {
				case ConstantValue.MODE_CIRCLE:
					//列表循环所做操作
					changeTextColor(Color.WHITE);
					if((MediaUtil.getInstance().getAllMusic().size()-1)== MediaUtil.CURRENTMUSIC){
						MediaUtil.CURRENTMUSIC = 0;
					}else if(MediaUtil.CURRENTMUSIC < MediaUtil.getInstance().getAllMusic().size()-1){
						MediaUtil.CURRENTMUSIC++;
					}
					
					String path = MediaUtil.getInstance().getAllMusic().get(MediaUtil.CURRENTMUSIC ).getPath();
					
					Intent intent= new Intent(activityActivity, MediaService.class);
					intent.putExtra("path", path);
					intent.putExtra("option", ConstantValue.OPTION_PLAY);
					
					activityActivity.startService(intent);
					changeTextColor(Color.GREEN);
					imgPlay.setImageResource(R.drawable.appwidget_pause);
					MediaUtil.CURRENT_MODE = ConstantValue.MODE_CIRCLE;
					break;
				case ConstantValue.MODE_ORDER:
					//列表播放所做操作
					if(MediaUtil.CURRENTMUSIC<MediaUtil.getInstance().getAllMusic().size()-1){
						changeTextColor(Color.WHITE);
						MediaUtil.CURRENTMUSIC ++;
						String path_order = MediaUtil.getInstance().getAllMusic().get(MediaUtil.CURRENTMUSIC ).getPath();
						
						Intent intent_order= new Intent(activityActivity, MediaService.class);
						intent_order.putExtra("path", path_order);
						intent_order.putExtra("option", ConstantValue.OPTION_PLAY);
						
						activityActivity.startService(intent_order);
						changeTextColor(Color.GREEN);
						imgPlay.setImageResource(R.drawable.appwidget_pause);
						MediaUtil.CURRENT_MODE = ConstantValue.MODE_ORDER;
					}
					break;
				case ConstantValue.MODE_SINGLE:
					//单曲循环
					String path_single = MediaUtil.getInstance().getAllMusic().get(MediaUtil.CURRENTMUSIC ).getPath();
					
					Intent intent_single= new Intent(activityActivity, MediaService.class);
					intent_single.putExtra("path", path_single);
					intent_single.putExtra("option", ConstantValue.OPTION_PLAY);
					
					activityActivity.startService(intent_single);
					imgPlay.setImageResource(R.drawable.appwidget_pause);
					MediaUtil.CURRENT_MODE = ConstantValue.MODE_SINGLE;
					break;
				case ConstantValue.MODE_RANDOM:
					//随机播放
					changeTextColor(Color.WHITE);
					MediaUtil.CURRENTMUSIC = new Random().nextInt(MediaUtil.getInstance().getAllMusic().size());
					String path_random = MediaUtil.getInstance().getAllMusic().get(MediaUtil.CURRENTMUSIC).getPath();
					
					Intent intent_random= new Intent(activityActivity, MediaService.class);
					intent_random.putExtra("path", path_random);
					intent_random.putExtra("option", ConstantValue.OPTION_PLAY);
					
					activityActivity.startService(intent_random);
					changeTextColor(Color.GREEN);
					imgPlay.setImageResource(R.drawable.appwidget_pause);
					MediaUtil.CURRENT_MODE = ConstantValue.MODE_RANDOM;
					break;
				}
				break;
			}
		};
	};
	private static TextView txtLapse;
	private static TextView txtDuration;
	private static SeekBar skbGuage;
	private ImageButton indMain;
	private ImageButton indList;
	private ImageButton indLyric;
	private ImageButton imgBtnVolume;
	private ScrollableViewGroup scrollableViewGroup;
	private static LyricShow lyricShow;
	private ImageView imgPlayMode;
	private MyBroadcastReceiver broadcastReceiver;
	private NotificationManager notificationManager;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		activityActivity = this;
		new MyAsyncTask().execute();
		init();
		lyrcUtil = new LyrcUtil(activityActivity);
	}
	
	private void init() {
		initListView();
		//初始化底部控件操作
		initBottomView();
		initProgressView();
		initTitleView();
		initLrcView();
	}

	private void initLrcView() {
		lyricShow = (LyricShow) findViewById(R.id.LyricShow);
	}

	private void initTitleView() {
		indMain = (ImageButton) findViewById(R.id.IndMain);
		indList = (ImageButton) findViewById(R.id.IndList);
		indLyric = (ImageButton) findViewById(R.id.IndLyric);
		imgBtnVolume = (ImageButton) findViewById(R.id.ImgBtnVolume);
		scrollableViewGroup = (ScrollableViewGroup) findViewById(R.id.ViewFlipper);
		
		scrollableViewGroup.setOnCurrentViewChangedListener(new OnCurrentViewChangedListener() {
			@Override
			public void onCurrentViewChanged(View view, int currentview) {
				//当前的所处页
				switch (currentview) {
				case 0:
					//所处第0页，第0页为选中状态
					indMain.setEnabled(false);
					indList.setEnabled(true);
					indLyric.setEnabled(true);
					break;
				case 1:
					indMain.setEnabled(true);
					indList.setEnabled(false);
					indLyric.setEnabled(true);
					break;
				case 2:
					indMain.setEnabled(true);
					indList.setEnabled(true);
					indLyric.setEnabled(false);
					break;
				}
			}
		});
		
		indMain.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//根据当前对应页面索引，设置所处页
				scrollableViewGroup.setCurrentView(0);
			}
		});
		indList.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				scrollableViewGroup.setCurrentView(1);
			}
		});
		indLyric.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				scrollableViewGroup.setCurrentView(2);
			}
		});
		
		imgBtnVolume.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//对声音去做处理
				AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
				//获取当前音乐声音的最大值
				int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
				Log.i(tag, "maxVolume = "+maxVolume);//15
				//根据获取音量的最大值，去设置音量的大小
				audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 5, 0);
			}
		});
	}

	private void initProgressView() {
		txtLapse = (TextView) findViewById(R.id.txtLapse);
		txtDuration = (TextView) findViewById(R.id.txtDuration);
		skbGuage = (SeekBar) findViewById(R.id.skbGuage);
		//进度条拖拽的事件监听
		skbGuage.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			
			//停止拖动时候调用方法
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				//定位当前歌曲的播放地方
				int position = seekBar.getProgress();
				startMediaService(null, ConstantValue.PULL_PROGRESS,position);
			}
			//开始拖动时调用方法
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}
			//拖动过程中调用方法
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				// TODO Auto-generated method stub
				
			}
		});
	}

	private void initBottomView() {
		indPlayMode = (ImageButton) findViewById(R.id.IndPlayMode);
		btnPrev = (ImageButton) findViewById(R.id.btnPrev);
		btnPlay = (ImageButton) findViewById(R.id.btnPlay);
		btnNext = (ImageButton) findViewById(R.id.btnNext);
		indMenu = (ImageButton) findViewById(R.id.IndMenu);
		imgPlay = (ImageView) findViewById(R.id.imgPlay);
		imgPlayMode = (ImageView) findViewById(R.id.imgPlayMode);
		
		indPlayMode.setOnClickListener(this);
		btnPrev.setOnClickListener(this);
		btnPlay.setOnClickListener(this);
		btnNext.setOnClickListener(this);
		indMenu.setOnClickListener(this);
		
	}

	private void initListView() {
		playList = (ListView) findViewById(R.id.PlayList);
		adapter = new MusicListAdapter(this);
		playList.setAdapter(adapter);
		playList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,long arg3) {
				//在MediaUtil.CURRENTOPTION没有变化前，将前一个播放的歌曲，变成白色
				changeTextColor(Color.WHITE);
				String path = MediaUtil.getInstance().getAllMusic().get(position).getPath();
				startMediaService(path, ConstantValue.OPTION_PLAY);
				imgPlay.setImageResource(R.drawable.appwidget_pause);
				MediaUtil.CURRENTMUSIC  = position;
				//在MediaUtil.CURRENTOPTION变化后，将后一个选中播放的歌曲，变成绿色
				changeTextColor(Color.GREEN);
			}
		});
	}

	class MyAsyncTask extends AsyncTask<Void, Void, Void>{
		ProgressDialog dialog = null;
		@Override
		protected void onPreExecute() {
			//线程执行前方法
			super.onPreExecute();
			dialog = new ProgressDialog(MainActivity.this) ;
			dialog.setIcon(R.drawable.ic_launcher);
			dialog.setTitle("请稍后，正在加载...");
			dialog.show();
		}
		
		@Override
		protected Void doInBackground(Void... params) {
			MediaUtil.getInstance().initMusic(MainActivity.this);
			return null;
		}
		@Override
		protected void onPostExecute(Void result) {
			//线程执行后的方法
			super.onPostExecute(result);
			if(dialog!=null){
				dialog.dismiss();
			}
			//测试数据是否获取成功
			List<Music> musicList = MediaUtil.getInstance().getAllMusic();
			for(Music music:musicList){
				Log.i(tag, music.toString());
			}
			adapter.notifyDataSetChanged();
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.IndPlayMode:
			//切换播放模式  列表循环---->列表播放------->随机播放-------->单曲循环
			switch (MediaUtil.CURRENT_MODE) {
			case ConstantValue.MODE_CIRCLE:
				//如果是列表循环，就转变成列表播放
				Toast.makeText(getApplicationContext(), "列表播放", 0).show();
				MediaUtil.CURRENT_MODE = ConstantValue.MODE_ORDER;
				imgPlayMode.setImageResource(R.drawable.icon_playmode_normal);
				break;
			case ConstantValue.MODE_ORDER:
				//如果是列表播放，转变成随机播放
				Toast.makeText(getApplicationContext(), "随机播放", 0).show();
				MediaUtil.CURRENT_MODE = ConstantValue.MODE_RANDOM;
				imgPlayMode.setImageResource(R.drawable.icon_playmode_shuffle);
				break;
			case ConstantValue.MODE_RANDOM:
				//如果是列表循环，就转变成列表播放
				Toast.makeText(getApplicationContext(), "单曲循环", 0).show();
				MediaUtil.CURRENT_MODE = ConstantValue.MODE_SINGLE;
				imgPlayMode.setImageResource(R.drawable.icon_playmode_single);
				break;
			case ConstantValue.MODE_SINGLE:
				//如果是列表循环，就转变成列表播放
				Toast.makeText(getApplicationContext(), "列表循环", 0).show();
				MediaUtil.CURRENT_MODE = ConstantValue.MODE_CIRCLE;
				imgPlayMode.setImageResource(R.drawable.icon_playmode_repeat);
				break;
			}
			break;
		case R.id.btnPrev:
			//上一首
			if(MediaUtil.CURRENTMUSIC>0){
				changeTextColor(Color.WHITE);
				MediaUtil.CURRENTMUSIC --;
				String path = MediaUtil.getInstance().getAllMusic().get(MediaUtil.CURRENTMUSIC).getPath();
				startMediaService(path, ConstantValue.OPTION_PLAY);
				imgPlay.setImageResource(R.drawable.appwidget_pause);
				//在MediaUtil.CURRENTOPTION变化后，将后一个选中播放的歌曲，变成绿色
				changeTextColor(Color.GREEN);
			}
			break;
		case R.id.btnNext:
			//下一首
			if(MediaUtil.CURRENTMUSIC<MediaUtil.getInstance().getAllMusic().size()-1){
				changeTextColor(Color.WHITE);
				MediaUtil.CURRENTMUSIC ++;
				String path = MediaUtil.getInstance().getAllMusic().get(MediaUtil.CURRENTMUSIC).getPath();
				startMediaService(path, ConstantValue.OPTION_PLAY);
				imgPlay.setImageResource(R.drawable.appwidget_pause);
				//在MediaUtil.CURRENTOPTION变化后，将后一个选中播放的歌曲，变成绿色
				changeTextColor(Color.GREEN);
			}
			break;
		case R.id.btnPlay:
			//（播放，暂停）按钮
			switch (MediaUtil.CURRENTOPTION) {
			//停止---->播放----->暂停------>继续播放
			case ConstantValue.OPTION_STOP:
				String path = MediaUtil.getInstance().getAllMusic().get(MediaUtil.CURRENTMUSIC).getPath();
				//开启服务去播放歌曲（传递播放的状态）
				startMediaService(path,ConstantValue.OPTION_PLAY);
				imgPlay.setImageResource(R.drawable.appwidget_pause);
				break;
			case ConstantValue.OPTION_PLAY:
				startMediaService(null, ConstantValue.OPTION_PAUSE);
				imgPlay.setImageResource(R.drawable.img_playback_bt_play);
				break;
			case ConstantValue.OPTION_PAUSE:
				startMediaService(null,ConstantValue.OPTION_CONTINUE);
				imgPlay.setImageResource(R.drawable.appwidget_pause);
				break;
			case ConstantValue.OPTION_CONTINUE:
				startMediaService(null, ConstantValue.OPTION_PAUSE);
				imgPlay.setImageResource(R.drawable.img_playback_bt_play);
				break;
			}
			
			break;
		case R.id.IndMenu:
			if(broadcastReceiver == null){
				broadcastReceiver = new MyBroadcastReceiver();
				IntentFilter filter = new IntentFilter();
				filter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
				filter.addDataScheme("file");
				registerReceiver(broadcastReceiver, filter);
			}
			
			//重新加载sd内容
			Intent intent = new Intent();
			intent.setAction(Intent.ACTION_MEDIA_MOUNTED);
			intent.setData(Uri.parse("file://"+Environment.getExternalStorageDirectory()));
			sendBroadcast(intent);
			
			break;

		}
	}
	
	//播放歌曲的路径，告知服务需要做的事情(状态码)
	private void startMediaService(String path,int option) {
		Intent intent= new Intent(this, MediaService.class);
		intent.putExtra("path", path);
		intent.putExtra("option", option);
		startService(intent);
	}
	
	//播放歌曲的路径，告知服务需要做的事情(状态码)
	private void startMediaService(String path,int option,int position) {
		Intent intent= new Intent(this, MediaService.class);
		intent.putExtra("path", path);
		intent.putExtra("option", option);
		intent.putExtra("position", position);
		startService(intent);
	}
	//改变TextView的颜色
	private	static void changeTextColor(int color) {
		//通过当前播放歌曲的MediaUtil.CURRENTOPTION，去获取需要的TextView，并且对其进行一个颜色的改变
		TextView textView = (TextView) playList.findViewWithTag(MediaUtil.CURRENTMUSIC);
		//防止滚动过程中TextView 不存在
		if(textView!=null){
			textView.setTextColor(color);
		}
		
	}

	public void setLRCText(String lrcString) {
		TextView miniLyricShow = (TextView) findViewById(R.id.MiniLyricShow);
		miniLyricShow.setText(lrcString);
	}
	
	class MyBroadcastReceiver extends BroadcastReceiver{
		@Override
		public void onReceive(Context context, Intent intent) {
			//接收到广播后，需要加载数据
			new MyAsyncTask().execute();
		}
	}
	
	@Override
	protected void onDestroy() {
		if(broadcastReceiver!=null){
			unregisterReceiver(broadcastReceiver);
		}
		super.onDestroy();
		stopService(new Intent(activityActivity, MediaService.class));
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if(event.getKeyCode() == KeyEvent.KEYCODE_BACK){
			//如果点中回退按钮，开启notifycation，然后去跳转到桌面
			showNotifycation();
			
			Intent intent = new Intent();
			intent.setAction(Intent.ACTION_MAIN);
			intent.addCategory(Intent.CATEGORY_HOME);
			startActivity(intent);
		}
		if(event.getKeyCode() == KeyEvent.KEYCODE_MENU){
			//弹出对话框
			AlertDialog alertDialog = new AlertDialog.Builder(this).create();
			alertDialog.setIcon(R.drawable.ic_launcher);
			alertDialog.setTitle("是否退出?");
			alertDialog.setButton("是", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					finish();
				}
			});
			
			alertDialog.setButton2("否", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});
			alertDialog.show();
		}
		return super.onKeyDown(keyCode, event);
	}

	private void showNotifycation() {
		notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		
		Notification notification = new Notification();
		notification.icon = R.drawable.ic_launcher;
		//设置是否点击可以消除一个标示
		notification.flags = Notification.FLAG_AUTO_CANCEL;
		
		Intent intent = new Intent(this,MainActivity.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		notification.setLatestEventInfo(getApplicationContext(), "小霸王音乐", "点击当前提醒返回小霸王音乐界面", pendingIntent);
		
		notificationManager.notify(1,notification);
	}
}
