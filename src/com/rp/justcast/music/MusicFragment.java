package com.rp.justcast.music;

import java.util.List;

import android.annotation.TargetApi;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.rp.justcast.R;

public class MusicFragment extends ListFragment  implements LoaderManager.LoaderCallbacks<List<MusicAlbum>> {
	private static final String TAG = MusicFragment.class.getSimpleName();
	MusicAlbumsAdapter musicAlbumsAdapter = null;
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		getListView().setFastScrollEnabled(true);
		musicAlbumsAdapter = new MusicAlbumsAdapter(getActivity());
		setListAdapter(musicAlbumsAdapter);
		setListShown(false);
		getLoaderManager().initLoader(0, null, this);
	}

	/*@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		musicAlbumsAdapter = new MusicAlbumsAdapter(getActivity());
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View v = inflater.inflate(R.layout.music_list_view, container, false);
		listView = (ListView) v.findViewById(R.id.music_list);
		listView.setAdapter(musicAlbumsAdapter);
		listView.setOnItemClickListener(new OnAlbumClickListener());
		return v;
	}
	*/
	private class OnAlbumClickListener implements OnItemClickListener {
		
		@TargetApi(VERSION_CODES.JELLY_BEAN)
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			MusicAlbum album = (MusicAlbum) musicAlbumsAdapter.getItem(position);
			Fragment f = MusicDetailsFragment.newInstance(album.getAlbumId());
			FragmentTransaction mTx =  getFragmentManager().beginTransaction();
			mTx.replace(R.id.music_list, f).addToBackStack(null);
			mTx.commit();
		}
		
	}

	@Override
	public Loader<List<MusicAlbum>> onCreateLoader(int arg0, Bundle arg1) {
		return new MusicAlbumLoader(getActivity(), true);
	}


	@Override
	public void onLoadFinished(Loader<List<MusicAlbum>> arg0, List<MusicAlbum> data) {
		musicAlbumsAdapter.setData(data);
		if (isResumed()) {
			setListShown(true);
		} else {
			setListShownNoAnimation(true);
		}
	}


	@Override
	public void onLoaderReset(Loader<List<MusicAlbum>> arg0) {
		musicAlbumsAdapter.setData(null);		
	}

}
