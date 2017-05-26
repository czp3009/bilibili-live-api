package com.hiczp.bilibili.live.danmu.api.entity;

/**
 * Created by czp on 17-5-24.
 */
public class WelcomeGuardEntity extends JSONEntity {
    public WelcomeGuardEntityData data;
    public Integer roomid;

    public class WelcomeGuardEntityData {
        public Integer uid;
        public String username;
        public Integer guard_level;
    }
}
