package client.server;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FileUploader extends Thread {

    private Logger logger;
    private final static int BUFFER_SIZE = 8192;
    private SocketChannel clientSc;
    private String filePath;

    FileUploader(SocketChannel clientSc, String filePath) {
        this.logger = Logger.getLogger("clientFileUploader");
        this.clientSc = clientSc;
        this.filePath = filePath;
    }

    public void run() {

        try (var fis = new FileInputStream(filePath)) {
            ByteBuffer buffer = ByteBuffer.allocateDirect(BUFFER_SIZE);

            byte[] fileBuffer = new byte[BUFFER_SIZE];
            int bytesRead = -1;
            while ((bytesRead = fis.read(fileBuffer)) > 0) {
                byte[] readBuffer = Arrays.copyOfRange(fileBuffer, 0, bytesRead);
                clientSc.write(ByteBuffer.wrap(readBuffer));
            }
            clientSc.close(); //what if
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error in file upload", e);
        }
    }
}
