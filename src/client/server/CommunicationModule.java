package client.server;

import client.communication.CommunicatorWithServer;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class CommunicationModule extends Thread {

    private static final int BUFFER_SIZE = 4096;
    private boolean isStopped;
    private InetSocketAddress inetSocketAddress;

    public CommunicationModule(InetSocketAddress inetSocketAddress){
        this.inetSocketAddress = inetSocketAddress;
        isStopped = false;
    }

    public void run() {
        try (ServerSocketChannel serverSocketChannel = ServerSocketChannel.open()) {
            serverSocketChannel.bind(inetSocketAddress);
            serverSocketChannel.configureBlocking(false);
            Selector selector = Selector.open();
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
            System.out.print((InetSocketAddress) serverSocketChannel.getLocalAddress());
            while (!isStopped) {
                int readyChannels = selector.selectNow();
                if (readyChannels == 0) {
                    continue;
                }
                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> keyIterator = selectedKeys.iterator();

                while (keyIterator.hasNext()) {
                    SelectionKey key = keyIterator.next();
                    if (key.isReadable()) {
                        SocketChannel sc = (SocketChannel) key.channel();
                        buffer.clear();
                        int numberOfBytesRead = sc.read(buffer);
                        if (numberOfBytesRead < 0) {
                            sc.close();
                            continue;
                        }
                        buffer.flip();
                        byte[] byteArray = new byte[buffer.remaining()];
                        buffer.get(byteArray);
                        String message = new String(byteArray, "UTF-8");
                        message = message.substring(0, message.lastIndexOf(System.lineSeparator()));
                        if(message.equals("disconnect")){
                            writeToBuffer(sc, buffer, "Disconnected");
                            sc.close();
                            continue;
                        }
                        InetSocketAddress iaddr = (InetSocketAddress) sc.getRemoteAddress();
                        String response = "response";
                        writeToBuffer(sc, buffer, response);
                    } else if (key.isAcceptable()) {
                        acceptChannel(selector, key);
                    }
                    keyIterator.remove();
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeToBuffer(SocketChannel sc, ByteBuffer buffer, String response) {
        try {
            buffer.clear();
            buffer.put(response.getBytes("UTF-8"));
            buffer.flip();
            sc.write(buffer);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void acceptChannel(Selector selector, SelectionKey key) {
        ServerSocketChannel sockChannel = (ServerSocketChannel) key.channel();
        SocketChannel accept = null;
        try {
            accept = sockChannel.accept();
            accept.configureBlocking(false);
            accept.register(selector, SelectionKey.OP_READ);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

}
