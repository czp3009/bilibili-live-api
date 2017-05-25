package com.hiczp.bilibili.live.danmu.api.entity.jsonEntity;

import com.hiczp.bilibili.live.danmu.api.entity.JSONEntity;

/**
 * Created by czp on 17-5-24.
 */
public class WelcomeEntity extends JSONEntity {
    public WelcomeEntityData data;
    public Integer roomid;

    public class WelcomeEntityData {
        public Integer uid;
        public String uname;
        public Boolean isadmin;
        public Boolean vip;
    }
}
