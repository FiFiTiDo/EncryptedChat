package chat.encryption;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.security.NoSuchAlgorithmException;

public class KeyGeneratorMain {
    public static void main(String[] args) throws NoSuchAlgorithmException, IOException {
        KeyGenerator keyGen = KeyGenerator.getInstance("DES");
        SecretKey key = keyGen.generateKey();

        KeyGenerator macKeyGen = KeyGenerator.getInstance("HmacSHA512");
        SecretKey macKey = macKeyGen.generateKey();

        File file = new File("SECRET_KEY");
        file.createNewFile();
        ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(file));
        os.writeObject(key);
        os.writeObject(macKey);
        os.close();
    }
}
