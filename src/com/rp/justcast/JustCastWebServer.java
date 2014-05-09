package com.rp.justcast;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import android.util.Log;

public class JustCastWebServer extends AbstractWebServer {
	private static final String TAG = "JustCastWebServer";
	/**
	 * Common mime type for dynamic content: binary
	 */
	//public static final String MIME_DEFAULT_BINARY = "application/octet-stream";
	public static final String MIME_DEFAULT_BINARY = "image/jpeg";

	/**
	 * Hashtable mapping (String)FILENAME_EXTENSION -> (String)MIME_TYPE
	 */
	private static final Map<String, String> MIME_TYPES = new HashMap<String, String>() {
		{
			put("css", "text/css");
			put("htm", "text/html");
			put("html", "text/html");
			put("xml", "text/xml");
			put("java", "text/x-java-source, text/java");
			put("md", "text/plain");
			put("txt", "text/plain");
			put("asc", "text/plain");
			put("gif", "image/gif");
			put("jpg", "image/jpeg");
			put("jpeg", "image/jpeg");
			put("png", "image/png");
			put("mp3", "audio/mpeg");
			put("m3u", "audio/mpeg-url");
			put("mp4", "video/mp4");
			put("ogv", "video/ogg");
			put("flv", "video/x-flv");
			put("mov", "video/quicktime");
			put("swf", "application/x-shockwave-flash");
			put("js", "application/javascript");
			put("pdf", "application/pdf");
			put("doc", "application/msword");
			put("ogg", "application/x-ogg");
			put("zip", "application/octet-stream");
			put("exe", "application/octet-stream");
			put("class", "application/octet-stream");
			put("vob", "video/mpeg");
		}
	};

	private final List<File> rootDirs;
	private final boolean quiet;

	public JustCastWebServer(String host, int port, File wwwroot, boolean quiet) {
		super(host, port);
		this.quiet = quiet;
		this.rootDirs = new ArrayList<File>();
		this.rootDirs.add(wwwroot);
	}

	public JustCastWebServer(String host, int port, List<File> wwwroots, boolean quiet) {
		super(host, port);
		this.quiet = quiet;
		this.rootDirs = new ArrayList<File>(wwwroots);
	}

	/**
	 * URL-encodes everything between "/"-characters. Encodes spaces as '%20'
	 * instead of '+'.
	 */
	private String encodeUri(String uri) {
		String newUri = "";
		StringTokenizer st = new StringTokenizer(uri, "/ ", true);
		while (st.hasMoreTokens()) {
			String tok = st.nextToken();
			if (tok.equals("/"))
				newUri += "/";
			else if (tok.equals(" "))
				newUri += "%20";
			else {
				try {
					newUri += URLEncoder.encode(tok, "UTF-8");
				} catch (UnsupportedEncodingException ignored) {
				}
			}
		}
		return newUri;
	}

	public Response serve(IHTTPSession session) {
		Map<String, String> header = session.getHeaders();
		Map<String, String> parms = session.getParms();
		String uri = session.getUri();

		if (!quiet) {
			System.out.println(session.getMethod() + " '" + uri + "' ");

			Iterator<String> e = header.keySet().iterator();
			while (e.hasNext()) {
				String value = e.next();
				System.out.println("  HDR: '" + value + "' = '" + header.get(value) + "'");
			}
			e = parms.keySet().iterator();
			while (e.hasNext()) {
				String value = e.next();
				System.out.println("  PRM: '" + value + "' = '" + parms.get(value) + "'");
			}
		}

		return respond(Collections.unmodifiableMap(header), uri, parms);
	}

	private Response respond(Map<String, String> headers, String uri, Map<String, String> parms) {
		// Remove URL arguments
		uri = uri.trim().replace(File.separatorChar, '/');
		if (uri.indexOf('?') >= 0) {
			uri = uri.substring(0, uri.indexOf('?'));
		}

		// Prohibit getting out of current directory
		if (uri.startsWith("src/main") || uri.endsWith("src/main") || uri.contains("../")) {
			return createResponse(Response.Status.FORBIDDEN, JustCastWebServer.MIME_PLAINTEXT, "FORBIDDEN: Won't serve ../ for security reasons.");
		}

		String path = parms.get("path");

		boolean canServeUri = canServeUri(path);
		if (!canServeUri) {
			return createResponse(Response.Status.NOT_FOUND, JustCastWebServer.MIME_PLAINTEXT, "Error 404, file not found.");
		}
		// Browsers get confused without '/' after the directory, send a
		// redirect.
		// File f = new File(homeDir, uri);
		File f = new File(path);
		if (f.isDirectory() && !uri.endsWith("/")) {
			uri += "/";
			Response res = createResponse(Response.Status.REDIRECT, JustCastWebServer.MIME_HTML, "<html><body>Redirected: <a href=\"" + uri + "\">" + uri + "</a></body></html>");
			res.addHeader("Location", uri);
			return res;
		}

		String mimeTypeForFile = getMimeTypeForFile(path);
		Response response = null;
		response = serveFile(uri, headers, f, mimeTypeForFile);
		return response != null ? response : createResponse(Response.Status.NOT_FOUND, JustCastWebServer.MIME_PLAINTEXT, "Error 404, file not found.");
	}

	private boolean canServeUri(String uri) {
		boolean canServeUri;
		File f = new File(uri);
		canServeUri = f.exists();
		return canServeUri;
	}

	/**
	 * Serves file from homeDir and its' subdirectories (only). Uses only URI,
	 * ignores all headers and HTTP parameters.
	 */
	Response serveFile(String uri, Map<String, String> header, File file, String mime) {
		Response res;
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
					res = createResponse(Response.Status.RANGE_NOT_SATISFIABLE, JustCastWebServer.MIME_PLAINTEXT, "");
					res.addHeader("Content-Range", "bytes 0-0/" + fileLen);
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
					FileInputStream fis = new FileInputStream(file) {
						@Override
						public int available() throws IOException {
							return (int) dataLen;
						}
					};
					fis.skip(startFrom);
					Log.d(TAG, "Chunked Response Starts["+startFrom+"] ends ["+endAt+"]");
					res = createResponse(Response.Status.PARTIAL_CONTENT, mime, fis);
					//res = createResponse(Response.Status.OK, mime, fis);
					//res.setChunkedTransfer(true);
					res.addHeader("Connection", "close");
					res.addHeader("Content-Length", "" + dataLen);
					res.addHeader("Content-Range", "bytes " + startFrom + "-" + endAt + "/" + fileLen);
					res.addHeader("ETag", etag);
				}
			} else {
				if (etag.equals(header.get("if-none-match")))
					res = createResponse(Response.Status.NOT_MODIFIED, mime, "");
				else {
					res = createResponse(Response.Status.OK, mime, new FileInputStream(file));
					res.addHeader("Content-Length", "" + fileLen);
					res.addHeader("ETag", etag);
				}
			}
		} catch (IOException ioe) {
			res = createResponse(Response.Status.FORBIDDEN, JustCastWebServer.MIME_PLAINTEXT, "FORBIDDEN: Reading file failed.");
		}
		res.addHeader("Connection", "close");
		return res;
	}

	// Get MIME type from file name extension, if possible
	private String getMimeTypeForFile(String uri) {
		int dot = uri.lastIndexOf('.');
		String mime = null;
		if (dot >= 0) {
			mime = MIME_TYPES.get(uri.substring(dot + 1).toLowerCase());
		}
		return mime == null ? MIME_DEFAULT_BINARY : mime;
	}

	// Announce that the file server accepts partial content requests
	private Response createResponse(Response.Status status, String mimeType, InputStream message) {
		Response res = new Response(status, mimeType, message);
		res.addHeader("Accept-Ranges", "bytes");
		return res;
	}

	// Announce that the file server accepts partial content requests
	private Response createResponse(Response.Status status, String mimeType, String message) {
		Response res = new Response(status, mimeType, message);
		res.addHeader("Accept-Ranges", "bytes");
		return res;
	}

}
