package com.rp.justcast;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;

import org.apache.http.conn.util.InetAddressUtils;

import android.annotation.TargetApi;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Build.VERSION_CODES;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.MediaRouteActionProvider;
import android.support.v7.media.MediaRouteSelector;
import android.support.v7.media.MediaRouter;
import android.support.v7.media.MediaRouter.RouteInfo;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.cast.Cast;
import com.google.android.gms.cast.Cast.ApplicationConnectionResult;
import com.google.android.gms.cast.CastDevice;
import com.google.android.gms.cast.CastMediaControlIntent;
import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.RemoteMediaPlayer;
import com.google.android.gms.cast.RemoteMediaPlayer.MediaChannelResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

/**
 * Main activity to send messages to the receiver.
 */
public class MainActivity extends ActionBarActivity {

	private static final String TAG = MainActivity.class.getSimpleName();
	private static final String IMAGE_CACHE_DIR = "thumbs";

	private MediaRouter mMediaRouter;
	private MediaRouteSelector mMediaRouteSelector;
	private MediaRouter.Callback mMediaRouterCallback;
	private CastDevice mSelectedDevice;
	private GoogleApiClient mApiClient;
	private Cast.Listener mCastListener;
	private ConnectionCallbacks mConnectionCallbacks;
	private ConnectionFailedListener mConnectionFailedListener;
	private boolean mApplicationStarted;
	private boolean mWaitingForReconnect;
	private RemoteMediaPlayer mRemoteMediaPlayer;
	private Intent justCastServiceIntent;
	Bitmap mPlaceHolderBitmap;
	private ImageAdapter imageAdapter;

	private String myHost = getIPAddress(true);
	private int myPort = 8111;

	TextView textTargetUri;
	ImageView targetImage;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		getSupportActionBar();

		// Configure Cast device discovery
		mMediaRouter = MediaRouter.getInstance(getApplicationContext());
		mMediaRouteSelector = new MediaRouteSelector.Builder().addControlCategory(CastMediaControlIntent.categoryForCast(getResources().getString(R.string.app_id)))
				.addControlCategory(CastMediaControlIntent.CATEGORY_CAST).build();
		mMediaRouterCallback = new MyMediaRouterCallback();

		/*if (getFragmentManager().findFragmentByTag(TAG) == null) {
			final FragmentTransaction ft = getFragmentManager().beginTransaction();
			ft.add(android.R.id.content, new ImageGridFragment() {

				@TargetApi(VERSION_CODES.JELLY_BEAN)
				@Override
				public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
					String path = itemList.get(position);
					Toast.makeText(getActivity().getApplicationContext(), path, Toast.LENGTH_LONG).show();
					Log.d(TAG, "Image clicked sending to chromecast");
					sendMessage(path);
				}
			}, TAG);
			ft.commit();
		}*/

		ImageCache.ImageCacheParams cacheParams = new ImageCache.ImageCacheParams(this, IMAGE_CACHE_DIR);

		cacheParams.setMemCacheSizePercent(0.25f); // Set memory cache to 25% of
													// app memory

		final GridView gridview = (GridView) findViewById(R.id.gridview);

		FragmentManager fm = getFragmentManager();
		imageAdapter = new ImageAdapter(this, fm, cacheParams);
		gridview.setAdapter(imageAdapter);

		gridview.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
				// Toast.makeText(MainActivity.this, "" + position,
				// Toast.LENGTH_SHORT).show();
				String path = imageAdapter.itemList.get(position);
				//Toast.makeText(getApplicationContext(), path, Toast.LENGTH_LONG).show();
				Log.d(TAG, "Image clicked sending to chromecast");
				sendMessage(path);
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		// Start media router discovery
		mMediaRouter.addCallback(mMediaRouteSelector, mMediaRouterCallback, MediaRouter.CALLBACK_FLAG_PERFORM_ACTIVE_SCAN);
	}

	@Override
	protected void onPause() {
		if (isFinishing()) {
			// End media router discovery
			mMediaRouter.removeCallback(mMediaRouterCallback);
		}
		if(imageAdapter != null) {
			imageAdapter.imageWorker.flushCache();
		}
		super.onPause();
	}

	@Override
	public void onDestroy() {
		if(imageAdapter != null) {
			imageAdapter.imageWorker.clearCache();
		}
		teardown();
		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.main, menu);
		MenuItem mediaRouteMenuItem = menu.findItem(R.id.media_route_menu_item);
		MediaRouteActionProvider mediaRouteActionProvider = (MediaRouteActionProvider) MenuItemCompat.getActionProvider(mediaRouteMenuItem);
		// Set the MediaRouteActionProvider selector for device discovery.
		mediaRouteActionProvider.setRouteSelector(mMediaRouteSelector);
		return true;
	}

	/**
	 * Callback for MediaRouter events
	 */
	private class MyMediaRouterCallback extends MediaRouter.Callback {

		@Override
		public void onRouteSelected(MediaRouter router, RouteInfo info) {
			Log.d(TAG, "onRouteSelected");
			// Handle the user route selection.
			mSelectedDevice = CastDevice.getFromBundle(info.getExtras());
			if (justCastServiceIntent == null) {
				justCastServiceIntent = new Intent(getApplicationContext(), JustCastService.class);
				justCastServiceIntent.putExtra("myHost", myHost);
				justCastServiceIntent.putExtra("myPort", myPort);
				startService(justCastServiceIntent);
			}
			launchReceiver();
		}

		@Override
		public void onRouteUnselected(MediaRouter router, RouteInfo info) {
			Log.d(TAG, "onRouteUnselected: info=" + info);
			teardown();
			mSelectedDevice = null;
		}
	}

	/**
	 * Start the receiver app
	 */
	private void launchReceiver() {
		try {
			mCastListener = new Cast.Listener() {

				@Override
				public void onApplicationDisconnected(int errorCode) {
					Log.d(TAG, "application has stopped");
					teardown();
				}

			};
			// Connect to Google Play services
			mConnectionCallbacks = new ConnectionCallbacks();
			mConnectionFailedListener = new ConnectionFailedListener();
			Cast.CastOptions.Builder apiOptionsBuilder = Cast.CastOptions.builder(mSelectedDevice, mCastListener);
			mApiClient = new GoogleApiClient.Builder(this).addApi(Cast.API, apiOptionsBuilder.build()).addConnectionCallbacks(mConnectionCallbacks)
					.addOnConnectionFailedListener(mConnectionFailedListener).build();

			mApiClient.connect();
		} catch (Exception e) {
			Log.e(TAG, "Failed launchReceiver", e);
		}
	}

	/**
	 * Google Play services callbacks
	 */
	private class ConnectionCallbacks implements GoogleApiClient.ConnectionCallbacks {
		@Override
		public void onConnected(Bundle connectionHint) {
			Log.d(TAG, "onConnected");

			if (mApiClient == null) {
				// We got disconnected while this runnable was pending
				// execution.
				return;
			}

			try {
				if (mWaitingForReconnect) {
					mWaitingForReconnect = false;

					// Check if the receiver app is still running
					if ((connectionHint != null) && connectionHint.getBoolean(Cast.EXTRA_APP_NO_LONGER_RUNNING)) {
						Log.d(TAG, "App  is no longer running");
						teardown();
					} else {
						// Re-create the custom message channel
						try {
							Cast.CastApi.setMessageReceivedCallbacks(mApiClient, mRemoteMediaPlayer.getNamespace(), mRemoteMediaPlayer);
						} catch (IOException e) {
							Log.e(TAG, "Exception while creating channel", e);
						}
					}
				} else {
					// Launch the receiver app
					Cast.CastApi.launchApplication(mApiClient, getString(R.string.app_id), false).setResultCallback(new ResultCallback<Cast.ApplicationConnectionResult>() {

						@Override
						public void onResult(ApplicationConnectionResult result) {
							Status status = result.getStatus();
							Log.d(TAG, "ApplicationConnectionResultCallback.onResult: statusCode" + status.getStatusCode());
							if (status.isSuccess()) {
								mRemoteMediaPlayer = new RemoteMediaPlayer();
								mRemoteMediaPlayer.setOnStatusUpdatedListener(new RemoteMediaPlayer.OnStatusUpdatedListener() {
									@Override
									public void onStatusUpdated() {
										Log.d(TAG, "in onStatusUpdated");
									}
								});

								mRemoteMediaPlayer.setOnMetadataUpdatedListener(new RemoteMediaPlayer.OnMetadataUpdatedListener() {
									@Override
									public void onMetadataUpdated() {
										Log.d(TAG, "in onMetadataUpdated");
									}
								});

								try {
									Cast.CastApi.setMessageReceivedCallbacks(mApiClient, mRemoteMediaPlayer.getNamespace(), mRemoteMediaPlayer);
								} catch (IOException e) {
									Log.e(TAG, "Exception while creating media channel", e);
								}

								mRemoteMediaPlayer.requestStatus(mApiClient).setResultCallback(new ResultCallback<RemoteMediaPlayer.MediaChannelResult>() {
									@Override
									public void onResult(RemoteMediaPlayer.MediaChannelResult mediaChannelResult) {
										Status stat = mediaChannelResult.getStatus();
										if (stat.isSuccess()) {
											Log.d(TAG, "mMediaPlayer getMediaStatus success");
										} else {
											Log.d(TAG, "mMediaPlayer getMediaStatus failure");
										}
									}
								});

							} else {
								Log.e(TAG, "application could not launch");
								teardown();
							}
						}
					});
				}
			} catch (Exception e) {
				Log.e(TAG, "Failed to launch application", e);
			}
		}

		@Override
		public void onConnectionSuspended(int cause) {
			Log.d(TAG, "onConnectionSuspended");
			mWaitingForReconnect = true;
		}
	}

	/**
	 * Google Play services callbacks
	 */
	private class ConnectionFailedListener implements GoogleApiClient.OnConnectionFailedListener {
		@Override
		public void onConnectionFailed(ConnectionResult result) {
			Log.e(TAG, "onConnectionFailed ");

			teardown();
		}
	}

	/**
	 * Tear down the connection to the receiver
	 */
	private void teardown() {
		Log.d(TAG, "teardown");
		if (mApiClient != null) {
			if (mApplicationStarted) {
				if (mApiClient.isConnected()) {
					try {
						Cast.CastApi.stopApplication(mApiClient);
						/*
						 * if (mHelloWorldChannel != null) {
						 * Cast.CastApi.removeMessageReceivedCallbacks(
						 * mApiClient, mHelloWorldChannel.getNamespace());
						 * mHelloWorldChannel = null; }
						 */
						if (mRemoteMediaPlayer != null) {
							Cast.CastApi.removeMessageReceivedCallbacks(mApiClient, mRemoteMediaPlayer.getNamespace());
							mRemoteMediaPlayer = null;
						}
					} catch (IOException e) {
						Log.e(TAG, "Exception while removing channel", e);
					}
					mApiClient.disconnect();
				}
				mApplicationStarted = false;
				if (justCastServiceIntent != null) {
					stopService(justCastServiceIntent);
					justCastServiceIntent = null;
				}
			}
			mApiClient = null;
		}
		mSelectedDevice = null;
		mWaitingForReconnect = false;
	}

	/**
	 * Send a text message to the receiver
	 * 
	 * @param message
	 */
	private void sendMessage(String message) {
		if (mSelectedDevice == null) {
			Log.d(TAG, "No Device selected to cast");
			Toast.makeText(this, "No Device selected to cast, Please select a device using Cast button on top", Toast.LENGTH_LONG).show();
			return;
		}
		if (mApiClient == null || mRemoteMediaPlayer == null) {
			Log.w(TAG, "No API or No remote player");
			return;
		}
		/*
		 * try { message = URLEncoder.encode(message, "UTF-8"); } catch
		 * (UnsupportedEncodingException e1) { // TODO Auto-generated catch
		 * block e1.printStackTrace(); }
		 */
		MediaMetadata mediaMetadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_PHOTO);
		mediaMetadata.putString(MediaMetadata.KEY_TITLE, "My picture");
		String url = "http://" + myHost + ":" + myPort + "?path=" + message;
		Log.d(TAG, "Content URL sending to chromecast" + url);
		MediaInfo mediaInfo = new MediaInfo.Builder(url).setContentType("image/png").setStreamType(MediaInfo.STREAM_TYPE_BUFFERED).setMetadata(mediaMetadata).build();
		try {
			mRemoteMediaPlayer.load(mApiClient, mediaInfo, true).setResultCallback(new ResultCallback<RemoteMediaPlayer.MediaChannelResult>() {
				@Override
				public void onResult(MediaChannelResult result) {
					if (result.getStatus().isSuccess()) {
						Log.d(TAG, "Media loaded successfully");
					} else {
						Log.w(TAG, "Message sending failed" + result.getStatus().toString());
					}
				}
			});
		} catch (IllegalStateException e) {
			Log.e(TAG, "Problem occurred with media during loading", e);
		} catch (Exception e) {
			Log.e(TAG, "Problem opening media during loading", e);
		}

	}

	public static String getIPAddress(boolean useIPv4) {
		try {
			List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
			for (NetworkInterface intf : interfaces) {
				List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
				for (InetAddress addr : addrs) {
					if (!addr.isLoopbackAddress()) {
						String sAddr = addr.getHostAddress().toUpperCase();
						boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);
						if (useIPv4) {
							if (isIPv4)
								return sAddr;
						} else {
							if (!isIPv4) {
								int delim = sAddr.indexOf('%'); // drop ip6 port
																// suffix
								return delim < 0 ? sAddr : sAddr.substring(0, delim);
							}
						}
					}
				}
			}
		} catch (Exception ex) {
		} // for now eat exceptions
		return "";
	}

}
