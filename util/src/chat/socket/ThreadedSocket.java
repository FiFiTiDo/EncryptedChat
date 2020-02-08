package chat.socket;

import chat.encryption.CryptoManager;
import chat.encryption.EncryptionException;
import chat.messages.Message;
import chat.messages.PingMessage;
import chat.messages.TextMessage;
import com.google.gson.Gson;

import javax.crypto.CipherOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.*;

/**
 * The ThreadedSocket class.
 *
 * Prevents the duplication of code by handling all of the aspects of communication
 * for both the client and server.
 *
 * - Encrypts and decrypts the data
 * - Converts the raw JSON to and from a Message instance
 * - Handles reading from the socket
 */
public class ThreadedSocket extends Thread {
    private final Socket socket;
    private DataInputStream is;
    private DataOutputStream os;

    private CryptoManager cryptoManager;

    private OnMessageListener onMessageListener;
    private OnDisconnectListener onDisconnectListener;

    private Gson gson;
    private Thread heartbeatThread;

    /**
     * The ThreadedSocket constructor.
     *
     * Used in the client to create a socket from a host and port
     *
     * @param host The hostname of the server
     * @param port The port the server is listening on
     * @param cryptoManager The cryptography manager instance
     * @throws IOException Throws when failing to get the io streams
     */
    public ThreadedSocket(String host, int port, CryptoManager cryptoManager) throws IOException {
        this(new Socket(host, port), cryptoManager);
    }

    /**
     * The ThreadedSocket constructor.
     *
     * @param socket The socket to manage
     * @param cryptoManager The cryptography manager instance
     * @throws IOException Throws when failing to get the io streams
     */
    public ThreadedSocket(Socket socket, CryptoManager cryptoManager) throws IOException {
        this.socket = socket;
        this.is = new DataInputStream(socket.getInputStream());
        this.os = new DataOutputStream(socket.getOutputStream());
        this.cryptoManager = cryptoManager;
        this.gson = new Gson();

        heartbeatThread = new Thread(this::sendHeartbeat);
        heartbeatThread.start();
    }

    /**
     * Set the onMessageListener
     *
     * @param onMessageListener The new listener
     */
    public void setOnMessageListener(OnMessageListener onMessageListener) {
        this.onMessageListener = onMessageListener;
    }

    /**
     * Set the onDisconnectListener
     *
     * @param onDisconnectListener The new listener
     */
    public void setOnDisconnectListener(OnDisconnectListener onDisconnectListener) {
        this.onDisconnectListener = onDisconnectListener;
    }


    /**
     * Send a message and notifies the specified listener rather than the object's set listener
     *
     * @param message The message to send through the socket
     * @param listener An OnDisconnectListener
     */
    public synchronized void sendMessage(Message message, OnDisconnectListener listener) {
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
            if (e.getMessage().equals("Connection reset by peer: socket write error") || e.getMessage().equals("Socket closed")) {
                this.disconnect();
                listener.onDisconnect(this);
            } else {
                e.printStackTrace();
            }
        } catch (EncryptionException | IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Send a message
     *
     * Runs the onDisconnectListener which might not be desired when looping over socket instances
     * and the listener will remove the socket.
     *
     * @param message The message to send
     */
    public void sendMessage(Message message) {
        this.sendMessage(message, this.onDisconnectListener);
    }

    /**
     * Send a message
     *
     * Makes it easier to send a TextMessage
     *
     * @see ThreadedSocket#sendMessage(Message)
     * @param message The message to send
     */
    public void sendMessage(String message) {
        sendMessage(TextMessage.make(message));
    }

    /**
     * The thread worker that reads messages from the socket and sends it to the onMessageListener
     */
    @Override
    public void run() {
        while (!this.isInterrupted() && socket.isConnected()) {
            byte[] raw;
            byte[] hmac;
            try {
                int length = is.readInt(); // read length of incoming message
                int lengthHmac = is.readInt(); // read length of incoming hmac

                raw = new byte[length];
                is.readFully(raw, 0, raw.length); // read the message

                hmac = new byte[lengthHmac];
                is.readFully(hmac, 0, hmac.length); // read the message
            } catch (SocketTimeoutException | EOFException | NullPointerException e) {
                break;
            } catch (SocketException e) {
                // Socket disconnected
                if (e.getMessage().equals("Connection reset") || e.getMessage().equals("Socket closed"))
                    break;

                // Other SocketException type
                e.printStackTrace();
                break;
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }

            Message msg;
            try {
                if (!cryptoManager.checkIntegrity(raw, hmac)) {
                    System.out.println("Security Error: could not verify the integrity of the data.");
                    continue;
                }

                msg = gson.fromJson(cryptoManager.decrypt(raw), Message.class);
            } catch (EncryptionException e) {
                e.printStackTrace();
                break;
            }
            this.onMessageListener.onMessage(msg, this);
        }

        this.disconnect();
        this.onDisconnectListener.onDisconnect(this);
    }

    /**
     * Thread runnable for sending a pong message
     */
    private void sendHeartbeat() {
        while (true) {
            sendMessage(new PingMessage());
            try {
                TimeUnit.SECONDS.sleep(20);
            } catch (InterruptedException e) { break; }
        }
    }

    /**
     * Disconnect the socket connection
     */
    public void disconnect() {
        try {
            this.socket.close();
            this.is = null;
            this.os = null;
            this.heartbeatThread.interrupt();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
