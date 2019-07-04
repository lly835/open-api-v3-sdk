package com.okcoin.commons.okex.open.api.websocket;

import java.util.Map;

public interface WebSocket {

    void connect();

    void close();

    void login(String apiKey, String apiSecret, String passphrase);

    void subscribe(String... args);

    void unSubscribe(String... args);

    void sendPing();

    boolean checkSum(String data);

    /**
     * 针对zb
     * @param map
     */
    void addChannel(Map map);
}
