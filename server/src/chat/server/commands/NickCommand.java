package chat.server.commands;

import chat.messages.Message;
import chat.server.ClientHandler;
import chat.server.Server;

class NickCommand extends AbstractCommand {
    NickCommand(Server server) {
        super("nick", server);
    }

    @Override
    void onExecute(String[] args, Message message, ClientHandler client) {
        if (args.length < 1) {
            client.sendMessage("Missing argument name, usage: /nick <name>");
            return;
        }

        boolean matches = false;
        String nickname = args[0];
        for (ClientHandler other : getServer().getConnectedClients()) {
            if (other.getClientName().equalsIgnoreCase(nickname)) {
                client.sendMessage("That nickname is already in use.");
                matches = true;
                break;
            }
        }
        if (matches) return;

        String oldName = client.getClientName();
        client.setClientName(nickname);
        getServer().broadcast(oldName + " is now known as " + nickname);
    }
}
