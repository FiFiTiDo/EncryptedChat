package chat.server;

import chat.encryption.CryptoManager;
import chat.encryption.EncryptionException;
import chat.messages.Message;
import chat.messages.PingMessage;
import chat.messages.PongMessage;
import chat.messages.TextMessage;
import chat.server.commands.CommandExecutor;
import chat.socket.DisconnectedException;
import chat.socket.ThreadedSocket;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

/**
 * The Server class.
 *
 * Handles all the operations of the server aspect of the chat system including:
 *     - Messages
 *     - Commands
 *     - Accepting connections
 *     - etc.
 */
public class Server {
    private final List<ClientHandler> clients;
    private ServerSocket socket;
    private CryptoManager cryptoManager;
    private ServerConfig config;
    private CommandExecutor commandExecutor;

    /**
     * The constructor of the Server class
     *
     * Loads the configuration file, sets up the crypto manager, and sets up the command executor
     */
    Server() {
        try {
            File configFile = new File("config.yml");
            Yaml yaml = new Yaml();
            config = yaml.loadAs(new FileReader(configFile), ServerConfig.class);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.exit(1);
        }

        clients = new ArrayList<>();
        try {
            cryptoManager = CryptoManager.loadFromFile();
        } catch (EncryptionException e) {
            e.printStackTrace();
            System.exit(1);
        }

        this.commandExecutor = new CommandExecutor(this);
    }

    /**
     * The main loop of the server.
     *
     * This method accepts connections with clients and starts a
     * worker thread to handle receiving data from the connection.
     * Also sends a welcome message to the client when they connect.
     *
     * @throws IOException Throws when trying to instantiate a ClientHandler instance
     */
    void run() throws IOException {
        try {
            socket = new ServerSocket(config.getPort());
        } catch (BindException e) {
            System.out.println("Failed to bind on port " + config.getPort() + " as it is already in use.");
            return;
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Now listening on port " + config.getPort());

        Socket clientSocket;
        while (true) {
            try {
                clientSocket = socket.accept();
            } catch (IOException e) {
                System.out.println("I/O error: " + e);
                continue;
            }

            if (clientSocket == null) continue;

            UUID clientId = UUID.randomUUID();
            ClientHandler client = new ClientHandler(clientId, clientSocket, cryptoManager);
            clients.add(client);
            client.setOnMessageListener(this::handleMessage);
            client.setOnDisconnectListener(this::handleClientDisconnect);
            client.start();
            client.sendMessage("Welcome to the encrypted chat.");
            client.sendMessage("You are: " + clientId.toString());
            System.out.println("Client " + clientId.toString() + " connected.");
        }
    }

    /**
     * Handles messages set from the client
     *
     * This method only accepts ClientHandler instances for the second parameter and will
     * just return immediately if it is not a ClientHandler.
     *
     * @param msg The message the client sent
     * @param socket The client that sent the message (as a ThreadedSocket)
     */
    private void handleMessage(Message msg, ThreadedSocket socket) {
        if (!(socket instanceof ClientHandler)) return;
        ClientHandler client = (ClientHandler) socket;

        switch (msg.getCommand()) {
            case TextMessage.COMMAND:
                handleTextMessage(msg, client);
                break;
            case PingMessage.COMMAND:
                handlePingMessage(msg, client);
                break;
        }
    }

    /**
     * Handles TextMessage messages.
     *
     * All messages starting with a forward slash are considered commands otherwise the server
     * just attaches the sender's id and name and forwards the message to the other clients.
     *
     * @param msg The message from the client
     * @param client The client that sent the message
     */
    private void handleTextMessage(Message msg, ClientHandler client) {
        String raw = msg.getData(TextMessage.DATA_RAW_TEXT);
        System.out.println(client.getClientName() + "> " + raw);
        if (raw.startsWith("/")) {
            // Handle commands
            String[] parts = raw.split(" ");
            String command = parts[0].toLowerCase().substring(1);
            String[] args = Arrays.copyOfRange(parts, 1, parts.length);

            if (!commandExecutor.execute(command, args, msg, client))
                client.sendMessage("Unknown command: " + command);
        } else {
            // Regular message
            msg.putData(TextMessage.DATA_SENDER_ID, client.getClientId().toString());
            msg.putData(TextMessage.DATA_SENDER_NAME, client.getClientName());
            for (Iterator<ClientHandler> iter = this.clients.iterator(); iter.hasNext(); ) {
                ClientHandler other = iter.next();
                if (other.getClientId() != client.getClientId())
                    other.sendMessage(msg, socket -> {
                        iter.remove();
                        System.out.println(String.format("Client %s has disconnected.", client.getClientId()));
                    });
            }
        }
    }

    /**
     * Responds to a ping message with a pong message
     *
     * @param msg The message from the client
     * @param client The client that sent the message
     */
    private void handlePingMessage(Message msg, ClientHandler client) {
        client.sendMessage(new PongMessage());
    }

    /**
     * Broadcast a message to all clients connected to the server
     *
     * @param msg The message to broadcast
     */
    public void broadcast(String msg) {
        Message message = TextMessage.make(msg);
        for (Iterator<ClientHandler> iter = this.clients.iterator(); iter.hasNext(); ) {
            ClientHandler client = iter.next();
            client.sendMessage(message, socket -> {
                iter.remove();
                System.out.println(String.format("Client %s has disconnected.", client.getClientId()));
            });
        }
    }

    /**
     * Get the list of currently connected clients
     *
     * @return A list of connected clients
     */
    public List<ClientHandler> getConnectedClients() {
        return clients;
    }

    /**
     * Handles a client disconnecting from the server
     *
     * This method only accepts ClientHandler instances for the second parameter and will
     * just return immediately if it is not a ClientHandler.
     *
     * @param socket The client that disconnected (as a ThreadedSocket)
     */
    private void handleClientDisconnect(ThreadedSocket socket) {
        if (!(socket instanceof ClientHandler)) return;
        ClientHandler client = (ClientHandler) socket;
        clients.remove(client);
        System.out.println(String.format("Client %s has disconnected.", client.getClientId()));
    }
}
