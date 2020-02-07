package chat.socket;

import chat.encryption.CryptoManager;
import chat.encryption.EncryptionException;
import chat.messages.Message;
import chat.messages.TextMessage;
import com.google.gson.Gson;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;

public class ThreadedSocket extends Thread {
    private final Socket socket;
    private DataInputStream is;
    private volatile DataOutputStream os;

    private CryptoManager cryptoManager;

    private OnMessageListener onMessageListener;
    private OnDisconnectListener onDisconnectListener;

    private volatile Gson gson;

    public ThreadedSocket(String host, int port, CryptoManager cryptoManager) throws IOException {
        this(new Socket(host, port), cryptoManager);
    }

    public ThreadedSocket(Socket socket, CryptoManager cryptoManager) throws IOException {
        this.socket = socket;
        this.is = new DataInputStream(socket.getInputStream());
        this.os = new DataOutputStream(socket.getOutputStream());
        this.cryptoManager = cryptoManager;
        this.gson = new Gson();
    }

    public void setOnMessageListener(OnMessageListener onMessageListener) {
        this.onMessageListener = onMessageListener;
    }

    public void setOnDisconnectListener(OnDisconnectListener onDisconnectListener) {
        this.onDisconnectListener = onDisconnectListener;
    }

    public void sendMessageUnsafe(Message message) throws DisconnectedException {
        try {
            String json = gson.toJson(message);
            byte[] bytes = cryptoManager.encrypt(json);
            byte[] hmac = cryptoManager.generateHmac(bytes);

            this.os.writeInt(bytes.length);
            this.os.writeInt(hmac.length);
            this.os.write(bytes);
            this.os.write(hmac);
            this.os.flush();
        } catch (SocketException e) {
            if (e.getMessage().equals("Connection reset by peer: socket write error")) {
                this.disconnect();
                throw new DisconnectedException();
            } else {
                e.printStackTrace();
            }
        } catch (EncryptionException | IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessageUnsafe(String message) throws DisconnectedException {
        sendMessageUnsafe(TextMessage.make(message));
    }

    public void sendMessage(Message message) {
        try {
            sendMessageUnsafe(message);
        } catch (DisconnectedException e) {
            this.onDisconnectListener.onDisconnect(this);
        }
    }

    public void sendMessage(String message) {
        sendMessage(TextMessage.make(message));
    }

    @Override
    public void run() {
        while (!this.isInterrupted() && socket.isConnected()) {
            byte[] raw;
            byte[] hmac;
            try {
                int length = is.readInt(); // read length of incoming message
                int lengthHmac = is.readInt(); // read length of incoming hmac

                if (length <= 0) continue;

                raw = new byte[length];
                is.readFully(raw, 0, raw.length); // read the message

                hmac = new byte[lengthHmac];
                is.readFully(hmac, 0, hmac.length); // read the message
            } catch (SocketException e) {
                if (e.getMessage().equals("Connection reset")) {
                    this.disconnect();
                    this.onDisconnectListener.onDisconnect(this);
                } else if (e.getMessage().equals("Socket closed")) {
                    this.onDisconnectListener.onDisconnect(this);
                } else {
                    e.printStackTrace();
                }
                return;
            } catch (EOFException | NullPointerException e) {
                this.disconnect();
                this.onDisconnectListener.onDisconnect(this);
                return;
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }

            Message msg;
            try {
                if (!cryptoManager.checkIntegrity(raw, hmac)) {
                    System.out.println("Security Error: could not verify the integrity of the data.");
                    return;
                }

                msg = gson.fromJson(cryptoManager.decrypt(raw), Message.class);
            } catch (EncryptionException e) {
                e.printStackTrace();
                return;
            }
            this.onMessageListener.onMessage(msg, this);
        }
    }

    public void disconnect() {
        try {
            this.socket.close();
            this.is = null;
            this.os = null;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
