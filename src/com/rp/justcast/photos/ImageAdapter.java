package com.rp.justcast.photos;

import java.util.ArrayList;

import com.rp.justcast.JustCast;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar.LayoutParams;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

public class ImageAdapter extends BaseAdapter {
	private static final String TAG = "MainActivity";
	public ImageWorker imageWorker;
	private final Context mContext;
	public ArrayList<String> itemList = new ArrayList<String>();
	
	private int mItemHeight = 0;
    private int mNumColumns = 0;
    private int mActionBarHeight = 0;
	
	public ImageAdapter(Context context) {
		super();
        mContext = context;
        imageWorker = JustCast.getImageWorker();
        // Calculate ActionBar height
        TypedValue tv = new TypedValue();
        if (context.getTheme().resolveAttribute(
                android.R.attr.actionBarSize, tv, true)) {
            mActionBarHeight = TypedValue.complexToDimensionPixelSize(
                    tv.data, context.getResources().getDisplayMetrics());
        }
		initThumbnails();
	}

	private void initThumbnails() {
		Log.d(TAG, "initThumbnails");
		Cursor imageCursor = null;
		try {
			String[] columns = { MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA };
			String orderBy = MediaStore.Images.Media.DATE_TAKEN+ " desc" ;
			imageCursor = mContext.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns, null, null, orderBy);
			//int imageColumnIndex = imagecursor.getColumnIndex(MediaStore.Images.Media.DATA);
			if (null != imageCursor) {
				int count = imageCursor.getCount();
				Log.d(TAG, "Count of images" + count);
				for (int i = 0; i < count; i++) {
					imageCursor.moveToPosition(i);
					int dataColumnIndex = imageCursor.getColumnIndex(MediaStore.Images.Media.DATA);
					itemList.add(imageCursor.getString(dataColumnIndex));
					Log.d(TAG, "Image added" + itemList.get(i));
				}
			}
		} finally {
			if (null != imageCursor) {
				imageCursor.close();
			}
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
		if (convertView == null) { 
			Resources r = Resources.getSystem();
            float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 110, r.getDisplayMetrics());
			imageView = new ImageView(mContext);
			imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
			imageView.setLayoutParams(new GridView.LayoutParams((int)px, (int)px));
		} else {
			imageView = (ImageView) convertView;
		}
		
		loadBitmap(itemList.get(position), imageView);
		return imageView;
	}

	public void loadBitmap(String resId, ImageView imageView) {
		imageWorker.loadImage(resId, imageView);
	}
}
