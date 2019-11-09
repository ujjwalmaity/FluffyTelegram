package dev.ujjwal.fluffytelegram.events;

public class MqttBrokerInfo {
    private final boolean BROKER_STARTED;

    public MqttBrokerInfo(boolean BROKER_STARTED) {
        this.BROKER_STARTED = BROKER_STARTED;
    }

    public boolean isBROKER_STARTED() {
        return BROKER_STARTED;
    }
}
