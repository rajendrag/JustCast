package com.rp.justcast.compress;

import android.os.AsyncTask;

import com.rp.justcast.util.JustCastUtils;

import java.io.File;

/**
 * Created by rp on 2/14/15.
 */
public class ImageCompressionTask extends AsyncTask<File, Void, Void>{
    @Override
    protected Void doInBackground(File... params) {
        File in = params[0];
        JustCastUtils.compressImage(in);
        return null;
    }
}
