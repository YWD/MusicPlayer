package com.itheima31.musicplayer31.adapter;

import com.itheima31.musicplayer31.R;
import com.itheima31.musicplayer31.util.MediaUtil;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class MusicListAdapter extends BaseAdapter {
	private Context context;
	public MusicListAdapter(Context context){
		this.context = context;
	}
	@Override
	public int getCount() {
		return MediaUtil.getInstance().getAllMusic().size();
	}

	@Override
	public Object getItem(int position) {
		return MediaUtil.getInstance().getAllMusic().get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHoder viewHoder = null;
		if(convertView == null){
			viewHoder  = new ViewHoder();
			convertView = View.inflate(context, R.layout.listitem, null);
			viewHoder.listItemName = (TextView) convertView.findViewById(R.id.ListItemName);
			viewHoder.listItemContent = (TextView) convertView.findViewById(R.id.ListItemContent);
			
			convertView.setTag(viewHoder);
		}else{
			viewHoder = (ViewHoder) convertView.getTag();
		}
		
		viewHoder.listItemName.setText(MediaUtil.getInstance().getAllMusic().get(position).getTitle());
		viewHoder.listItemContent.setText(MediaUtil.getInstance().getAllMusic().get(position).getArtist());
		
		if(MediaUtil.CURRENTMUSIC == position){
			viewHoder.listItemName.setTextColor(Color.GREEN);
		}else{
			viewHoder.listItemName.setTextColor(Color.WHITE);
		}
		//将所有歌曲的名称都定位
		viewHoder.listItemName.setTag(position);
		return convertView;
	}

	class ViewHoder{
		TextView listItemName;
		TextView listItemContent;
	}
}
