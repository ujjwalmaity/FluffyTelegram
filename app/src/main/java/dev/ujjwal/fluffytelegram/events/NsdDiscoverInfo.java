package dev.ujjwal.fluffytelegram.events;

public class NsdDiscoverInfo {

    private final String name;
    private final String type;
    private final String host;
    private final int port;

    public NsdDiscoverInfo(String name, String type, String host, int port) {
        this.name = name;
        this.type = type;
        this.host = host;
        this.port = port;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }
}
