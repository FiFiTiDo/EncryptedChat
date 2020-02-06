package chat.client;

import chat.client.gui.GuiApplication;

public class ClientMain {
    public static Client client;

    public static void main(String[] args) throws Exception {
        client = new Client();
        client.run();
        GuiApplication.launch(GuiApplication.class, args);
    }
}
