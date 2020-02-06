package chat.socket;

public interface OnDisconnectListener {
    void onDisconnect(ThreadedSocket socket);
}
