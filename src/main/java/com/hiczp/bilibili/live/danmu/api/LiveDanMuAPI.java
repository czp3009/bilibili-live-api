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
    private Thread callbackDispatchThread;

    public LiveDanMuAPI(int roomId) {
        this.roomId = roomId;
    }

    public LiveDanMuAPI(String url) throws IOException, IllegalArgumentException {
        this(new URL(url));
    }

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

    public LiveDanMuAPI addCallback(ILiveDanMuCallback liveDanMuCallback) {
        callbacks.add(liveDanMuCallback);
        return this;
    }

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
        callbackDispatchThread = new Thread(new CallbackDispatchRunnable(this, inputStream, callbacks, printDebugInfo));
        callbackDispatchThread.start();

        return this;
    }

    public LiveDanMuAPI setPrintDebugInfo(Boolean printDebugInfo) {
        this.printDebugInfo = printDebugInfo;
        return this;
    }

    @Override
    public void close() throws IOException {
        if (socket != null) {
            socket.close();
            socket = null;
        }
        if (heartBeatThread != null && heartBeatThread.isAlive() && !heartBeatThread.isInterrupted()) {
            heartBeatThread.interrupt();
            heartBeatThread = null;
        }
        if (callbackDispatchThread != null && callbackDispatchThread.isAlive() && !callbackDispatchThread.isInterrupted()) {
            callbackDispatchThread.interrupt();
            callbackDispatchThread = null;
        }
    }
}
