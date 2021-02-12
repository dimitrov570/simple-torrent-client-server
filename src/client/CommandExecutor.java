package client;

import client.downloader.FileDownloader;

import java.io.File;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentMap;

public class CommandExecutor {

    FileDownloader fileDownloader;
    InetSocketAddress myInetSocketAddress;
    Map<String, String> filenameAndPathMap;
    ConcurrentMap<String, InetSocketAddress> userIpPortMap;
    Queue<String> serverQueries;

    public CommandExecutor(InetSocketAddress myInetSocketAddress, Map<String, String> filenameAndPathMap,
                           ConcurrentMap<String, InetSocketAddress> userIpPortMap, Queue<String> serverQueries,
                           FileDownloader fileDownloader) {
        this.myInetSocketAddress = myInetSocketAddress;
        this.fileDownloader = fileDownloader;
        this.filenameAndPathMap = filenameAndPathMap;
        this.userIpPortMap = userIpPortMap;
        this.serverQueries = serverQueries;
    }

    //lines are trimmed
    public void execute(String line) {
        if (line.startsWith("register")) {
            registerFiles(line);
        } else if (line.startsWith("unregister")) {
            unregisterFiles(line);
        } else if (line.startsWith("download")) {
            updateFiles(fileDownloader.download(line)); //register downloaded files and send info to server
        } else if (line.equals("local-ports")) {
            System.out.print(userIpPortMapToString());
        } else {
            serverQueries.add(line);
        }
    }

    private String userIpPortMapToString() {
        StringBuilder result = new StringBuilder();
        for (Map.Entry<String, InetSocketAddress> entry : userIpPortMap.entrySet()) {
            result.append(entry.getKey() + " - ");
            result.append(entry.getValue().toString().substring(1) + System.lineSeparator());
        }
        return result.toString();
    }

    private void updateFiles(String response) {
        String username = null;
        for (Map.Entry<String, InetSocketAddress> entry : userIpPortMap.entrySet()) {
            if (entry.getValue().equals(myInetSocketAddress)) {
                username = entry.getKey();
                System.out.print("found");
                break;
            }
        }
        if (username != null) {
            String fileName = getFileName(response);
            serverQueries.add("register " + username + " " + fileName);
            filenameAndPathMap.put(fileName, response);
        } else {
            System.out.println(response);
        }
    }

    private static String getFileName(String path) {

        String fileName = path;
        int indexOfLastFileSeparator = path.lastIndexOf(File.separator);
        if (indexOfLastFileSeparator != -1 && indexOfLastFileSeparator + 1 < path.length()) {
            fileName = path.substring(indexOfLastFileSeparator + 1);
        }
        return fileName;
    }

    private void registerFiles(String query) {
        final int filePathIndexStart = 2;
        String[] queryParts = query.split("\s+");
        if (queryParts.length <= 2) {
            System.out.println("Unknown command");
            return;
        }
        String fileName;
        boolean addedFiles = false;
        //add register + username and later only available files
        StringBuilder validQuery = new StringBuilder(queryParts[0] + " " + queryParts[1]);
        for (int i = filePathIndexStart; i < queryParts.length; ++i) {
            fileName = getFileName(queryParts[i]);
            if (Files.exists(Path.of(queryParts[i]))) {
                addedFiles = true;
                filenameAndPathMap.put(fileName, queryParts[i]);
                validQuery.append(" " + fileName);
            }
        }
        if (addedFiles) {
            serverQueries.add(validQuery.toString());
        } else {
            System.out.println("No files added");
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
