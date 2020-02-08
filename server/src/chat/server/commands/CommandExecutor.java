package chat.server.commands;

import chat.messages.Message;
import chat.server.ClientHandler;
import chat.server.Server;

import java.util.HashMap;
import java.util.Map;

public class CommandExecutor {
    private Map<String, AbstractCommand> commands;

    public CommandExecutor(Server server) {
        this.commands = new HashMap<>();
        this.registerCommand(new QuitCommand(server));
        this.registerCommand(new NickCommand(server));
    }

    private void registerCommand(AbstractCommand command) {
        this.commands.put(command.getLabel().toLowerCase(), command);
    }

    public boolean execute(String label, String[] args, Message message, ClientHandler client) {
        if (!this.commands.containsKey(label.toLowerCase())) return false;

        this.commands.get(label.toLowerCase()).onExecute(args, message, client);

        return true;
    }
}
