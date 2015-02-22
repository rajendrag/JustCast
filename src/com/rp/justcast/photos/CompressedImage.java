package com.rp.justcast.photos;

import java.io.File;

/**
 * Created by rp on 2/15/15.
 */
public class CompressedImage {

    private int byteCount;

    private File imageFile;

    public File getImageFile() {
        return imageFile;
    }

    public void setImageFile(File imageFile) {
        this.imageFile = imageFile;
    }

    public CompressedImage(File imageFile) {
        this.imageFile = imageFile;
    }

    public int getByteCount() {
        return byteCount;
    }

    public void setByteCount(int byteCount) {
        this.byteCount = byteCount;
    }

}
