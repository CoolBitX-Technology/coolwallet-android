package com.coolbitx.coolwallet.Service;

import org.java_websocket.WebSocketAdapter;
import org.java_websocket.WebSocketImpl;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;

import java.io.IOException;
import java.net.Socket;
import java.nio.channels.ByteChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;

/**
 * Created by ShihYi on 2016/4/13.
 */
public class DefaultSSLWebSocketClientFactory implements WebSocketClient.WebSocketClientFactory {
    protected SSLContext sslcontext;
    protected ExecutorService exec;

    public DefaultSSLWebSocketClientFactory(SSLContext sslContext ) {
        this( sslContext, Executors.newSingleThreadScheduledExecutor() );
    }

    public DefaultSSLWebSocketClientFactory(SSLContext sslContext , ExecutorService exec ) {
        if( sslContext == null || exec == null )
            throw new IllegalArgumentException();
        this.sslcontext = sslContext;
        this.exec = exec;
    }

    @Override
    public ByteChannel wrapChannel( SocketChannel channel, SelectionKey key, String host, int port ) throws IOException {
        SSLEngine e = sslcontext.createSSLEngine( host, port );
        e.setUseClientMode( true );
        return new com.coolbitx.coolwallet.Service.util.SSLSocketChannel2( channel, e, exec, key );
    }

    @Override
    public WebSocketImpl createWebSocket( WebSocketAdapter a, Draft d, Socket c ) {
        return new WebSocketImpl( a, d, c );
    }

    @Override
    public WebSocketImpl createWebSocket( WebSocketAdapter a, List<Draft> d, Socket s ) {
        return new WebSocketImpl( a, d, s );
    }
}