package client;

import client.communication.CommandsReader;
import client.communication.CommunicatorWithServer;
import client.communication.MapUpdater;
import client.downloader.FileDownloader;
import client.server.CommunicationModule;
import server.TorrentServer;

import java.net.InetSocketAddress;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TorrentClient extends Thread {
    private static final int SLEEP_TIME = 100;
    private Logger logger;
    private InetSocketAddress inetSocketAddress;
    private String serverHost;
    private int serverPort;
    private Map<String, String> filenameAndPathMap;
    private ConcurrentMap<String, InetSocketAddress> usersIpPortsMap;
    private Queue<String> serverQueries;
    private FileDownloader fileDownloader;
    private CommandsReader cmdReader;
    private CommandExecutor cmdExecutor;
    private CommunicatorWithServer cmnWithServer;
    private CommunicationModule cmnModule;
    private MapUpdater mapUpdater;
    private boolean isStopped;


    public TorrentClient(String serverHost, int serverPort) {
        logger = Logger.getLogger("torrentClient");
        this.serverQueries = new ArrayDeque<>();
        this.filenameAndPathMap = new HashMap<>();
        this.usersIpPortsMap = new ConcurrentHashMap<>();
        this.serverHost = serverHost;
        this.serverPort = serverPort;
        fileDownloader = new FileDownloader(usersIpPortsMap);
        cmnWithServer = new CommunicatorWithServer(serverQueries, serverHost, serverPort);
        cmdExecutor = new CommandExecutor(inetSocketAddress, filenameAndPathMap, usersIpPortsMap, serverQueries, fileDownloader);
        cmdReader = new CommandsReader(cmdExecutor);
        mapUpdater = new MapUpdater(usersIpPortsMap, serverHost, serverPort);
        isStopped = false;
    }

    public void run() {

        cmnWithServer.start();
        while ((inetSocketAddress = cmnWithServer.getServerInetSocketAddress()) == null) {
            try {
                Thread.sleep(SLEEP_TIME);
            } catch (InterruptedException e) {
                logger.log(Level.SEVERE, "Error while sleep()", e);
            }
        }

        cmnModule = new CommunicationModule(cmnWithServer.getServerInetSocketAddress(), filenameAndPathMap);
        cmnModule.start();
        cmdReader.start();
        mapUpdater.start();
        while (!isStopped && cmdReader.isAlive() && cmnWithServer.isAlive()) {
            try {
                Thread.sleep(SLEEP_TIME);
            } catch (InterruptedException e) {
                logger.log(Level.SEVERE, "Error while sleep()", e);
            }
        }
        cmnWithServer.terminate();
        cmnModule.terminate();
        cmdReader.terminate();
        mapUpdater.terminate();
        try {
            cmnWithServer.join();
            mapUpdater.join();
            cmnModule.join();
            mapUpdater.join();
        } catch (InterruptedException e) {
            logger.log(Level.SEVERE, "Error while join()", e);
        }

        System.out.println("Safe to close the application");
    }

    public static void main(String[] args) throws InterruptedException {
        TorrentClient testClient = new TorrentClient("localhost", 6666);
        testClient.start();
    }
}
