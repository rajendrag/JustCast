package com.rp.justcast;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.os.Bundle;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
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
import com.rp.justcast.music.MusicFragment;
import com.rp.justcast.photos.GalleryTabFragment;
import com.rp.justcast.photos.ImageWorker;
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

    private int backStackTitle = -1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		VideoCastManager.checkGooglePlayServices(this);

		mCastManager = JustCast.getCastManager();
		imageWorker = JustCast.getImageWorker(getSupportFragmentManager());
		// -- Adding MiniController

		/*
		 * mMini = (MiniController) findViewById(R.id.miniController1);
		 * mCastManager.addMiniController(mMini);
		 */

		mCastConsumer = new JCCastConsumer();

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
        backStackTitle = position;
        mTitle = mMenuTitles[position];
        mToolbar.setTitle(mTitle);
        setTitle(mTitle);
        FragmentManager supportFragmentManager = getSupportFragmentManager();
        FragmentTransaction ftx = supportFragmentManager.beginTransaction();
        addBackStackListener(supportFragmentManager);
        switch (position) {
		case 0:
			Fragment fg = new GalleryTabFragment();
			supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
			ftx.replace(R.id.content_frame, fg, "GalleryTabFragment");
			ftx.commit();
			break;
		case 1:
			ListFragment lf = VideoBrowserListFragment.newInstance();
			supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
			ftx.replace(R.id.content_frame, lf).addToBackStack(null);
			ftx.commit();
			break;
		case 2:
			Fragment mf = new MusicFragment();
            supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
			ftx.replace(R.id.content_frame, mf).addToBackStack(null);
			ftx.commit();
			break;
		default:
			break;
		}
		mDrawerLayout.closeDrawer(mDrawerList);
	}

    private void addBackStackListener(final FragmentManager fragmentManager) {
        fragmentManager.addOnBackStackChangedListener(
                new FragmentManager.OnBackStackChangedListener() {
                    public void onBackStackChanged() {
                        Log.d(TAG, "BackStackListener: position "+ backStackTitle);
                        if (backStackTitle != -1 && backStackTitle < mMenuTitles.length) {
                            mToolbar.setTitle(mMenuTitles[backStackTitle]);
                        }
                        backStackTitle = -1;
                    }
                });
    }

    public class DrawerItemClickListener implements OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			selectItem(position);
		}

	}
}
