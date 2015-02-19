package com.rp.justcast;

import android.support.v7.media.MediaRouter;
import android.util.Log;

import com.google.android.gms.cast.ApplicationMetadata;
import com.google.android.gms.cast.CastDevice;
import com.google.android.gms.cast.TextTrackStyle;
import com.google.android.gms.common.ConnectionResult;
import com.google.sample.castcompanionlibrary.cast.callbacks.IVideoCastConsumer;

import java.util.Locale;

/**
 * Created by rp on 2/14/15.
 */
public class JCCastConsumer implements IVideoCastConsumer {

    private static final String TAG = "JCCastConsumer";

    @Override
    public void onDataMessageSendFailed(int errorCode) {

    }

    @Override
    public void onDataMessageReceived(String message) {
        Log.d(TAG, "Media played successfully");

    }

    @Override
    public void onApplicationConnected(ApplicationMetadata appMetadata, String sessionId, boolean wasLaunched) {

    }

    @Override
    public boolean onApplicationConnectionFailed(int errorCode) {
        return false;
    }

    @Override
    public void onApplicationStopFailed(int errorCode) {

    }

    @Override
    public void onApplicationStatusChanged(String appStatus) {

    }

    @Override
    public void onVolumeChanged(double value, boolean isMute) {

    }

    @Override
    public void onApplicationDisconnected(int errorCode) {

    }

    @Override
    public void onRemoteMediaPlayerMetadataUpdated() {

    }

    @Override
    public void onRemoteMediaPlayerStatusUpdated() {

    }

    @Override
    public void onRemovedNamespace() {

    }

    @Override
    public void onTextTrackStyleChanged(TextTrackStyle style) {

    }

    @Override
    public void onTextTrackEnabledChanged(boolean isEnabled) {

    }

    @Override
    public void onTextTrackLocaleChanged(Locale locale) {

    }

    @Override
    public void onFailed(int resourceId, int statusCode) {

    }

    @Override
    public void onConnected() {

    }

    @Override
    public void onConnectionSuspended(int cause) {

    }

    @Override
    public void onDisconnected() {

    }

    @Override
    public boolean onConnectionFailed(ConnectionResult result) {
        return false;
    }

    @Override
    public void onCastDeviceDetected(MediaRouter.RouteInfo info) {

    }

    @Override
    public void onCastAvailabilityChanged(boolean castPresent) {

    }

    @Override
    public void onConnectivityRecovered() {

    }

    @Override
    public void onUiVisibilityChanged(boolean visible) {

    }

    @Override
    public void onReconnectionStatusChanged(int status) {

    }

    @Override
    public void onDeviceSelected(CastDevice device) {

    }
}
