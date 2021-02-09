package server;

import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

public class CommandExecutorTest {

    private static String UNKNOWN_COMMAND = "Unknown command";

    private Map<String, IpPortCombination> userIpPortMap;
    private Map<String, Set<String>> userFilesMap;
    private CommandExecutor testCommandExecutor;

    @Before
    public void setUp() {
        userFilesMap = new HashMap<>();
        userIpPortMap = new HashMap<>();
        testCommandExecutor = new CommandExecutor(userIpPortMap, userFilesMap);
    }

    @Test
    public void testRegisterFilesWithNotEnoughArguments() {
        assertEquals(UNKNOWN_COMMAND, testCommandExecutor.execute("register username"));
    }

    @Test
    public void testRegisterFilesWithValidArguments() {
        String[] usernames = {"usr1", "usr2"};
        String[] user1files = {"u1f1", "u1f2"};
        String[] user2files = {"u2f1"};

        //added leading and trailing whitespaces
        testCommandExecutor.execute("  register " + usernames[0] + " " + user1files[0] + " " + user1files[1] + "\t\t");
        testCommandExecutor.execute(" register   " + usernames[1] + "  " + user2files[0]);

        assertTrue("Not containing expected user", userFilesMap.containsKey(usernames[0]));
        assertTrue("Not containing expected user", userFilesMap.containsKey(usernames[1]));

        assertEquals("Number of registered files not as expected", 2, userFilesMap.get(usernames[0]).size());
        assertEquals("Number of registered files not as expected", 1, userFilesMap.get(usernames[1]).size());

        for (int i = 0; i < user1files.length; ++i) {
            assertTrue("Not containing expected file", userFilesMap.get(usernames[0]).contains(user1files[i]));
        }

        for (int i = 0; i < user2files.length; ++i) {
            assertTrue("Not containing expected file", userFilesMap.get(usernames[1]).contains(user2files[i]));
        }
    }

    @Test
    public void testUnregisterFilesWithNonExistingUser() {
        String expected = "User <non-existingUser> has not registered any files";
        assertEquals(expected, testCommandExecutor.execute("unregister non-existingUser file1"));
    }

    @Test
    public void testUnregisterFilesWithNonExistingFile() {
        testCommandExecutor.execute("register username file1");
        String expected = "Cannot remove non-existing file";
        assertEquals(expected, testCommandExecutor.execute("unregister username invalidFilename"));
    }

    @Test
    public void testUnregisterFilesWithValidArguments() {
        testCommandExecutor.execute("register username file1");
        String expected = "Succesfully unregistered files: file1";
        assertEquals(expected, testCommandExecutor.execute("unregister username file1"));
    }

    @Test
    public void testListFilesWithIllegalArguments() {
        assertEquals(UNKNOWN_COMMAND, testCommandExecutor.execute("list-files asdfasdff"));
    }

    @Test
    public void testListFilesWithValidArgument() {
        String[] usernames = {"usr1", "usr2"};
        String[] user1files = {"u1f1", "u1f2"};
        String[] user2files = {"u2f1"};

        //added leading and trailing whitespaces
        testCommandExecutor.execute("  register " + usernames[0] + " " + user1files[0] + " " + user1files[1] + "\t\t");
        testCommandExecutor.execute(" register   " + usernames[1] + "  " + user2files[0]);

        String response = testCommandExecutor.execute("\t list-files  "); //added leading and trailing whitespaces
        assertTrue("Not containing expected file", response.contains(usernames[0] + " : " + user1files[0]));
        assertTrue("Not containing expected file", response.contains(usernames[0] + " : " + user1files[1]));
        assertTrue("Not containing expected file", response.contains(usernames[1] + " : " + user2files[0]));
    }

    @Test
    public void testExecuteWithInvalidArguments(){
        assertEquals(UNKNOWN_COMMAND, testCommandExecutor.execute("unknown"));
        assertEquals(UNKNOWN_COMMAND, testCommandExecutor.execute("register"));
        assertEquals(UNKNOWN_COMMAND, testCommandExecutor.execute("unregister"));
    }
}