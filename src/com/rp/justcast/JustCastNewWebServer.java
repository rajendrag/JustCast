package com.rp.justcast;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.http.HttpException;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.impl.DefaultHttpResponseFactory;
import org.apache.http.impl.DefaultHttpServerConnection;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.BasicHttpProcessor;
import org.apache.http.protocol.HttpRequestHandlerRegistry;
import org.apache.http.protocol.HttpService;
import org.apache.http.protocol.ResponseConnControl;
import org.apache.http.protocol.ResponseContent;
import org.apache.http.protocol.ResponseDate;
import org.apache.http.protocol.ResponseServer;

import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class JustCastNewWebServer extends Thread {
	private static final String SERVER_NAME = "JustCastWebServer";
	private static final String ALL_PATTERN = "*";
	private static final String MESSAGE_PATTERN = "/message*";
	private static final String FOLDER_PATTERN = "/dir*";

	private boolean isRunning = false;
	private Context context = null;
	private int myPort = 0;
	private String myHost;

	private BasicHttpProcessor httpproc = null;
	private BasicHttpContext httpContext = null;
	private HttpService httpService = null;
	private HttpRequestHandlerRegistry registry = null;
	private NotificationManager notifyManager = null;

	public JustCastNewWebServer(Context context, String host, int serverPort) {
		super(SERVER_NAME);

		this.setContext(context);
		this.setNotifyManager(notifyManager);

		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
		this.myHost = host;
		this.myPort = serverPort;
		httpproc = new BasicHttpProcessor();
		httpContext = new BasicHttpContext();

		httpproc.addInterceptor(new ResponseDate());
		httpproc.addInterceptor(new ResponseServer());
		httpproc.addInterceptor(new ResponseContent());
		httpproc.addInterceptor(new ResponseConnControl());

		httpService = new HttpService(httpproc, new DefaultConnectionReuseStrategy(), new DefaultHttpResponseFactory());

		registry = new HttpRequestHandlerRegistry();

		//registry.register(ALL_PATTERN, new HomePageHandler(context));
		registry.register(ALL_PATTERN, new JustCastMediaServiceHandler(context, notifyManager));
		//registry.register(FOLDER_PATTERN, new FolderCommandHandler(context, serverPort));

		httpService.setHandlerResolver(registry);
	}

	@Override
	public void run() {
		super.run();

		try {
			ServerSocket myServerSocket = new ServerSocket();
			myServerSocket.bind((myHost != null) ? new InetSocketAddress(myHost, myPort) : new InetSocketAddress(myPort));
			myServerSocket.setReuseAddress(true);

			while (isRunning) {
				try {
					final Socket socket = myServerSocket.accept();

					DefaultHttpServerConnection serverConnection = new DefaultHttpServerConnection();

					serverConnection.bind(socket, new BasicHttpParams());

					httpService.handleRequest(serverConnection, httpContext);

					serverConnection.shutdown();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (HttpException e) {
					e.printStackTrace();
				}
			}

			myServerSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public synchronized void startThread() {
		isRunning = true;

		super.start();
	}

	public synchronized void stopThread() {
		isRunning = false;
	}

	public void setNotifyManager(NotificationManager notifyManager) {
		this.notifyManager = notifyManager;
	}

	public NotificationManager getNotifyManager() {
		return notifyManager;
	}

	public void setContext(Context context) {
		this.context = context;
	}

	public Context getContext() {
		return context;
	}
}