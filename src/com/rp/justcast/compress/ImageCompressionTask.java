package com.rp.justcast.compress;

import android.os.AsyncTask;

import com.rp.justcast.JustCastUtils;
import com.rp.justcast.util.CircularByteBuffer;

/**
 * Created by rp on 2/14/15.
 */
public class ImageCompressionTask extends AsyncTask<CircularByteBuffer, Void, Void>{
    @Override
    protected Void doInBackground(CircularByteBuffer... params) {
        CircularByteBuffer in = params[0];
        JustCastUtils.compressImage(in);
        return null;
    }
}
