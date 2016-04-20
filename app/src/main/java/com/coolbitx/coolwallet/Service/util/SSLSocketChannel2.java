package com.coolbitx.coolwallet.Service.util;

//import org.java_websocket.WrappedByteChannel;

import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLSession;

/**
 * Created by ShihYi on 2016/4/13.
 */
public class SSLSocketChannel2 implements ByteChannel, WrappedByteChannel {
    protected static ByteBuffer emptybuffer = ByteBuffer.allocate(0);

    protected ExecutorService exec;

    protected List<Future<?>> tasks;

    /** raw payload incomming */
    protected ByteBuffer inData;
    /** encrypted data outgoing */
    protected ByteBuffer outCrypt;
    /** encrypted data incoming */
    protected ByteBuffer inCrypt;

    /** the underlying channel */
    protected SocketChannel socketChannel;
    /** used to set interestOP SelectionKey.OP_WRITE for the underlying channel */
    protected SelectionKey selectionKey;

    protected SSLEngineResult engineResult;
    protected SSLEngine sslEngine;


    private SSLEngineResult.Status lastReadEngineStatus = SSLEngineResult.Status.BUFFER_UNDERFLOW;

    public SSLSocketChannel2(SocketChannel channel , SSLEngine sslEngine , ExecutorService exec , SelectionKey key) throws IOException {
        if(channel == null || sslEngine == null || exec == null)
            throw new IllegalArgumentException("parameter must not be null");

        this.socketChannel = channel;
        this.sslEngine = sslEngine;
        this.exec = exec;

        tasks = new ArrayList<Future<?>>(3);
        if(key != null) {
            key.interestOps(key.interestOps() | SelectionKey.OP_WRITE);
            this.selectionKey = key;
        }
        createBuffers(sslEngine.getSession());
        // kick off handshake
        handshake();
    }

    private void handshake() throws IOException {
        sslEngine.beginHandshake();
        handleHandshakeStatus(sslEngine.getHandshakeStatus());
    }

    private void handleHandshakeStatus(SSLEngineResult.HandshakeStatus status) throws IOException {

        if(status == SSLEngineResult.HandshakeStatus.NEED_TASK) {
            consumeDelegatedTasks();
        }

        //Cleanup complete delegated tasks and run incomplete tasks ourselves if we're in blocking mode.
        if(!tasks.isEmpty()) {
            Iterator<Future<?>> it = tasks.iterator();
            while (it.hasNext()) {
                Future<?> f = it.next();
                if(f.isDone()) {
                    it.remove();
                } else {
                    if(isBlocking())
                        consumeFutureUninterruptible(f);
                    return;
                }
            }
        }

        if(status == SSLEngineResult.HandshakeStatus.NEED_WRAP) {
            write(emptybuffer);
        }

        //Ditto for unwrap. Trigger a read.
        if(status == SSLEngineResult.HandshakeStatus.NEED_UNWRAP) {
            read(null);
        }

        if(status == SSLEngineResult.HandshakeStatus.FINISHED) {
            //We've just completed a handshake, reinstantiate the buffers with new negotiated sizes.
            createBuffers(sslEngine.getSession());
        }
    }

    private void consumeFutureUninterruptible(Future<?> f) {
        try {
            boolean interrupted = false;
            while (true) {
                try {
                    f.get();
                    break;
                } catch (InterruptedException e) {
                    interrupted = true;
                }
            }
            if(interrupted)
                Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    protected void consumeDelegatedTasks() {
        Runnable task;
        while ( ( task = sslEngine.getDelegatedTask() ) != null ) {
            tasks.add( exec.submit( task ) );
        }
    }

    protected void createBuffers(SSLSession session) {
        int appBufferMax = session.getApplicationBufferSize();
        int netBufferMax = session.getPacketBufferSize();

        if(inData == null) {
            inData = ByteBuffer.allocate(appBufferMax);
            outCrypt = ByteBuffer.allocate(netBufferMax);
            inCrypt = ByteBuffer.allocate(netBufferMax);
        } else {
            if(inData.capacity() != appBufferMax)
                inData = ByteBuffer.allocate(appBufferMax);
            if(outCrypt.capacity() != netBufferMax)
                outCrypt = ByteBuffer.allocate(netBufferMax);
            if(inCrypt.capacity() != netBufferMax)
                inCrypt = ByteBuffer.allocate(netBufferMax);
        }
        inData.rewind();
        inData.flip();
        inCrypt.rewind();
        inCrypt.flip();
        outCrypt.rewind();
        outCrypt.flip();
    }

    public int write(ByteBuffer src) throws IOException {

        int written = 0;

        if(src != null) {
            outCrypt.compact();
            engineResult = sslEngine.wrap(src, outCrypt);
            outCrypt.flip();
        }

        if(outCrypt.hasRemaining())
            written = socketChannel.write(outCrypt);

        if(src != null) {
            if (engineResult.getStatus() == SSLEngineResult.Status.BUFFER_OVERFLOW) {
                //Buffer isn't big enough. Resize to required.
                ByteBuffer b = ByteBuffer.allocate(sslEngine.getSession().getPacketBufferSize() + outCrypt.position());
                outCrypt.compact();
                outCrypt.flip();
                b.put(outCrypt);
                outCrypt = b;
                outCrypt.flip();
                //Write out remaining.
                written += write(src);
            } else {
                handleHandshakeStatus(engineResult.getHandshakeStatus());
            }
        }

        return written;
    }

    public int read(ByteBuffer dst) throws IOException {

        if (dst != null && !dst.hasRemaining())
            return 0;

        int read = 0;

        if (dst != null) {
            if (inData.hasRemaining()) {
                read += transferTo(inData, dst);
            }

            if (!dst.hasRemaining()) {
                return read;
            }

            inData.clear(); //inData has been completely read out, clear it to load it with more data.
        }

        if (!inCrypt.hasRemaining())
            inCrypt.clear();
        else
            inCrypt.compact();

        if ((isBlocking() && inCrypt.position() == 0) || lastReadEngineStatus == SSLEngineResult.Status.BUFFER_UNDERFLOW) {
            int cryptRead = socketChannel.read(inCrypt);
            if (cryptRead == -1) {
                return -1;
            }
        }
        inCrypt.flip();

        unwrap();

        if (dst != null) {
            read += transferTo(inData, dst);

            handleHandshakeStatus(engineResult.getHandshakeStatus());

            if (read == 0 && isBlocking()) {
                return read(dst); //We might be redoing the handshake. Try to read again.
            }
        } else {
            handleHandshakeStatus(engineResult.getHandshakeStatus());
        }

        return read;
    }

    private synchronized void unwrap() throws IOException {

        engineResult = sslEngine.unwrap(inCrypt, inData);
        lastReadEngineStatus = engineResult.getStatus();
        inData.flip();

        if(lastReadEngineStatus == SSLEngineResult.Status.BUFFER_OVERFLOW) {
            //Resize the inData buffer.
            ByteBuffer b = ByteBuffer.allocate(sslEngine.getSession().getApplicationBufferSize() + inData.position());
            b.put(inData);
            inData = b;
            unwrap();
        }
    }

    public void close() throws IOException {
        sslEngine.closeOutbound();
        sslEngine.getSession().invalidate();
        if(socketChannel.isOpen())
            write(emptybuffer);
        socketChannel.close();
    }

    public Socket socket() {
        return socketChannel.socket();
    }

    @Override
    public boolean isOpen() {
        return socketChannel.isOpen();
    }

    @Override
    public boolean isNeedWrite() {
        return outCrypt.hasRemaining();
    }

    @Override
    public void writeMore() throws IOException {
        write(null);
    }

    @Override
    public boolean isNeedRead() {
        return inData.hasRemaining() || (inCrypt.hasRemaining() && lastReadEngineStatus != SSLEngineResult.Status.BUFFER_UNDERFLOW);
    }

    @Override
    public int readMore(ByteBuffer dst) throws IOException {
        return read(dst);
    }

    private int transferTo(ByteBuffer from, ByteBuffer to) {
        int fremain = from.remaining();
        int toremain = to.remaining();
        if(fremain > toremain ) {
            // FIXME there should be a more efficient transfer method
            int limit = Math.min( fremain, toremain );
            for( int i = 0 ; i < limit ; i++ ) {
                to.put( from.get() );
            }
            return limit;
        } else {
            to.put( from );
            return fremain;
        }

    }

    @Override
    public boolean isBlocking() {
        return socketChannel.isBlocking();
    }
}
