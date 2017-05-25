package com.hiczp.bilibili.live.danmu.api;

import com.hiczp.bilibili.live.danmu.api.entity.jsonEntity.*;

/**
 * Created by czp on 17-5-24.
 */
public interface ILiveDanMuCallback {
    //连接断开
    void onDisconnect();

    //收到在线人数数据包
    void onOnlineCountPackage(int onlineCount);

    //收到弹幕消息数据包
    void onDanMuMSGPackage(DanMuMSGEntity danMuMSGEntity);

    //收到系统消息数据包
    void onSysMSGPackage(SysMSGEntity sysMSGEntity);

    //收到礼物数据包
    void onSendGiftPackage(SendGiftEntity sendGiftEntity);

    //收到系统礼物数据包
    void onSysGiftPackage(SysGiftEntity sysGiftEntity);

    //收到欢迎数据包
    void onWelcomePackage(WelcomeEntity welcomeEntity);

    //收到欢迎管理员数据包
    void onWelcomeGuardPackage(WelcomeGuardEntity welcomeGuardEntity);

    //直播开始
    void onLivePackage(LiveEntity liveEntity);

    //直播结束
    void onPreparingPackage(PreparingEntity preparingEntity);
}
