package com.btx.abero.media;

/**
 * Created by abero on 2018/4/25.
 */

public enum FileType {

    VIDEO(0),
    AUDIO(1),
    PICTURE(2);

    private int value;

    FileType(int value) {
        this.value = value;
    }

    public int value() {
        return this.value;
    }

}
