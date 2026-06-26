package com.tuservidor.vanillaplus.set;

import java.util.UUID;

public class PlayerSetState {
    public final UUID playerId;
    public String armorSetId;
    public String swordSetId;
    public String rangedSetId;
    public String amuletSetId;
    public String tier;
    public long lastUpdated;

    public PlayerSetState(UUID playerId) {
        this.playerId = playerId;
        this.lastUpdated = System.currentTimeMillis();
    }
}
