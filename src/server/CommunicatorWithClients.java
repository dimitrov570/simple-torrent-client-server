package server;

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
import java.util.logging.Level;
import java.util.logging.Logger;

public class CommunicatorWithClients extends Thread {

    private Logger logger;
    private static final int BUFFER_SIZE = 32768;
    private CommandExecutor cmdExecutor;
    private static String serverHost;
    private static int serverPort;
    private boolean isStopped;

    CommunicatorWithClients(CommandExecutor cmdExecutor, String serverHost, int serverPort) {
        logger = Logger.getLogger("torrentServerCommunicationWithClients");
        this.cmdExecutor = cmdExecutor;
        this.serverHost = serverHost;
        this.serverPort = serverPort;
        isStopped = false;
    }

    public void run() {
        try (ServerSocketChannel serverSocketChannel = ServerSocketChannel.open()) {
            serverSocketChannel.bind(new InetSocketAddress(serverHost, serverPort));
            serverSocketChannel.configureBlocking(false);
            Selector selector = Selector.open();
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);

            while (!isStopped) {
                int readyChannels = selector.selectNow();
                if (readyChannels == 0) {
                    continue;
                }
                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> keyIterator = selectedKeys.iterator();
                int numberOfBytesRead = -1;
                while (keyIterator.hasNext()) {
                    SelectionKey key = keyIterator.next();
                    if (key.isReadable()) {
                        SocketChannel sc = (SocketChannel) key.channel();
                        buffer.clear();
                        if (sc.isOpen()) {
                            numberOfBytesRead = sc.read(buffer);
                        }
                        if (numberOfBytesRead < 0) {
                            sc.close();
                            continue;
                        }
                        buffer.flip();
                        byte[] byteArray = new byte[buffer.remaining()];
                        buffer.get(byteArray);
                        String message = new String(byteArray, "UTF-8");
                        message = message.substring(0, message.indexOf(System.lineSeparator()));
                        InetSocketAddress iaddr = (InetSocketAddress) sc.getRemoteAddress();
                        String response = cmdExecutor.execute(message, iaddr);
                        writeToBuffer(sc, buffer, response);
                        if (response.equals("Disconnected successfully" + System.lineSeparator())) {
                            sc.close();
                            continue;
                        }
                    } else if (key.isAcceptable()) {
                        acceptChannel(selector, key);
                    }
                    keyIterator.remove();
                }
            }
        } catch (SocketException e) {
            logger.log(Level.SEVERE, "Socket error", e);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "IO error", e);
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


    public void terminate() {
        isStopped = true;
    }
}