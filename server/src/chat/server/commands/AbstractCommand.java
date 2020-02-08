package chat.server.commands;

import chat.messages.Message;
import chat.server.ClientHandler;
import chat.server.Server;

abstract class AbstractCommand {
    private String label;
    private Server server;

    protected AbstractCommand(String label, Server server) {
        this.label = label;
        this.server = server;
    }

    public String getLabel() {
        return label;
    }

    protected Server getServer() {
        return server;
    }

    abstract void onExecute(String[] args, Message message, ClientHandler client);
}
