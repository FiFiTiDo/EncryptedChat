package main.java.chat.server;

interface OnMessageListener {
    void onMessage(String string, ClientHandler client);
}
