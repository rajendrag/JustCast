package com.rp.justcast.photos;

import java.util.ArrayList;
import java.util.List;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.sample.castcompanionlibrary.cast.VideoCastManager;
import com.google.sample.castcompanionlibrary.cast.exceptions.NoConnectionException;
import com.google.sample.castcompanionlibrary.cast.exceptions.TransientNetworkDisconnectionException;
import com.rp.justcast.JustCast;
import com.rp.justcast.JustCastUtils;
import com.rp.justcast.R;

public class PhotosFragment extends Fragment implements AdapterView.OnItemClickListener {
	private static final String TAG = "ImageGridFragment";

	private ImageAdapter mAdapter;
	private GridView mGridView;

	/**
	 * Empty constructor as per the Fragment documentation
	 */
	public PhotosFragment() {
	}

	public static final PhotosFragment newInstance(String albumName, ArrayList<String> images) {
		PhotosFragment fragment = new PhotosFragment();
	    Bundle bundle = new Bundle(2);
	    bundle.putString("albumName", albumName);
	    bundle.putStringArrayList("images", images);
	    fragment.setArguments(bundle);
	    return fragment ;
	}
	
	/*public PhotosFragment(String albumName, List<String> images) {
		if (images == null) {
			mAdapter = new ImageAdapter(getActivity(), albumName);
		} else {
			mAdapter = new ImageAdapter(getActivity(), images);
		}
	}*/

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		String albumName = getArguments().getString("albumName");
		ArrayList<String> imagesInAlbum = getArguments().getStringArrayList("images");
		if (imagesInAlbum == null) {
			mAdapter = new ImageAdapter(getActivity(), albumName);
		} else {
			mAdapter = new ImageAdapter(getActivity(), imagesInAlbum);
		}
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
		mAdapter.notifyDataSetChanged();
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@TargetApi(VERSION_CODES.JELLY_BEAN)
	@Override
	public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
		// JustCastUtils.showToast(getActivity(),
		// mAdapter.itemList.get(position));
		if (mGridView != null) {
			mGridView.requestFocusFromTouch();
			mGridView.setSelection((int) (mGridView.getAdapter()).getItemId(position));
		}
		if (JustCast.isSlideShowEnabled()) {
			if(JustCast.isSlideShowInProgress()) {
				Toast t = Toast.makeText(getActivity(), "Slideshow is in-progress", Toast.LENGTH_SHORT);
				t.show();
			} else {
				Intent slideShowIntent = new Intent(getActivity(), SlideShowService.class);
				slideShowIntent.putExtra("photosList", ((ImageAdapter)mAdapter).itemList);
				slideShowIntent.putExtra("selectedPosition", position);
				getActivity().startService(slideShowIntent);
			}
		} else {
			loadMedia(position);
		}

		// sendMessage
	}

	private void loadMedia(int position) {
		VideoCastManager castManager = JustCast.getCastManager(getActivity());
		if (!castManager.isConnected()) {
			JustCastUtils.showToast(getActivity(), R.string.no_device_to_cast);
			return;
		}
		MediaMetadata mediaMetadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_PHOTO);
		mediaMetadata.putString(MediaMetadata.KEY_TITLE, "picture");
		String url = JustCast.addJustCastServerParam(mAdapter.itemList.get(position));
		Log.d(TAG, "Content URL sending to chromecast" + url);
		MediaInfo mediaInfo = new MediaInfo.Builder(url).setContentType("image/png").setStreamType(MediaInfo.STREAM_TYPE_BUFFERED).setMetadata(mediaMetadata).build();
		try {
			castManager.loadMedia(mediaInfo, true, 0);
		} catch (TransientNetworkDisconnectionException e) {
			// e.printStackTrace();
		} catch (NoConnectionException e) {
			// e.printStackTrace();
		}
	}

	
	public List<String> getImagesFromAdapter() {
		return mAdapter.itemList;
	}
}
