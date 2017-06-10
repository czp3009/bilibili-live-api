package com.hiczp.bilibili.live.danmu.api;

import com.alibaba.fastjson.JSON;
import com.hiczp.bilibili.live.danmu.api.entity.DanMuResponseEntity;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * DanMu send API.
 * Created by czp on 17-6-6.
 */
public class LiveDanMuSender {
    private String urlString;
    private URL url;
    private String roomId;
    private String random;
    private String cookies;

    /**
     * Class construction. Need URL in string.
     *
     * @param url URL of room
     */
    public LiveDanMuSender(String url) {
        this.urlString = url;
    }

    /**
     * Class construction. Need URL of room.
     */
    public LiveDanMuSender(URL url) {
        this.url = url;
    }

    /*public static String generateCookies(String username, String password) {
        return null;
    }*/

    private void resolveRoomIdAndRandom() throws IOException, IllegalArgumentException {
        if (url == null) {
            url = new URL(urlString);
        }
        if (roomId == null || random == null) {
            String scriptText = Jsoup.parse(url, 10000).head().select("script").last().data();
            Matcher matcher;
            //得到 ROOMID
            matcher = Pattern.compile("var ROOMID = (\\d+);").matcher(scriptText);
            if (matcher.find()) {
                roomId = matcher.group(1);
            } else {
                throw new IllegalArgumentException("Invalid URL");
            }
            //得到 DANMU_RND
            matcher = Pattern.compile("var DANMU_RND = (\\d+);").matcher(scriptText);
            if (matcher.find()) {
                random = matcher.group(1);
            } else {
                throw new IllegalArgumentException("Invalid URL");
            }
        }
    }

    /**
     * Setup cookies.
     *
     * @param cookies all needed cookies in a string
     * @return self reference
     */
    public LiveDanMuSender setCookies(String cookies) {
        this.cookies = cookies;
        return this;
    }

    /**
     * Setup cookies.
     *
     * @param DedeUserID        cookie named 'DedeUserID'
     * @param DedeUserID__ckMd5 cookie named 'DedeUserID__ckMd5'
     * @param SESSDATA          cookie named 'SESSDATA'
     * @return self reference
     */
    public LiveDanMuSender setCookies(String DedeUserID, String DedeUserID__ckMd5, String SESSDATA) {
        this.cookies = String.format("%s; %s; %s", DedeUserID, DedeUserID__ckMd5, SESSDATA);
        return this;
    }

    /**
     * Validate cookies.
     *
     * @return is cookies valid
     * @throws IOException when network error
     */
    public boolean testLogin() throws IOException {
        try (CloseableHttpClient closeableHttpClient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet("http://api.live.bilibili.com/User/getUserInfo");
            httpGet.setHeader("Cookie", cookies);
            String code = JSON.parseObject(EntityUtils.toString(closeableHttpClient.execute(httpGet).getEntity())).getString("code");
            return code.equals("REPONSE_OK");
        }
    }

    /**
     * Send DanMu.
     *
     * @param message DanMu content
     */
    public DanMuResponseEntity send(String message) throws IOException, IllegalArgumentException {
        return send("16777215", "25", "1", message);
    }

    /**
     * Send DanMu.
     *
     * @param color    color of DanMu
     * @param fontSize font size of DanMu
     * @param mode     DanMu mode
     * @param message  DanMu content
     * @return server response entity
     */
    public DanMuResponseEntity send(String color, String fontSize, String mode, String message) throws IOException, IllegalArgumentException {
        resolveRoomIdAndRandom();
        CloseableHttpClient closeableHttpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost("http://live.bilibili.com/msg/send");
        httpPost.setHeader("Cookie", cookies);
        httpPost.setEntity(
                new UrlEncodedFormEntity(
                        Arrays.asList(
                                new BasicNameValuePair("color", color),
                                new BasicNameValuePair("fontsize", fontSize),
                                new BasicNameValuePair("mode", mode),
                                new BasicNameValuePair("msg", message),
                                new BasicNameValuePair("random", random),
                                new BasicNameValuePair("roomid", roomId)
                        ),
                        StandardCharsets.UTF_8
                )
        );
        DanMuResponseEntity danMuResponseEntity = JSON.parseObject(EntityUtils.toString(closeableHttpClient.execute(httpPost).getEntity()), DanMuResponseEntity.class);
        closeableHttpClient.close();
        return danMuResponseEntity;
    }
}
