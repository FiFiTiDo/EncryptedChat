package chat.server;

import chat.encryption.Encryptor;
import chat.socket.ThreadedSocket;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.UUID;

class ClientHandler extends ThreadedSocket {
    private final UUID id;
    private String name;

    public ClientHandler(UUID id, Socket socket, Encryptor encryptor) throws IOException {
        super(socket, encryptor);

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
