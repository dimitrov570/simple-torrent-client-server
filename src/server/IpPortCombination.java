package server;

import java.net.InetAddress;

public record IpPortCombination(InetAddress address,int port) {

    @Override
    public String toString() {
        return address.toString().substring(1) + ":" + port; //substring(1) to remove forward slash from address
    }

}
