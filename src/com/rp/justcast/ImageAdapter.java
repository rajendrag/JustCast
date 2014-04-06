package com.rp.justcast;

import java.util.ArrayList;

import android.app.FragmentManager;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar.LayoutParams;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.rp.justcast.ImageCache.ImageCacheParams;

public class ImageAdapter extends BaseAdapter {
	private static final String TAG = "MainActivity";
	private ImageWorker imageWorker;
	private final Context mContext;
	// A static dataset to back the GridView adapter
	static ArrayList<String> itemList = new ArrayList<String>();
	
	public ImageAdapter(Context context, FragmentManager fm, ImageCacheParams cacheParams) {
		super();
		mContext = context;
		imageWorker = new ImageWorker(context);
		imageWorker.addImageCache(fm, cacheParams);
		imageWorker.setLoadingImage(R.drawable.empty_photo);
		initThumbnails();
	}

	private void initThumbnails() {
		Log.d(TAG, "initThumbnails");
		String[] columns = { MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA };
		String orderBy = MediaStore.Images.Media.DATE_TAKEN;
		Cursor imagecursor = mContext.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns, null, null, orderBy);
		int imageColumnIndex = imagecursor.getColumnIndex(MediaStore.Images.Media.DATA);

		int count = imagecursor.getCount();
		Log.d(TAG, "Count of images" + count);
		for (int i = 0; i < count; i++) {
			imagecursor.moveToPosition(i);
			int dataColumnIndex = imagecursor.getColumnIndex(MediaStore.Images.Media.DATA);

			/*
			 * thumbnails[i] = MediaStore.Images.Thumbnails.getThumbnail(
			 * mContext.getContentResolver(), id,
			 * MediaStore.Images.Thumbnails.MICRO_KIND, null);
			 */

			itemList.add(imagecursor.getString(dataColumnIndex));
			Log.d(TAG, "Image added" + itemList.get(i));
		}
	}

	@Override
	public int getCount() {
		return itemList.size();
	}

	@Override
	public Object getItem(int position) {
		return itemList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup container) {
		ImageView imageView;
		if (convertView == null) { // if it's not recycled, initialize some
									// attributes
			imageView = new ImageView(mContext);
			imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
			imageView.setLayoutParams(new GridView.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		} else {
			imageView = (ImageView) convertView;
		}
		loadBitmap(itemList.get(position), imageView);// Load image into
														// ImageView
		return imageView;
	}

	public void loadBitmap(String resId, ImageView imageView) {
		imageWorker.loadImage(resId, imageView);
	}

}
