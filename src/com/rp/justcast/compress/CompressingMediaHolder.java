package com.rp.justcast.compress;

import android.graphics.Bitmap;

import com.rp.justcast.photos.CompressedImage;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by rp on 2/14/15.
 */
public class CompressingMediaHolder {

    private Map<String, CompressedImage> lockFactory = new LinkedHashMap<String, CompressedImage>(3, 0.75f, true) {

        @Override
        protected boolean removeEldestEntry(Map.Entry<String, CompressedImage> eldest) {
            return size() > 3;
        }

    };

    private static final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private static final Lock read          = readWriteLock.readLock();
    private static final Lock                   write         = readWriteLock.writeLock();

    public CompressedImage get(String eTag) {
        read.lock();
        try {
            return lockFactory.get(eTag);
        } finally {
            read.unlock();
        }
    }

    public void put(String eTag, CompressedImage cbb) {
        write.lock();
        try {
            lockFactory.put(eTag, cbb);
        } finally {
            write.unlock();
        }
    }

}
