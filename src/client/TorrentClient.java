package client;

import client.communication.CommunicatorWithServer;
import client.server.CommunicationModule;
import server.TorrentServer;

import java.net.InetSocketAddress;
import java.util.ArrayDeque;
import java.util.Queue;

public class TorrentClient extends Thread {
    private InetSocketAddress inetSocketAddress;
    private Queue<String> serverQueries;
    private CommunicatorWithServer cmnWithServer;
    private CommunicationModule cmnModule;
    private String serverHost;
    private int serverPort;

    public TorrentClient(String serverHost, int serverPort){
        serverQueries = new ArrayDeque<>();
        this.serverHost = serverHost;
        this.serverPort = serverPort;
        cmnWithServer = new CommunicatorWithServer(serverQueries,serverHost, serverPort);
    }

    public void run(){

        cmnWithServer.start();
        while((inetSocketAddress = cmnWithServer.getServerInetSocketAddress()) == null){
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };
        cmnModule = new CommunicationModule(cmnWithServer.getServerInetSocketAddress());
        cmnModule.start();
    }

    public static void main(String[] args) throws InterruptedException {
        TorrentClient testClient = new TorrentClient("localhost", 6666);
        testClient.start();
    }
}
