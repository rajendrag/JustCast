package com.rp.justcast.music;

public class MusicAlbum {
	private String albumId;

	private String albumTitle;

	private String albumArtist;

	private String albumArt;
	
	private String duration;
	
	private String size;

	public MusicAlbum() {
		
	}
	
	public MusicAlbum(String albumId, String albumTitle, String albumArtist, String albumArt) {
		this.albumId = albumId;
		this.albumTitle = albumTitle;
		this.albumArtist = albumArtist;
		this.albumArt = albumArt;
	}
	
	public String getAlbumId() {
		return albumId;
	}

	public void setAlbumId(String albumId) {
		this.albumId = albumId;
	}

	public String getAlbumTitle() {
		return albumTitle;
	}

	public void setAlbumTitle(String albumTitle) {
		this.albumTitle = albumTitle;
	}

	public String getAlbumArtist() {
		return albumArtist;
	}

	public void setAlbumArtist(String albumArtist) {
		this.albumArtist = albumArtist;
	}

	public String getAlbumArt() {
		return albumArt;
	}

	public void setAlbumArt(String albumArt) {
		this.albumArt = albumArt;
	}

	public String getDuration() {
		return duration;
	}

	public void setDuration(String duration) {
		this.duration = duration;
	}

	public String getSize() {
		return size;
	}

	public void setSize(String size) {
		this.size = size;
	}

}
