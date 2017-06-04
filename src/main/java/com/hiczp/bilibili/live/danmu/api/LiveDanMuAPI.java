package com.hiczp.bilibili.live.danmu.api;

import org.jsoup.Jsoup;
import org.jsoup.parser.Parser;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * DanMu receiver API.
 * <p>Here is a sample of usage:
 * <pre>
 * new LiveDanMuAPI("http://live.bilibili.com/545342")
 *      .setPrintDebugInfo(true)
 *      .addCallback(new LiveDanMuCallback())
 *      .connect();
 * </pre>
 * Created by czp on 17-5-24.
 */
public class LiveDanMuAPI implements Closeable {
    private static final String CID_INFO_URL = "http://live.bilibili.com/api/player?id=cid:";
    private static final int LIVE_SERVER_PORT = 788;

    private int roomId;
    private Socket socket;
    private List<ILiveDanMuCallback> callbacks = new ArrayList<>();
    private Boolean printDebugInfo = false;
    private Thread heartBeatThread;

    /**
     * Class constructor, need room id.
     *
     * @param roomId the id of room
     */
    public LiveDanMuAPI(int roomId) {
        this.roomId = roomId;
    }

    /**
     * Class constructor, need URL of room in String.
     *
     * @param url the URL of room in String
     */
    public LiveDanMuAPI(String url) throws IOException, IllegalArgumentException {
        this(new URL(url));
    }

    /**
     * Class constructor, need URL of room.
     *
     * @param url the URL of room
     */
    public LiveDanMuAPI(URL url) throws IOException, IllegalArgumentException {
        //在HTML中获取房间号
        String scriptText = Jsoup.parse(url, 10000).head().select("script").last().data();
        Matcher matcher = Pattern.compile("var ROOMID = (\\d+);").matcher(scriptText);
        if (matcher.find()) {
            roomId = Integer.valueOf(matcher.group(1));
        } else {
            throw new IllegalArgumentException("Invalid URL");
        }
    }

    /**
     * Add callback class, it will be called on data incoming or lost connection.
     *
     * @param liveDanMuCallback the class which implements from ILiveDanMuCallback
     * @return self reference
     */
    public LiveDanMuAPI addCallback(ILiveDanMuCallback liveDanMuCallback) {
        callbacks.add(liveDanMuCallback);
        return this;
    }

    /**
     * Connect to live server.
     *
     * @return self reference
     * @throws IOException              when socket error
     * @throws IllegalArgumentException when room id invalid
     */
    public LiveDanMuAPI connect() throws IOException, IllegalArgumentException {
        //获得服务器地址
        String serverAddress;
        try (InputStream inputStream = new URL(CID_INFO_URL + roomId).openStream()) {
            serverAddress = Jsoup.parse(inputStream,
                    StandardCharsets.UTF_8.toString(),
                    "",
                    Parser.xmlParser())
                    .select("server").first()
                    .text();
        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException("Invalid RoomID");
        } catch (NullPointerException e) {
            throw new SocketException("Network error");
        }

        socket = new Socket(serverAddress, LIVE_SERVER_PORT);
        OutputStream outputStream = socket.getOutputStream();

        //发送进房数据包
        outputStream.write(PackageRepository.getJoinPackage(roomId));
        outputStream.flush();

        InputStream inputStream = socket.getInputStream();

        if (!PackageRepository.readAndValidateJoinSuccessPackage(inputStream)) {
            socket.close();
            throw new SocketException("Join live channel failed");
        }

        //定时发送心跳包
        heartBeatThread = new Thread(new HeartBeatRunnable(outputStream));
        heartBeatThread.start();
        //启动回调分发线程
        new Thread(new CallbackDispatchRunnable(this, inputStream, callbacks, printDebugInfo)).start();

        //回调
        callbacks.forEach(ILiveDanMuCallback::onConnect);

        return this;
    }

    /**
     * Set print debug info, default is false.
     *
     * @param printDebugInfo true for print, false for not
     * @return self reference
     */
    public LiveDanMuAPI setPrintDebugInfo(Boolean printDebugInfo) {
        this.printDebugInfo = printDebugInfo;
        return this;
    }

    /**
     * Close the connect and interrupt thread.
     *
     * @throws IOException inherit from Closeable
     * @see Closeable
     */
    @Override
    public void close() throws IOException {
        if (socket != null) {
            socket.close();
        }
        if (heartBeatThread != null) {
            heartBeatThread.interrupt();
        }
    }
}
