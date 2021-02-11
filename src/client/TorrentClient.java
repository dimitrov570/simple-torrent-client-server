package client;

import client.communication.CommandsReader;
import client.communication.CommunicatorWithServer;
import client.communication.MapUpdater;
import client.server.CommunicationModule;
import server.TorrentServer;

import java.net.InetSocketAddress;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

public class TorrentClient extends Thread {
    private InetSocketAddress inetSocketAddress;
    private String serverHost;
    private int serverPort;
    Map<String, String> filenameAndPathMap;
    Map<String, String> usersIpPortsMap;
    private Queue<String> serverQueries;
    private CommandsReader cmdReader;
    private CommandExecutor cmdExecutor;
    private CommunicatorWithServer cmnWithServer;
    private CommunicationModule cmnModule;
    private MapUpdater mapUpdater;


    public TorrentClient(String serverHost, int serverPort) {
        serverQueries = new ArrayDeque<>();
        filenameAndPathMap = new HashMap<>();
        this.usersIpPortsMap = new HashMap<>();
        this.serverHost = serverHost;
        this.serverPort = serverPort;
        cmnWithServer = new CommunicatorWithServer(serverQueries, serverHost, serverPort);
        cmdExecutor = new CommandExecutor(filenameAndPathMap, serverQueries);
        cmdReader = new CommandsReader(cmdExecutor);
        mapUpdater = new MapUpdater(usersIpPortsMap, serverHost, serverPort);
    }

    public void run() {

        cmnWithServer.start();
        while ((inetSocketAddress = cmnWithServer.getServerInetSocketAddress()) == null) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        ;
        cmnModule = new CommunicationModule(cmnWithServer.getServerInetSocketAddress());
        cmnModule.start();
        cmdReader.start();
        mapUpdater.start();
    }

    public static void main(String[] args) throws InterruptedException {
        TorrentClient testClient = new TorrentClient("localhost", 6666);
        testClient.start();
    }
}
