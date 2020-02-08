package chat.messages;

import java.util.HashMap;

public class PingMessage extends Message {
    public static final String COMMAND = "PING";

    public PingMessage() {
        super(COMMAND, new HashMap<>());
    }
}
