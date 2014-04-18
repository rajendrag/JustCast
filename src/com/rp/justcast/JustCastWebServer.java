package com.rp.justcast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.util.Map;
import java.util.Random;

import android.util.Log;

import com.rp.justcast.AbstractWebServer.Response;
import com.rp.justcast.AbstractWebServer.Response.Status;

public class JustCastWebServer extends AbstractWebServer {
	//Thread serverThread;
	
	private static final String TAG = JustCastWebServer.class.getSimpleName();
	
	public JustCastWebServer(String hostname, int port) {
		super(hostname, port);
	}

	/*@Override
	public void start() {
		serverThread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					JustCastWebServer.super.start();
				} catch (IOException e) {
					e.printStackTrace();
					throw new RuntimeException(e);
				}
			}
		});
		serverThread.start();
	}

	@Override
	public void stop() {
		super.stop();
		try {
			serverThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}*/
	
	@Override
	public Response serve(IHTTPSession session) {
		Method method = session.getMethod();
		String uri = session.getUri();
		Log.d(TAG, method + " '" + uri + "' ");
		// File root = Environment.getExternalStorageDirectory();
		try {
			Map<String, String> parms = session.getParms();
			String path = parms.get("path");
			Log.d(TAG, "request for media on sdCard " + path);
			File request = new File(path);
			InputStream mbuffer = new FileInputStream(request);
			FileNameMap fileNameMap = URLConnection.getFileNameMap();
			String mimeType = fileNameMap.getContentTypeFor(path);

			Response streamResponse = new Response(Status.PARTIAL_CONTENT, mimeType, mbuffer);
			Random rnd = new Random();
			String etag = Integer.toHexString(rnd.nextInt());
			streamResponse.addHeader("ETag", etag);
			streamResponse.addHeader("Connection", "close");
			//streamResponse.setChunkedTransfer(true); 
            Log.d("TEAONLY", "Starting streaming server");
			return streamResponse;
		} catch (Exception e) {
			Log.e(TAG, e.getMessage(), e);
			return new Response(Response.Status.NOT_FOUND, MIME_PLAINTEXT, e.getMessage());
		}

	}

}
