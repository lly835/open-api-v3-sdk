package com.okcoin.commons.okex.open.api.websocket;

import io.netty.channel.*;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.util.CharsetUtil;
import org.apache.commons.compress.compressors.deflate64.Deflate64CompressorInputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class WebSocketClientHandler extends SimpleChannelInboundHandler<Object> {
    private ChannelPromise handshakeFuture;
    private final WebSocketClientHandshaker handshaker;
    private WebSocketClient webSocketClient;
    private WebSocketListener listener;

    public WebSocketClientHandler(WebSocketClient webSocketClient, WebSocketClientHandshaker handshaker, WebSocketListener listener) {
        this.handshaker = handshaker;
        this.webSocketClient = webSocketClient;
        this.listener = listener;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        Channel ch = ctx.channel();
        if (!handshaker.isHandshakeComplete()) {
            handshaker.finishHandshake(ch, (FullHttpResponse) msg);
            handshakeFuture.setSuccess();
            this.listener.onWebsocketOpen(this.webSocketClient);
            this.webSocketClient.beginTimer();
            return;
        }

        if (msg instanceof FullHttpResponse) {
            FullHttpResponse response = (FullHttpResponse) msg;
            throw new IllegalStateException(
                    "Unexpected FullHttpResponse (getStatus=" + response.status() +
                            ", content=" + response.content().toString(CharsetUtil.UTF_8) + ')');
        }

        WebSocketFrame frame = (WebSocketFrame) msg;
        if (frame instanceof PongWebSocketFrame){
            this.listener.onWebsocketPong(webSocketClient);
        }
        else if (frame instanceof CloseWebSocketFrame) {
            CloseWebSocketFrame closeFrame = (CloseWebSocketFrame)frame;
            int closeCode = closeFrame.statusCode();
            this.listener.onWebsocketClose(webSocketClient, closeCode);
        }
        else if (frame instanceof TextWebSocketFrame) {
            TextWebSocketFrame textFrame = (TextWebSocketFrame) frame;
            this.listener.onTextMessage(webSocketClient, textFrame.text());
        }
        else if (frame instanceof BinaryWebSocketFrame) {
            //这里处理收到的逻辑
            String msgStr = decode(msg);
            if (msgStr.equals("pong")) {
                this.listener.onWebsocketPong(webSocketClient);
            } else {
                this.listener.onTextMessage(webSocketClient, msgStr);
            }
        }
        else {
            this.listener.handleCallbackError(webSocketClient, new RuntimeException("cannot decode message"));
        }
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        handshakeFuture = ctx.newPromise();
    }

    public ChannelFuture handshakeFuture() {
        return handshakeFuture;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        handshaker.handshake(ctx.channel());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
    }

    private static String uncompress(byte[] bytes) {
        try (final ByteArrayOutputStream out = new ByteArrayOutputStream();
             final ByteArrayInputStream in = new ByteArrayInputStream(bytes);
             final Deflate64CompressorInputStream zin = new Deflate64CompressorInputStream(in)) {
            final byte[] buffer = new byte[1024];
            int offset;
            while (-1 != (offset = zin.read(buffer))) {
                out.write(buffer, 0, offset);
            }
            return out.toString();
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String decode(Object msg){
        BinaryWebSocketFrame frameBinary = (BinaryWebSocketFrame)msg;
        byte[] bytes = new byte[frameBinary.content().readableBytes()];
        frameBinary.content().readBytes(bytes);
        String str = uncompress(bytes);
        return str;
    }
}
