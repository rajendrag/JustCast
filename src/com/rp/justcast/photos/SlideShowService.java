package com.rp.justcast.photos;

import java.util.List;

import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.sample.castcompanionlibrary.cast.VideoCastManager;
import com.google.sample.castcompanionlibrary.cast.exceptions.NoConnectionException;
import com.google.sample.castcompanionlibrary.cast.exceptions.TransientNetworkDisconnectionException;
import com.rp.justcast.JustCast;
import com.rp.justcast.JustCastUtils;
import com.rp.justcast.R;

import android.app.IntentService;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

public class SlideShowService extends IntentService {
	
	VideoCastManager castManager;
	
	public SlideShowService() {
		super("JustCastSlideShowService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		List<String> photos =  (List<String>) intent.getExtras().get("photosList");
		int selectedPosition = intent.getIntExtra("selectedPosition", 0);
		if(null != photos && selectedPosition < photos.size() ) {
			if(castManager == null) {
				castManager = JustCast.getCastManager(this);
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
				SystemClock.sleep(6000);
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
		MediaInfo mediaInfo = new MediaInfo.Builder(url).setContentType("image/png").setStreamType(MediaInfo.STREAM_TYPE_BUFFERED).setMetadata(mediaMetadata).build();
		try {
			castManager.loadMedia(mediaInfo, true, 0);
		} catch (TransientNetworkDisconnectionException e) {
			// e.printStackTrace();
		} catch (NoConnectionException e) {
			// e.printStackTrace();
		}
	}
}
