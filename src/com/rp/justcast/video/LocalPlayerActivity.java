/*
 * Copyright (C) 2013 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.rp.justcast.video;

import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Point;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.VideoView;

import com.androidquery.AQuery;
import com.google.android.gms.cast.ApplicationMetadata;
import com.google.android.gms.cast.CastStatusCodes;
import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.libraries.cast.companionlibrary.cast.VideoCastManager;
import com.google.android.libraries.cast.companionlibrary.cast.callbacks.VideoCastConsumerImpl;
import com.google.android.libraries.cast.companionlibrary.utils.Utils;
import com.google.android.libraries.cast.companionlibrary.widgets.MiniController;
import com.rp.justcast.JustCast;
import com.rp.justcast.settings.CastPreference;
import com.rp.justcast.util.JustCastUtils;
import com.rp.justcast.R;

public class LocalPlayerActivity extends ActionBarActivity {

    private static final String TAG = "LocalPlayerActivity";
    private VideoView mVideoView;
    private TextView mTitleView;
    private TextView mDescriptionView;
    private TextView mStartText;
    private TextView mEndText;
    private SeekBar mSeekbar;
    private ImageView mPlayPause;
    private ProgressBar mLoading;
    private View mControllers;
    private View mContainer;
    private ImageView mCoverArt;
    private VideoCastManager mCastManager;
    private Timer mSeekbarTimer;
    private Timer mControllersTimer;
    private PlaybackLocation mLocation;
    private PlaybackState mPlaybackState;
    private final Handler mHandler = new Handler();
    private Point mDisplaySize;
    private final float mAspectRatio = 72f / 128;
    private AQuery mAquery;
    private MediaInfo mSelectedMedia;
    private boolean mShouldStartPlayback;
    private boolean mControllersVisible;
    private int mDuration;
    private MiniController mMini;
    protected MediaInfo mRemoteMediaInformation;
    private VideoCastConsumerImpl mCastConsumer;
    private TextView mAuthorView;

    /*
     * indicates whether we are doing a local or a remote playback
     */
    public static enum PlaybackLocation {
        LOCAL,
        REMOTE;
    }

    /*
     * List of various states that we can be in
     */
    public static enum PlaybackState {
        PLAYING, PAUSED, BUFFERING, IDLE;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.player_activity);
        mAquery = new AQuery(this);
        loadViews();
        mCastManager = VideoCastManager.getInstance();
        setupActionBar();
        setupControlsCallbacks();
        setupMiniController();
        setupCastListener();
        // see what we need to play and were
        Bundle b = getIntent().getExtras();
        if (null != b) {
            mSelectedMedia = com.google.android.libraries.cast.companionlibrary.utils.Utils
                    .bundleToMediaInfo(getIntent().getBundleExtra("media"));
            mShouldStartPlayback = b.getBoolean("shouldStart");
            int startPosition = b.getInt("startPosition", 0);
            mVideoView.setVideoURI(Uri.parse(mSelectedMedia.getContentId()));
            Log.d(TAG, "Setting url of the VideoView to: " + mSelectedMedia.getContentId());
            if (mShouldStartPlayback) {
                // this will be the case only if we are coming from the
                // CastControllerActivity by disconnecting from a device
                mPlaybackState = PlaybackState.PLAYING;
                updatePlaybackLocation(PlaybackLocation.LOCAL);
                updatePlayButton(mPlaybackState);
                if (startPosition > 0) {
                    mVideoView.seekTo(startPosition);
                }
                mVideoView.start();
                startControllersTimer();
            } else {
                // we should load the video but pause it
                // and show the album art.
                if (mCastManager.isConnected()) {
                    updatePlaybackLocation(PlaybackLocation.REMOTE);
                } else {
                    updatePlaybackLocation(PlaybackLocation.LOCAL);
                }
                mPlaybackState = PlaybackState.PAUSED;
                updatePlayButton(mPlaybackState);
            }
        }
        if (null != mTitleView) {
            updateMetadata(true);
        }
    }

    private void setupCastListener() {
        mCastConsumer = new VideoCastConsumerImpl() {
            @Override
            public void onApplicationConnected(ApplicationMetadata appMetadata,
                                               String sessionId, boolean wasLaunched) {
                Log.d(TAG, "onApplicationLaunched() is reached");
                if (null != mSelectedMedia) {

                    if (mPlaybackState == PlaybackState.PLAYING) {
                        mVideoView.pause();
                        try {
                            loadRemoteMedia(mSeekbar.getProgress(), true);
                            finish();
                        } catch (Exception e) {
                            JustCastUtils.handleException(LocalPlayerActivity.this, e);
                        }
                        return;
                    } else {
                        updatePlaybackLocation(PlaybackLocation.REMOTE);
                    }
                }
            }

            @Override
            public void onApplicationDisconnected(int errorCode) {
                Log.d(TAG, "onApplicationDisconnected() is reached with errorCode: " + errorCode);
                updatePlaybackLocation(PlaybackLocation.LOCAL);
            }

            @Override
            public void onDisconnected() {
                Log.d(TAG, "onDisconnected() is reached");
                mPlaybackState = PlaybackState.PAUSED;
                mLocation = PlaybackLocation.LOCAL;
            }

            @Override
            public void onRemoteMediaPlayerMetadataUpdated() {
                try {
                    mRemoteMediaInformation = mCastManager.getRemoteMediaInformation();
                } catch (Exception e) {
                    // silent
                }
            }

            @Override
            public void onFailed(int resourceId, int statusCode) {

            }

            @Override
            public void onConnectionSuspended(int cause) {
                Utils.showToast(LocalPlayerActivity.this,
                        R.string.connection_temp_lost);
            }

            @Override
            public void onConnectivityRecovered() {
                Utils.showToast(LocalPlayerActivity.this,
                        R.string.connection_recovered);
            }

            @Override
            public void onApplicationConnectionFailed(int errorCode) {
                switch (errorCode) {
                    case CastStatusCodes.APPLICATION_NOT_FOUND:
                        Log.d(TAG, "onApplicationConnectionFailed(): failed due to: "
                                + "ERROR_APPLICATION_NOT_FOUND");
                        Utils.showToast(LocalPlayerActivity.this, R.string.failed_to_find_app);
                        break;
                    case CastStatusCodes.TIMEOUT:
                        Log.d(TAG, "onApplicationConnectionFailed(): failed due to: ERROR_TIMEOUT");
                        Utils.showToast(LocalPlayerActivity.this,
                                R.string.failed_app_launch_timeout);
                        break;
                    default:
                        Log.d(TAG, "onApplicationConnectionFailed(): failed due to: error code="
                                + errorCode);
                        Utils.showToast(LocalPlayerActivity.this,
                                R.string.failed_to_launch_app);
                        break;
                }
            }

            @Override
            public void onConnectionFailed(ConnectionResult result) {
                Utils.showToast(LocalPlayerActivity.this, R.string.failed_to_connect);
            }
        };
    }

    private void setupMiniController() {
        mMini = (MiniController) findViewById(R.id.miniController1);
        mCastManager.addMiniController(mMini);
    }

    private void updatePlaybackLocation(PlaybackLocation location) {
        this.mLocation = location;
        if (location == PlaybackLocation.LOCAL) {
            if (mPlaybackState == PlaybackState.PLAYING ||
                    mPlaybackState == PlaybackState.BUFFERING) {
                setCoverArtStatus(null);
                startControllersTimer();
            } else {
                stopControllersTimer();
                setCoverArtStatus(com.google.android.libraries.cast.companionlibrary.utils.Utils.
                        getImageUrl(mSelectedMedia, 0));
            }

            getSupportActionBar().setTitle("");
        } else {
            stopControllersTimer();
            setCoverArtStatus(com.google.android.libraries.cast.companionlibrary.utils.Utils.
                    getImageUrl(mSelectedMedia, 0));
            updateControllersVisibility(true);
        }
    }

    private void play(int position) {
        startControllersTimer();
        switch (mLocation) {
            case LOCAL:
                mVideoView.seekTo(position);
                mVideoView.start();
                break;
            case REMOTE:
                mPlaybackState = PlaybackState.BUFFERING;
                updatePlayButton(mPlaybackState);
                try {
                    mCastManager.play(position);
                } catch (Exception e) {
                    JustCastUtils.handleException(this, e);
                }
                break;
            default:
                break;
        }
        restartTrickplayTimer();
    }

    private void togglePlayback() {
        stopControllersTimer();
        switch (mPlaybackState) {
            case PAUSED:
                switch (mLocation) {
                    case LOCAL:
                        mVideoView.start();
                        if (!mCastManager.isConnecting() ) {
                            Log.d(TAG, "Playing locally...");
                            mCastManager.clearPersistedConnectionInfo(
                                    VideoCastManager.CLEAR_SESSION);
                        }
                        mPlaybackState = PlaybackState.PLAYING;
                        startControllersTimer();
                        restartTrickplayTimer();
                        updatePlaybackLocation(PlaybackLocation.LOCAL);
                        break;
                    case REMOTE:
                        try {
                            mCastManager.checkConnectivity();
                            loadRemoteMedia(0, true);
                            finish();
                        } catch (Exception e) {
                            JustCastUtils.handleException(LocalPlayerActivity.this, e);
                            return;
                        }
                        break;
                    default:
                        break;
                }
                break;

            case PLAYING:
                mPlaybackState = PlaybackState.PAUSED;
                mVideoView.pause();
                break;

            case IDLE:
                mVideoView.setVideoURI(Uri.parse(mSelectedMedia.getContentId()));
                mVideoView.seekTo(0);
                mVideoView.start();
                mPlaybackState = PlaybackState.PLAYING;
                restartTrickplayTimer();
                break;

            default:
                break;
        }
        updatePlayButton(mPlaybackState);
    }

    private void loadRemoteMedia(int position, boolean autoPlay) {
        mCastManager.startVideoCastControllerActivity(this, mSelectedMedia, position, autoPlay);
    }

    private void setCoverArtStatus(String url) {
        if (null != url) {
            mAquery.id(mCoverArt).image(url);
            mCoverArt.setVisibility(View.VISIBLE);
            mVideoView.setVisibility(View.INVISIBLE);
        } else {
            mCoverArt.setVisibility(View.GONE);
            mVideoView.setVisibility(View.VISIBLE);
        }
    }

    private void stopTrickplayTimer() {
        Log.d(TAG, "Stopped TrickPlay Timer");
        if (null != mSeekbarTimer) {
            mSeekbarTimer.cancel();
        }
    }

    private void restartTrickplayTimer() {
        stopTrickplayTimer();
        mSeekbarTimer = new Timer();
        mSeekbarTimer.scheduleAtFixedRate(new UpdateSeekbarTask(), 100, 1000);
        Log.d(TAG, "Restarted TrickPlay Timer");
    }

    private void stopControllersTimer() {
        if (null != mControllersTimer) {
            mControllersTimer.cancel();
        }
    }

    private void startControllersTimer() {
        if (null != mControllersTimer) {
            mControllersTimer.cancel();
        }
        if (mLocation == PlaybackLocation.REMOTE) {
            return;
        }
        mControllersTimer = new Timer();
        mControllersTimer.schedule(new HideControllersTask(), 5000);
    }

    // should be called from the main thread
    private void updateControllersVisibility(boolean show) {
        if (show) {
            getSupportActionBar().show();
            mControllers.setVisibility(View.VISIBLE);
        } else {
            getSupportActionBar().hide();
            mControllers.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause() was called");
        if (mLocation == PlaybackLocation.LOCAL) {

            if (null != mSeekbarTimer) {
                mSeekbarTimer.cancel();
                mSeekbarTimer = null;
            }
            if (null != mControllersTimer) {
                mControllersTimer.cancel();
            }
            // since we are playing locally, we need to stop the playback of
            // video (if user is not watching, pause it!)
            mVideoView.pause();
            mPlaybackState = PlaybackState.PAUSED;
            updatePlayButton(PlaybackState.PAUSED);
        }
        mCastManager.removeVideoCastConsumer(mCastConsumer);
        mMini.removeOnMiniControllerChangedListener(mCastManager);
        mCastManager.decrementUiCounter();
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop() was called");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy() is called");
        if (null != mCastManager) {
            mMini.removeOnMiniControllerChangedListener(mCastManager);
            mCastManager.removeMiniController(mMini);
            mCastConsumer = null;
        }
        stopControllersTimer();
        stopTrickplayTimer();
        super.onDestroy();
    }

    @Override
    protected void onStart() {
        Log.d(TAG, "onStart was called");
        super.onStart();
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume() was called");
        mCastManager = VideoCastManager.getInstance();
        mCastManager.addVideoCastConsumer(mCastConsumer);
        mMini.setOnMiniControllerChangedListener(mCastManager);
        mCastManager.incrementUiCounter();
        super.onResume();
    }

    private class HideControllersTask extends TimerTask {

        @Override
        public void run() {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    updateControllersVisibility(false);
                    mControllersVisible = false;
                }
            });

        }
    }

    private class UpdateSeekbarTask extends TimerTask {

        @Override
        public void run() {
            mHandler.post(new Runnable() {

                @Override
                public void run() {
                    int currentPos = 0;
                    if (mLocation == PlaybackLocation.LOCAL) {
                        currentPos = mVideoView.getCurrentPosition();
                        updateSeekbar(currentPos, mDuration);
                    }
                }
            });
        }
    }

    private void setupControlsCallbacks() {
        mVideoView.setOnErrorListener(new OnErrorListener() {

            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                Log.e(TAG, "OnErrorListener.onError(): VideoView encountered an " +
                        "error, what: " + what + ", extra: " + extra);
                String msg = "";
                if (extra == MediaPlayer.MEDIA_ERROR_TIMED_OUT) {
                    msg = getString(R.string.video_error_media_load_timeout);
                } else if (what == MediaPlayer.MEDIA_ERROR_SERVER_DIED) {
                    msg = getString(R.string.video_error_server_unaccessible);
                } else {
                    msg = getString(R.string.video_error_unknown_error);
                }
                JustCastUtils.showErrorDialog(LocalPlayerActivity.this, msg);
                mVideoView.stopPlayback();
                mPlaybackState = PlaybackState.IDLE;
                updatePlayButton(mPlaybackState);
                return true;
            }
        });

        mVideoView.setOnPreparedListener(new OnPreparedListener() {

            @Override
            public void onPrepared(MediaPlayer mp) {
                Log.d(TAG, "onPrepared is reached");
                mDuration = mp.getDuration();
                mEndText.setText(com.google.android.libraries.cast.companionlibrary.utils.Utils
                        .formatMillis(mDuration));
                mSeekbar.setMax(mDuration);
                restartTrickplayTimer();
            }
        });

        mVideoView.setOnCompletionListener(new OnCompletionListener() {

            @Override
            public void onCompletion(MediaPlayer mp) {
                stopTrickplayTimer();
                mPlaybackState = PlaybackState.IDLE;
                updatePlayButton(PlaybackState.IDLE);
            }
        });

        mVideoView.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (!mControllersVisible) {
                    updateControllersVisibility(true);
                }
                startControllersTimer();
                return false;
            }
        });

        mSeekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (mPlaybackState == PlaybackState.PLAYING) {
                    play(seekBar.getProgress());
                } else if (mPlaybackState != PlaybackState.IDLE) {
                    mVideoView.seekTo(seekBar.getProgress());
                }
                startControllersTimer();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                stopTrickplayTimer();
                mVideoView.pause();
                stopControllersTimer();
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                mStartText.setText(com.google.android.libraries.cast.companionlibrary.utils.Utils
                        .formatMillis(progress));
            }
        });

        mPlayPause.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                togglePlayback();
            }
        });
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (mCastManager.onDispatchVolumeKeyEvent(event, JustCast.VOLUME_INCREMENT)) {
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    private void updateSeekbar(int position, int duration) {
        mSeekbar.setProgress(position);
        mSeekbar.setMax(duration);
        mStartText.setText(com.google.android.libraries.cast.companionlibrary.utils.Utils
                .formatMillis(position));
        mEndText.setText(com.google.android.libraries.cast.companionlibrary.utils.Utils
                .formatMillis(duration));
    }

    private void updatePlayButton(PlaybackState state) {
        switch (state) {
            case PLAYING:
                mLoading.setVisibility(View.INVISIBLE);
                mPlayPause.setVisibility(View.VISIBLE);
                mPlayPause.setImageDrawable(
                        getResources().getDrawable(R.drawable.ic_av_pause_dark));
                break;
            case PAUSED:
            case IDLE:
                mLoading.setVisibility(View.INVISIBLE);
                mPlayPause.setVisibility(View.VISIBLE);
                mPlayPause.setImageDrawable(
                        getResources().getDrawable(R.drawable.ic_av_play_dark));
                break;
            case BUFFERING:
                mPlayPause.setVisibility(View.INVISIBLE);
                mLoading.setVisibility(View.VISIBLE);
                break;
            default:
                break;
        }
    }

    @SuppressLint("NewApi")
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
            }
            updateMetadata(false);
            mContainer.setBackgroundColor(getResources().getColor(R.color.Black));

        } else {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
            getWindow().clearFlags(
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
            }
            updateMetadata(true);
            mContainer.setBackgroundColor(getResources().getColor(R.color.White));

        }
    }

    private void updateMetadata(boolean visible) {
        if (!visible) {
            mDescriptionView.setVisibility(View.GONE);
            mTitleView.setVisibility(View.GONE);
            mAuthorView.setVisibility(View.GONE);
            mDisplaySize = JustCastUtils.getDisplaySize(this);
            RelativeLayout.LayoutParams lp = new
                    RelativeLayout.LayoutParams(mDisplaySize.x,
                    mDisplaySize.y + getSupportActionBar().getHeight());
            lp.addRule(RelativeLayout.CENTER_IN_PARENT);
            mVideoView.setLayoutParams(lp);
            mVideoView.invalidate();
        } else {
            MediaMetadata mm = mSelectedMedia.getMetadata();
            mDescriptionView.setText(mm.getString(MediaMetadata.KEY_STUDIO));
            mTitleView.setText(mm.getString(MediaMetadata.KEY_TITLE));
            mAuthorView.setText(mm.getString(MediaMetadata.KEY_SUBTITLE));
            mDescriptionView.setVisibility(View.VISIBLE);
            mTitleView.setVisibility(View.VISIBLE);
            mAuthorView.setVisibility(View.VISIBLE);
            mDisplaySize = JustCastUtils.getDisplaySize(this);
            RelativeLayout.LayoutParams lp = new
                    RelativeLayout.LayoutParams(mDisplaySize.x,
                    (int) (mDisplaySize.x * mAspectRatio));
            lp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            mVideoView.setLayoutParams(lp);
            mVideoView.invalidate();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main, menu);
        mCastManager.addMediaRouterButton(menu, R.id.media_route_menu_item);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            /*case R.id.action_settings:
                Intent i = new Intent(LocalPlayerActivity.this, CastPreference.class);
                startActivity(i);
                break;*/

        }
        return true;
    }

    private void setupActionBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
    }

    private void loadViews() {
        mVideoView = (VideoView) findViewById(R.id.videoView1);
        mTitleView = (TextView) findViewById(R.id.textView1);
        mDescriptionView = (TextView) findViewById(R.id.textView2);
        mDescriptionView.setMovementMethod(new ScrollingMovementMethod());
        mAuthorView = (TextView) findViewById(R.id.textView3);
        mStartText = (TextView) findViewById(R.id.startText);
        mEndText = (TextView) findViewById(R.id.endText);
        mSeekbar = (SeekBar) findViewById(R.id.seekBar1);
        // mVolBar = (SeekBar) findViewById(R.id.seekBar2);
        mPlayPause = (ImageView) findViewById(R.id.imageView2);
        mLoading = (ProgressBar) findViewById(R.id.progressBar1);
        // mVolumeMute = (ImageView) findViewById(R.id.imageView2);
        mControllers = findViewById(R.id.controllers);
        mContainer = findViewById(R.id.container);
        mCoverArt = (ImageView) findViewById(R.id.coverArtView);
    }
}
