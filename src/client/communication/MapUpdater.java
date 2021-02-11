package client.communication;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileStore;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;
import java.util.Scanner;

import static java.nio.file.StandardOpenOption.CREATE;

public class MapUpdater extends Thread {

    private static final int BUFFER_SIZE = 32768;
    private ByteBuffer buffer;
    private Map<String, String> usersIpPortsMap;
    private String backupFileName;
    private String serverHost;
    private int serverPort;

    public MapUpdater(Map<String, String> usersIpPortsMap, String serverHost, int serverPort) {
        super("map-updater");
        this.usersIpPortsMap = usersIpPortsMap;
        this.serverHost = serverHost;
        this.serverPort = serverPort;
        buffer = ByteBuffer.allocateDirect(BUFFER_SIZE);
    }

    public void run() {
        try (SocketChannel socketChannel = SocketChannel.open();
             Scanner scanner = new Scanner(System.in)) {
            socketChannel.connect(new InetSocketAddress(serverHost, serverPort));
            var serverInetSocketAddress = (InetSocketAddress) socketChannel.socket().getLocalSocketAddress();
            backupFileName = serverInetSocketAddress.toString().replace(":", "-");
            backupFileName = backupFileName.substring(1) + ".txt";
            System.out.println("Map updater connected --- " + backupFileName);
            socketChannel.configureBlocking(false);

            String message = "list-ports" + System.lineSeparator();

            while (true) {
                buffer.clear(); // switch to writing mode
                buffer.put(message.getBytes()); // buffer fill

                buffer.flip(); // switch to reading mode
                socketChannel.write(buffer); // buffer drain

                buffer.clear(); // switch to writing mode
                socketChannel.read(buffer); // buffer fill

                buffer.flip(); // switch to reading mode
                byte[] byteArray = new byte[buffer.remaining()];
                buffer.get(byteArray);
                String reply = new String(byteArray, "UTF-8"); // buffer drain
                updateMap(reply);
                Thread.sleep(30000);
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("There is a problem with the network communication");
            e.printStackTrace();
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
        for (String line : lines) {
            lineParts = line.split(" - ");
            usersIpPortsMap.put(lineParts[0], lineParts[1]);
        }
    }

}
