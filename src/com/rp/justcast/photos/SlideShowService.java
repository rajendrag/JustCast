package com.rp.justcast.photos;

import java.util.List;

import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.sample.castcompanionlibrary.cast.VideoCastManager;
import com.google.sample.castcompanionlibrary.cast.exceptions.NoConnectionException;
import com.google.sample.castcompanionlibrary.cast.exceptions.TransientNetworkDisconnectionException;
import com.rp.justcast.JustCast;
import com.rp.justcast.util.JustCastUtils;
import com.rp.justcast.R;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.preference.PreferenceManager;

public class SlideShowService extends IntentService {
	private static final String SLIDE_SHOW_INTERVAL = "com.rp.justcast.ssInterval";
	
	VideoCastManager castManager;
	
	public SlideShowService() {
		super("JustCastSlideShowService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		List<String> photos =  (List<String>) intent.getExtras().get("photosList");
		int selectedPosition = intent.getIntExtra("selectedPosition", 0);
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		if(null != photos && selectedPosition < photos.size() ) {
			if(castManager == null) {
				castManager = JustCast.getCastManager();
			}
			if (!castManager.isConnected()) {
				JustCastUtils.showToast(this, R.string.no_device_to_cast);
				JustCast.updateSlideShow(false);
				return;
			}
			JustCast.setSlideShowInProgress(true);
			if(!JustCast.isSlideShowEnabled()) {
				JustCast.toggleSlideShow();
			}
			for(int i = selectedPosition; i< photos.size(); i++) {
				if(!JustCast.isSlideShowEnabled()) {
					break;
				}
				loadMedia(photos.get(i));
				int currentInterval = preferences.getInt(SLIDE_SHOW_INTERVAL, 6);
				SystemClock.sleep(currentInterval*1000);
			}
		}
	}

	@Override
	public void onDestroy() {
		JustCast.updateSlideShow(false);
	}
	
	private void loadMedia(String imagePath) {
		
		MediaMetadata mediaMetadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_PHOTO);
		mediaMetadata.putString(MediaMetadata.KEY_TITLE, "picture");
		String url = JustCast.addJustCastServerParam(imagePath);
		//Log.d(TAG, "Content URL sending to chromecast" + url);
		MediaInfo mediaInfo = new MediaInfo.Builder(url).setContentType("image/jpeg").setStreamType(MediaInfo.STREAM_TYPE_BUFFERED).setMetadata(mediaMetadata).build();
		try {
			castManager.loadMedia(mediaInfo, true, 0);
		} catch (TransientNetworkDisconnectionException e) {
			// e.printStackTrace();
		} catch (NoConnectionException e) {
			// e.printStackTrace();
		}
	}
}
