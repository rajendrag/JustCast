package com.rp.justcast;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.media.MediaRouter.RouteInfo;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.sample.castcompanionlibrary.cast.VideoCastManager;
import com.google.sample.castcompanionlibrary.cast.callbacks.IVideoCastConsumer;
import com.google.sample.castcompanionlibrary.cast.callbacks.VideoCastConsumerImpl;
import com.rp.justcast.photos.ImageWorker;
import com.rp.justcast.photos.PhotosFragment;
import com.rp.justcast.settings.CastPreference;
import com.rp.justcast.video.VideoBrowserListFragment;

/**
 * Main activity to send messages to the receiver.
 */
public class MainActivity extends ActionBarActivity {

	private static final String TAG = MainActivity.class.getSimpleName();

	private Intent justCastServiceIntent;
	// Bitmap mPlaceHolderBitmap;
	// private ImageAdapter imageAdapter;

	private DrawerLayout mDrawerLayout;
	private ActionBarDrawerToggle mDrawerToggle;
	private CharSequence mDrawerTitle;
	private CharSequence mTitle;
	private String[] mMenuTitles;
	private ListView mDrawerList;

	private VideoCastManager mCastManager;
	private IVideoCastConsumer mCastConsumer;
	// private MiniController mMini;
	private MenuItem mediaRouteMenuItem;
	private ImageWorker imageWorker;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		VideoCastManager.checkGooglePlaySevices(this);
		ActionBar actionBar = getSupportActionBar();

		mCastManager = JustCast.getCastManager(this);
		imageWorker = JustCast.initImageWorker(getSupportFragmentManager());
		// -- Adding MiniController
		/*
		 * mMini = (MiniController) findViewById(R.id.miniController1);
		 * mCastManager.addMiniController(mMini);
		 */

		mCastConsumer = new VideoCastConsumerImpl() {

			@Override
			public void onFailed(int resourceId, int statusCode) {

			}

			@Override
			public void onConnectionSuspended(int cause) {
				Log.d(TAG, "onConnectionSuspended() was called with cause: " + cause);
				JustCastUtils.showToast(MainActivity.this, R.string.connection_temp_lost);
			}

			@Override
			public void onConnectivityRecovered() {
				JustCastUtils.showToast(MainActivity.this, R.string.connection_recovered);
			}

			@Override
			public void onCastDeviceDetected(final RouteInfo info) {
				if (!CastPreference.isFtuShown(MainActivity.this)) {
					CastPreference.setFtuShown(MainActivity.this);

					Log.d(TAG, "Route is visible: " + info);
					new Handler().postDelayed(new Runnable() {

						@Override
						public void run() {
							if (mediaRouteMenuItem.isVisible()) {
								Log.d(TAG, "Cast Icon is visible: " + info.getName());
								// showFtu();
							}
						}
					}, 1000);
				}
			}
		};

		setupActionBar(actionBar);
		mCastManager.reconnectSessionIfPossible(this, false);

		// getSupportActionBar();

		mTitle = mDrawerTitle = getTitle();
		mMenuTitles = getResources().getStringArray(R.array.left_side_menu);
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerList = (ListView) findViewById(R.id.left_drawer);

		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
		// Set the adapter for the list view
		mDrawerList.setAdapter(new ArrayAdapter<String>(this, R.layout.drawer_list_item, mMenuTitles));
		// Set the list's click listener
		mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);

		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.drawable.ic_drawer, R.string.drawer_open, R.string.drawer_close) {

			public void onDrawerClosed(View view) {
				super.onDrawerClosed(view);
				getActionBar().setTitle(mTitle);
				invalidateOptionsMenu();
			}

			public void onDrawerOpened(View drawerView) {
				super.onDrawerOpened(drawerView);
				getActionBar().setTitle(mDrawerTitle);
				invalidateOptionsMenu();
			}
		};

		// Set the drawer toggle as the DrawerListener
		mDrawerLayout.setDrawerListener(mDrawerToggle);
		// mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

		// Configure Cast device discovery
		/*
		 * mMediaRouter = MediaRouter.getInstance(getApplicationContext());
		 * mMediaRouteSelector = new
		 * MediaRouteSelector.Builder().addControlCategory
		 * (CastMediaControlIntent
		 * .categoryForCast(getResources().getString(R.string.app_id)))
		 * .addControlCategory(CastMediaControlIntent.CATEGORY_CAST).build();
		 * mMediaRouterCallback = new MyMediaRouterCallback();
		 */

		if (savedInstanceState == null) {
			selectItem(0);
		}

		if (justCastServiceIntent == null) {
			justCastServiceIntent = new Intent(getApplicationContext(), JustCastService.class);
			startService(justCastServiceIntent);
		}
	}

	private void setupActionBar(ActionBar actionBar) {
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		getSupportActionBar().setIcon(R.drawable.ic_launcher);
		getSupportActionBar().setDisplayShowTitleEnabled(false);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		mDrawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		mDrawerToggle.onConfigurationChanged(newConfig);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// If the nav drawer is open, hide action items related to the content
		// view
		boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
		// menu.findItem(R.id.action_websearch).setVisible(!drawerOpen);
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Pass the event to ActionBarDrawerToggle, if it returns
		// true, then it has handled the app icon touch event
		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
		// Handle your other action bar items...

		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onResume() {
		/*
		 * super.onResume(); // Start media router discovery
		 * mMediaRouter.addCallback(mMediaRouteSelector, mMediaRouterCallback,
		 * MediaRouter.CALLBACK_FLAG_PERFORM_ACTIVE_SCAN);
		 */
		Log.d(TAG, "onResume() was called");
		mCastManager = JustCast.getCastManager(this);
		if (null != mCastManager) {
			mCastManager.addVideoCastConsumer(mCastConsumer);
			mCastManager.incrementUiCounter();
		}
		imageWorker.setExitTasksEarly(false);
		super.onResume();
	}

	@Override
	protected void onPause() {
		if (isFinishing()) {
			// End media router discovery
			// mMediaRouter.removeCallback(mMediaRouterCallback);
		}
		mCastManager.decrementUiCounter();
		mCastManager.removeVideoCastConsumer(mCastConsumer);
		imageWorker.setPauseWork(false);
		imageWorker.setExitTasksEarly(true);
		imageWorker.flushCache();
		super.onPause();
	}

	@Override
	public void onDestroy() {

		if (justCastServiceIntent != null) {
			stopService(justCastServiceIntent);
			justCastServiceIntent = null;
		}

		if (null != mCastManager) {
			// mMini.removeOnMiniControllerChangedListener(mCastManager);
			// mCastManager.removeMiniController(mMini);
			mCastManager.clearContext(this);
		}
		imageWorker.closeCache();
		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		/*
		 * super.onCreateOptionsMenu(menu);
		 * getMenuInflater().inflate(R.menu.main, menu); MenuItem
		 * mediaRouteMenuItem = menu.findItem(R.id.media_route_menu_item);
		 * MediaRouteActionProvider mediaRouteActionProvider =
		 * (MediaRouteActionProvider)
		 * MenuItemCompat.getActionProvider(mediaRouteMenuItem); // Set the
		 * MediaRouteActionProvider selector for device discovery.
		 * mediaRouteActionProvider.setRouteSelector(mMediaRouteSelector);
		 */

		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.main, menu);

		mediaRouteMenuItem = mCastManager.addMediaRouterButton(menu, R.id.media_route_menu_item);
		return true;
	}

	private void selectItem(int position) {
		mDrawerList.setItemChecked(position, true);
		switch (position) {
		case 0:
			Fragment fg = new PhotosFragment();
			FragmentTransaction tx = getSupportFragmentManager().beginTransaction();
			tx.replace(R.id.content_frame, fg);
			tx.commit();
			break;
		case 1:
			ListFragment lf = VideoBrowserListFragment.newInstance();
			FragmentTransaction vTx = getSupportFragmentManager().beginTransaction();
			vTx. replace(R.id.content_frame, lf);
			vTx.commit();
			break;
		default:
			break;
		}

		setTitle(mMenuTitles[position]);
		mDrawerLayout.closeDrawer(mDrawerList);
	}

	/**
	 * Send a text message to the receiver
	 * 
	 * @param message
	 */
	private void sendMessage(String message) {
		/*
		 * if (mSelectedDevice == null) { Log.d(TAG,
		 * "No Device selected to cast"); Toast.makeText(this,
		 * "No Device selected to cast, Please select a device using Cast button on top"
		 * , Toast.LENGTH_LONG).show(); return; } if (mApiClient == null ||
		 * mRemoteMediaPlayer == null) { Log.w(TAG,
		 * "No API or No remote player"); return; }
		 */
		/*
		 * try { message = URLEncoder.encode(message, "UTF-8"); } catch
		 * (UnsupportedEncodingException e1) { // TODO Auto-generated catch
		 * block e1.printStackTrace(); }
		 */
		/*
		 * MediaMetadata mediaMetadata = new
		 * MediaMetadata(MediaMetadata.MEDIA_TYPE_PHOTO);
		 * mediaMetadata.putString(MediaMetadata.KEY_TITLE, "My picture");
		 * String url = "http://" + myHost + ":" + myPort + "?path=" + message;
		 * Log.d(TAG, "Content URL sending to chromecast" + url); MediaInfo
		 * mediaInfo = new
		 * MediaInfo.Builder(url).setContentType("image/png").setStreamType
		 * (MediaInfo.STREAM_TYPE_BUFFERED).setMetadata(mediaMetadata).build();
		 * try { mRemoteMediaPlayer.load(mApiClient, mediaInfo,
		 * true).setResultCallback(new
		 * ResultCallback<RemoteMediaPlayer.MediaChannelResult>() {
		 * 
		 * @Override public void onResult(MediaChannelResult result) { if
		 * (result.getStatus().isSuccess()) { Log.d(TAG,
		 * "Media loaded successfully"); } else { Log.w(TAG,
		 * "Message sending failed" + result.getStatus().toString()); } } }); }
		 * catch (IllegalStateException e) { Log.e(TAG,
		 * "Problem occurred with media during loading", e); } catch (Exception
		 * e) { Log.e(TAG, "Problem opening media during loading", e); }
		 */

	}

	public class DrawerItemClickListener implements OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			selectItem(position);
			Toast.makeText(view.getContext(), "" + position, Toast.LENGTH_SHORT).show();
		}

	}
}
