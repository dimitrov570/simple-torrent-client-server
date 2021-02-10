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

    public String execute(String query, InetSocketAddress scInetAddress) {
        String[] queryParts = query.trim().split("\s+", 2); //splitting into two parts
        return switch (queryParts[0]) {
            case "register" -> registerFiles(queryParts, scInetAddress) + System.lineSeparator();
            case "unregister" -> unregisterFiles(queryParts) + System.lineSeparator();
            case "list-files" -> listFiles(queryParts); //to check if there are no more arguments
            case "list-ports" -> userIpPortMapToString(queryParts);
            default -> UNKNOWN_COMMAND + System.lineSeparator();
        };
    }

    private String registerFiles(String[] queryParts, InetSocketAddress scInetAddress) {
        if (queryParts.length <= 1) {
            return UNKNOWN_COMMAND;
        }

        String[] arguments = queryParts[1].split("\s+");

        if (arguments.length <= 1) {
            return UNKNOWN_COMMAND;
        }

        String username = arguments[0];

        if (!userFilesMap.containsKey(username)) {
            userFilesMap.put(username, new HashSet<>());
        }

        userIpPortMap.put(username, scInetAddress);

        boolean hasAlreadyExistingFiles = false;

        StringBuilder responseSuccess = new StringBuilder();
        StringBuilder responseAlreadyExists = new StringBuilder();
        responseSuccess.append("Successfully registered files:");
        responseAlreadyExists.append("Already existing files: ");

        for (int i = 1; i < arguments.length; ++i) {
            if (!userFilesMap.get(username).contains(arguments[i])) {
                userFilesMap.get(username).add(arguments[i]); //add file path for current user
                responseSuccess.append(" " + arguments[i]);
            } else {
                hasAlreadyExistingFiles = true;
                responseAlreadyExists.append(" " + arguments[i]);
            }
        }

        if (hasAlreadyExistingFiles) {
            return responseSuccess.toString() + System.lineSeparator() + responseAlreadyExists.toString();
        }
        return responseSuccess.toString();
    }

    private String unregisterFiles(String[] queryParts) {
        if (queryParts.length <= 1) {
            return UNKNOWN_COMMAND;
        }

        String[] arguments = queryParts[1].split("\s+");

        if (arguments.length <= 1) {
            return UNKNOWN_COMMAND;
        }

        String username = arguments[0];

        if (!userFilesMap.containsKey(username) || userFilesMap.get(username).isEmpty()) {
            return "User <" + username + "> has not registered any files";
        }

        boolean hasNonExisting = false;
        boolean hasExisting = false;
        StringBuilder responseSuccess = new StringBuilder();
        StringBuilder responseNotExisting = new StringBuilder();
        responseSuccess.append("Successfully unregistered files:");
        responseNotExisting.append("Cannot unregister non-existing files:");

        for (int i = 1; i < arguments.length; ++i) {
            if (!userFilesMap.get(username).remove(arguments[i])) {
                hasNonExisting = true;
                responseNotExisting.append(" " + arguments[i]);
            } else {
                hasExisting = true;
                responseSuccess.append(" " + arguments[i]);
            }
        }

        if (hasExisting) {
            if (hasNonExisting) {
                return responseSuccess + System.lineSeparator() + responseNotExisting;
            } else {
                return responseSuccess.toString();
            }
        } else if (hasNonExisting) {
            return responseNotExisting.toString();
        }

        return "Nothing to remove"; //will never get here
    }

    private String listFiles(String[] queryParts) {
        if (queryParts.length != 1) {
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

    private String userIpPortMapToString(String[] queryParts) {
        if (queryParts.length != 1) {
            return UNKNOWN_COMMAND;
        }

        StringBuilder result = new StringBuilder();
        for (Map.Entry<String, InetSocketAddress> entry : userIpPortMap.entrySet()) {
            result.append(entry.getKey() + " - ");
            result.append(entry.getValue().toString().substring(1) + System.lineSeparator());
        }
        return result.toString();
    }

}