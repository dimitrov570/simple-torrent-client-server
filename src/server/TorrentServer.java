package server;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class TorrentServer {
    private static final String HOST_NAME = "localhost";
    private static final int SERVER_PORT = 6666;
    private Map<String, InetSocketAddress> userIpPortMap;
    private Map<String, Set<String>> userFilesMap;
    private CommandExecutor cmdExecutor;
    private CommunicatorWithClients communicator;

    TorrentServer() {
        userIpPortMap = new HashMap<>();
        userFilesMap = new HashMap<>();
        cmdExecutor = new CommandExecutor(userIpPortMap, userFilesMap);
        communicator = new CommunicatorWithClients(cmdExecutor, HOST_NAME, SERVER_PORT);
    }

    public void start() {
        communicator.start();
    }

    public void stop() throws InterruptedException {
        communicator.terminate();
        communicator.join();
    }

    public static void main(String[] args) throws InterruptedException {
        TorrentServer test = new TorrentServer();
        test.start();
        Thread.sleep(10000);
       // test.stop();
        System.out.println("End");
    }
}
