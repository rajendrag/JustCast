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

public class MusicDetailsAdapter extends ArrayAdapter<MusicAlbum> {

	private Context mContext;
	private ImageWorker imageWorker;
	String albumId;
	
	
	public MusicDetailsAdapter(Context context, ImageWorker imageWorker) {
		super(context, 0);
		mContext = context;
		this.imageWorker = imageWorker;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		
		ViewHolder holder;
		LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		MusicAlbum song = getItem(position);
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.music_details_row, null);
			holder = new ViewHolder();
			holder.imgView = (ImageView) convertView.findViewById(R.id.music_detail_thumb_image);
			holder.titleView = (TextView) convertView.findViewById(R.id.music_detail_title);
			holder.artist = (TextView) convertView.findViewById(R.id.music_detail_artist);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		AQuery aq = new AQuery(convertView);
		imageWorker.loadVideoThumbNail(song.getAlbumArt(), holder.imgView);
		/*if (!mm.getImages().isEmpty()) {
			aq.id(holder.imgView).width(110).image(mm.getImages().get(0).getUrl().toString(), true, true, 0, R.drawable.default_video, null, 0, mAspectRatio);
		}*/
		aq.id(holder.titleView).text(song.getAlbumTitle());
		aq.id(holder.artist).text(song.getAlbumArtist());
		return convertView;
	}
	
	private class ViewHolder {
		TextView titleView;
		TextView artist;
		ImageView imgView;
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