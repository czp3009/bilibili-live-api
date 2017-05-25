package com.hiczp.bilibili.live.danmu.api.entity;

import java.util.Random;

/**
 * Created by czp on 17-5-24.
 */
public class JoinEntity {
    public int roomid;
    public long uid;

    public JoinEntity(int roomid) {
        this.roomid = roomid;
        uid = (long) (1e14 + 2e14 * new Random().nextDouble());
    }
}
