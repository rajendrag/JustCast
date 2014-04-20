/*
 * Copyright (C) 2013 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.rp.justcast.video;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;

import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.common.images.WebImage;
import com.rp.justcast.JustCast;

public class VideoProvider {

	private static final String TAG = "VideoProvider";
	private static String TAG_MEDIA = "videos";

	private static List<MediaInfo> mediaList;

	public static List<MediaInfo> buildMedia() throws JSONException {

		if (null != mediaList) {
			return mediaList;
		}

		String[] columns = { MediaStore.Video.Media._ID, MediaStore.Video.Media.DATA, MediaStore.Video.Media.TITLE, MediaStore.Video.Media.DURATION };
		String orderBy = MediaStore.Images.Media.DATE_TAKEN + " desc";
		Cursor videoCursor = null;
		try {
			videoCursor = JustCast.getmAppContext().getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, columns, null, null, orderBy);
			videoCursor.moveToFirst();
			long fileId = videoCursor.getLong(videoCursor.getColumnIndex(MediaStore.Video.Media._ID));
			Log.w(TAG, "Building Media");
			Log.w(TAG, "Video Count" + videoCursor.getCount());
			int count = videoCursor.getCount();
			Log.d(TAG, "Count of images" + count);
			mediaList = new ArrayList<MediaInfo>();
			for (int i = 0; i < count; i++) {
				videoCursor.moveToPosition(i);
				int dataColumnIndex = videoCursor.getColumnIndex(MediaStore.Video.Media.DATA);
				int titleIndex = videoCursor.getColumnIndex(MediaStore.Video.Media.TITLE);
				Log.w(TAG, "Video added" + videoCursor.getString(dataColumnIndex));
				String path = videoCursor.getString(dataColumnIndex);
				String title = videoCursor.getString(titleIndex);
				MediaMetadata movieMetadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE);
				movieMetadata.putString("VIDEO_PATH", path);
				movieMetadata.putString(MediaMetadata.KEY_SUBTITLE, title);
				movieMetadata.putString(MediaMetadata.KEY_TITLE, title);
				movieMetadata.putString(MediaMetadata.KEY_STUDIO, title);
				path = JustCast.addJustCastServerParam(path);
				MediaInfo mediaInfo = new MediaInfo.Builder(path).setStreamType(MediaInfo.STREAM_TYPE_BUFFERED).setContentType(getMediaType()).setMetadata(movieMetadata).build();
				mediaList.add(mediaInfo);
			}
		} finally {
			videoCursor.close();
		}
		return mediaList;
	}

	private static MediaInfo buildMediaInfo(String title, String subTitle, String studio, String url, String imgUrl, String bigImageUrl) {
		MediaMetadata movieMetadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE);

		movieMetadata.putString(MediaMetadata.KEY_SUBTITLE, subTitle);
		movieMetadata.putString(MediaMetadata.KEY_TITLE, title);
		movieMetadata.putString(MediaMetadata.KEY_STUDIO, studio);
		movieMetadata.addImage(new WebImage(Uri.parse(imgUrl)));
		movieMetadata.addImage(new WebImage(Uri.parse(bigImageUrl)));

		return new MediaInfo.Builder(url).setStreamType(MediaInfo.STREAM_TYPE_BUFFERED).setContentType(getMediaType()).setMetadata(movieMetadata).build();
	}

	private static String getMediaType() {
		return "video/mp4";
	}

}
