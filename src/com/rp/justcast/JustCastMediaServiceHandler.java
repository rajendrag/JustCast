package com.rp.justcast;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.entity.ContentProducer;
import org.apache.http.entity.EntityTemplate;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;

import android.app.NotificationManager;
import android.content.Context;
import android.net.Uri;
import android.util.Log;
//import com.rp.justcast.AbstractWebServer.Response;

public class JustCastMediaServiceHandler implements HttpRequestHandler {
	private static final long[] VIBRATE = { 0, 100, 200, 200, 100, 300 };

	private Context context = null;
	private NotificationManager notifyManager = null;

	public JustCastMediaServiceHandler(Context context, NotificationManager notifyManager) {
		this.context = context;
		this.notifyManager = notifyManager;
	}

	@Override
	public void handle(final HttpRequest request, final HttpResponse response, final HttpContext httpContext) throws HttpException, IOException {
		String uriString = request.getRequestLine().getUri();
		Uri uri = Uri.parse(uriString);
		final String message = URLDecoder.decode(uri.getQueryParameter("path"));

		// AppLog.logString("Message URI: " + uriString);

		// displayMessage(message);
		Header[] allHeaders = request.getAllHeaders();
		final Map<String, String> headers = new LinkedHashMap<String, String>();
		for(Header header : allHeaders) {
			headers.put(header.getName(), header.getValue());
		}
		HttpEntity entity = new EntityTemplate(new ContentProducer() {
			public void writeTo(final OutputStream outstream) throws IOException {
				
				
				//OutputStreamWriter writer = new OutputStreamWriter(outstream, "UTF-8");
				// String resp = Utility.openHTMLString(context,
				// R.raw.messagesend);
				String resp = "Hello Ramya Baby!!";
				File f = new File(message);
				String mime = "image/png";
				InputStream is = serveFile(response, headers, f, mime);
				if (is != null) {
					send(outstream, is, headers, mime);
				}
				//writer.write(resp);
				outstream.flush();
			}
		});
		//response.setHeader("Content-Type", "text/html");
		//response.setHeader("","");
		response.setEntity(entity);
	}

	
	private InputStream serveFile(HttpResponse res, Map<String, String> header, File file, String mime) {
		FileInputStream fis = null;
		try {
			// Calculate etag
			String etag = Integer.toHexString((file.getAbsolutePath() + file.lastModified() + "" + file.length()).hashCode());

			// Support (simple) skipping:
			long startFrom = 0;
			long endAt = -1;
			String range = header.get("range");
			if (range != null) {
				if (range.startsWith("bytes=")) {
					range = range.substring("bytes=".length());
					int minus = range.indexOf('-');
					try {
						if (minus > 0) {
							startFrom = Long.parseLong(range.substring(0, minus));
							endAt = Long.parseLong(range.substring(minus + 1));
						}
					} catch (NumberFormatException ignored) {
					}
				}
			}

			// Change return code and add Content-Range header when skipping is
			// requested
			long fileLen = file.length();
			if (range != null && startFrom >= 0) {
				if (startFrom >= fileLen) {
					//res = createResponse(Response.Status.RANGE_NOT_SATISFIABLE, JustCastWebServer.MIME_PLAINTEXT, "");
					res.setHeader("Content-Range", "bytes 0-0/" + fileLen);
					res.addHeader("ETag", etag);
				} else {
					if (endAt < 0) {
						endAt = fileLen - 1;
					}
					long newLen = endAt - startFrom + 1;
					if (newLen < 0) {
						newLen = 0;
					}

					final long dataLen = newLen;
					fis = new FileInputStream(file) {
						@Override
						public int available() throws IOException {
							return (int) dataLen;
						}
					};
					fis.skip(startFrom);
					//Log.d(TAG, "Chunked Response Starts["+startFrom+"] ends ["+endAt+"]");
					//res = createResponse(Response.Status.PARTIAL_CONTENT, mime, fis);
					//res = createResponse(Response.Status.OK, mime, fis);
					//res.setChunkedTransfer(true);
					res.addHeader("Connection", "close");
					res.addHeader("Content-Length", "" + dataLen);
					res.addHeader("Content-Range", "bytes " + startFrom + "-" + endAt + "/" + fileLen);
					res.addHeader("ETag", etag);
				}
			} else {
				if (etag.equals(header.get("if-none-match"))){
					//res = createResponse(Response.Status.NOT_MODIFIED, mime, "");
				} else {
					//res = createResponse(Response.Status.OK, mime, new FileInputStream(file));
					res.addHeader("Content-Length", "" + fileLen);
					res.addHeader("ETag", etag);
				}
			}
		} catch (IOException ioe) {
			//res = createResponse(Response.Status.FORBIDDEN, JustCastWebServer.MIME_PLAINTEXT, "FORBIDDEN: Reading file failed.");
		}
		res.addHeader("Connection", "close");
		return fis;
	}
	
	
	/**
	 * Sends given response to the socket.
	 */
	private void send(OutputStream outputStream, InputStream data, Map<String, String> header, String mime) {
		SimpleDateFormat gmtFrmt = new SimpleDateFormat("E, d MMM yyyy HH:mm:ss 'GMT'", Locale.US);
		gmtFrmt.setTimeZone(TimeZone.getTimeZone("GMT"));
		StringBuilder headers = new StringBuilder();
		try {
			/*if (status == null) {
				throw new Error("sendResponse(): Status can't be null.");
			}*/
			//PrintWriter pw = new PrintWriter(new BufferedOutputStream(outputStream, 32 * 1024));
			//pw.print("HTTP/1.1 " + status.getDescription() + " \r\n");
			//headers.append("HTTP/1.1 " + status.getDescription() + " \r\n");
			if (mime != null) {
				//pw.print("Content-Type: " + mime + "\r\n");
				headers.append("Content-Type: " + mime + "\r\n");
				header.put("Content-Type", mime);
			}

			if (header == null || header.get("Date") == null) {
				//pw.print("Date: " + gmtFrmt.format(new Date()) + "\r\n");
				headers.append("Date: " + gmtFrmt.format(new Date()) + "\r\n");
				header.put("Date", gmtFrmt.format(new Date()));
			}

			/*if (header != null) {
				for (String key : header.keySet()) {
					String value = header.get(key);
					pw.print(key + ": " + value + "\r\n");
					headers.append(key + ": " + value + "\r\n");
				}
			}*/

			// pw.print("Connection: keep-alive\r\n");
			//pw.print("Connection: close\r\n");
			header.put("Connection", "close");
			//if (requestMethod != Method.HEAD && chunkedTransfer) {
				//sendAsChunked(outputStream, pw);
			//} else {
				sendAsFixedLength(outputStream, data, header);
			//}

			 outputStream.flush();
			//safeClose(data);
		} catch (Throwable ioe) {
			ioe.printStackTrace();
			//Log.e(TAG, "Error while writing response", ioe);
			// Couldn't write? No can do.
		}
	}

	private void sendAsFixedLength(OutputStream outputStream, InputStream data, Map<String, String> header) throws IOException {
		int pending = data != null ? data.available() : 0; // This is to support partial sends, see serveFile()
		Log.d("", "Sending pending data " + pending);
		//pw.print("Content-Length: " + pending + "\r\n");
		header.put("Content-Length", String.valueOf(pending));
		//pw.print("\r\n");
		//pw.flush();

		if ( data != null) {
			int BUFFER_SIZE = 64 * 1024;
			byte[] buff = new byte[BUFFER_SIZE];
			while (pending > 0) {
				int read = data.read(buff, 0, ((pending > BUFFER_SIZE) ? BUFFER_SIZE : pending));
				if (read <= 0) {
					break;
				}
				outputStream.write(buff, 0, read);
				// outputStream.flush();
				pending -= read;
				Log.d("", "Sent " + read);
			}
		}
	}

}