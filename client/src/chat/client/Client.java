package chat.client;

import chat.encryption.CryptoManager;
import chat.encryption.EncryptionException;
import chat.messages.Message;
import chat.messages.TextMessage;
import chat.socket.ThreadedSocket;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;

public class Client implements Runnable {
    private ThreadedSocket socket;
    private volatile boolean connected = false;
    private ClientConfig config;

    Client() throws IOException, EncryptionException {
        try {
            File configFile = new File("config.yml");
            Yaml yaml = new Yaml();
            config = yaml.loadAs(new FileReader(configFile), ClientConfig.class);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.exit(1);
        }

        socket = new ThreadedSocket(config.getHost(), config.getPort(), CryptoManager.loadFromFile(config.getKeyFile()));
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
        System.exit(1); // Force the console reading thread to stop
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
            System.exit(1);
        }).start();
    }
}
