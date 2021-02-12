package server;

import org.junit.Test;

import java.net.Socket;
import java.nio.channels.SocketChannel;
import java.util.Arrays;

import static org.junit.Assert.*;

public class TorrentServerTest {

    @Test
    public void testServerResponseToUnknownCommand() throws Exception {
        TorrentServer test = new TorrentServer();
        test.start();
        Socket sc = new Socket("localhost", 6666);
        sc.getOutputStream().write(("lol" + System.lineSeparator()).getBytes());
        byte[] responseByteArray = new byte[512];
        int bytesRead = sc.getInputStream().read(responseByteArray);
        String response = new String(Arrays.copyOfRange(responseByteArray, 0, bytesRead), "UTF-8");
        assertEquals("Unknown command" + System.lineSeparator(), response);
        test.stop();
        test.join();
    }

}