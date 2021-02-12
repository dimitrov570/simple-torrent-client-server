package client.communication;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MapUpdater extends Thread {
    private static final int SLEEP_TIME = 100;
    private Logger logger;
    private static final int BUFFER_SIZE = 32768;
    private ByteBuffer buffer;
    private ConcurrentMap<String, InetSocketAddress> usersIpPortsMap;
    private String backupFileName;
    private String serverHost;
    private int serverPort;
    boolean isStopped;

    public MapUpdater(ConcurrentMap<String, InetSocketAddress> usersIpPortsMap, String serverHost, int serverPort) {
        super("map-updater");
        this.logger = Logger.getLogger("mapUpdater");
        this.usersIpPortsMap = usersIpPortsMap;
        this.serverHost = serverHost;
        this.serverPort = serverPort;
        buffer = ByteBuffer.allocateDirect(BUFFER_SIZE);
        isStopped = false;
    }

    public void run() {
        try (SocketChannel socketChannel = SocketChannel.open();
             Scanner scanner = new Scanner(System.in)) {
            socketChannel.connect(new InetSocketAddress(serverHost, serverPort));
            var serverInetSocketAddress = (InetSocketAddress) socketChannel.socket().getLocalSocketAddress();
            backupFileName = serverInetSocketAddress.toString().replace(":", "-");
            backupFileName = backupFileName.substring(1) + ".txt";
            //System.out.println("Map updater connected --- " + backupFileName);
            socketChannel.configureBlocking(false);

            String message = "list-ports" + System.lineSeparator();

            while (!isStopped) {
                writeToBuffer(socketChannel, buffer, message);

                buffer.clear(); // switch to writing mode
                socketChannel.read(buffer); // buffer fill

                buffer.flip(); // switch to reading mode
                byte[] byteArray = new byte[buffer.remaining()];
                buffer.get(byteArray);
                String reply = new String(byteArray, "UTF-8"); // buffer drain
                updateMap(reply);
                Thread.sleep(SLEEP_TIME);
            }
            writeToBuffer(socketChannel, buffer, "disconnect" + System.lineSeparator());
            buffer.clear(); // switch to writing mode
            socketChannel.read(buffer); // buffer fill
            Files.deleteIfExists(Path.of(backupFileName));
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error while sleep()", e);
        } catch (InterruptedException e) {
            logger.log(Level.SEVERE, "Error with network communication", e);
        }
    }

    public void terminate() {
        isStopped = true;
    }

    private void writeToBuffer(SocketChannel sc, ByteBuffer buffer, String message) {
        try {
            if (!sc.isConnected()) {
                return;
            }
            buffer.clear();
            buffer.put(message.getBytes("UTF-8"));
            buffer.flip();
            sc.write(buffer);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void updateMap(String update) throws IOException {
        if (update == null || update.isEmpty()) {
            return;
        }
        Path path = Path.of(backupFileName);
        Files.write(path, update.getBytes());
        String[] lines = update.split(System.lineSeparator());
        String[] lineParts;
        usersIpPortsMap.clear();
        for (String line : lines) {
            lineParts = line.split(" - ");
            if (lineParts.length == 2) {
                InetSocketAddress addr = createInetSocketAddress(lineParts[1]);
                if (addr != null) {
                    usersIpPortsMap.put(lineParts[0], addr);
                }
            }
        }
    }

    private InetSocketAddress createInetSocketAddress(String str) {
        String[] parts = str.split(":");
        if (parts.length != 2) {
            return null;
        }
        return new InetSocketAddress(parts[0], Integer.valueOf(parts[1]));

    }
}
