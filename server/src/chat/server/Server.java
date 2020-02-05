package chat.server;

import chat.encryption.Encryptor;

import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

class Server {
    private static final int PORT = 9000;

    private ServerSocket socket;
    private final List<ClientHandler> clients;
    private Encryptor encryptor;

    Server() {
        clients = new ArrayList<>();
        try {
            encryptor = new Encryptor();
        } catch (NoSuchPaddingException | InvalidKeyException | ClassNotFoundException | IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    void run() throws IOException {
        try {
            socket = new ServerSocket(PORT);
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Now listening on port " + PORT);

        Socket clientSocket = null;
        while (true) {
            try {
                clientSocket = socket.accept();
            } catch (IOException e) {
                System.out.println("I/O error: " + e);
            }

            if (clientSocket == null) continue;

            UUID clientId = UUID.randomUUID();
            ClientHandler client = new ClientHandler(clientId, clientSocket, encryptor);
            clients.add(client);
            client.setOnMessageListener(this::handleMessage);
            client.setOnDisconnectListener(this::handleClientDisconnect);
            client.start();
            client.sendMessage("Welcome to the encrypted chat.");
            client.sendMessage("You are: " + clientId.toString());
            System.out.println("Client " + clientId.toString() + " connected.");
        }
    }

    private void handleMessage(String msg, ClientHandler client) {
        System.out.println(client.getClientName() + "> " + msg);
        if (msg.startsWith("/")) {
            // Handle commands

            String[] parts = msg.split(" ");
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
                    client.sendMessage(client.getClientName() + "> " + msg);
            }
        } else {
            for (Iterator<ClientHandler> iter = this.clients.iterator(); iter.hasNext(); ) {
                ClientHandler other = iter.next();
                if (other.getClientId() != client.getClientId()) {
                    try {
                        other.sendMessageUnsafe(client.getClientName() + "> " + msg);
                    } catch (ClientDisconnectedException e) {
                        iter.remove();
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
            } catch (ClientDisconnectedException e) {
                iter.remove();
            }
        }
    }

    private void handleClientDisconnect(ClientHandler client) {
        clients.remove(client);
    }
}
