package chat.server;

import chat.encryption.CryptoManager;
import chat.encryption.EncryptionException;
import chat.messages.Message;
import chat.messages.TextMessage;
import chat.socket.DisconnectedException;
import chat.socket.ThreadedSocket;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

class Server {
    private final List<ClientHandler> clients;
    private ServerSocket socket;
    private CryptoManager cryptoManager;
    private ServerConfig config;

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
    }

    void run() throws IOException {
        try {
            socket = new ServerSocket(config.getPort());
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Now listening on port " + config.getPort());

        Socket clientSocket = null;
        while (true) {
            try {
                clientSocket = socket.accept();
            } catch (IOException e) {
                System.out.println("I/O error: " + e);
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

    private void handleMessage(Message msg, ThreadedSocket socket) {
        if (!(socket instanceof ClientHandler)) return;
        ClientHandler client = (ClientHandler) socket;

        if (msg.getCommand().equals(TextMessage.COMMAND)) {
            String raw = msg.getData(TextMessage.DATA_RAW_TEXT);
            System.out.println(client.getClientName() + "> " + raw);
            if (raw.startsWith("/")) {
                // Handle commands

                String[] parts = raw.split(" ");
                String command = parts[0].toLowerCase().substring(1);
                String[] args = Arrays.copyOfRange(parts, 1, parts.length);

                switch (command) {
                    case "quit":
                        client.disconnect();
                        break;
                    case "nick":
                        if (args.length < 1) {
                            client.sendMessage("Missing argument name, usage: /nick <name>");
                        } else {
                            boolean matches = false;
                            String nickname = args[0];
                            for (ClientHandler other : clients) {
                                if (other.getClientName().equalsIgnoreCase(nickname)) {
                                    client.sendMessage("That nickname is already in use.");
                                    matches = true;
                                    break;
                                }
                            }
                            if (!matches) {
                                String oldName = client.getClientName();
                                client.setClientName(nickname);
                                broadcast(oldName + " is now known as " + nickname);
                            }
                        }
                        break;
                    default:
                        client.sendMessage("Unknown command: " + command);
                }
            } else {
                msg.putData(TextMessage.DATA_SENDER_ID, client.getClientId().toString());
                msg.putData(TextMessage.DATA_SENDER_NAME, client.getClientName());
                for (Iterator<ClientHandler> iter = this.clients.iterator(); iter.hasNext(); ) {
                    ClientHandler other = iter.next();
                    if (other.getClientId() != client.getClientId()) {
                        try {
                            other.sendMessageUnsafe(msg);
                        } catch (DisconnectedException e) {
                            iter.remove();
                        }
                    }
                }
            }
        }
    }

    private void broadcast(String msg) {
        for (Iterator<ClientHandler> iter = this.clients.iterator(); iter.hasNext(); ) {
            ClientHandler client = iter.next();
            try {
                client.sendMessageUnsafe(msg);
            } catch (DisconnectedException e) {
                iter.remove();
            }
        }
    }

    private void handleClientDisconnect(ThreadedSocket socket) {
        if (!(socket instanceof ClientHandler)) return;
        ClientHandler client = (ClientHandler) socket;
        clients.remove(client);
    }
}
