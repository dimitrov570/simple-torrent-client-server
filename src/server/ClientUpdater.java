package server;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

public class ClientUpdater extends Thread {

    private Map<String,IpPortCombination> userIpPortMap;

    ClientUpdater(Map<String, IpPortCombination> userIpPortMap){
        this.userIpPortMap = userIpPortMap;
    }

    public void run(){

    }


    private StringBuilder mapToString() {
        StringBuilder result = new StringBuilder();
        for(Map.Entry<String, IpPortCombination> entry : userIpPortMap.entrySet()){
            result.append(entry.getKey()+" - ");
            result.append(entry.getValue() + System.lineSeparator());
        }
        return result;
    }


    public static void main(String[] args) throws Exception {
        Map<String,IpPortCombination> userIpPortMap = new HashMap<>();

        ClientUpdater test = new ClientUpdater(userIpPortMap);

        for(int i=0; i<1000000; ++i) {
            userIpPortMap.put("ivajlo" + i, new IpPortCombination(InetAddress.getByName("192.168.4.2"), 33));
        }

        System.out.println("sleeping...");
        Thread.sleep(4000);
        System.out.println("started");
        System.out.println(test.mapToString());
    }

}
