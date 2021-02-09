package server;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class TorrentServer {
    private Map<String, IpPortCombination> userIpPortMap;
    private Map<String, Set<String>> userFilesMap;
    private CommandExecutor cmdExecutor;
    private CommunicatorWithClients communicator;

    TorrentServer() {
        userIpPortMap = new HashMap<>();
        userFilesMap = new HashMap<>();
        cmdExecutor = new CommandExecutor(userIpPortMap, userFilesMap);
        communicator = new CommunicatorWithClients(cmdExecutor);
        communicator.start();
    }

    public void stop(){
        communicator.terminate();
    }

    public static void main(String[] args) {

    }
}
