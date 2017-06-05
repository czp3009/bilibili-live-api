package com.hiczp.bilibili.live.danmu.api.test;

import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * Created by czp on 17-6-5.
 */
public class SendDanMuTest {
    public static void main(String[] args) {
        try (CloseableHttpClient closeableHttpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost("http://live.bilibili.com/msg/send");
            httpPost.setEntity(
                    new UrlEncodedFormEntity(
                            Arrays.asList(
                                    new BasicNameValuePair("color", "16777215"),
                                    new BasicNameValuePair("fontsize", "25"),
                                    new BasicNameValuePair("mode", "1"),
                                    new BasicNameValuePair("msg", "这是自动发送的弹幕"),
                                    new BasicNameValuePair("rnd", "1496673939"),
                                    new BasicNameValuePair("roomid", "1110317")
                            ),
                            StandardCharsets.UTF_8
                    )
            );
            httpPost.setHeader("Cookie", "DedeUserID=20293030; DedeUserID__ckMd5=cdff5c8e58b793cc; SESSDATA=b465ca15%2C1499247962%2Cc3ebfdc5");
            System.out.println(EntityUtils.toString(closeableHttpClient.execute(httpPost).getEntity()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
