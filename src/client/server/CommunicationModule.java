package client.server;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CommunicationModule extends Thread {

    private Logger logger;
    private static final int MAX_EXECUTOR_THREADS = 10;
    private static int BUFFER_SIZE = 512;
    private Map<String, String> filenameAndPathMap;
    private InetSocketAddress inetSocketAddress;
    private boolean isStopped;
    private ExecutorService executor;

    public CommunicationModule(InetSocketAddress inetSocketAddress, Map<String, String> filenameAndPathMap) {
        super("communication-module");
        this.logger = Logger.getLogger("clientMiniServerCommunicationModule");
        this.inetSocketAddress = inetSocketAddress;
        this.filenameAndPathMap = filenameAndPathMap;
        isStopped = false;
    }

    public void run() {
        executor = Executors.newFixedThreadPool(MAX_EXECUTOR_THREADS);

        try (ServerSocketChannel serverSocketChannel = ServerSocketChannel.open()) {
            serverSocketChannel.bind(inetSocketAddress);
            serverSocketChannel.configureBlocking(false);
            Selector selector = Selector.open();
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
            while (!isStopped) {
                int readyChannels = selector.select(3000);
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

                        String response = getFilePath(message);
                        if (response.equals("Unkown command") || response.equals("File not available")) {
                            writeToBuffer(sc, buffer, response);
                            sc.close();
                        } else {
                            writeToBuffer(sc, buffer, "Started upload");
                            FileUploader uploader = new FileUploader(sc, response);
                            executor.execute(uploader);
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
        } finally {
            executor.shutdownNow();
        }
    }

    private String getFilePath(String message) {
        String[] messageParts = message.trim().split("\s+");
        if (messageParts.length != 2 || !messageParts[0].equals("download")) {
            return "Unknown command";
        }
        String fileName = messageParts[1];
        String filePath = filenameAndPathMap.get(messageParts[1]);
        if (filePath == null || !Files.exists(Path.of(filePath))) {
            return "File not available";
        }
        return filePath;
    }


    public void terminate() {
        isStopped = true;
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
