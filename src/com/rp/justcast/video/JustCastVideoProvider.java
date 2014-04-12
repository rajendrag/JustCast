package com.rp.justcast.video;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.net.Uri;
import android.util.Log;

import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.common.images.WebImage;

public class JustCastVideoProvider {
	private static final String TAG = "JustCastVideoProvider";
    private static String TAG_MEDIA = "videos";
    private static String THUMB_PREFIX_URL =
            "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/";
    private static String TAG_CATEGORIES = "categories";
    private static String TAG_NAME = "name";
    private static String TAG_STUDIO = "studio";
    private static String TAG_SOURCES = "sources";
    private static String TAG_SUBTITLE = "subtitle";
    private static String TAG_THUMB = "image-480x270"; // "thumb";
    private static String TAG_IMG_780_1200 = "image-780x1200";
    private static String TAG_TITLE = "title";

    private static List<MediaInfo> mediaList;

    protected JSONObject parseUrl(String urlString) {
        InputStream is = null;
        try {
            java.net.URL url = new java.net.URL(urlString);
            URLConnection urlConnection = url.openConnection();
            is = new BufferedInputStream(urlConnection.getInputStream());
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    urlConnection.getInputStream(), "iso-8859-1"), 8);
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            String json = sb.toString();
            return new JSONObject(json);
        } catch (Exception e) {
            Log.d(TAG, "Failed to parse the json for media list", e);
            return null;
        } finally {
            if (null != is) {
                try {
                    is.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
    }

    public static List<MediaInfo> buildMedia(String url) throws JSONException {

        if (null != mediaList) {
            return mediaList;
        }
        mediaList = new ArrayList<MediaInfo>();
        JSONObject jsonObj = new JustCastVideoProvider().parseUrl(url);
        JSONArray categories = jsonObj.getJSONArray(TAG_CATEGORIES);
        if (null != categories) {
            for (int i = 0; i < categories.length(); i++) {
                JSONObject category = categories.getJSONObject(i);
                category.getString(TAG_NAME);
                JSONArray videos = category.getJSONArray(getJsonMediaTag());
                if (null != videos) {
                    for (int j = 0; j < videos.length(); j++) {
                        JSONObject video = videos.getJSONObject(j);
                        String subTitle = video.getString(TAG_SUBTITLE);
                        JSONArray videoUrls = video.getJSONArray(TAG_SOURCES);
                        if (null == videoUrls || videoUrls.length() == 0) {
                            continue;
                        }
                        String videoUrl = videoUrls.getString(0);
                        String imageurl = getThumbPrefix() + video.getString(TAG_THUMB);
                        String bigImageurl = getThumbPrefix() + video.getString(TAG_IMG_780_1200);
                        String title = video.getString(TAG_TITLE);
                        String studio = video.getString(TAG_STUDIO);
                        mediaList.add(buildMediaInfo(title, studio, subTitle, videoUrl, imageurl,
                                bigImageurl));
                    }
                }
            }
        }
        return mediaList;
    }

    private static MediaInfo buildMediaInfo(String title,
            String subTitle, String studio, String url, String imgUrl, String bigImageUrl) {
        MediaMetadata movieMetadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE);

        movieMetadata.putString(MediaMetadata.KEY_SUBTITLE, subTitle);
        movieMetadata.putString(MediaMetadata.KEY_TITLE, title);
        movieMetadata.putString(MediaMetadata.KEY_STUDIO, studio);
        movieMetadata.addImage(new WebImage(Uri.parse(imgUrl)));
        movieMetadata.addImage(new WebImage(Uri.parse(bigImageUrl)));

        return new MediaInfo.Builder(url)
                .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
                .setContentType(getMediaType())
                .setMetadata(movieMetadata)
                .build();
    }

    private static String getMediaType() {
        return "video/mp4";
    }

    private static String getJsonMediaTag() {
        return TAG_MEDIA;
    }

    private static String getThumbPrefix() {
        return THUMB_PREFIX_URL;
    }
}
