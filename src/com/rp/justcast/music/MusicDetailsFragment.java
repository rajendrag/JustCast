package com.rp.justcast.music;

import java.util.List;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.common.images.WebImage;
import com.google.sample.castcompanionlibrary.utils.Utils;
import com.rp.justcast.JustCast;
import com.rp.justcast.R;
import com.rp.justcast.photos.ImageWorker;
import com.rp.justcast.video.LocalPlayerActivity;

public class MusicDetailsFragment extends ListFragment implements LoaderManager.LoaderCallbacks<List<MusicAlbum>> {
	private static final String TAG = MusicDetailsFragment.class.getSimpleName();

	MusicDetailsAdapter musicDetailsAdapter = null;
	ListView listView = null;

	MusicAlbum album;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		getListView().setFastScrollEnabled(true);
        ImageWorker imageWorker = JustCast.getImageWorker(getFragmentManager());
		musicDetailsAdapter = new MusicDetailsAdapter(getActivity(), imageWorker);
		setEmptyText(getString(R.string.no_music_found));
		setListAdapter(musicDetailsAdapter);
		setListShown(false);
		getLoaderManager().initLoader(0, null, this);
	}

	/*
	 * @Override public View onCreateView(LayoutInflater inflater, ViewGroup
	 * container, Bundle savedInstanceState) { final View v =
	 * inflater.inflate(R.layout.music_details_view, container, false); listView
	 * = (ListView) v.findViewById(R.id.music_details);
	 * listView.setAdapter(musicAlbumsAdapter);
	 * listView.setOnItemClickListener(this); return v; }
	 */

	/*
	 * @TargetApi(VERSION_CODES.JELLY_BEAN)
	 * 
	 * @Override public void onItemClick(AdapterView<?> parent, View v, int
	 * position, long id) {
	 * 
	 * // JustCastUtils.showToast(getActivity(), //
	 * mAdapter.itemList.get(position)); if (listView != null) {
	 * listView.requestFocusFromTouch(); listView.setSelection((int)
	 * (listView.getAdapter()).getItemId(position)); } VideoCastManager
	 * castManager = JustCast.getCastManager(getActivity());
	 * 
	 * if (!castManager.isConnected()) { JustCastUtils.showToast(getActivity(),
	 * R.string.no_device_to_cast); return; }
	 * 
	 * MusicAlbum album = (MusicAlbum) musicAlbumsAdapter.getItem(position);
	 * MediaMetadata mm = new
	 * MediaMetadata(MediaMetadata.MEDIA_TYPE_MUSIC_TRACK);
	 * mm.putString(MediaMetadata.KEY_ALBUM_TITLE, album.getAlbumTitle());
	 * mm.addImage(new
	 * WebImage(Uri.parse(JustCast.addJustCastServerParam(album.getAlbumArt
	 * ())))); // mm.putString(MediaMetadata.KEY_TITLE, //
	 * musicCursor.getString(displayNameIndex)); String path =
	 * JustCast.addJustCastServerParam(album.getAlbumId()); Log.d(TAG,
	 * "Music track path=>" + path); MediaInfo mediaInfo = new
	 * MediaInfo.Builder(
	 * path).setStreamType(MediaInfo.STREAM_TYPE_BUFFERED).setContentType
	 * (getMediaType()).setMetadata(mm).build(); try {
	 * castManager.loadMedia(mediaInfo, true, 0); } catch
	 * (TransientNetworkDisconnectionException e) { // e.printStackTrace(); }
	 * catch (NoConnectionException e) { // e.printStackTrace(); }
	 * 
	 * // sendMessage }
	 */

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		MusicAlbum selectedMedia = musicDetailsAdapter.getItem(position);
		if(JustCast.isSlideShowInProgress()) {
        	Toast t = Toast.makeText(getActivity(), "Slideshow ended", Toast.LENGTH_SHORT);
        	t.show();
        	//JustCast.setSlideShowInProgress(false);
        	if(JustCast.isSlideShowEnabled()) {
        		JustCast.toggleSlideShow();
        	}
        }
		handleNavigation(selectedMedia, false);
	}

	/*private void handleNavigation(MediaInfo info, boolean autoStart) {
        Intent intent = new Intent(getActivity(), LocalPlayerActivity.class);
        intent.putExtra("media", Utils.fromMediaInfo(info));
        intent.putExtra("shouldStart", autoStart);
        getActivity().startActivity(intent);
    }*/
	
	private void handleNavigation(MusicAlbum album, boolean autoStart) {
		// JustCastUtils.showToast(getActivity(),
		// mAdapter.itemList.get(position));
		if (listView != null) {
			listView.requestFocusFromTouch();
			//listView.setSelection((int) (listView.getAdapter()).getItemId(position));
		}
		/*VideoCastManager castManager = JustCast.getCastManager(getActivity());

		if (!castManager.isConnected()) {
			JustCastUtils.showToast(getActivity(), R.string.no_device_to_cast);
			return;
		}*/
		/*MediaMetadata movieMetadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE);
		movieMetadata.putString("VIDEO_PATH", path);
		movieMetadata.putString(MediaMetadata.KEY_SUBTITLE, title);
		movieMetadata.putString(MediaMetadata.KEY_TITLE, title);
		movieMetadata.putString(MediaMetadata.KEY_STUDIO, title);*/
		
		MediaMetadata mm = new MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE);
		mm.putString(MediaMetadata.KEY_TITLE, this.album.getAlbumTitle());
		mm.putString(MediaMetadata.KEY_SUBTITLE, album.getAlbumTitle());
		mm.putString(MediaMetadata.KEY_ARTIST, album.getAlbumArtist());
		mm.putString(MediaMetadata.KEY_STUDIO, this.album.getAlbumTitle());
		mm.addImage(new WebImage(Uri.parse(JustCast.addJustCastServerParam(album.getAlbumArt()))));
		// mm.putString(MediaMetadata.KEY_TITLE,
		// musicCursor.getString(displayNameIndex));
		String path = JustCast.addJustCastServerParam(album.getAlbumId());
		Log.d(TAG, "Music track path=>" + path);
		MediaInfo mediaInfo = new MediaInfo.Builder(path).setStreamType(MediaInfo.STREAM_TYPE_BUFFERED).setContentType(getMediaType()).setMetadata(mm).build();
		try {
			Intent intent = new Intent(getActivity(), LocalPlayerActivity.class);
	        intent.putExtra("media", Utils.fromMediaInfo(mediaInfo));
	        intent.putExtra("shouldStart", autoStart);
	        getActivity().startActivity(intent);
			//castManager.loadMedia(mediaInfo, true, 0);
		} catch (Exception e) {
			// e.printStackTrace();
		}

	}

	public static MusicDetailsFragment newInstance(MusicAlbum album) {
		MusicDetailsFragment f = new MusicDetailsFragment();
		f.album = album;
		Bundle b = new Bundle();
		f.setArguments(b);
		return f;
	}

	public static MusicDetailsFragment newInstance(MusicAlbum album, Bundle b) {
		MusicDetailsFragment f = new MusicDetailsFragment();
		f.album = album;
		f.setArguments(b);
		return f;
	}

	private String getMediaType() {
		return JustCast.MUSIC_CONTENT_TYPE;
	}

	@Override
	public Loader<List<MusicAlbum>> onCreateLoader(int arg0, Bundle arg1) {
		return new MusicAlbumLoader(getActivity(), album);
	}

	@Override
	public void onLoadFinished(Loader<List<MusicAlbum>> arg0, List<MusicAlbum> data) {
		musicDetailsAdapter.setData(data);
		if (isResumed()) {
			setListShown(true);
		} else {
			setListShownNoAnimation(true);
		}

	}

	@Override
	public void onLoaderReset(Loader<List<MusicAlbum>> arg0) {
		musicDetailsAdapter.setData(null);
	}

}
