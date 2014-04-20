package com.rp.justcast.music;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.support.v4.content.AsyncTaskLoader;

public class MusicAlbumLoader extends AsyncTaskLoader<List<MusicAlbum>> {
	Context mContext;
	String albumId;
	
	public MusicAlbumLoader(Context context, String albumId) {
		super(context);
		mContext = context;
		this.albumId = albumId;
	}

	@Override
	public List<MusicAlbum> loadInBackground() {
		if (null != albumId) {
			return getSongsInAlbum();
		}
		List<MusicAlbum> albumList = new ArrayList<MusicAlbum>();
		Cursor musicCursor = null;
		try {
			musicCursor = mContext.getContentResolver().query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
					new String[] { MediaStore.Audio.Albums._ID, MediaStore.Audio.Albums.ALBUM, MediaStore.Audio.Albums.ALBUM_ART, MediaStore.Audio.Albums.ARTIST }, null, null, null);
			int count = musicCursor.getCount();
			// Log.d(TAG, "Count of images" + count);
			for (int i = 0; i < count; i++) {
				musicCursor.moveToPosition(i);
				String albumId = musicCursor.getString(musicCursor.getColumnIndex(MediaStore.Audio.Albums._ID));
				String albumName = musicCursor.getString(musicCursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM));
				String albumArt = musicCursor.getString(musicCursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART));
				String albumArtist = musicCursor.getString(musicCursor.getColumnIndex(MediaStore.Audio.Albums.ARTIST));
				MusicAlbum album = new MusicAlbum(albumId, albumName, albumArtist, albumArt);
				albumList.add(album);
			}
		} finally {
			if (null != musicCursor) {
				musicCursor.close();
			}
		}
		return albumList;
	}
	
	private List<MusicAlbum> getSongsInAlbum() {
		List<MusicAlbum> songsInAlbum = new ArrayList<MusicAlbum>();
		String[] proj = {MediaStore.Audio.Media._ID, MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.DISPLAY_NAME, MediaStore.Audio.Media.ARTIST,
				MediaStore.Audio.Media.DURATION, MediaStore.Audio.Media.SIZE };
		Cursor musicCursor = null;
		try {
			musicCursor = mContext.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, proj, MediaStore.Audio.Media.ALBUM_ID+ "=?", 
	                new String[] {String.valueOf(albumId)}, null);
			int count = musicCursor.getCount();
			// Log.d(TAG, "Count of images" + count);
			for (int i = 0; i < count; i++) {
				musicCursor.moveToPosition(i);
				
				String songId = musicCursor.getString(musicCursor.getColumnIndex(MediaStore.Audio.Media._ID));
				String songTitle = musicCursor.getString(musicCursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME));
				String songArt = musicCursor.getString(musicCursor.getColumnIndex(MediaStore.Audio.Media.DATA));
				String songArtist = musicCursor.getString(musicCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
				String duration = musicCursor.getString(musicCursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
				String size = musicCursor.getString(musicCursor.getColumnIndex(MediaStore.Audio.Media.SIZE));
				MusicAlbum album = new MusicAlbum(songId, songTitle, songArtist, songArt);
				album.setDuration(duration);
				album.setSize(size);
				songsInAlbum.add(album);
				
			}
		} finally {
			if (null != musicCursor) {
				musicCursor.close();
			}
		}
		return songsInAlbum;
	}
	
	@Override
    protected void onStartLoading() {
        super.onStartLoading();
        forceLoad();
    }

    /**
     * Handles a request to stop the Loader.
     */
    @Override
    protected void onStopLoading() {
        // Attempt to cancel the current load task if possible.
        cancelLoad();
    }


}
