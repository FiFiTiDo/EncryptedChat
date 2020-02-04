package chat.encryption;

import javax.crypto.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class Encryptor {
    private Cipher encrypt;
    private Cipher decrypt;
    private SecretKey key;

    public Encryptor() throws NoSuchPaddingException, NoSuchAlgorithmException, IOException, ClassNotFoundException, InvalidKeyException {
        ObjectInputStream is = new ObjectInputStream(new FileInputStream(new File("SECRET_KEY")));
        key = (SecretKey) is.readObject();
        is.close();

        encrypt = Cipher.getInstance("DES/ECB/PKCS5Padding");
        encrypt.init(Cipher.ENCRYPT_MODE, key);

        decrypt = Cipher.getInstance("DES/ECB/PKCS5Padding");
        decrypt.init(Cipher.DECRYPT_MODE, key);
    }

    public byte[] encrypt(String string) throws BadPaddingException, IllegalBlockSizeException {
        return encrypt.doFinal(string.getBytes());
    }

    public String decrypt(byte[] bytes) throws BadPaddingException, IllegalBlockSizeException {
        return new String(decrypt.doFinal(bytes));
    }

    public SecretKey getKey() {
        return key;
    }
}
