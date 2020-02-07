package chat.encryption;

public class EncryptionException extends Exception {
    EncryptionException() {
        super();
    }

    EncryptionException(String message) {
        super(message);
    }

    EncryptionException(String message, Throwable cause) {
        super(message, cause);
    }

    EncryptionException(Throwable cause) {
        super(cause);
    }
}
