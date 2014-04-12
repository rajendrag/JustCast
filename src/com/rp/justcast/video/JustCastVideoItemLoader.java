package com.rp.justcast.video;

import java.util.List;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import com.google.android.gms.cast.MediaInfo;

public class JustCastVideoItemLoader  extends AsyncTaskLoader<List<MediaInfo>> {

	private static final String TAG = "JustCastVideoItemLoader";
    private final String mUrl;

    public JustCastVideoItemLoader(Context context, String url) {
        super(context);
        this.mUrl = url;
    }

    @Override
    public List<MediaInfo> loadInBackground() {
        try {
            return JustCastVideoProvider.buildMedia(mUrl);
        } catch (Exception e) {
            Log.e(TAG, "Failed to fetch media data", e);
            return null;
        }
    }

    @Override
    protected void onStartLoading() {
        super.onStartLoading();
        forceLoad();
    }

    /**
     * Handles a request to stop the Loader.
     */
    @Override
    protected void onStopLoading() {
        // Attempt to cancel the current load task if possible.
        cancelLoad();
    }
}