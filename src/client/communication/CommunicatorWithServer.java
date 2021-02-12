package client.communication;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Queue;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CommunicatorWithServer extends Thread {
    private static final int SLEEP_TIME = 100;
    private static final int BUFFER_SIZE = 512;
    private ByteBuffer buffer;
    private Logger logger;
    private Queue<String> serverQueries;
    private int serverPort;
    private String serverHost;
    private InetSocketAddress serverInetSocketAddress;
    private boolean isStopped;
    private boolean userDisconnected;

    public CommunicatorWithServer(Queue<String> serverQueries, String serverHost, int serverPort) {
        super("communicator-with-server");
        logger = Logger.getLogger("communicationWithServer");
        this.serverQueries = serverQueries;
        this.serverHost = serverHost;
        this.serverPort = serverPort;
        buffer = ByteBuffer.allocateDirect(BUFFER_SIZE);
        isStopped = false;
        userDisconnected = false;
    }

    public InetSocketAddress getServerInetSocketAddress() {
        return serverInetSocketAddress;
    }

    public void run() {
        try (SocketChannel socketChannel = SocketChannel.open();
             Scanner scanner = new Scanner(System.in)) {
            socketChannel.connect(new InetSocketAddress(serverHost, serverPort));
            serverInetSocketAddress = (InetSocketAddress) socketChannel.socket().getLocalSocketAddress();

            while (!isStopped && !userDisconnected) {
                String message = serverQueries.poll();
                if (message == null) {
                    Thread.sleep(SLEEP_TIME);
                    continue;
                }
                message = message.trim() + System.lineSeparator();

                if (message.equals("disconnect" + System.lineSeparator())) {
                    userDisconnected = true;
                }

                writeToBuffer(socketChannel, buffer, message);

                buffer.clear(); // switch to writing mode

                socketChannel.read(buffer); // buffer fill

                buffer.flip(); // switch to reading mode
                byte[] byteArray = new byte[buffer.remaining()];
                buffer.get(byteArray);
                String reply = new String(byteArray, "UTF-8"); // buffer drain

                if (!userDisconnected) {
                    System.out.print(reply);
                }
            }
            if (isStopped) {
                writeToBuffer(socketChannel, buffer, "disconnect" +
                        System.lineSeparator()); //prevent server from crashing
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error while sleep()", e);
        } catch (InterruptedException e) {
            System.err.println("There is a problem with the network communication");
            logger.log(Level.SEVERE, "Error with network communication", e);
        }
    }

    private void writeToBuffer(SocketChannel sc, ByteBuffer buffer, String message) {
        try {
            buffer.clear();
            buffer.put(message.getBytes("UTF-8"));
            buffer.flip();
            sc.write(buffer);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void terminate() {
        isStopped = true;
    }

}
