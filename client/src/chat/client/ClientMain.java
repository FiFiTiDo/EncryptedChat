package chat.client;

import chat.encryption.Encryptor;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.Scanner;

public class ClientMain {
    private static Encryptor encryptor;

    private static volatile DataOutputStream output;

    private static volatile boolean connected = false;

    public static void main(String[] args) throws Exception {
        encryptor = new Encryptor();
        Socket socket = new Socket("127.0.0.1", 9000);
        DataInputStream input = new DataInputStream(socket.getInputStream());
        output = new DataOutputStream(socket.getOutputStream());
        connected = true;

        Thread consoleThread = new Thread(() -> {
            Scanner scanner = new Scanner(System.in);
            while (connected) {
                String line = scanner.nextLine();
                try {
                    byte[] encrypted = encryptor.encrypt(line);

                    System.out.println("Key: " + new String(encryptor.getKey().getEncoded()) + ", Raw: " + line + ", Encrypted: " + new String(encrypted));
                    System.out.flush();

                    output.writeInt(encrypted.length);
                    output.write(encrypted);
                    output.flush();
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.flush();
                }
            }
        });
        consoleThread.start();

        while (true) {
            try {
                int length = input.readInt(); // read length of incoming message

                if (length <= 0) continue;

                byte[] message = new byte[length];
                input.readFully(message, 0, message.length); // read the message

                String decrypted = encryptor.decrypt(message);

                System.out.println("Key: " + new String(encryptor.getKey().getEncoded()) + ", Encrypted: " + new String(message) + ", Decrypted: " + decrypted);
                System.out.flush();

                System.out.println(decrypted);
                System.out.flush();
            } catch (BadPaddingException | IllegalBlockSizeException e) {
                e.printStackTrace();
                break;
            } catch (SocketException e) {
                if (e.getMessage().equals("Connection reset")) {
                    System.out.println("Lost connection to the server");
                    System.out.flush();
                } else {
                    e.printStackTrace();
                    System.out.flush();
                }
                break;
            } catch (IOException e) {
                e.printStackTrace();
                System.out.flush();
                break;
            }
        }
        consoleThread.interrupt();
        System.exit(1);
    }
}
