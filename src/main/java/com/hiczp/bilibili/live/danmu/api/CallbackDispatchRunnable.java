package com.hiczp.bilibili.live.danmu.api;

import com.alibaba.fastjson.JSON;
import com.hiczp.bilibili.live.danmu.api.entity.JSONEntity;
import com.hiczp.bilibili.live.danmu.api.entity.jsonEntity.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

/**
 * Created by czp on 17-5-24.
 */
class CallbackDispatchRunnable implements Runnable {
    private LiveDanMuAPI liveDanMuAPI;
    private InputStream inputStream;
    private List<ILiveDanMuCallback> callbacks;
    private Boolean printDebugInfo = false;

    CallbackDispatchRunnable(LiveDanMuAPI liveDanMuAPI, InputStream inputStream, List<ILiveDanMuCallback> callbacks, Boolean printDebugInfo) {
        this.liveDanMuAPI = liveDanMuAPI;
        this.inputStream = inputStream;
        this.callbacks = callbacks;
        this.printDebugInfo = printDebugInfo;
    }

    private void dispatch() throws Exception {
        byte[] packageBytes = PackageRepository.readNextPackage(inputStream);
        ByteBuffer byteBuffer = ByteBuffer.wrap(packageBytes);
        byteBuffer.position(PackageRepository.PACKAGE_LENGTH_BYTES_LENGTH);
        byte[] protocolBytes = new byte[PackageRepository.PACKAGE_PROTOCOL_BYTES_LENGTH];
        byteBuffer.get(protocolBytes);
        Consumer<ILiveDanMuCallback> consumer = null;
        if (Arrays.equals(protocolBytes, PackageRepository.DAN_MU_DATA_PACKAGE_PROTOCOL_BYTES)) {    //json数据包
            byte[] jsonBytes = new byte[byteBuffer.remaining()];
            byteBuffer.get(jsonBytes);
            if (printDebugInfo) {
                System.out.println(new String(jsonBytes));
            }
            String cmd = ((JSONEntity) JSON.parseObject(jsonBytes, JSONEntity.class)).cmd;
            switch (cmd) {
                case "LIVE": {
                    consumer = iLiveDanMuCallback -> iLiveDanMuCallback.onLivePackage(JSON.parseObject(jsonBytes, LiveEntity.class));
                }
                break;
                case "PREPARING": {
                    consumer = iLiveDanMuCallback -> iLiveDanMuCallback.onPreparingPackage(JSON.parseObject(jsonBytes, PreparingEntity.class));
                }
                break;
                case "DANMU_MSG": {
                    consumer = iLiveDanMuCallback -> iLiveDanMuCallback.onDanMuMSGPackage(JSON.parseObject(jsonBytes, DanMuMSGEntity.class));
                }
                break;
                case "SYS_MSG": {
                    consumer = iLiveDanMuCallback -> iLiveDanMuCallback.onSysMSGPackage(JSON.parseObject(jsonBytes, SysMSGEntity.class));
                }
                break;
                case "SEND_GIFT": {
                    consumer = iLiveDanMuCallback -> iLiveDanMuCallback.onSendGiftPackage(JSON.parseObject(jsonBytes, SendGiftEntity.class));
                }
                break;
                case "SYS_GIFT": {
                    consumer = iLiveDanMuCallback -> iLiveDanMuCallback.onSysGiftPackage(JSON.parseObject(jsonBytes, SysGiftEntity.class));
                }
                break;
                case "WELCOME": {
                    consumer = iLiveDanMuCallback -> iLiveDanMuCallback.onWelcomePackage(JSON.parseObject(jsonBytes, WelcomeEntity.class));
                }
                break;
                case "WELCOME_GUARD": {
                    consumer = iLiveDanMuCallback -> iLiveDanMuCallback.onWelcomeGuardPackage(JSON.parseObject(jsonBytes, WelcomeGuardEntity.class));
                }
                break;
                default: {
                    if (printDebugInfo) {
                        System.out.println("Unknown json above");
                    }
                }
            }
        } else if (Arrays.equals(protocolBytes, PackageRepository.ONLINE_COUNT_PACKAGE_PROTOCOL_BYTES)) {    //在线人数数据包
            int onlineCount = byteBuffer.getInt();
            if (printDebugInfo) {
                System.out.println("Viewers: " + onlineCount);
            }
            consumer = iLiveDanMuCallback -> iLiveDanMuCallback.onOnlineCountPackage(onlineCount);
        } else {    //未知数据包
            if (printDebugInfo) {
                System.out.println("Unknown package below");
                Utils.printBytes(packageBytes);
                System.out.println();
            }
        }
        if (consumer != null) {
            callbacks.forEach(consumer);
        }
    }

    @Override
    public void run() {
        while (true) {
            try {
                dispatch();
            } catch (IOException e) {   //socket异常时退出
                break;
            } catch (Exception e) { //其他错误时显示错误信息并继续监听下一个数据包
                e.printStackTrace();
            }
        }

        try {
            liveDanMuAPI.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        callbacks.forEach(ILiveDanMuCallback::onDisconnect);
    }
}
