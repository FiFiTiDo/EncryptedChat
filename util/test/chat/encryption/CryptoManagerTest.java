package chat.encryption;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import java.security.NoSuchAlgorithmException;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CryptoManagerTest {
    private CryptoManager cryptoManager;

    @BeforeAll
    void setup() throws EncryptionException, NoSuchAlgorithmException {
        KeyGenerator keyGen = KeyGenerator.getInstance("DES");
        SecretKey key = keyGen.generateKey();

        KeyGenerator macKeyGen = KeyGenerator.getInstance("HmacSHA512");
        SecretKey macKey = macKeyGen.generateKey();

        this.cryptoManager = new CryptoManager(key, macKey);
    }

    @Test
    void decryptedShouldEqualSource() throws EncryptionException {
        String plaintext = "gndsjogpnopugwgjeriopgh";
        byte[] encrypted = cryptoManager.encrypt(plaintext);
        String decrypted = cryptoManager.decrypt(encrypted);

        assertEquals(plaintext, decrypted);
    }

    @Test
    void checkIntegrityOfUntamperedData() throws EncryptionException {
        String plaintext = "gndsjogpnopugwgjeriopgh";
        byte[] encrypted = cryptoManager.encrypt(plaintext);
        byte[] hmac = cryptoManager.generateHmac(encrypted);

        assertTrue(cryptoManager.checkIntegrity(encrypted, hmac));
    }

    @Test
    void checkIntegrityOfTamperedData() throws EncryptionException {
        String plaintext = "gndsjogpnopugwgjeriopgh";
        String plaintext2 = "bhfjdsujiofgerwhjdiofgprhg";
        byte[] encrypted = cryptoManager.encrypt(plaintext);
        byte[] encrypted2 = cryptoManager.encrypt(plaintext2);
        byte[] hmac = cryptoManager.generateHmac(encrypted);

        assertFalse(cryptoManager.checkIntegrity(encrypted2, hmac));
    }
}
