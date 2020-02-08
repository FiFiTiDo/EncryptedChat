package chat.server;

public class ServerConfig {
    private int port;
    private DatabaseConfig database;
    private String keyFile;

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public DatabaseConfig getDatabase() {
        return database;
    }

    public void setDatabase(DatabaseConfig database) {
        this.database = database;
    }

    public String getKeyFile() {
        return keyFile;
    }

    public void setKeyFile(String keyFile) {
        this.keyFile = keyFile;
    }
}
