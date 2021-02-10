package server;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

public class ClientUpdater extends Thread {

    private CommandExecutor commandExecutor;

    ClientUpdater(CommandExecutor commandExecutor){
        this.commandExecutor = commandExecutor;
    }

    public void run(){


    }


    public static void main(String[] args) throws Exception {

        InetSocketAddress addr = new InetSocketAddress("192.123.123.9",3);

        System.out.println(addr);


        Map<String,InetSocketAddress> userIpPortMap = new HashMap<>();

        CommandExecutor test = new CommandExecutor(userIpPortMap, null);

         for(int i=0; i<1000000; ++i) {
            userIpPortMap.put("ivajlo" + i, new InetSocketAddress("192.168.4.2", 33));
        }

        System.out.println("sleeping...");
        Thread.sleep(4000);
        System.out.println("started");
        System.out.println(test.execute("list-ports"));
    }

}
