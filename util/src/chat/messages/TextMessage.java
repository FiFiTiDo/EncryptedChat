package chat.messages;

import java.util.HashMap;

public class TextMessage extends Message {
    public static final String COMMAND = "TEXT";
    public static final String DATA_CHANNEL = "channel";
    public static final String DATA_SENDER_ID = "sender_id";
    public static final String DATA_SENDER_NAME = "sender_name";
    public static final String DATA_RAW_TEXT = "raw_text";

    public TextMessage(String channel, String text) {
        super(COMMAND, new HashMap<String, String>() {{
            put(DATA_CHANNEL, channel);
            put(DATA_RAW_TEXT, text);
        }});
    }

    public static TextMessage make(String channel, String text) {
        return new TextMessage(channel, text);
    }

    public static TextMessage make(String text) {
        return new TextMessage("", text);
    }
}
