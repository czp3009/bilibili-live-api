package com.hiczp.bilibili.live.danmu.api.test;

import com.alibaba.fastjson.JSON;
import com.hiczp.bilibili.live.danmu.api.entity.UserInfoEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.util.Scanner;

/**
 * Created by czp on 17-6-23.
 */
public class GetUserInfoText {
    public static void main(String[] args) {
        try (CloseableHttpClient closeableHttpClient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet("http://api.live.bilibili.com/User/getUserInfo");
            System.out.println("Cookies:");
            httpGet.setHeader("Cookie", new Scanner(System.in).nextLine());
            UserInfoEntity userInfoEntity = JSON.parseObject(EntityUtils.toString(closeableHttpClient.execute(httpGet).getEntity()), UserInfoEntity.class);
            System.out.println(JSON.toJSONString(userInfoEntity));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
