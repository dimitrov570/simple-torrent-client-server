package client.communication;

import java.nio.ByteBuffer;

public class CommunicatorWithServer {
    private static final int BUFFER_SIZE = 512;
    private static final int SERVER_PORT = 6666;
    private static final String SERVER_HOST = "localhost";
    private static ByteBuffer buffer = ByteBuffer.allocateDirect(BUFFER_SIZE);
}
