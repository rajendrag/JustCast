package com.rp.justcast;

import java.io.IOException;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class JustCastService extends Service {

	private static final String TAG = JustCastService.class.getName();
	private JustCastWebServer webServer;

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (webServer == null) {
			String myHost = intent.getStringExtra("myHost");
			int myPort = intent.getIntExtra("myPort", 8111);
			webServer = new JustCastWebServer(myHost, myPort);
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
