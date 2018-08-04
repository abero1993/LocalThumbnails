package com.btx.abero.media;

import java.io.Serializable;

/**
 * Created by abero on 2018/4/21.
 */

public class FileInfo implements Serializable {

    private String mName;
    private String mPath;
    private long mLastModified;
    private int mFileType;

    public String getName() {
        return mName;
    }

    public void setName(String mName) {
        this.mName = mName;
    }

    public String getPath() {
        return mPath;
    }

    public void setPath(String mPath) {
        this.mPath = mPath;
    }

    public long getLastModified() {
        return mLastModified;
    }

    public void setLastModified(long mLastModified) {
        this.mLastModified = mLastModified;
    }

    public int getFileType() {
        return mFileType;
    }

    public void setFileType(int type) {
        mFileType = type;
    }
}
