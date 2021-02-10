package server;

import java.net.InetSocketAddress;
import java.util.*;

public class CommandExecutor {

    private static String UNKNOWN_COMMAND = "Unknown command";

    private Map<String, InetSocketAddress> userIpPortMap;
    private Map<String, Set<String>> userFilesMap;

    CommandExecutor(Map<String, InetSocketAddress> userIpPortMap, Map<String, Set<String>> userFilesMap) {
        this.userIpPortMap = userIpPortMap;
        this.userFilesMap = userFilesMap;
    }

    public String execute(String query) {
        String[] queryParts = query.trim().split("\s+", 2); //splitting into two parts
        return switch (queryParts[0]) {
            case "register" -> registerFiles(queryParts);
            case "unregister" -> unregisterFiles(queryParts);
            case "list-files" -> listFiles(queryParts); //to check if there are no more arguments
            case "list-ports" -> userIpPortMapToString().toString();
            default -> UNKNOWN_COMMAND;
        };
    }

    private String registerFiles(String[] info) {
        if(info.length <= 1){
            return UNKNOWN_COMMAND;
        }

        String[] arguments = info[1].split("\s+");

        if (arguments.length <= 1) {
            return UNKNOWN_COMMAND;
        }

        String username = arguments[0];

        if (!userFilesMap.containsKey(username)) {
            userFilesMap.put(username, new HashSet<>());
        }

        StringBuilder response = new StringBuilder();
        response.append("Successfully registered files:");

        for (int i = 1; i < arguments.length; ++i) {
            userFilesMap.get(username).add(arguments[i]); //add file path for current user
            response.append(" " + arguments[i]);
        }

        return response.toString();
    }

    private String unregisterFiles(String[] info) {
        if(info.length <= 1){
            return UNKNOWN_COMMAND;
        }

        String[] arguments = info[1].split("\s+");

        if (arguments.length <= 1) {
            return UNKNOWN_COMMAND;
        }

        String username = arguments[0];

        if(!userFilesMap.containsKey(username)){
            return "User <" + username + "> has not registered any files";
        }

        StringBuilder response = new StringBuilder();
        response.append("Successfully unregistered files:");

        for (int i = 1; i < arguments.length; ++i) {
            if (!userFilesMap.get(username).remove(arguments[i])) {
                return "Cannot remove non-existing file";
            }
            response.append(" " + arguments[i]);
        }
        return response.toString();
    }

    private String listFiles(String[] info) {
        if (info.length != 1) {
            return UNKNOWN_COMMAND;
        }

        StringBuilder result = new StringBuilder();

        for (Map.Entry<String, Set<String>> entry : userFilesMap.entrySet()) {
            String username = entry.getKey();
            for (String fileName : entry.getValue()) {
                result.append(username + " : " + fileName + System.lineSeparator());
            }
        }
        return result.toString();
    }

    private StringBuilder userIpPortMapToString() {
        StringBuilder result = new StringBuilder();
        for (Map.Entry<String, InetSocketAddress> entry : userIpPortMap.entrySet()) {
            result.append(entry.getKey() + " - ");
            result.append(entry.getValue().toString().substring(1) + System.lineSeparator());
        }
        return result;
    }

}
