package com.rp.justcast.music;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.support.v4.content.AsyncTaskLoader;

public class MusicAlbumLoader extends AsyncTaskLoader<List<MusicAlbum>> {
	Context mContext;
	boolean isAlbumLoader;
	
	public MusicAlbumLoader(Context context, boolean isAlbumLoader) {
		super(context);
		mContext = context;
		this.isAlbumLoader = isAlbumLoader;
	}

	@Override
	public List<MusicAlbum> loadInBackground() {
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
