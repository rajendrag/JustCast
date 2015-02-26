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

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.cast.MediaInfo;
import com.google.sample.castcompanionlibrary.cast.VideoCastManager;
import com.google.sample.castcompanionlibrary.utils.Utils;
import com.google.sample.castcompanionlibrary.widgets.MiniController;
import com.netcompss.ffmpeg4android.GeneralUtils;
import com.netcompss.loader.LoadJNI;
import com.rp.justcast.JustCast;
import com.rp.justcast.R;
import com.rp.justcast.compress.CompressingMediaHolder;
import com.rp.justcast.photos.CompressedImage;
import com.rp.justcast.util.JustCastUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

public class VideoBrowserListFragment extends ListFragment implements
        LoaderManager.LoaderCallbacks<List<MediaInfo>> {
    private static final String TAG = "VideoListFragment";
    private VideoListAdapter mAdapter;
    private MiniController mMini;
    private VideoCastManager mCastManager;
    
    public VideoBrowserListFragment() {
    	
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	View v = super.onCreateView(inflater, container, savedInstanceState);
    	mCastManager = JustCast.getCastManager();
		// -- Adding MiniController
		mMini = (MiniController) getActivity().findViewById(R.id.miniController1);
		mCastManager.addMiniController(mMini);
		mMini.setOnMiniControllerChangedListener(mCastManager);
    	//container.addView(mMini);
    	return v;
    }
    
    /*
     * (non-Javadoc)
     * @see android.support.v4.app.Fragment#onActivityCreated(android.os.Bundle)
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getListView().setFastScrollEnabled(true);
        mAdapter = new VideoListAdapter(getActivity());
        setEmptyText(getString(R.string.no_video_found));
        setListAdapter(mAdapter);
        setListShown(false);
        getLoaderManager().initLoader(0, null, this);
    }

    /*
     * (non-Javadoc)
     * @see android.support.v4.app.LoaderManager.LoaderCallbacks#onLoadFinished(android
     * .support.v4.content.Loader, java.lang.Object)
     */
    @Override
    public void onLoadFinished(Loader<List<MediaInfo>> arg0, List<MediaInfo> data) {
        mAdapter.setData(data);
        if (isResumed()) {
            setListShown(true);
        } else {
            setListShownNoAnimation(true);
        }
    }

    /*
     * (non-Javadoc)
     * @see android.support.v4.app.LoaderManager.LoaderCallbacks#onLoaderReset(android
     * .support.v4.content.Loader)
     */
    @Override
    public void onLoaderReset(Loader<List<MediaInfo>> arg0) {
        mAdapter.setData(null);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        MediaInfo selectedMedia = mAdapter.getItem(position);
        if(JustCast.isSlideShowInProgress()) {
        	Toast t = Toast.makeText(getActivity(), "Slideshow ended", Toast.LENGTH_SHORT);
        	t.show();
        	JustCast.setSlideShowInProgress(true);
        	if(JustCast.isSlideShowEnabled()) {
        		JustCast.toggleSlideShow();
        	}
        }
        handleNavigation(selectedMedia, false);
    }

    private void handleNavigation(MediaInfo info, boolean autoStart) {
        String path = info.getMetadata().getString("VIDEO_PATH");
        printVideoInfo(path);
        Intent intent = new Intent(getActivity(), LocalPlayerActivity.class);
        intent.putExtra("media", Utils.fromMediaInfo(info));
        intent.putExtra("shouldStart", autoStart);
        getActivity().startActivity(intent);
    }

    /*
     * (non-Javadoc)
     * @see android.support.v4.app.LoaderManager.LoaderCallbacks#onCreateLoader(int,
     * android.os.Bundle)
     */
    @Override
    public Loader<List<MediaInfo>> onCreateLoader(int arg0, Bundle arg1) {
        return new VideoItemLoader(getActivity());
    }

	public static VideoBrowserListFragment newInstance() {
        VideoBrowserListFragment f = new VideoBrowserListFragment();
        Bundle b = new Bundle();
        f.setArguments(b);
        return f;
    }

    public static VideoBrowserListFragment newInstance(Bundle b) {
        VideoBrowserListFragment f = new VideoBrowserListFragment();
        f.setArguments(b);
        return f;
    }

    public void printVideoInfo(String path) {
        LoadJNI vk = new LoadJNI();
        try {
            String baseFolder =  Environment.getExternalStorageDirectory().getAbsolutePath();
            String workFolder = JustCast.getmAppContext().getFilesDir().getAbsolutePath();
            String vkLogPath = workFolder + "/vk.log";
            GeneralUtils.deleteFileUtil(vkLogPath);
            Log.d(TAG, "vk log (native log) path: " + vkLogPath);
            String outFile = baseFolder+path.substring(path.lastIndexOf(File.separator));
            //String[] complexCommand = {"ffmpeg","-i", path};
            Log.d(TAG, outFile);
            //String[] complexCommand = {"ffmpeg","-y" ,"-i", path,"-strict","experimental","-s", "160x120","-r","25", "-vcodec", "libx264", "-b", "150k", "-ab","48000", "-ac", "2", "-ar", "22050", outFile};
            String[] complexCommand = {"ffmpeg","-y" ,"-i", path,"-strict","experimental","-s", "160x120","-r","25", "-vcodec", "libx264", "-b", "150k", "-ab","48000", "-ac", "2", "-ar", "22050", outFile};
            //String[] complexCommand = {"ffmpeg", "-y", "-i", path, "-codec:v", "libx264", "-crf", "23", "-preset", "fast", "-codec:a", "libfdk_aac", "-vf", "scale=-1:720,format=yuv420p", outFile};
            long start = System.currentTimeMillis();
            vk.run(complexCommand , workFolder , JustCast.getmAppContext());
            long end = System.currentTimeMillis();
            Log.i(TAG, "Took "+ (end-start)/1000+" secs");
            Log.i(TAG, "ffmpeg4android finished successfully");
            File input = new File(path);
            File output = new File(outFile);
            CompressedImage i = new CompressedImage(output);
            JustCast.getCompressingMediaHolder().put(JustCastUtils.getETag(input), i);
            Log.d(TAG, "INput ["+input.length()+"] Output ["+output.length()+"]");
            Log.i(TAG, readFile(vkLogPath));
            //GeneralUtils.copyFileToFolder(vkLogPath, baseFolder);
        } catch (Throwable e) {
            Log.e("test", "vk run exception.", e);
        }

    }

    public String readFile(String path) {
        StringBuilder text = new StringBuilder();
        try {
            File sdcard = Environment.getExternalStorageDirectory();
            File file = new File(path);

            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while ((line = br.readLine()) != null) {
                text.append(line);
                text.append('\n');
            }
            br.close() ;
        }catch (IOException e) {
            e.printStackTrace();
        }
        return text.toString();
    }
}
