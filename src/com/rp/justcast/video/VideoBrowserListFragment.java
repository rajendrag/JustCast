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

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.cast.MediaInfo;
import com.google.android.libraries.cast.companionlibrary.cast.VideoCastManager;
import com.google.android.libraries.cast.companionlibrary.utils.Utils;
import com.google.android.libraries.cast.companionlibrary.widgets.MiniController;
import com.rp.justcast.JustCast;
import com.rp.justcast.R;
import com.rp.justcast.photos.ImageWorker;

public class VideoBrowserListFragment extends ListFragment implements
        LoaderManager.LoaderCallbacks<List<MediaInfo>> {

    private VideoListAdapter mAdapter;
    private MiniController mMini;
    private VideoCastManager mCastManager;

    public VideoBrowserListFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);
        mCastManager = VideoCastManager.getInstance();
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
        ImageWorker imageWorker = JustCast.getImageWorker();
        mAdapter = new VideoListAdapter(getActivity(), imageWorker);
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
        Intent intent = new Intent(getActivity(), LocalPlayerActivity.class);
        intent.putExtra("media", Utils.mediaInfoToBundle(info));
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

}
