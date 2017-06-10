package com.hiczp.bilibili.live.danmu.api.test;

import com.hiczp.bilibili.live.danmu.api.LiveDanMuSender;
import com.hiczp.bilibili.live.danmu.api.entity.DanMuResponseEntity;

import java.util.Scanner;

/**
 * Created by czp on 17-6-5.
 */
public class LiveDanMuSenderTest {
    public static void main(String[] args) {
        System.out.println("请输入 cookies:");
        LiveDanMuSender liveDanMuSender =
                new LiveDanMuSender("http://live.bilibili.com/1110317")
                        .setCookies(new Scanner(System.in).nextLine());
        try {
            boolean isLogin = liveDanMuSender.testLogin();
            System.out.println("登录状态: " + isLogin);
            if (isLogin) {
                for (int i = 0; i < 10; i++) {
                    DanMuResponseEntity danMuResponseEntity = liveDanMuSender.send("这是自动发送的弹幕" + i);
                    System.out.println(danMuResponseEntity);
                    Thread.sleep(1000);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
