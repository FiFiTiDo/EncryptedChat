package chat.messages;

import java.util.HashMap;

public class PongMessage extends Message {
    public static final String COMMAND = "PING";

    public PongMessage() {
        super(COMMAND, new HashMap<>());
    }
}
