package server;

public class CommunicatorWithClients extends Thread {

    private CommandExecutor cmdExecutor;

    CommunicatorWithClients(CommandExecutor cmdExecutor){
        this.cmdExecutor = cmdExecutor;
    }

    public void run(){
        //set up network communication
        //add ip address and socket to cmd executor arguments
    }


    public void terminate(){
        //implement
    }
}
