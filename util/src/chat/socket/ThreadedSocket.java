package chat.socket;

import chat.encryption.Encryptor;
import chat.messages.Message;
import chat.messages.TextMessage;
import com.google.gson.Gson;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
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

    private Encryptor encryptor;

    private OnMessageListener onMessageListener;
    private OnDisconnectListener onDisconnectListener;

    private volatile Gson gson;

    public ThreadedSocket(String host, int port, Encryptor encryptor) throws IOException {
        this(new Socket(host, port), encryptor);
    }

    public ThreadedSocket(Socket socket, Encryptor encryptor) throws IOException {
        this.socket = socket;
        this.is = new DataInputStream(socket.getInputStream());
        this.os = new DataOutputStream(socket.getOutputStream());
        this.encryptor = encryptor;
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
            byte[] bytes = encryptor.encrypt(json);

            this.os.writeInt(bytes.length);
            this.os.write(bytes);
            this.os.flush();
        } catch (SocketException e) {
            if (e.getMessage().equals("Connection reset by peer: socket write error")) {
                this.disconnect();
                throw new DisconnectedException();
            } else {
                e.printStackTrace();
            }
        } catch (BadPaddingException | IllegalBlockSizeException | IOException e) {
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
            try {
                int length = is.readInt(); // read length of incoming message

                if (length <= 0) continue;

                byte[] raw = new byte[length];
                is.readFully(raw, 0, raw.length); // read the message

                Message msg = gson.fromJson(encryptor.decrypt(raw), Message.class);
                this.onMessageListener.onMessage(msg, this);
            } catch (SocketException e) {
                if (e.getMessage().equals("Connection reset")) {
                    this.disconnect();
                    this.onDisconnectListener.onDisconnect(this);
                } else {
                    e.printStackTrace();
                }
                return;
            } catch (EOFException e) {
                this.disconnect();
                this.onDisconnectListener.onDisconnect(this);
            } catch (BadPaddingException | IllegalBlockSizeException | IOException e) {
                e.printStackTrace();
                return;
            }
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
