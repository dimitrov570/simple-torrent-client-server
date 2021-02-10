package client.communication;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Queue;
import java.util.Scanner;

public class CommunicatorWithServer extends Thread {
    private static final int BUFFER_SIZE = 512;
    private Queue<String> serverQueries;
    private int serverPort;
    private String serverHost;
    private ByteBuffer buffer;
    private InetSocketAddress serverInetSocketAddress;

    public CommunicatorWithServer(Queue<String> serverQueries, String serverHost, int serverPort) {
        this.serverQueries = serverQueries;
        this.serverHost = serverHost;
        this.serverPort = serverPort;
        buffer = ByteBuffer.allocateDirect(BUFFER_SIZE);
    }

    public InetSocketAddress getServerInetSocketAddress(){
        return serverInetSocketAddress;
    }

    public void run() {
        try (SocketChannel socketChannel = SocketChannel.open();
             Scanner scanner = new Scanner(System.in)) {
            socketChannel.connect(new InetSocketAddress(serverHost, serverPort));
            serverInetSocketAddress = (InetSocketAddress) socketChannel.socket().getLocalSocketAddress();
            System.out.println("Connected to the server.");

            while (true) {
                String message = serverQueries.poll();
                if(message == null){
                    Thread.sleep(100);
                    continue;
                }
                message = message.trim();

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

                System.out.print(reply);

                if (message.equals("disconnect" + System.lineSeparator())) {
                    break;
                }
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("There is a problem with the network communication");
            e.printStackTrace();
        }
    }

}
