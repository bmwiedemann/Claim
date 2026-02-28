package net.yigitguven.claim.core;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Represents the data associated with a claimed chunk.
 */
public class ClaimData {
    public enum PermissionLevel {
        BUILD,
        INTERACT,
        CONTAINERS,
        MANAGE
    }

    private final UUID ownerUUID;
    private final String ownerName;
    private final Map<UUID, Set<PermissionLevel>> trustedPlayers;
    private String name;

    public ClaimData(UUID ownerUUID, String ownerName) {
        this.ownerUUID = ownerUUID;
        this.ownerName = ownerName;
        this.trustedPlayers = new HashMap<>();
        this.name = null;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public UUID getOwnerUUID() {
        return ownerUUID;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public boolean isTrusted(UUID playerUUID, PermissionLevel level) {
        if (ownerUUID.equals(playerUUID)) return true;
        Set<PermissionLevel> levels = trustedPlayers.get(playerUUID);
        return levels != null && (levels.contains(level) || levels.contains(PermissionLevel.MANAGE));
    }

    public void trust(UUID playerUUID, PermissionLevel level) {
        trustedPlayers.computeIfAbsent(playerUUID, k -> new HashSet<>()).add(level);
    }

    public void untrust(UUID playerUUID) {
        trustedPlayers.remove(playerUUID);
    }
    
    public Map<UUID, Set<PermissionLevel>> getTrustedPlayers() {
        return trustedPlayers;
    }
}
