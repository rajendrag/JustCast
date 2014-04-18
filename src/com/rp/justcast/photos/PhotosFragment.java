package com.rp.justcast.photos;

import android.annotation.TargetApi;
import android.app.Fragment;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.RemoteMediaPlayer;
import com.google.android.gms.cast.RemoteMediaPlayer.MediaChannelResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.sample.castcompanionlibrary.cast.exceptions.NoConnectionException;
import com.google.sample.castcompanionlibrary.cast.exceptions.TransientNetworkDisconnectionException;
import com.rp.justcast.JustCast;
import com.rp.justcast.JustCastUtils;
import com.rp.justcast.R;

public class PhotosFragment extends Fragment implements AdapterView.OnItemClickListener {
	private static final String TAG = "ImageGridFragment";
	private static final String IMAGE_CACHE_DIR = "thumbs";

	private int mImageThumbSize;
	private int mImageThumbSpacing;
	private ImageAdapter mAdapter;
	private ImageWorker mImageFetcher;
	private GridView mGridView; 

	/**
	 * Empty constructor as per the Fragment documentation
	 */
	public PhotosFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);

		mImageThumbSize = getResources().getDimensionPixelSize(R.dimen.image_thumbnail_size);
		mImageThumbSpacing = getResources().getDimensionPixelSize(R.dimen.image_thumbnail_spacing);

		ImageCache.ImageCacheParams cacheParams = new ImageCache.ImageCacheParams(getActivity(), IMAGE_CACHE_DIR);

		cacheParams.setMemCacheSizePercent(0.25f); // Set memory cache to 25% of
													// app memory

		// The ImageFetcher takes care of loading images into our ImageView
		// children asynchronously
		mImageFetcher = new ImageWorker(getActivity());
		mImageFetcher.setLoadingImage(R.drawable.empty_photo);
		mImageFetcher.addImageCache(getFragmentManager(), cacheParams);
		mAdapter = new ImageAdapter(getActivity(), mImageFetcher);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		final View v = inflater.inflate(R.layout.image_grid_fragment, container, false);
		mGridView = (GridView) v.findViewById(R.id.imageGrid);
		mGridView.setAdapter(mAdapter);
		mGridView.setOnItemClickListener(this);
		mGridView.setSelector(R.drawable.list_selector);
		mGridView.setDrawSelectorOnTop(true);
		mGridView.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
		return v;
	}

	@Override
	public void onResume() {
		super.onResume();
		mImageFetcher.setExitTasksEarly(false);
		mAdapter.notifyDataSetChanged();
	}

	@Override
	public void onPause() {
		super.onPause();
		mImageFetcher.setPauseWork(false);
		mImageFetcher.setExitTasksEarly(true);
		mImageFetcher.flushCache();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mImageFetcher.closeCache();
	}

	@TargetApi(VERSION_CODES.JELLY_BEAN)
	@Override
	public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
		//JustCastUtils.showToast(getActivity(), mAdapter.itemList.get(position));
		if(mGridView != null) {
			mGridView.requestFocusFromTouch();
			mGridView.setSelection((int)(mGridView.getAdapter()).getItemId(position));
		}
		MediaMetadata mediaMetadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_PHOTO);
		mediaMetadata.putString(MediaMetadata.KEY_TITLE, "My picture");
		String url = JustCast.addJustCastServerParam(mAdapter.itemList.get(position));
		Log.d(TAG, "Content URL sending to chromecast" + url);
		MediaInfo mediaInfo = new MediaInfo.Builder(url).setContentType("image/png").setStreamType(MediaInfo.STREAM_TYPE_BUFFERED).setMetadata(mediaMetadata).build();
		try {
			JustCast.getCastManager(getActivity()).loadMedia(mediaInfo, true, 0);
		} catch (TransientNetworkDisconnectionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoConnectionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// sendMessage
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// inflater.inflate(R.menu.main_menu, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		/*
		 * switch (item.getItemId()) { case R.id.clear_cache:
		 * mImageFetcher.clearCache(); Toast.makeText(getActivity(),
		 * R.string.clear_cache_complete_toast, Toast.LENGTH_SHORT).show();
		 * return true; }
		 */
		return super.onOptionsItemSelected(item);
	}

}
