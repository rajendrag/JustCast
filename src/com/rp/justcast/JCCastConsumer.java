package com.rp.justcast;

import android.os.Handler;
import android.support.v7.media.MediaRouter;
import android.util.Log;

import com.google.android.gms.cast.ApplicationMetadata;
import com.google.android.gms.cast.CastDevice;
import com.google.android.gms.cast.TextTrackStyle;
import com.google.android.gms.common.ConnectionResult;
import com.google.sample.castcompanionlibrary.cast.callbacks.IVideoCastConsumer;
import com.google.sample.castcompanionlibrary.cast.callbacks.VideoCastConsumerImpl;
import com.rp.justcast.settings.CastPreference;
import com.rp.justcast.util.JustCastUtils;

import java.util.Locale;

/**
 * Created by rp on 2/14/15.
 */
public class JCCastConsumer extends VideoCastConsumerImpl {

    private static final String TAG = "JCCastConsumer";

    @Override
    public void onDataMessageSendFailed(int errorCode) {
        super.onDataMessageSendFailed(errorCode);
    }

    @Override
    public void onDataMessageReceived(String message) {
        Log.d(TAG, "Media played successfully");
        super.onDataMessageReceived(message);
        if (JustCast.isSlideShowInProgress()) {
            if (null != JustCast.getSlidShowObj()) {
                JustCast.getSlidShowObj().sendNext();
            }
        }
    }

    @Override
    public void onFailed(int resourceId, int statusCode) {
        JustCastUtils.showToast(JustCast.getmAppContext(), "Connection failed");
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.d(TAG, "onConnectionSuspended() was called with cause: " + cause);
        JustCastUtils.showToast(JustCast.getmAppContext(), R.string.connection_temp_lost);
    }

    @Override
    public void onConnectivityRecovered() {
        JustCastUtils.showToast(JustCast.getmAppContext(), R.string.connection_recovered);
    }

    @Override
    public void onCastDeviceDetected(final MediaRouter.RouteInfo info) {
        if (!CastPreference.isFtuShown(JustCast.getmAppContext())) {
            CastPreference.setFtuShown(JustCast.getmAppContext());

            Log.d(TAG, "Route is visible: " + info);
            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    //if (mediaRouteMenuItem.isVisible()) {
                        Log.d(TAG, "Cast Icon is visible: " + info.getName());
                        // showFtu();
                    //}
                }
            }, 1000);
        }
    }

    @Override
    public void onRemoteMediaPlayerMetadataUpdated() {
        Log.d(TAG, "Remote Media Player Metadata updated");
    }
}
