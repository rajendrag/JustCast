package com.rp.justcast;

import java.util.ArrayList;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

public class ImageAdapter extends BaseAdapter {
	private static final String TAG = "MainActivity";
	private Context mContext;
	ArrayList<String> itemList = new ArrayList<String>();
	
	public ImageAdapter(Context c) {
		mContext = c;
		initThumbnails();
	}

	private void initThumbnails() {
		Log.d(TAG, "initThumbnails");
		String[] columns = {MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA };
	    String orderBy = MediaStore.Images.Media.DATE_TAKEN;
	    Cursor imagecursor = mContext.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns, null,null, orderBy);
	    int imageColumnIndex = imagecursor.getColumnIndex(MediaStore.Images.Media.DATA);

	    int count = imagecursor.getCount();
	    Log.d(TAG, "Count of images"+count);
	    for (int i = 0; i < count; i++) {
	        imagecursor.moveToPosition(i);
	        int dataColumnIndex = imagecursor.getColumnIndex(MediaStore.Images.Media.DATA);
	        
	        /*thumbnails[i] = MediaStore.Images.Thumbnails.getThumbnail(
	                mContext.getContentResolver(), id,
	                MediaStore.Images.Thumbnails.MICRO_KIND, null);*/

	        add(imagecursor.getString(dataColumnIndex));
	        Log.d(TAG, "Image added"+itemList.get(i));
	    }
	}
	
	void add(String path) {
		itemList.add(path);
	}

	@Override
	public int getCount() {
		return itemList.size();
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ImageView imageView;
		if (convertView == null) { // if it's not recycled, initialize some
									// attributes
			imageView = new ImageView(mContext);
			imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
			//imageView.setImageURI(Uri.parse(itemList.get(position)));
			imageView.setPadding(8, 8, 8, 8);
			//imageView.setLayoutParams(new GridView.LayoutParams(100, 100));
		} else {
			imageView = (ImageView) convertView;
		}

		Bitmap bm = decodeSampledBitmapFromUri(itemList.get(position), 80, 80);

		imageView.setImageBitmap(bm);
		return imageView;
	}

	public Bitmap decodeSampledBitmapFromUri(String path, int reqWidth,
			int reqHeight) {

		Bitmap bm = null;
		// First decode with inJustDecodeBounds=true to check dimensions
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(path, options);

		// Calculate inSampleSize
		options.inSampleSize = calculateInSampleSize(options, reqWidth,
				reqHeight);

		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;
		bm = BitmapFactory.decodeFile(path, options);

		return bm;
	}

	public int calculateInSampleSize(

	BitmapFactory.Options options, int reqWidth, int reqHeight) {
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {
			if (width > height) {
				inSampleSize = Math.round((float) height / (float) reqHeight);
			} else {
				inSampleSize = Math.round((float) width / (float) reqWidth);
			}
		}

		return inSampleSize;
	}
}