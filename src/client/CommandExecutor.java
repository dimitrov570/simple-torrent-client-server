package client;

import java.io.File;
import java.util.Map;
import java.util.Queue;

public class CommandExecutor {

    Map<String, String> filenameAndPathMap;
    Queue<String> serverQueries;

    public CommandExecutor(Map<String, String> filenameAndPathMap, Queue<String> serverQueries) {
        this.filenameAndPathMap = filenameAndPathMap;
        this.serverQueries = serverQueries;
    }

    //lines are trimmed
    public void execute(String line) {
        if (line.startsWith("register")) {
            registerFiles(line);
        } else if (line.startsWith("unregister")) {
            unregisterFiles(line);
        } else if (line.startsWith("download")) {
            //TODO start downloader
        }
        serverQueries.add(line);
    }

    public static String getFileName(String path) {

        String fileName = path;
        int indexOfLastFileSeparator = path.lastIndexOf(File.separator);
        if (indexOfLastFileSeparator != -1 && indexOfLastFileSeparator + 1 < path.length()) {
            fileName = path.substring(indexOfLastFileSeparator + 1);
        }
        return fileName;
    }

    private void registerFiles(String query) {
        String[] queryParts = query.split("\s+");
        if (queryParts.length <= 2) {
            return;
        }
        String fileName;
        for (int i = 2; i < queryParts.length; ++i) {
            fileName = getFileName(queryParts[1]);
            filenameAndPathMap.put(fileName,queryParts[i]);
        }
    }

    private void unregisterFiles(String query) {
        String[] queryParts = query.split("\s+");
        if (queryParts.length <= 2) {
            return;
        }
        String fileName;
        for (int i = 2; i < queryParts.length; ++i) {
            fileName = getFileName(queryParts[1]);
            filenameAndPathMap.remove(fileName);
        }
    }

}
