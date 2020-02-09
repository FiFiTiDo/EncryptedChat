package chat.server.commands;

import chat.messages.Message;
import chat.server.ClientHandler;
import chat.server.Server;

class QuitCommand extends AbstractCommand {
    QuitCommand(Server server) {
        super("quit", server);
    }

    @Override
    void onExecute(String[] args, Message message, ClientHandler client) {
        client.disconnect();
    }
}
