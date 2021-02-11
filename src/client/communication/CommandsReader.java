package client.communication;

import client.CommandExecutor;

import java.util.Scanner;

public class CommandsReader extends Thread {
    private CommandExecutor cmdExecutor;

    public CommandsReader(CommandExecutor cmdExecutor){
        this.cmdExecutor = cmdExecutor;
    }

    public void run() {

        Scanner scanner = new Scanner(System.in);
        String line;
        while(true){
            System.out.print("Enter command: ");
            line = scanner.nextLine();
            line = line.trim();
            cmdExecutor.execute(line);
            if(line.equals("disconnect")){
                break;
            }
            try {
                Thread.sleep(100); //wait for response to get from server
            } catch (InterruptedException e) {
                e.printStackTrace();
                continue;
            }
        }

    }

}
