package client.communication;

import client.CommandExecutor;

import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CommandsReader extends Thread {
    private static final int SLEEP_TIME = 500;
    private Logger logger;
    private CommandExecutor cmdExecutor;
    private boolean isStopped;

    public CommandsReader(CommandExecutor cmdExecutor) {
        logger = Logger.getLogger("commandsReader");
        this.cmdExecutor = cmdExecutor;
        isStopped = false;
    }

    public void run() {

        Scanner scanner = new Scanner(System.in);
        String line;
        while (!isStopped) {
            System.out.print("Enter command: ");
            line = scanner.nextLine();
            line = line.trim();
            cmdExecutor.execute(line);
            if (line.equals("disconnect")) {
                System.out.println("Don't close the application until you get a message!");
                break;
            }
            try {
                Thread.sleep(SLEEP_TIME); //wait for response to get from server
            } catch (InterruptedException e) {
                logger.log(Level.SEVERE, "Error while sleep()", e);
                continue;
            }
        }

    }

    public void terminate() {
        isStopped = true;
    }

}
