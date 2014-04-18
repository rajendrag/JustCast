package com.rp.justcast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class JustCastService extends Service {

	private static final String TAG = JustCastService.class.getName();
	private AbstractWebServer webServer;

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (webServer == null) {
			String myHost = JustCast.getMyHost();
			int myPort = JustCast.getMyPort();
			//webServer = new JustCastWebServer(myHost, myPort);
			 List<File> rootDirs = new ArrayList<File>();
		        boolean quiet = true;
		        Map<String, String> options = new HashMap<String, String>();

		        if (rootDirs.isEmpty()) {
		            rootDirs.add(new File(".").getAbsoluteFile());
		        }
			webServer = new JustCastNewServer(myHost, myPort, rootDirs, quiet);
			try {
				webServer.start();
				Log.d(TAG, "Web server started");
			} catch (IOException ioe) {
				ioe.printStackTrace();
				Log.e(TAG, "The server could not start.", ioe);
			}
		}
		
		
       

		
		
		
		
		
		return Service.START_NOT_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO for communication return IBinder implementation
		return null;
	}

	@Override
	public boolean stopService(Intent intent) {
		webServer.stop();
		return super.stopService(intent);
	}
}
