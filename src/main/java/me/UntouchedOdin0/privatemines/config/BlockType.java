package me.UntouchedOdin0.privatemines.config;

public enum BlockType {
    SPAWNPOINT("Spawnpoint"),
    CORNER("Mine-Corner"),
    NPC("Sell-NPC");

    private final String name;

    BlockType(String name) {
        this.name = name;
    }

    public static BlockType fromName(String name) {
        for (BlockType value : values()) {
            if (value.name.equals(name)) {
                return value;
            }
        }

        return null;
    }

    public String getName() {
        return this.name;
    }
}
