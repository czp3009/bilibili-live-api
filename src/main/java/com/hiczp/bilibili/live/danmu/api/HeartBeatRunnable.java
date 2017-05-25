package com.hiczp.bilibili.live.danmu.api;

import java.io.OutputStream;

/**
 * Created by czp on 17-5-24.
 */
class HeartBeatRunnable implements Runnable {
    private OutputStream outputStream;

    HeartBeatRunnable(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    @Override
    public void run() {
        while (true) {
            try {
                outputStream.write(PackageRepository.getHeartBeatPackageBytes());
                outputStream.flush();
                Thread.sleep(30000);
            } catch (Exception e) {
                break;
            }
        }
    }
}
