package chat.server;

import chat.encryption.CryptoManager;
import chat.socket.ThreadedSocket;

import java.io.IOException;
import java.net.Socket;
import java.util.UUID;

public class ClientHandler extends ThreadedSocket {
    private final UUID id;
    private String name;

    ClientHandler(UUID id, Socket socket, CryptoManager cryptoManager) throws IOException {
        super(socket, cryptoManager);

        this.id = id;
        this.name = id.toString();
    }

    UUID getClientId() {
        return this.id;
    }

    public String getClientName() {
        return name;
    }

    public void setClientName(String name) {
        this.name = name;
    }
}
