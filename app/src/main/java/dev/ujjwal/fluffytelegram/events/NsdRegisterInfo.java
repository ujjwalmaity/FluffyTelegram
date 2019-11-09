package dev.ujjwal.fluffytelegram.events;

public class NsdRegisterInfo {

    private final String name;

    public NsdRegisterInfo(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
