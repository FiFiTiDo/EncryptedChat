package chat.encryption;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.security.MessageDigest;

public class CryptoManager {
    private Cipher encrypt;
    private Cipher decrypt;
    private SecretKey key;
    private SecretKey macKey;
    private Mac mac;

    public CryptoManager() throws EncryptionException {
        File keyFile = new File("SECRET_KEY");
        if (!keyFile.exists())
            throw new EncryptionException("Key file does not exist, run the util jar to create a key file.");

        try {
            ObjectInputStream is = new ObjectInputStream(new FileInputStream(keyFile));
            key = (SecretKey) is.readObject();
            macKey = (SecretKey) is.readObject();
            is.close();

            encrypt = Cipher.getInstance("DES/ECB/PKCS5Padding");
            encrypt.init(Cipher.ENCRYPT_MODE, key);

            decrypt = Cipher.getInstance("DES/ECB/PKCS5Padding");
            decrypt.init(Cipher.DECRYPT_MODE, key);

            mac = Mac.getInstance("HmacSHA512");
            mac.init(macKey);
        } catch (Exception e) {
            throw new EncryptionException(e);
        }
    }

    public byte[] encrypt(String string) throws EncryptionException {
        try {
            return encrypt.doFinal(string.getBytes());
        } catch (Exception e) {
            throw new EncryptionException("Failed to encrypt the data.", e);
        }
    }

    public String decrypt(byte[] bytes) throws EncryptionException {
        try {
            return new String(decrypt.doFinal(bytes));
        } catch (Exception e) {
            throw new EncryptionException("Failed to encrypt the data.", e);
        }
    }

    public byte[] generateHmac(byte[] bytes) throws EncryptionException {
        try {
            return mac.doFinal(bytes);
        } catch (Exception e) {
            throw new EncryptionException("Failed to generate hmac the data.", e);
        }
    }

    public boolean checkIntegrity(byte[] bytes, byte[] hmac) throws EncryptionException {
        try {
            return MessageDigest.isEqual(mac.doFinal(bytes), hmac);
        } catch (Exception e) {
            throw new EncryptionException("Failed to verify integrity the data.", e);
        }
    }

    public SecretKey getKey() {
        return key;
    }
}
