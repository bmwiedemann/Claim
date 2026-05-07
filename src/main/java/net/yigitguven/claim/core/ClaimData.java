package net.yigitguven.claim.core;

import net.minecraft.core.BlockPos;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ClaimData
{
    public final int claimId;
    public String displayName;
    public final UUID ownerUUID;
    public final BlockPos pos1;
    public final BlockPos pos2;
    
    // New data fields
    public List<UUID> trustedPlayers;
    public PermissionMode permissionMode;
    public long createdAt;
    public int color;
    public String description;

    public enum PermissionMode {
        PUBLIC, PRIVATE
    }

    public ClaimData(int claimId, String displayName, UUID ownerUUID, BlockPos pos1, BlockPos pos2)
    {
        this(claimId, displayName, ownerUUID, pos1, pos2, new ArrayList<>(), PermissionMode.PRIVATE, System.currentTimeMillis(), 0xFF55FF7D, "");
    }

    public ClaimData(int claimId, String displayName, UUID ownerUUID, BlockPos pos1, BlockPos pos2, 
                    List<UUID> trustedPlayers, PermissionMode permissionMode, long createdAt, int color, String description)
    {
        this.claimId = claimId;
        this.displayName = displayName;
        this.ownerUUID = ownerUUID;
        this.pos1 = pos1;
        this.pos2 = pos2;
        this.trustedPlayers = trustedPlayers;
        this.permissionMode = permissionMode;
        this.createdAt = createdAt;
        this.color = color;
        this.description = description;
    }

    public long getBlockCount()
    {
        long x = Math.abs(pos1.getX() - pos2.getX()) + 1;
        long y = Math.abs(pos1.getY() - pos2.getY()) + 1;
        long z = Math.abs(pos1.getZ() - pos2.getZ()) + 1;
        return x * y * z;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClaimData that = (ClaimData) o;
        return claimId == that.claimId &&
                java.util.Objects.equals(displayName, that.displayName) &&
                java.util.Objects.equals(ownerUUID, that.ownerUUID) &&
                java.util.Objects.equals(pos1, that.pos1) &&
                java.util.Objects.equals(pos2, that.pos2);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(claimId, displayName, ownerUUID, pos1, pos2);
    }
}
