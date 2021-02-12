package server;

import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.net.InetSocketAddress;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class TorrentServer {
    private Logger logger;
    private static final String HOST_NAME = "localhost";
    private static final int SERVER_PORT = 6666;
    private ConcurrentMap<String, InetSocketAddress> userIpPortMap;
    private ConcurrentMap<String, Set<String>> userFilesMap;
    private CommandExecutor cmdExecutor;
    private CommunicatorWithClients communicator;

    TorrentServer() {
        logger = Logger.getLogger("torrentServer");
        userIpPortMap = new ConcurrentHashMap<>();
        userFilesMap = new ConcurrentHashMap<>();
        cmdExecutor = new CommandExecutor(userIpPortMap, userFilesMap);
        communicator = new CommunicatorWithClients(cmdExecutor, HOST_NAME, SERVER_PORT);
    }

    public void start() {
        communicator.start();
    }

    public void stop() {
        communicator.terminate();
        try {
            communicator.join();
        } catch (InterruptedException e) {
            logger.log(Level.SEVERE, "Error while join()", e);
        }
    }

    public void join() throws InterruptedException {
        communicator.join();
    }

    public static void main(String[] args) throws InterruptedException {
        TorrentServer test = new TorrentServer();
        test.start();
        Scanner is = new Scanner(System.in);
        String input = is.nextLine();
        while (!input.equals("stop")) {
            input = is.nextLine();
        }
        System.out.println("Stopping server");
        test.stop();
    }
}
