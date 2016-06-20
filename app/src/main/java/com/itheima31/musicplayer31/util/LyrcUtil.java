package com.itheima31.musicplayer31.util;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

import com.itheima31.musicplayer31.MainActivity;

import android.util.Log;

/**
 * 
 *
 * 专门用来解析歌词，.lrc;.txt;
 *
 */
public class LyrcUtil {
	private static final String TAG = "LRCUtils";

	private static final String tag = null;

	MainActivity activity;

	private static Vector<Timelrc> lrclist;
	private boolean IsLyricExist = false;
	private int lastLine = 0;

	public Vector<Timelrc> getLrcList(){
		
		return lrclist;
	}
	
	public void setNullLrcList(){
		
		lrclist = null;
	}
	
	public LyrcUtil(MainActivity activityActivity) {
		activity = activityActivity;
	}

	public void RefreshLRC(int current) {
		if (IsLyricExist) {
			for (int i = 1; i < lrclist.size(); i++) {
				//比对方法，通过时间戳给传递进去，比对前一个和后一个的时间，显示相应歌词
				if (current < lrclist.get(i).getTimePoint())
					if (i == 1 || current >= lrclist.get(i - 1).getTimePoint()) {
						//将断定出来的演奏到的歌词传递给activity对应方法
						activity.setLRCText(lrclist.get(i - 1).getLrcString());
						
						lastLine = i - 1;
						// playlrcText.setText(lrclist.get(i-1).getLrcString());
					}
			}
		}
	}
	//封装歌词的做法
	public void ReadLRC(File f) {
		try {
			if (f==null || !f.exists()) {
				IsLyricExist = false;
				lrclist = null;
				activity.setLRCText("歌词不存在");
			} else {
				activity.setLRCText("歌词存在");
				//Vector ArrayList lrclist封装歌词文件的javabean
				
				lrclist = new Vector<Timelrc>();
				IsLyricExist = true;
				
				
				InputStream is = new BufferedInputStream(new FileInputStream(f));
				BufferedReader br = new BufferedReader(new InputStreamReader(
						is, GetCharset(f)));
				String strTemp = "";
				while ((strTemp = br.readLine()) != null) {
					// Log.d(TAG,"strTemp = "+strTemp);
					strTemp = AnalyzeLRC(strTemp); //返回当前的歌词，重点算法，说一下
				}
				br.close();
				is.close();
				//按集合中的时间戳去排序
				Collections.sort(lrclist, new Sort());

				for(int i=0;i<lrclist.size();i++){
					
					Timelrc one = lrclist.get(i);
					if(i+1<lrclist.size()){
						Timelrc two = lrclist.get(i+1);
						//前后两个对象的时间一减就是当前歌词的持续时间
						one.setSleepTime(two.getTimePoint()-one.getTimePoint());
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	//解析一句歌词做法(将歌词进行封装，将歌词截取出来，对象在后面需要对其进行排序)
	private String AnalyzeLRC(String LRCText) {
		try {
			int pos1 = LRCText.indexOf("["); 
			int pos2 = LRCText.indexOf("]"); 
			
			if (pos1 == 0 && pos2 != -1) {
				//看看当前的一行有几个时间，把每个时间的毫秒至存储至time[]数组中，匹配时间戳的个数去生产Long类型的数组
				Long time[] = new Long[GetPossiblyTagCount(LRCText)-1];
				
				Log.i(tag, "time.length = "+time.length);
				
				//存放的就是字符串中第一个时间点的毫秒值
				time[0] = TimeToLong(LRCText.substring(pos1 + 1, pos2)); 
				
				if (time[0] == -1)
					return ""; // LRCText
				String strLineRemaining = LRCText;
				int i = 1;
				//进过while循环后time[]数组中存放的都是时间点的毫秒值
				while (pos1 == 0 && pos2 != -1) {
					//反复截取，直至获取歌词文字
					strLineRemaining = strLineRemaining.substring(pos2 + 1); 
					pos1 = strLineRemaining.indexOf("[");
					pos2 = strLineRemaining.indexOf("]");
					if (pos2 != -1) {
						time[i] = TimeToLong(strLineRemaining.substring(
								pos1 + 1, pos2));
						if (time[i] == -1) 
							return ""; // LRCText
						i++;
					}
				}

				Timelrc tl = new Timelrc();
				//将每个歌词对象存放到集合中去
				for (int j = 0; j < time.length; j++) {
					if (time[j] != null) {
						Log.i(tag, " time["+j+"].intValue() = "+time[j].intValue());
						//设置当前歌词的时间戳
						tl.setTimePoint(time[j].intValue());
						//设置当前歌词至对象
						tl.setLrcString(strLineRemaining);
						//加入集合
						lrclist.add(tl);
						tl = new Timelrc();
						
					}
				}
				return strLineRemaining;
			} else
				return "";
		} catch (Exception e) {
			return "";
		}
	}

	private int GetPossiblyTagCount(String Line) {
		String strCount1[] = Line.split("\\[");
		String strCount2[] = Line.split("\\]");
		if (strCount1.length == 0 && strCount2.length == 0)
			return 1;
		else if (strCount1.length > strCount2.length)
			return strCount1.length;
		else
			return strCount2.length;
	}

	public long TimeToLong(String Time) {
		try {
			String[] s1 = Time.split(":");
			int min = Integer.parseInt(s1[0]);
			String[] s2 = s1[1].split("\\.");
			int sec = Integer.parseInt(s2[0]);
			int mill = 0;
			if (s2.length > 1)
				mill = Integer.parseInt(s2[1]);
			return min * 60 * 1000 + sec * 1000 + mill * 10;
		} catch (Exception e) {
			return -1;
		}
	}

	
	
	public String GetCharset(File file) {
		String charset = "GBK";
		byte[] first3Bytes = new byte[3];
		try {
			boolean checked = false;
			BufferedInputStream bis = new BufferedInputStream(
					new FileInputStream(file));
			bis.mark(0);
			int read = bis.read(first3Bytes, 0, 3);
			if (read == -1)
				return charset;
			if (first3Bytes[0] == (byte) 0xFF && first3Bytes[1] == (byte) 0xFE) {
				charset = "UTF-16LE";
				checked = true;
			} else if (first3Bytes[0] == (byte) 0xFE
					&& first3Bytes[1] == (byte) 0xFF) {
				charset = "UTF-16BE";
				checked = true;
			} else if (first3Bytes[0] == (byte) 0xEF
					&& first3Bytes[1] == (byte) 0xBB
					&& first3Bytes[2] == (byte) 0xBF) {
				charset = "UTF-8";
				checked = true;
			}
			bis.reset();
			if (!checked) {
				int loc = 0;
				while ((read = bis.read()) != -1) {
					loc++;
					if (read >= 0xF0)
						break;
					if (0x80 <= read && read <= 0xBF)
						break;
					if (0xC0 <= read && read <= 0xDF) {
						read = bis.read();
						if (0x80 <= read && read <= 0xBF) 
							continue;
						else
							break;
					} else if (0xE0 <= read && read <= 0xEF) {
						read = bis.read();
						if (0x80 <= read && read <= 0xBF) {
							read = bis.read();
							if (0x80 <= read && read <= 0xBF) {
								charset = "UTF-8";
								break;
							} else
								break;
						} else
							break;
					}
				}
			}
			bis.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return charset;
	}

	private class Sort implements Comparator<Timelrc> {
		public Sort() {
		}

		public int compare(Timelrc tl1, Timelrc tl2) {
			return sortUp(tl1, tl2);
		}

		private int sortUp(Timelrc tl1, Timelrc tl2) {
			if (tl1.getTimePoint() < tl2.getTimePoint())
				return -1;
			else if (tl1.getTimePoint() > tl2.getTimePoint())
				return 1;
			else
				return 0;
		}
	}
	/**
	 * 歌词的实体类
	 * @author yu
	 *
	 */
	public static class Timelrc {
		/**
		 * 歌词内容
		 */
		private String lrcString;
		/**
		 * 歌词显示多长时间
		 */
		private int sleepTime;
		/**
		 * 歌词时间点,也可以叫时间戳
		 */
		private int timePoint;

		Timelrc() {
			lrcString = null;
			sleepTime = 0;
			timePoint = 0;
		}

		public void setLrcString(String lrc) {
			lrcString = lrc;
		}

		public void setSleepTime(int time) {
			sleepTime = time;
		}

		public void setTimePoint(int tPoint) {
			timePoint = tPoint;
		}

		public String getLrcString() {
			return lrcString;
		}

		public int getSleepTime() {
			return sleepTime;
		}

		public int getTimePoint() {
			return timePoint;
		}
	}
} 

