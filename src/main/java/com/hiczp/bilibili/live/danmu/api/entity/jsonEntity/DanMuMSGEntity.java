package com.hiczp.bilibili.live.danmu.api.entity.jsonEntity;

import com.alibaba.fastjson.JSONArray;
import com.hiczp.bilibili.live.danmu.api.entity.JSONEntity;

/**
 * Created by czp on 17-5-24.
 */
public class DanMuMSGEntity extends JSONEntity {
    public JSONArray info;  //info中有许多内容含义不明

    //Unix时间戳
    public Long getTimestamp() {
        return info.getJSONArray(0).getLong(4);
    }

    public String getDanMuContent() {
        return info.getString(1);
    }

    public Integer getSenderId() {
        return info.getJSONArray(2).getInteger(0);
    }

    public String getSenderNick() {
        return info.getJSONArray(2).getString(1);
    }

    public Boolean getSenderIsGuard() {
        return info.getJSONArray(2).getBoolean(2);
    }

    public Boolean getSenderIsVIP() {
        return info.getJSONArray(2).getBoolean(3);
    }

    public Integer getSenderLevel() {
        return info.getJSONArray(4).getInteger(0);
    }

    public String getSenderRank() {
        return info.getJSONArray(4).getString(3);
    }
}
