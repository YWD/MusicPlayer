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
			//������Ϣ
			switch (msg.what) {
			case ConstantValue.UPDATE_PROGRESS_MSG:
				//����ʱ�䣬���н�����
				Bundle bundle = msg.getData();
				//��ǰ���Ÿ����ܳ���
				int total = bundle.getInt("total", -1);
				//��ǰ���ŵ���λ��
				int position = bundle.getInt("position",-1);
				
				//��ȡָ����ʽ��ʱ��
				String totalstr = MediaUtil.durationToString(total+"");
				//��ȡ������ʱ����ַ�����ʽ
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
					//�б�ѭ����������
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
					//�б�����������
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
					//����ѭ��
					String path_single = MediaUtil.getInstance().getAllMusic().get(MediaUtil.CURRENTMUSIC ).getPath();
					
					Intent intent_single= new Intent(activityActivity, MediaService.class);
					intent_single.putExtra("path", path_single);
					intent_single.putExtra("option", ConstantValue.OPTION_PLAY);
					
					activityActivity.startService(intent_single);
					imgPlay.setImageResource(R.drawable.appwidget_pause);
					MediaUtil.CURRENT_MODE = ConstantValue.MODE_SINGLE;
					break;
				case ConstantValue.MODE_RANDOM:
					//�������
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
		//��ʼ���ײ��ؼ�����
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
				//��ǰ������ҳ
				switch (currentview) {
				case 0:
					//������0ҳ����0ҳΪѡ��״̬
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
				//���ݵ�ǰ��Ӧҳ����������������ҳ
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
				//������ȥ������
				AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
				//��ȡ��ǰ�������������ֵ
				int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
				Log.i(tag, "maxVolume = "+maxVolume);//15
				//���ݻ�ȡ���������ֵ��ȥ���������Ĵ�С
				audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 5, 0);
			}
		});
	}

	private void initProgressView() {
		txtLapse = (TextView) findViewById(R.id.txtLapse);
		txtDuration = (TextView) findViewById(R.id.txtDuration);
		skbGuage = (SeekBar) findViewById(R.id.skbGuage);
		//��������ק���¼�����
		skbGuage.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			
			//ֹͣ�϶�ʱ����÷���
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				//��λ��ǰ�����Ĳ��ŵط�
				int position = seekBar.getProgress();
				startMediaService(null, ConstantValue.PULL_PROGRESS,position);
			}
			//��ʼ�϶�ʱ���÷���
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}
			//�϶������е��÷���
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
				//��MediaUtil.CURRENTOPTIONû�б仯ǰ����ǰһ�����ŵĸ�������ɰ�ɫ
				changeTextColor(Color.WHITE);
				String path = MediaUtil.getInstance().getAllMusic().get(position).getPath();
				startMediaService(path, ConstantValue.OPTION_PLAY);
				imgPlay.setImageResource(R.drawable.appwidget_pause);
				MediaUtil.CURRENTMUSIC  = position;
				//��MediaUtil.CURRENTOPTION�仯�󣬽���һ��ѡ�в��ŵĸ����������ɫ
				changeTextColor(Color.GREEN);
			}
		});
	}

	class MyAsyncTask extends AsyncTask<Void, Void, Void>{
		ProgressDialog dialog = null;
		@Override
		protected void onPreExecute() {
			//�߳�ִ��ǰ����
			super.onPreExecute();
			dialog = new ProgressDialog(MainActivity.this) ;
			dialog.setIcon(R.drawable.ic_launcher);
			dialog.setTitle("���Ժ����ڼ���...");
			dialog.show();
		}
		
		@Override
		protected Void doInBackground(Void... params) {
			MediaUtil.getInstance().initMusic(MainActivity.this);
			return null;
		}
		@Override
		protected void onPostExecute(Void result) {
			//�߳�ִ�к�ķ���
			super.onPostExecute(result);
			if(dialog!=null){
				dialog.dismiss();
			}
			//���������Ƿ��ȡ�ɹ�
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
			//�л�����ģʽ  �б�ѭ��---->�б���------->�������-------->����ѭ��
			switch (MediaUtil.CURRENT_MODE) {
			case ConstantValue.MODE_CIRCLE:
				//������б�ѭ������ת����б���
				Toast.makeText(getApplicationContext(), "�б���", 0).show();
				MediaUtil.CURRENT_MODE = ConstantValue.MODE_ORDER;
				imgPlayMode.setImageResource(R.drawable.icon_playmode_normal);
				break;
			case ConstantValue.MODE_ORDER:
				//������б��ţ�ת����������
				Toast.makeText(getApplicationContext(), "�������", 0).show();
				MediaUtil.CURRENT_MODE = ConstantValue.MODE_RANDOM;
				imgPlayMode.setImageResource(R.drawable.icon_playmode_shuffle);
				break;
			case ConstantValue.MODE_RANDOM:
				//������б�ѭ������ת����б���
				Toast.makeText(getApplicationContext(), "����ѭ��", 0).show();
				MediaUtil.CURRENT_MODE = ConstantValue.MODE_SINGLE;
				imgPlayMode.setImageResource(R.drawable.icon_playmode_single);
				break;
			case ConstantValue.MODE_SINGLE:
				//������б�ѭ������ת����б���
				Toast.makeText(getApplicationContext(), "�б�ѭ��", 0).show();
				MediaUtil.CURRENT_MODE = ConstantValue.MODE_CIRCLE;
				imgPlayMode.setImageResource(R.drawable.icon_playmode_repeat);
				break;
			}
			break;
		case R.id.btnPrev:
			//��һ��
			if(MediaUtil.CURRENTMUSIC>0){
				changeTextColor(Color.WHITE);
				MediaUtil.CURRENTMUSIC --;
				String path = MediaUtil.getInstance().getAllMusic().get(MediaUtil.CURRENTMUSIC).getPath();
				startMediaService(path, ConstantValue.OPTION_PLAY);
				imgPlay.setImageResource(R.drawable.appwidget_pause);
				//��MediaUtil.CURRENTOPTION�仯�󣬽���һ��ѡ�в��ŵĸ����������ɫ
				changeTextColor(Color.GREEN);
			}
			break;
		case R.id.btnNext:
			//��һ��
			if(MediaUtil.CURRENTMUSIC<MediaUtil.getInstance().getAllMusic().size()-1){
				changeTextColor(Color.WHITE);
				MediaUtil.CURRENTMUSIC ++;
				String path = MediaUtil.getInstance().getAllMusic().get(MediaUtil.CURRENTMUSIC).getPath();
				startMediaService(path, ConstantValue.OPTION_PLAY);
				imgPlay.setImageResource(R.drawable.appwidget_pause);
				//��MediaUtil.CURRENTOPTION�仯�󣬽���һ��ѡ�в��ŵĸ����������ɫ
				changeTextColor(Color.GREEN);
			}
			break;
		case R.id.btnPlay:
			//�����ţ���ͣ����ť
			switch (MediaUtil.CURRENTOPTION) {
			//ֹͣ---->����----->��ͣ------>��������
			case ConstantValue.OPTION_STOP:
				String path = MediaUtil.getInstance().getAllMusic().get(MediaUtil.CURRENTMUSIC).getPath();
				//��������ȥ���Ÿ��������ݲ��ŵ�״̬��
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
			
			//���¼���sd����
			Intent intent = new Intent();
			intent.setAction(Intent.ACTION_MEDIA_MOUNTED);
			intent.setData(Uri.parse("file://"+Environment.getExternalStorageDirectory()));
			sendBroadcast(intent);
			
			break;

		}
	}
	
	//���Ÿ�����·������֪������Ҫ��������(״̬��)
	private void startMediaService(String path,int option) {
		Intent intent= new Intent(this, MediaService.class);
		intent.putExtra("path", path);
		intent.putExtra("option", option);
		startService(intent);
	}
	
	//���Ÿ�����·������֪������Ҫ��������(״̬��)
	private void startMediaService(String path,int option,int position) {
		Intent intent= new Intent(this, MediaService.class);
		intent.putExtra("path", path);
		intent.putExtra("option", option);
		intent.putExtra("position", position);
		startService(intent);
	}
	//�ı�TextView����ɫ
	private	static void changeTextColor(int color) {
		//ͨ����ǰ���Ÿ�����MediaUtil.CURRENTOPTION��ȥ��ȡ��Ҫ��TextView�����Ҷ������һ����ɫ�ĸı�
		TextView textView = (TextView) playList.findViewWithTag(MediaUtil.CURRENTMUSIC);
		//��ֹ����������TextView ������
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
			//���յ��㲥����Ҫ��������
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
			//������л��˰�ť������notifycation��Ȼ��ȥ��ת������
			showNotifycation();
			
			Intent intent = new Intent();
			intent.setAction(Intent.ACTION_MAIN);
			intent.addCategory(Intent.CATEGORY_HOME);
			startActivity(intent);
		}
		if(event.getKeyCode() == KeyEvent.KEYCODE_MENU){
			//�����Ի���
			AlertDialog alertDialog = new AlertDialog.Builder(this).create();
			alertDialog.setIcon(R.drawable.ic_launcher);
			alertDialog.setTitle("�Ƿ��˳�?");
			alertDialog.setButton("��", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					finish();
				}
			});
			
			alertDialog.setButton2("��", new DialogInterface.OnClickListener() {
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
		//�����Ƿ�����������һ����ʾ
		notification.flags = Notification.FLAG_AUTO_CANCEL;
		
		Intent intent = new Intent(this,MainActivity.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		notification.setLatestEventInfo(getApplicationContext(), "С��������", "�����ǰ���ѷ���С�������ֽ���", pendingIntent);
		
		notificationManager.notify(1,notification);
	}
}
