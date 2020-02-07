package chat.client;

import chat.encryption.CryptoManager;
import chat.encryption.EncryptionException;
import chat.messages.Message;
import chat.messages.TextMessage;
import chat.socket.ThreadedSocket;
import javafx.application.Platform;

import java.io.IOException;
import java.util.Scanner;

public class Client implements Runnable {
    private ThreadedSocket socket;
    private volatile boolean connected = false;

    Client() throws IOException, EncryptionException {
        socket = new ThreadedSocket("127.0.0.1", 9000, new CryptoManager());
        socket.setOnDisconnectListener(this::handleDisconnected);
        socket.setOnMessageListener(this::handleMessage);
    }

    private void handleDisconnected(ThreadedSocket socket) {
        this.connected = false;
    }

    private void handleMessage(Message message, ThreadedSocket socket) {
        if (message.getCommand().equals(TextMessage.COMMAND)) {
            String sender = message.getDataOrDefault(TextMessage.DATA_SENDER_NAME, null);
            String raw = message.getData(TextMessage.DATA_RAW_TEXT);
            if (sender == null) { // Message from the server
                System.out.println(raw);
            } else {
                System.out.println(sender + "> " + raw);
            }
        }
    }

    public void disconnect() {
        this.connected = false;
        socket.disconnect();
    }

    @Override
    public void run() {
        socket.start();

        new Thread(() -> {
            connected = true;

            Scanner scanner = new Scanner(System.in);
            while (connected) {
                String line = scanner.nextLine();
                try {
                    this.socket.sendMessage(line);
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.flush();
                    break;
                }
            }
            System.out.println("Exiting program...");
            Platform.exit();
        });
    }
}
