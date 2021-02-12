package client.downloader;

import java.io.BufferedOutputStream;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FileDownloader {

    private Logger logger;
    private static int COMMAND_PARTS_LENGTH = 4;
    private static int OUTPUT_FILE_INDEX = 3;
    private static int INPUT_FILE_INDEX = 2;
    private static int USERNAME_INDEX = 1;
    private static int BUFFER_SIZE = 8192;
    private ConcurrentMap<String, InetSocketAddress> userIpPortMap;
    private byte[] buffer;

    public FileDownloader(ConcurrentMap<String, InetSocketAddress> userIpPortMap) {
        this.logger = Logger.getLogger("clientFileDownloader");
        this.buffer = new byte[BUFFER_SIZE];
        this.userIpPortMap = userIpPortMap;
    }

    public String download(String command) {
        String[] commandParts = command.trim().split("\s+");
        if (commandParts.length != COMMAND_PARTS_LENGTH) {
            return "Unknown command"; //should throw exceptions here
        }
        String outputFile = commandParts[OUTPUT_FILE_INDEX];
        String username = commandParts[USERNAME_INDEX];
        String inputFile = commandParts[INPUT_FILE_INDEX];
        InetSocketAddress serverAddr = userIpPortMap.get(username);
        if (serverAddr == null) {
            return "User not available! Try again";  //should throw exceptions here
        }

        File outputFileObject = new File(outputFile);
        try {
            outputFileObject.createNewFile();
            outputFileObject.getParentFile().mkdirs();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error while creating output file", e);
        }
        String returnResponse = "Successfully downloaded file"; //response to be returned
        try (Socket socket = new Socket(serverAddr.getAddress(), serverAddr.getPort());
             var bfos = new BufferedOutputStream(new FileOutputStream(outputFileObject));
             var outputStream = new PrintWriter(socket.getOutputStream(), true);
             var inputStream = new BufferedInputStream(socket.getInputStream())) {
            String query = commandParts[0] + " " + inputFile;
            outputStream.println(query); //add line separator
            outputStream.flush();

            int numberOfBytesRead;
            numberOfBytesRead = inputStream.read(buffer);
            String response = new String(buffer, StandardCharsets.UTF_8);
            if (response.startsWith("Started upload")) { //no line separator
                while ((numberOfBytesRead = inputStream.read(buffer)) > 0) {
                    bfos.write(buffer, 0, numberOfBytesRead);
                    bfos.flush();
                }
                outputStream.print("File received");
            } else {
                returnResponse = "Error while downloading"; //should throw exception here
                outputStream.write("disconnect" + System.lineSeparator());
            }
            System.out.println(returnResponse);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "IO error in downloader", e);
        }
        if (returnResponse.equals("Successfully downloaded file")) {
            return outputFile; //should make it better
        }
        return returnResponse;
    }
}

