package com.itheima31.musicplayer31.domain;

public class Music {
	private String title;
	private String artist;
	private String id;
	private String path;
	private String duration;
	public Music() {
		super();
		// TODO Auto-generated constructor stub
	}
	public Music(String title, String artist, String id, String path,
			String duration) {
		super();
		this.title = title;
		this.artist = artist;
		this.id = id;
		this.path = path;
		this.duration = duration;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getArtist() {
		return artist;
	}
	public void setArtist(String artist) {
		this.artist = artist;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public String getDuration() {
		return duration;
	}
	public void setDuration(String duration) {
		this.duration = duration;
	}
	@Override
	public String toString() {
		return "Music [title=" + title + ", artist=" + artist + ", id=" + id
				+ ", path=" + path + ", duration=" + duration + "]";
	}
	
}
