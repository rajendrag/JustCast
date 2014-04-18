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

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.rp.justcast.R;

/**
 * An {@link ArrayAdapter} to populate the list of videos.
 */
public class VideoListAdapter extends ArrayAdapter<MediaInfo> {

    private final Context mContext;
    private final float mAspectRatio = 9f / 16f;

    /**
     * @param context
     * @param resource
     */
    public VideoListAdapter(Context context) {
        super(context, 0);
        this.mContext = context;
    }

    /*
     * (non-Javadoc)
     * @see android.widget.ArrayAdapter#getView(int, android.view.View, android.view.ViewGroup)
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;
        LayoutInflater inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        MediaMetadata mm = getItem(position).getMetadata();

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.browse_row, null);
            holder = new ViewHolder();
            holder.imgView = (ImageView) convertView.findViewById(R.id.imageView1);
            holder.titleView = (TextView) convertView.findViewById(R.id.textView1);
            holder.descrView = (TextView) convertView.findViewById(R.id.textView2);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        AQuery aq = new AQuery(convertView);
        if (!mm.getImages().isEmpty()) {
            aq.id(holder.imgView).width(110).image(mm.getImages().get(0).getUrl().toString(),
                    true, true, 0, R.drawable.default_video, null, 0, mAspectRatio);
        }
        aq.id(holder.titleView).text(mm.getString(MediaMetadata.KEY_TITLE));
        aq.id(holder.descrView).text(mm.getString(MediaMetadata.KEY_SUBTITLE));

        return convertView;
    }

    private class ViewHolder {
        TextView titleView;
        TextView descrView;
        ImageView imgView;
    }

    public void setData(List<MediaInfo> data) {
        clear();
        if (data != null) {
            for (MediaInfo item : data) {
                add(item);
            }
        }

    }
}
