package chat.socket;

import chat.messages.Message;

public interface OnMessageListener {
    void onMessage(Message msg, ThreadedSocket socket);
}
