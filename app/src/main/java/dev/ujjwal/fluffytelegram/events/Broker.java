package dev.ujjwal.fluffytelegram.events;

public class Broker {

    private final String BROKER_URL;

    public Broker(String BROKER_URL) {
        this.BROKER_URL = BROKER_URL;
    }

    public String getBROKER_URL() {
        return BROKER_URL;
    }
}
