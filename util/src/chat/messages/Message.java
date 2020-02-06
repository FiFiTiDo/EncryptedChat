package chat.messages;

import java.util.Map;

public class Message {
    private String command;
    private Map<String, String> data;

    Message() {}

    Message(String command, Map<String, String> data) {
        this.command = command;
        this.data = data;
    }

    public String getCommand() {
        return command;
    }


    public void putData(String key, String value) {
        data.put(key, value);
    }

    public String getData(String key) {
        return data.get(key);
    }

    public String getDataOrDefault(String key, String defVal) {
        return data.getOrDefault(key, defVal);
    }

    public Map<String, String> getAllData() {
        return data;
    }
}
