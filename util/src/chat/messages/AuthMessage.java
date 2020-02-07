package chat.messages;

import java.util.HashMap;

public class AuthMessage extends Message {
    public static final String COMMAND = "AUTH";
    public static final String DATA_USERNAME = "username";
    public static final String DATA_PASSWORD = "password";

    public AuthMessage(String username, String password) {
        super(COMMAND, new HashMap<>() {{
            put(DATA_USERNAME, username);
            put(DATA_PASSWORD, password);
        }});
    }
}
