package main.java.chat.server;

import main.java.chat.encryption.Encryptor;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.UUID;

class ClientHandler extends Thread {
    private final UUID id;
    private String name;
    private final Socket socket;
    private DataInputStream is;
    private volatile DataOutputStream os;

    private Encryptor encryptor;

    private OnMessageListener onMessageListener;
    private OnDisconnectListener onDisconnectListener;

    public ClientHandler(UUID id, Socket socket, Encryptor encryptor) throws IOException {
        this.id = id;
        this.socket = socket;
        this.name = id.toString();
        this.is = new DataInputStream(socket.getInputStream());
        this.os = new DataOutputStream(socket.getOutputStream());
        this.encryptor = encryptor;
    }

    public void setOnMessageListener(OnMessageListener onMessageListener) {
        this.onMessageListener = onMessageListener;
    }

    public void setOnDisconnectListener(OnDisconnectListener onDisconnectListener) {
        this.onDisconnectListener = onDisconnectListener;
    }

    void sendMessageUnsafe(String message) throws ClientDisconnectedException {
        try {
            byte[] bytes = encryptor.encrypt(message);

            this.os.writeInt(bytes.length);
            this.os.write(bytes);
            this.os.flush();
        } catch (BadPaddingException | IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (SocketException e) {
            if (e.getMessage().equals("Connection reset by peer: socket write error")) {
                this.disconnect();
                throw new ClientDisconnectedException();
            } else {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void sendMessage(String message) {
        try {
            sendMessageUnsafe(message);
        } catch (ClientDisconnectedException e) {
            this.onDisconnectListener.onDisconnect(this);
        }
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

    @Override
    public void run() {
        while (!this.isInterrupted() && socket.isConnected()) {
            try {
                int length = is.readInt(); // read length of incoming message

                if (length <= 0) continue;

                byte[] message = new byte[length];
                is.readFully(message, 0, message.length); // read the message

                this.onMessageListener.onMessage(encryptor.decrypt(message), this);
            } catch (BadPaddingException | IllegalBlockSizeException e) {
                e.printStackTrace();
                return;
            } catch (SocketException e) {
                if (e.getMessage().equals("Connection reset")) {
                    System.out.println("Client " + id.toString() + " disconnected.");
                } else {
                    e.printStackTrace();
                }
                return;
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }
    }

    void disconnect() {
        System.out.println("Client " + this.id.toString() + " disconnected.");
        try {
            this.socket.close();
            this.is = null;
            this.os = null;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
