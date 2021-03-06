package com.rp.justcast.photos;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.preference.PreferenceManager;

import com.google.android.libraries.cast.companionlibrary.cast.VideoCastManager;
import com.rp.justcast.JustCast;
import com.rp.justcast.R;
import com.rp.justcast.util.JustCastUtils;

import java.io.File;
import java.util.List;

/**
 * Created by rp on 2/19/15.
 */
public class SlideShow {

    private static final String SLIDE_SHOW_INTERVAL = "com.rp.justcast.ssInterval";

    private List<String> queue;

    private int position;

    private VideoCastManager castManager;

    public SlideShow (List<String> queue, int startPosition) {
        this.queue = queue;
        this.position = startPosition;
    }

    public void start() {
        loadMedia(queue.get(position));
        position++;
    }

    public void sendNext() {

        if (position < queue.size()) {
            if(castManager == null) {
                castManager = VideoCastManager.getInstance();
            }
            if (!castManager.isConnected()) {
                JustCastUtils.showToast(JustCast.getmAppContext(), R.string.no_device_to_cast);
                JustCast.updateSlideShow(false);
                return;
            }
            //JustCast.setSlideShowInProgress(true);
            String file = queue.get(position);
            new SlideShowSenderTask().execute(file);
        } else {
            JustCast.updateSlideShow(false);
        }
        position++;
    }

    private void loadMedia(String filePath) {
        if(!JustCast.isSlideShowEnabled()) {
            JustCast.toggleSlideShow();
        } else {
            // compress and send
            File f = new File(filePath);
            JustCastUtils.compressAndLoadMedia(f);
        }
    }

    private class SlideShowSenderTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {
            String path = params[0];
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(JustCast.getmAppContext());
            final int currentInterval = preferences.getInt(SLIDE_SHOW_INTERVAL, 6);
            SystemClock.sleep(currentInterval * 1000);
            loadMedia(path);
            return null;
        }
    }
}
