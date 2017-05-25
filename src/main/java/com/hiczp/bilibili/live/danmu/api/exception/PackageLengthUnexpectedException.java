package com.hiczp.bilibili.live.danmu.api.exception;

/**
 * Created by czp on 17-5-25.
 */
public class PackageLengthUnexpectedException extends RuntimeException {
    public PackageLengthUnexpectedException(String message, Object... objects) {
        super(String.format(message, objects));
    }
}
