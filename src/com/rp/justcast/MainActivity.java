package com.rp.justcast;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Handler;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.media.MediaRouter.RouteInfo;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.google.sample.castcompanionlibrary.cast.VideoCastManager;
import com.google.sample.castcompanionlibrary.cast.callbacks.IVideoCastConsumer;
import com.google.sample.castcompanionlibrary.cast.callbacks.VideoCastConsumerImpl;
import com.rp.justcast.music.MusicFragment;
import com.rp.justcast.photos.GalleryTabFragment;
import com.rp.justcast.photos.ImageWorker;
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
	private TypedArray menuIcons;
	private List<com.rp.justcast.JustCastMenuItem> menuItems;
	private MenuAdapter menuAdapter;

	private VideoCastManager mCastManager;
	private IVideoCastConsumer mCastConsumer;
	//private MiniController mMini;
	private MenuItem mediaRouteMenuItem;

	private ImageWorker imageWorker;
    private Toolbar mToolbar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		VideoCastManager.checkGooglePlayServices(this);

		mCastManager = JustCast.getCastManager();
		imageWorker = JustCast.initImageWorker(getSupportFragmentManager());
		// -- Adding MiniController

		/*
		 * mMini = (MiniController) findViewById(R.id.miniController1);
		 * mCastManager.addMiniController(mMini);
		 */

		mCastConsumer = new VideoCastConsumerImpl() {

			@Override
			public void onFailed(int resourceId, int statusCode) {
				JustCastUtils.showToast(MainActivity.this, "Connection failed");
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

		setupActionBar();
		mCastManager.reconnectSessionIfPossible();

		// getSupportActionBar();

		mTitle = mDrawerTitle = getTitle();
		mMenuTitles = getResources().getStringArray(R.array.left_side_menu);
		menuIcons = getResources().obtainTypedArray(R.array.menu_icons);
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerList = (ListView) findViewById(R.id.left_drawer);

		menuItems = new ArrayList<JustCastMenuItem>();

		for (int i = 0; i < mMenuTitles.length; i++) {
			JustCastMenuItem items = new JustCastMenuItem(mMenuTitles[i], menuIcons.getResourceId(i, -1));
			menuItems.add(items);
		}

		menuIcons.recycle();

		menuAdapter = new MenuAdapter(getApplicationContext(), menuItems);

		mDrawerList.setAdapter(menuAdapter);

		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

		// Set the adapter for the list view
		// Set the list's click listener
		mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar, R.string.drawer_open, R.string.drawer_close) {

			public void onDrawerClosed(View view) {
				super.onDrawerClosed(view);
                Log.d(TAG, "Drawer Closed");
				mToolbar.setTitle(mTitle);
				invalidateOptionsMenu();
			}

			public void onDrawerOpened(View drawerView) {
				super.onDrawerOpened(drawerView);
                Log.d(TAG, "Drawer Opened");
                mToolbar.setTitle(mDrawerTitle);
				invalidateOptionsMenu();
			}
		};

		// Set the drawer toggle as the DrawerListener
		mDrawerLayout.setDrawerListener(mDrawerToggle);


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

    private void setupActionBar() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        //mToolbar.setLogo(R.drawable.ic_launcher);
        mToolbar.setTitle(mTitle);
        setSupportActionBar(mToolbar);
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
		mCastManager = JustCast.getCastManager();
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
			/*if (null != mMini) {
				mMini.removeOnMiniControllerChangedListener(mCastManager);
			}
			mCastManager.removeMiniController(mMini);*/
			mCastManager.clearContext(this);
		}
		imageWorker.closeCache();
		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.main, menu);

		mediaRouteMenuItem = mCastManager.addMediaRouterButton(menu, R.id.media_route_menu_item);
		return true;
	}

	private void selectItem(int position) {
		//mDrawerList.setItemChecked(position, true);
        switch (position) {
		case 0:
			/*Fragment fg = new PhotosFragment();
			FragmentTransaction tx = getSupportFragmentManager().beginTransaction();
			tx.replace(R.id.content_frame, fg);
			tx.commit();*/
			Fragment fg = new GalleryTabFragment();
			FragmentTransaction tx = getSupportFragmentManager().beginTransaction();
			tx.replace(R.id.content_frame, fg, "GalleryTabFragment");
			tx.commit();
			break;
		case 1:
			/*
			 * Fragment vf = new VideoLayoutFragment(); FragmentTransaction vtx
			 * = getSupportFragmentManager().beginTransaction();
			 * vtx.replace(R.id.content_frame, vf); vtx.commit();
			 */
			ListFragment lf = VideoBrowserListFragment.newInstance();
			FragmentTransaction vTx = getSupportFragmentManager().beginTransaction();
			vTx.replace(R.id.content_frame, lf);
			vTx.commit();
			break;
		case 2:
			Fragment mf = new MusicFragment();
			FragmentTransaction mTx = getSupportFragmentManager().beginTransaction();
			mTx.replace(R.id.content_frame, mf);
			mTx.commit();
			break;
		default:
			break;
		}
        mTitle = mMenuTitles[position];
        mToolbar.setTitle(mTitle);
		setTitle(mTitle);
		mDrawerLayout.closeDrawer(mDrawerList);
	}

	public class DrawerItemClickListener implements OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			selectItem(position);
			// Toast.makeText(view.getContext(), "" + position,
			// Toast.LENGTH_SHORT).show();
		}

	}
}
