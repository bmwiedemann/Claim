package net.yigitguven.claim.core;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Represents the data associated with a claimed chunk.
 */
public class ClaimData {
    private final UUID ownerUUID;
    private final String ownerName;
    private final Set<UUID> trustedPlayers;

    public ClaimData(UUID ownerUUID, String ownerName) {
        this.ownerUUID = ownerUUID;
        this.ownerName = ownerName;
        this.trustedPlayers = new HashSet<>();
    }

    public UUID getOwnerUUID() {
        return ownerUUID;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public Set<UUID> getTrustedPlayers() {
        return trustedPlayers;
    }

    public boolean isTrusted(UUID playerUUID) {
        return ownerUUID.equals(playerUUID) || trustedPlayers.contains(playerUUID);
    }

    public void trust(UUID playerUUID) {
        trustedPlayers.add(playerUUID);
    }

    public void untrust(UUID playerUUID) {
        trustedPlayers.remove(playerUUID);
    }
}
