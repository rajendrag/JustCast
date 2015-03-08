package com.rp.justcast.music;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.rp.justcast.JustCast;
import com.rp.justcast.R;
import com.rp.justcast.photos.ImageWorker;

public class MusicAlbumsAdapter extends ArrayAdapter<MusicAlbum> {

	Context mContext;
	ImageWorker imageWorker;

	public MusicAlbumsAdapter(Context context, ImageWorker imageWorker) {
		super(context, 0);
		mContext = context;
		this.imageWorker = imageWorker;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		MusicAlbum album = getItem(position);

		if (convertView == null) {
			convertView = inflater.inflate(R.layout.music_row, null);
			holder = new ViewHolder();
			holder.imgView = (ImageView) convertView.findViewById(R.id.music_thumb_image);
			holder.titleView = (TextView) convertView.findViewById(R.id.music_title);
			holder.artist = (TextView) convertView.findViewById(R.id.music_artist);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		AQuery aq = new AQuery(convertView);
		imageWorker.loadImage(album.getAlbumArt(), holder.imgView);
		aq.id(holder.titleView).text(album.getAlbumTitle());
		aq.id(holder.artist).text(album.getAlbumArtist());
		return convertView;

	}

	private class ViewHolder {
		TextView titleView;
		TextView artist;
		ImageView imgView;
	}

	private String getMediaType() {
		return JustCast.MUSIC_CONTENT_TYPE;
	}

	public void setData(List<MusicAlbum> data) {
		clear();
		if (data != null) {
			for (MusicAlbum item : data) {
				add(item);
			}
		}

	}
}
