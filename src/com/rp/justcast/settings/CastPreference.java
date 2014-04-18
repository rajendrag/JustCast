package com.rp.justcast.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

import com.google.sample.castcompanionlibrary.cast.VideoCastManager;
import com.google.sample.castcompanionlibrary.utils.Utils;
import com.rp.justcast.JustCast;
import com.rp.justcast.R;

public class CastPreference extends PreferenceActivity implements OnSharedPreferenceChangeListener {

	public static final String APP_DESTRUCTION_KEY = "application_destruction";
	public static final String FTU_SHOWN_KEY = "ftu_shown";
	public static final String VOLUME_SELCTION_KEY = "volume_target";
	public static final String TERMINATION_POLICY_KEY = "termination_policy";
	public static final String STOP_ON_DISCONNECT = "1";
	public static final String CONTINUE_ON_DISCONNECT = "0";
	private ListPreference mVolumeListPreference;
	private SharedPreferences mPrefs;
	private VideoCastManager mCastManager;
	boolean mStopOnExit;
	private ListPreference mTerminationListPreference;

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
	}

	public static boolean isDestroyAppOnDisconnect(Context ctx) {
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(ctx);
		return sharedPref.getBoolean(APP_DESTRUCTION_KEY, false);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		
	}


	public static boolean isFtuShown(Context ctx) {
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(ctx);
		return sharedPref.getBoolean(FTU_SHOWN_KEY, false);
	}

	public static void setFtuShown(Context ctx) {
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(ctx);
		sharedPref.edit().putBoolean(FTU_SHOWN_KEY, true).commit();
	}

	@Override
	protected void onResume() {
		if (null != mCastManager) {
			mCastManager.incrementUiCounter();
		}
		super.onResume();
	}

	@Override
	protected void onPause() {
		if (null != mCastManager) {
			mCastManager.decrementUiCounter();
		}
		super.onPause();
	}

}
