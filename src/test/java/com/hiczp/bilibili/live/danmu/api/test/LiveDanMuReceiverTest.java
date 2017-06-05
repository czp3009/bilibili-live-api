package com.hiczp.bilibili.live.danmu.api.test;

import com.hiczp.bilibili.live.danmu.api.LiveDanMuReceiver;

/**
 * Created by czp on 17-5-24.
 */
class LiveDanMuReceiverTest {
    public static void main(String[] args) {
        LiveDanMuReceiver liveDanMuReceiver =
                new LiveDanMuReceiver("http://live.bilibili.com/545342")
                        .setPrintDebugInfo(true)
                        .addCallback(new LiveDanMuCallback());
        try {
            liveDanMuReceiver.connect();
            Thread.sleep(35000);
            liveDanMuReceiver.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
