package chat.encryption;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.security.MessageDigest;

/**
 * The cryptography management class.
 *
 * This class handles all of the functionality used to ensure the
 * confidentiality and integrity of data sent between the server
 * and the clients.
 */
public class CryptoManager {
    private Cipher encrypt;
    private Cipher decrypt;
    private Mac mac;

    /**
     * Constructor for the CryptoManager class.
     *
     * @throws EncryptionException Thrown to simplify all other exceptions into just one exception, caught exceptions are
     *     used as the cause for the thrown EncryptionException.
     */
    public CryptoManager(SecretKey encKey, SecretKey macKey) throws EncryptionException {
        try {
            encrypt = Cipher.getInstance("DES/ECB/PKCS5Padding");
            encrypt.init(Cipher.ENCRYPT_MODE, encKey);

            decrypt = Cipher.getInstance("DES/ECB/PKCS5Padding");
            decrypt.init(Cipher.DECRYPT_MODE, encKey);

            mac = Mac.getInstance("HmacSHA512");
            mac.init(macKey);
        } catch (Exception e) {
            throw new EncryptionException(e);
        }
    }

    /**
     * Encrypt the message
     *
     * @param string The message to be encrypted
     * @return The cipher text
     * @throws EncryptionException Throws when it fails to encrypt, meant to simplify catching exceptions
     */
    public byte[] encrypt(String string) throws EncryptionException {
        try {
            return encrypt.doFinal(string.getBytes());
        } catch (Exception e) {
            throw new EncryptionException("Failed to encrypt the data.", e);
        }
    }

    /**
     * Decrypt the cipher text
     *
     * @param bytes The cipher text
     * @return The decrypted message
     * @throws EncryptionException Throws when it fails to decrypt, meant to simplify catching exceptions
     */
    public String decrypt(byte[] bytes) throws EncryptionException {
        try {
            return new String(decrypt.doFinal(bytes));
        } catch (Exception e) {
            throw new EncryptionException("Failed to encrypt the data.", e);
        }
    }

    /**
     * Generate an HMAC for the cipher text
     *
     * @param bytes The cipher text
     * @return The HMAC
     * @throws EncryptionException Throws when it fails to generate an HMAC, meant to simplify catching exceptions
     */
    public byte[] generateHmac(byte[] bytes) throws EncryptionException {
        try {
            return mac.doFinal(bytes);
        } catch (Exception e) {
            throw new EncryptionException("Failed to generate hmac for the data.", e);
        }
    }

    /**
     * Check the integrity of the cipher text
     *
     * @param bytes The cipher text
     * @param hmac The given HMAC
     * @return If the generated HMAC matches the given HMAC
     * @throws EncryptionException Throws when it fails to generate an HMAC, meant to simplify catching exceptions
     */
    public boolean checkIntegrity(byte[] bytes, byte[] hmac) throws EncryptionException {
        try {
            return MessageDigest.isEqual(mac.doFinal(bytes), hmac);
        } catch (Exception e) {
            throw new EncryptionException("Failed to verify the integrity of the data.", e);
        }
    }


    /**
     * Loads the secret keys from the given key file
     *
     * @return The crypto manager instance
     * @throws EncryptionException Throws when it is unable to read the keys from the file or
     *     the constructor threw an exception.
     */
    public static CryptoManager loadFromFile(File keyFile) throws EncryptionException {
        try {
            ObjectInputStream is = new ObjectInputStream(new FileInputStream(keyFile));
            SecretKey key = (SecretKey) is.readObject();
            SecretKey macKey = (SecretKey) is.readObject();
            is.close();

            return new CryptoManager(key, macKey);
        } catch (Exception e) {
            throw new EncryptionException(e);
        }
    }

    /**
     * Loads the secret keys from the default key file "SECRET_KEY"
     *
     * @return The crypto manager instance
     * @throws EncryptionException Throws when it is unable to read the keys from the file or
     *      the constructor threw an exception.
     */
    public static CryptoManager loadFromFile() throws EncryptionException {
        File keyFile = new File("SECRET_KEY");
        if (!keyFile.exists())
            throw new EncryptionException("Key file does not exist, run the util jar to create a key file.");

        return loadFromFile(keyFile);
    }
}
