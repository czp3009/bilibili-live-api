package com.hiczp.bilibili.live.danmu.api;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.hiczp.bilibili.live.danmu.api.entity.DanMuResponseEntity;
import com.hiczp.bilibili.live.danmu.api.entity.UserInfoEntity;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import sun.security.rsa.RSAPublicKeyImpl;

import javax.crypto.Cipher;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.interfaces.RSAPublicKey;
import java.util.Arrays;
import java.util.Base64;

/**
 * DanMu send API.
 * Created by czp on 17-6-6.
 */
public class LiveDanMuSender {
    private String urlString;
    private URL url;
    private Integer roomId;
    private Long random;
    private Integer roomURL;
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

    /**
     * Cipher password of Bilibili account, use for username and password login which feature is not finished yet.
     *
     * @param password password of Bilibili account
     * @return ciphered password
     */
    public static String cipherPassword(String password) throws Exception {
        String hash;
        String key;
        //获取 hash 和 key
        try (CloseableHttpClient closeableHttpClient = HttpClients.createMinimal()) {
            JSONObject jsonObject = JSON.parseObject(
                    EntityUtils.toString(
                            closeableHttpClient.execute(
                                    new HttpGet("http://passport.bilibili.com/login?act=getkey")
                            ).getEntity()
                    )
            );
            hash = jsonObject.getString("hash");
            key = jsonObject.getString("key");
        }

        //计算密码密文
        RSAPublicKey rsaPublicKey = new RSAPublicKeyImpl(
                Base64.getDecoder().decode(
                        key.replace("-----BEGIN PUBLIC KEY-----", "")
                                .replace("-----END PUBLIC KEY-----", "")
                                .replace("\n", "")
                                .getBytes()
                )
        );
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, rsaPublicKey);
        return new String(
                Base64.getEncoder().encode(
                        cipher.doFinal((hash + password).getBytes())
                )
        );
    }

    /**
     * Validate cookies.
     *
     * @param cookies cookies of user
     * @return is cookies valid
     * @throws IOException when network error
     */
    public static boolean testLogin(String cookies) throws IOException {
        return getUserInfo(cookies).code.equals("REPONSE_OK");
    }

    /**
     * Get user info.
     *
     * @param cookies cookies of user
     * @return UserInfoEntity
     * @throws IOException when network error
     * @see UserInfoEntity
     */
    public static UserInfoEntity getUserInfo(String cookies) throws IOException {
        try (CloseableHttpClient closeableHttpClient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet("http://api.live.bilibili.com/User/getUserInfo");
            httpGet.setHeader("Cookie", cookies);
            return JSON.parseObject(EntityUtils.toString(closeableHttpClient.execute(httpGet).getEntity()), UserInfoEntity.class);
        }
    }

    /**
     * Validate cookies.
     *
     * @return is cookies valid
     * @throws IOException when network error
     */
    public boolean testLogin() throws IOException {
        return testLogin(cookies);
    }

    /**
     * Get user info.
     *
     * @return UserInfoEntity
     * @throws IOException when network error
     * @see UserInfoEntity
     */
    public UserInfoEntity getUserInfo() throws IOException {
        return getUserInfo(cookies);
    }

    private void resolveRoomData() throws IOException, IllegalArgumentException {
        if (url == null) {
            url = new URL(urlString);
        }
        if (roomId == null || random == null || roomURL == null) {
            ScriptEntity scriptEntity = Utils.resolveScriptPartInHTML(url);
            roomId = scriptEntity.roomId;
            random = scriptEntity.random;
            roomURL = scriptEntity.roomURL;
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
     * Are cookies set.
     */
    public boolean isCookiesSet() {
        return cookies != null;
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
        resolveRoomData();
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
                                new BasicNameValuePair("random", random.toString()),
                                new BasicNameValuePair("roomid", roomId.toString())
                        ),
                        StandardCharsets.UTF_8
                )
        );
        DanMuResponseEntity danMuResponseEntity = JSON.parseObject(EntityUtils.toString(closeableHttpClient.execute(httpPost).getEntity()), DanMuResponseEntity.class);
        closeableHttpClient.close();
        return danMuResponseEntity;
    }

    /**
     * Get URL of room.
     */
    public URL getUrl() {
        return url;
    }

    /**
     * Get ROOMID
     */
    public Integer getRoomId() {
        return roomId;
    }

    /**
     * Get DANMU_RND.
     */
    public Long getRandom() {
        return random;
    }

    /**
     * Get ROOMURL.
     */
    public Integer getRoomURL() {
        return roomURL;
    }
}
