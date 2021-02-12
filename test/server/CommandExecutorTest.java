package server;

import org.junit.Before;
import org.junit.Test;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static org.junit.Assert.*;

public class CommandExecutorTest {

    private static String UNKNOWN_COMMAND = "Unknown command";
    private static final InetSocketAddress DEFAULT_ISA = new InetSocketAddress("192.99.99.2", 6666);

    private ConcurrentMap<String, InetSocketAddress> userIpPortMap;
    private ConcurrentMap<String, Set<String>> userFilesMap;
    private CommandExecutor testCommandExecutor;

    @Before
    public void setUp() {
        userFilesMap = new ConcurrentHashMap<>();
        userIpPortMap = new ConcurrentHashMap<>();
        testCommandExecutor = new CommandExecutor(userIpPortMap, userFilesMap);
    }

    @Test
    public void testRegisterFilesWithNotEnoughArguments() {
        assertEquals(UNKNOWN_COMMAND + System.lineSeparator(), testCommandExecutor.execute("register username", null));
    }

    @Test
    public void testRegisterFilesWithValidArguments() {
        String[] usernames = {"usr1", "usr2"};
        String[] user1files = {"u1f1", "u1f2"};
        String[] user2files = {"u2f1"};

        //added leading and trailing whitespaces
        testCommandExecutor.execute("  register " + usernames[0] + " " + user1files[0] + " " + user1files[1] + "\t\t", DEFAULT_ISA);
        testCommandExecutor.execute(" register   " + usernames[1] + "  " + user2files[0], DEFAULT_ISA);

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
        String expected = "User <non-existingUser> has not registered any files" + System.lineSeparator();
        assertEquals(expected, testCommandExecutor.execute("unregister non-existingUser file1", DEFAULT_ISA));
    }

    @Test
    public void testUnregisterFilesWithNonExistingFile() {
        testCommandExecutor.execute("register username file1", DEFAULT_ISA);
        String expected = "Cannot unregister non-existing files: invalidFilename" + System.lineSeparator();
        assertEquals(expected, testCommandExecutor.execute("unregister username invalidFilename", DEFAULT_ISA));
    }

    @Test
    public void testUnregisterFilesWithValidArguments() {
        testCommandExecutor.execute("register username file1", DEFAULT_ISA);
        String expected = "Successfully unregistered files: file1" + System.lineSeparator();
        assertEquals(expected, testCommandExecutor.execute("unregister username file1", DEFAULT_ISA));
    }

    @Test
    public void testListFilesWithIllegalArguments() {
        assertEquals(UNKNOWN_COMMAND, testCommandExecutor.execute("list-files asdfasdff", DEFAULT_ISA));
    }

    @Test
    public void testListFilesWithValidArgument() {
        String[] usernames = {"usr1", "usr2"};
        String[] user1files = {"u1f1", "u1f2"};
        String[] user2files = {"u2f1"};

        //added leading and trailing whitespaces
        testCommandExecutor.execute("  register " + usernames[0] + " " + user1files[0] + " " + user1files[1] + "\t\t", DEFAULT_ISA);
        testCommandExecutor.execute(" register   " + usernames[1] + "  " + user2files[0], DEFAULT_ISA);

        String response = testCommandExecutor.execute("\t list-files  ", DEFAULT_ISA); //added leading and trailing whitespaces
        assertTrue("Not containing expected file", response.contains(usernames[0] + " : " + user1files[0]));
        assertTrue("Not containing expected file", response.contains(usernames[0] + " : " + user1files[1]));
        assertTrue("Not containing expected file", response.contains(usernames[1] + " : " + user2files[0]));
    }

    @Test
    public void testListIpPortsWithValidArguments() {
        testCommandExecutor.execute("register usr1 f1", DEFAULT_ISA);
        testCommandExecutor.execute("register usr2 f2", DEFAULT_ISA);
        String expected1 = "usr1 - " + DEFAULT_ISA.toString().substring(1);
        String expected2 = "usr2 - " + DEFAULT_ISA.toString().substring(1);
        String actual = testCommandExecutor.execute("list-ports", DEFAULT_ISA);

        assertTrue(actual.contains(expected1));
        assertTrue(actual.contains(expected2));
    }

    @Test
    public void testDisconnectUserWithValidArguments() {
        String username = "username";
        testCommandExecutor.execute("  register " + username + " f1" + "\t\t", DEFAULT_ISA);
        testCommandExecutor.execute("disconnect", DEFAULT_ISA);
        assertTrue("No users should be present in map", userIpPortMap.isEmpty());
    }

    @Test
    public void testExecuteWithInvalidArguments() {
        String expected = UNKNOWN_COMMAND + System.lineSeparator();
        assertEquals(expected, testCommandExecutor.execute("unknown", DEFAULT_ISA));
        assertEquals(expected, testCommandExecutor.execute("register", DEFAULT_ISA));
        assertEquals(expected, testCommandExecutor.execute("unregister", DEFAULT_ISA));
    }
}