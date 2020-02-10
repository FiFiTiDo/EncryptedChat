package chat.messages;

import java.util.HashMap;

public class TextMessage extends Message {
    public static final String COMMAND = "TEXT";
    public static final String DATA_ROOM = "room";
    public static final String DATA_SENDER_ID = "sender_id";
    public static final String DATA_SENDER_NAME = "sender_name";
    public static final String DATA_RAW_TEXT = "raw_text";

    public TextMessage(String room, String text) {
        super(COMMAND, new HashMap<>() {{
            put(DATA_ROOM, room);
            put(DATA_RAW_TEXT, text);
        }});
    }

    public static TextMessage make(String room, String text) {
        return new TextMessage(room, text);
    }

    public static TextMessage make(String text) {
        return new TextMessage("", text);
    }
}
