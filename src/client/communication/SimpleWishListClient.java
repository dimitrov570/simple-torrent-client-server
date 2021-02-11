package client.communication;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Scanner;


public class SimpleWishListClient {

    private static final int BUFFER_SIZE = 512;
    private static final int SERVER_PORT = 7777;
    private static final String SERVER_HOST = "localhost";
    private static ByteBuffer buffer = ByteBuffer.allocateDirect(BUFFER_SIZE);


    public static void main(String[] args) {

        try (SocketChannel socketChannel = SocketChannel.open();
             Scanner scanner = new Scanner(System.in)) {
            socketChannel.connect(new InetSocketAddress(SERVER_HOST, SERVER_PORT));

            System.out.println("Connected to the server.");

            while (true) {
                System.out.print("Enter message: ");
                String message = scanner.nextLine() + System.lineSeparator(); // read a line from the console

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
        } catch (IOException e) {
            System.err.println("There is a problem with the network communication");
            e.printStackTrace();
        }
    }
}