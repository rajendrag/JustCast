package com.rp.justcast.photos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.ImageColumns;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.ViewGroup;

public class AlbumTabAdapter extends FragmentStatePagerAdapter  {
	private static final String TAG = "AlbumTabAdapter";
	private static final String SHOW_ALBUMS= "com.rp.justcast.albums";
	
	Context ctxt = null;
	List<String> albums = new ArrayList<String>();
	Map<String, ArrayList<String>> albumVsImages = new HashMap<String, ArrayList<String>>();
	FragmentManager mgr = null;
	
	//
	public AlbumTabAdapter(Context ctxt, FragmentManager mgr) {
		super(mgr);
		this.mgr = mgr;
		this.ctxt = ctxt;
		initTabs();
	}

	private void initTabs() {
		Log.d(TAG, "initTabs");
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ctxt);
		int showAlbums = preferences.getInt(SHOW_ALBUMS, 0);
		if(showAlbums == 0) {
			albums.add("All");
			return;
		}
		Cursor albumCursor = null;
		try {
			String[] PROJECTION_BUCKET = { ImageColumns.BUCKET_ID, ImageColumns.BUCKET_DISPLAY_NAME, ImageColumns.DATE_TAKEN, ImageColumns.DATA };
			String BUCKET_GROUP_BY = "1) GROUP BY 1,(2";
			String BUCKET_ORDER_BY = "MAX(datetaken) DESC";

			Uri images = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
			albumCursor = ctxt.getContentResolver().query(images, PROJECTION_BUCKET, BUCKET_GROUP_BY, null, BUCKET_ORDER_BY);

			Log.i("ListingImages", " query count=" + albumCursor.getCount());

			if (albumCursor.moveToFirst()) {
				int bucketColumn = albumCursor.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
				do {
					// Get the field values
					albums.add(albumCursor.getString(bucketColumn));

				} while (albumCursor.moveToNext());
			}
		} finally {
			if (null != albumCursor) {
				albumCursor.close();
			}
		}
	}

	@Override
	public int getCount() {
		return albums.size();
	}

	@Override
	public Fragment getItem(int position) {
		String albumName = albums.get(position);
		ArrayList<String> images = albumVsImages.get(albumName);
		//images_list_container
		return PhotosFragment.newInstance(albumName, images);
	}

	@Override
	public String getPageTitle(int position) {
		return albums.get(position);
	}
	
	/*@Override
	public int getItemPosition(Object object) {
		return POSITION_NONE;
	}*/
	
	/*@Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        FragmentManager manager = ((Fragment)object).getFragmentManager();
        FragmentTransaction trans = manager.beginTransaction();
        trans.remove((Fragment)object);
        trans.commit();
    }*/

}
