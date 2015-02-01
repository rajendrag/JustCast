package com.rp.justcast.photos;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.rp.justcast.JustCast;
import com.rp.justcast.R;

public class GalleryTabFragment extends Fragment {

	private static final String SHOW_ALBUMS = "com.rp.justcast.albums";
	private static final String SLIDE_SHOW_INTERVAL = "com.rp.justcast.ssInterval";

	private AlbumTabAdapter mAdapter;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View result = inflater.inflate(R.layout.imagepager, container, false);
		ViewPager pager = (ViewPager) result.findViewById(R.id.imagePager);
		pager.setAdapter(buildAdapter());
		setHasOptionsMenu(true);
		return (result);
	}

	private PagerAdapter buildAdapter() {
		// int slideShowInterval = preferences.getInt(SLIDE_SHOW_INTERVAL, 6);
		mAdapter = new AlbumTabAdapter(getActivity(), getChildFragmentManager());
		// adapter.notifyDataSetChanged();
		return mAdapter;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// inflater.inflate(R.menu.main_menu, menu);
		MenuItem item = menu.add(Menu.NONE, 111, 10, R.string.slideshow);
		item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		if (JustCast.isSlideShowEnabled()) {
			item.setIcon(R.drawable.ic_av_pause_light);
		} else {
			item.setIcon(R.drawable.ic_av_play_light);
		}
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
		int albums = preferences.getInt(SHOW_ALBUMS, 0);
		// if albums is 0, then we are showing all photos together and we need
		// to enable the 'Show Albums' option
		// if albums is 1, we are showing 'Albums' and we need to enable the
		// 'All Photos' option
		if (albums == 0) {
			MenuItem showAlbums = menu.add(Menu.NONE, 112, 12, R.string.show_albums);
			showAlbums.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
		} else {
			MenuItem showAll = menu.add(Menu.NONE, 113, 13, R.string.all_photos);
			showAll.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
		}
		MenuItem interval = menu.add(Menu.NONE, 114, 14, R.string.slideshow_interval);
		interval.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
		switch (item.getItemId()) {
		case 111:
			JustCast.toggleSlideShow();
			if (JustCast.isSlideShowEnabled()) {
				if (!JustCast.getCastManager().isConnected()) {
					Toast.makeText(getActivity(), R.string.no_device_to_cast, Toast.LENGTH_LONG).show();
					JustCast.updateSlideShow(false);
					item.setIcon(R.drawable.ic_av_play_light);
				} else {
					item.setIcon(R.drawable.ic_av_pause_light);
					Toast.makeText(getActivity(), R.string.select_start_image, Toast.LENGTH_LONG).show();
				}
			} else {
				if (JustCast.isSlideShowInProgress()) {
					JustCast.updateSlideShow(false);
				}
				item.setIcon(R.drawable.ic_av_play_light);
			}
			break;
		case 112:
			// clicked on 'Show Albums'
			preferences.edit().putInt(SHOW_ALBUMS, 1).commit();
			refresh();
			break;
		case 113:
			// clicked on 'All Photos'
			preferences.edit().putInt(SHOW_ALBUMS, 0).commit();
			refresh();
			break;
		case 114:
			showSlideShowSettingsDialog();
			break;

		}
		return super.onOptionsItemSelected(item);
	}

	private void refresh() {
		Fragment frg = null;
		frg = getFragmentManager().findFragmentByTag("GalleryTabFragment");
		final FragmentTransaction ft = getFragmentManager().beginTransaction();
		ft.remove(frg);
		// ft.attach(frg);
		ft.commit();

		final FragmentTransaction newTx = getFragmentManager().beginTransaction();
		Fragment fg = new GalleryTabFragment();
		newTx.replace(R.id.content_frame, fg, "GalleryTabFragment");
		newTx.commit();
	}

	public void showSlideShowSettingsDialog() {
		final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
		final int currentInterval = preferences.getInt(SLIDE_SHOW_INTERVAL, 6);
		final MyInt current = new MyInt();
		current.tempInt = currentInterval;
		View layout = View.inflate(getActivity(), R.layout.slideshow_settings_view, null);
	    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity()).setView(layout);
	    //AlertDialog alertDialog = builder.create();
	    //alertDialog.show();
	    SeekBar sb = (SeekBar)layout.findViewById(R.id.interval_seekbar);
	    final TextView txtView = (TextView) layout.findViewById(R.id.current_interval);
	    txtView.setKeyListener(null);
	    txtView.setText(currentInterval + " Seconds");
	    sb.setProgress(currentInterval);
	    sb.setMax(60);
	    sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
	        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser){
	        	current.tempInt = progress;
	        	txtView.setText(progress +" Seconds");
	        }

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			}
	    });
		
		// Button OK
	    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				preferences.edit().putInt(SLIDE_SHOW_INTERVAL, current.tempInt).commit();
				dialog.dismiss();
			}

		});

	    builder.create();
	    builder.show();

	}
	class MyInt {
		int tempInt;
	}
}
