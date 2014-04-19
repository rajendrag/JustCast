package com.rp.justcast;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;

import org.apache.http.conn.util.InetAddressUtils;

import android.app.Application;
import android.content.Context;
import android.support.v4.app.FragmentManager;

import com.google.sample.castcompanionlibrary.cast.VideoCastManager;
import com.google.sample.castcompanionlibrary.utils.Utils;
import com.rp.justcast.photos.ImageCache;
import com.rp.justcast.photos.ImageWorker;
import com.rp.justcast.settings.CastPreference;

public class JustCast extends Application {
	
	private static String APPLICATION_ID;
    private static VideoCastManager mCastMgr = null;
    public static final double VOLUME_INCREMENT = 0.05;
    private static Context mAppContext;
    
    private static ImageWorker imageWorker = null;
    private static final String IMAGE_CACHE_DIR = "thumbs";
    
    private static String myHost = "";
	private static int myPort;


    /*
     * (non-Javadoc)
     * @see android.app.Application#onCreate()
     */
    @Override
    public void onCreate() {
        super.onCreate();
        setMyHost(getIPAddress(true));
        //look for available port, for now 
        setMyPort(8111);
        setmAppContext(getApplicationContext());
        APPLICATION_ID = getString(R.string.app_id);
        Utils.saveFloatToPreference(getApplicationContext(),
                VideoCastManager.PREFS_KEY_VOLUME_INCREMENT, (float) VOLUME_INCREMENT);

    }

    public static VideoCastManager getCastManager(Context context) {
        if (null == mCastMgr) {
            mCastMgr = VideoCastManager.initialize(context, APPLICATION_ID,
                    null, null);
            mCastMgr.enableFeatures(
                    VideoCastManager.FEATURE_NOTIFICATION |
                            VideoCastManager.FEATURE_LOCKSCREEN |
                            VideoCastManager.FEATURE_DEBUGGING);

        }
        mCastMgr.setContext(context);
        String destroyOnExitStr = Utils.getStringFromPreference(context,
                CastPreference.TERMINATION_POLICY_KEY);
        mCastMgr.setStopOnDisconnect(null != destroyOnExitStr
                && CastPreference.STOP_ON_DISCONNECT.equals(destroyOnExitStr));
        return mCastMgr;
    }
    
    public static ImageWorker initImageWorker(FragmentManager fragmentManger) {
    	if(imageWorker == null) {
    		ImageCache.ImageCacheParams cacheParams = new ImageCache.ImageCacheParams(mAppContext, IMAGE_CACHE_DIR);
    		cacheParams.setMemCacheSizePercent(0.25f); // Set memory cache to 25% of app memory
    		imageWorker = new ImageWorker(mAppContext);
    		imageWorker.setLoadingImage(R.drawable.empty_photo);
    		imageWorker.addImageCache(fragmentManger, cacheParams);
    	}
    	return imageWorker;
    }
    
    public static ImageWorker getImageWorker() {
    	return imageWorker;
    }

	public static Context getmAppContext() {
		return mAppContext;
	}

	public static void setmAppContext(Context mAppContext) {
		JustCast.mAppContext = mAppContext;
	}
	
	private static String getIPAddress(boolean useIPv4) {
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

	public static String getMyHost() {
		return myHost;
	}

	public static void setMyHost(String myHost) {
		JustCast.myHost = myHost;
	}

	public static int getMyPort() {
		return myPort;
	}

	public static void setMyPort(int myPort) {
		JustCast.myPort = myPort;
	}
	
	public static String getJustCastServerUrl() {
		return "http://"+myHost+":"+myPort+"/";
	}
	
	public static final String addJustCastServerParam(String path) {
		return getJustCastServerUrl()+"?path="+path;
	}
}
