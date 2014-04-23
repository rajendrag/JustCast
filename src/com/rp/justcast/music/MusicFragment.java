package com.rp.justcast.music;

import java.util.List;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.View;
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
	
	@Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        MusicAlbum album = (MusicAlbum) musicAlbumsAdapter.getItem(position);
		Fragment f = MusicDetailsFragment.newInstance(album);
		FragmentTransaction mTx =  getFragmentManager().beginTransaction();
		mTx.replace(R.id.content_frame, f).addToBackStack("Albums");
		mTx.commit();
    }


	@Override
	public Loader<List<MusicAlbum>> onCreateLoader(int arg0, Bundle arg1) {
		return new MusicAlbumLoader(getActivity(), null);
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
