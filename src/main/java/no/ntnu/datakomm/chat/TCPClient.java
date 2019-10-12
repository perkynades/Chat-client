package no.ntnu.datakomm.chat;

import java.io.*;
import java.net.*;
import java.util.LinkedList;
import java.util.List;

public class TCPClient {
    private PrintWriter toServer;
    private BufferedReader fromServer;
    private Socket connection;
    private OutputStream outputStream;

    // Hint: if you want to store a message for the last error, store it here
    private String lastError = null;

    private final List<ChatListener> listeners = new LinkedList<>();

    /**
     * Connect to a chat server.
     *
     * @param host host name or IP address of the chat server
     * @param port TCP port of the chat server
     * @return True on success, false otherwise
     */
    public boolean connect(String host, int port) {
        boolean connected = false;
        try {
            connection = new Socket(host, port);
            connected = true;
            return connected;
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
            connected = false;
        }
        return connected;
    }

    /**
     * Close the socket. This method must be synchronized, because several
     * threads may try to call it. For example: When "Disconnect" button is
     * pressed in the GUI thread, the connection will get closed. Meanwhile, the
     * background thread trying to read server's response will get error in the
     * input stream and may try to call this method when the socket is already
     * in the process of being closed. with "synchronized" keyword we make sure
     * that no two threads call this method in parallel.
     */
    public synchronized void disconnect() {
        if (isConnectionActive()) {
            try {
                outputStream.close();
                connection.close();
                connection = null;
                onDisconnect();
            } catch (IOException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    /**
     * @return true if the connection is active (opened), false if not.
     */
    public boolean isConnectionActive() {
        return connection != null;
    }

    /**
     * Send a command to server. Also checks if the connection is alive or not.
     *
     * @param cmd A command. It should include the command word and optional attributes, according to the protocol.
     * @return true on success, false otherwise
     */
    private boolean sendCommand(String cmd) {
        boolean isCommandSent = false;
        if (isConnectionActive()) {
            try {
                outputStream = connection.getOutputStream();
                toServer = new PrintWriter(outputStream, true);
                toServer.println(cmd);
                toServer.println("");
                isCommandSent = true;
                return isCommandSent;
            } catch (IOException e) {
                System.out.println("Error: " + e.getMessage());
                isCommandSent = false;
                return isCommandSent;
            }
        } else {
            return isCommandSent;
        }
    }

    /**
     * Send a public message to all the recipients.
     *
     * @param message Message to send
     * @return true if message sent, false on error
     */
    public boolean sendPublicMessage(String message) {
        return sendCommand("msg " + message);
    }

    /**
     * Send a login request to the chat server.
     *
     * @param username Username to use
     */
    public void tryLogin(String username) {
        sendCommand("login " + username);
    }

    /**
     * Send a request for latest user list to the server. To get the new users,
     * clear your current user list and use events in the listener.
     */
    public void refreshUserList() {
        sendCommand("users");
    }

    /**
     * Send a private message to a single recipient.
     *
     * @param recipient username of the chat user who should receive the message
     * @param message   Message to send
     * @return true if message sent, false on error
     */
    public boolean sendPrivateMessage(String recipient, String message) {
        String privateMessage = "privmsg " + recipient + " " + message;
        return sendCommand(privateMessage);
    }


    /**
     * Send a request for the list of commands that server supports.
     */
    public void askSupportedCommands() {
        sendCommand("help");
    }


    /**
     * Wait for chat server's response
     *
     * @return one line of text (one command) received from the server
     */
    private String waitServerResponse() {
        String oneResponseLine = "";
        try {
            fromServer = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            oneResponseLine = this.fromServer.readLine();
        } catch (IOException e) {
            System.err.println(e.getMessage());
            this.disconnect();
        }
        return oneResponseLine;
    }

    /**
     * Get the last error message
     *
     * @return Error message or "" if there has been no error
     */
    public String getLastError() {
        if (lastError != null) {
            return lastError;
        } else {
            return "";
        }
    }

    /**
     * Start listening for incoming commands from the server in a new CPU thread.
     */
    public void startListenThread() {
        // Call parseIncomingCommands() in the new thread.
        Thread t = new Thread(this::parseIncomingCommands);
        t.start();
    }

    /**
     * Read incoming messages one by one, generate events for the listeners. A loop that runs until
     * the connection is closed.
     */
    private void parseIncomingCommands() {
        while (isConnectionActive()) {
            while (isConnectionActive()) {
                final int COMMAND_WORD_INDEX = 1;
                final int COMMAND_BODY = 2;
                try {
                    String response = waitServerResponse();
                    StringSplitter stringSplitter = new StringSplitter("\\s");
                    stringSplitter.split(response, 2);
                    String command = stringSplitter.getPart(COMMAND_WORD_INDEX);
                    switch (command) {
                        case "loginok":
                            onLoginResult(true, response);
                            break;

                        case "loginerr":
                            onLoginResult(false, stringSplitter.getPart(COMMAND_BODY));
                            break;

                        case "users":
                            String[] users = stringSplitter.getAllPartsFromString(stringSplitter.getPart(COMMAND_BODY));
                            onUsersList(users);
                            break;

                        case "msg":
                            stringSplitter.split(stringSplitter.getPart(COMMAND_BODY), 2);
                            onMsgReceived(false, stringSplitter.getPart(COMMAND_WORD_INDEX), stringSplitter.getPart(COMMAND_BODY));
                            break;

                        case "msgerr":
                            onMsgError(stringSplitter.getPart(COMMAND_BODY));
                            break;

                        case "cmderr":
                            onCmdError(stringSplitter.getPart(COMMAND_BODY));
                            break;

                        case "supported":
                            onSupported(stringSplitter.getSplittedString());
                            break;

                        default:
                            System.out.println("Default triggered. Response: " + response);
                    }
                } catch (IllegalArgumentException e) {
                    System.out.println("Error: "+ e.getMessage());
                } catch (IndexOutOfBoundsException e) {
                    System.out.println("Error: " + e.getMessage());
                } catch (Exception e) {
                    System.out.println("Oh no...");
                }
            }
        }
    }

    /**
     * Register a new listener for events (login result, incoming message, etc)
     *
     * @param listener
     */
    public void addListener(ChatListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    /**
     * Unregister an event listener
     *
     * @param listener
     */
    public void removeListener(ChatListener listener) {
        listeners.remove(listener);
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////////////////
    // The following methods are all event-notificators - notify all the listeners about a specific event.
    // By "event" here we mean "information received from the chat server".
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Notify listeners that login operation is complete (either with success or
     * failure)
     *
     * @param success When true, login successful. When false, it failed
     * @param errMsg  Error message if any
     */
    private void onLoginResult(boolean success, String errMsg) {
        for (ChatListener chatListener : listeners) {
            chatListener.onLoginResult(success, errMsg);
        }
    }

    /**
     * Notify listeners that socket was closed by the remote end (server or
     * Internet error)
     */
    private void onDisconnect() {
        for (ChatListener chatListener : listeners) {
            chatListener.onDisconnect();
        }
    }

    /**
     * Notify listeners that server sent us a list of currently connected users
     *
     * @param users List with usernames
     */
    private void onUsersList(String[] users) {
        // TODO Step 5: Implement this method
        for (ChatListener chatListener : listeners) {
            chatListener.onUserList(users);
        }
    }

    /**
     * Notify listeners that a message is received from the server
     *
     * @param priv   When true, this is a private message
     * @param sender Username of the sender
     * @param text   Message text
     */
    private void onMsgReceived(boolean priv, String sender, String text) {
        TextMessage textMessage = new TextMessage(sender, priv, text);
        for (ChatListener chatListener : listeners) {
            chatListener.onMessageReceived(textMessage);
        }
    }

    /**
     * Notify listeners that our message was not delivered
     *
     * @param errMsg Error description returned by the server
     */
    private void onMsgError(String errMsg) {
        for (ChatListener chatListener : listeners) {
            chatListener.onMessageError(errMsg);
        }
    }

    /**
     * Notify listeners that command was not understood by the server.
     *
     * @param errMsg Error message
     */
    private void onCmdError(String errMsg) {
        for (ChatListener chatListener : listeners) {
            chatListener.onCommandError(errMsg);
        }
    }

    /**
     * Notify listeners that a help response (supported commands) was received
     * from the server
     *
     * @param commands Commands supported by the server
     */
    private void onSupported(String[] commands) {
        for (ChatListener chatListener : listeners) {
            chatListener.onSupportedCommands(commands);
        }
    }
}
