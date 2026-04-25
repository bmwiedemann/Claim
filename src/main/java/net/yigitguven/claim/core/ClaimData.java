package net.yigitguven.claim.core;

import net.minecraft.core.BlockPos;
import java.util.UUID;

public class ClaimData
{
    public final int claimId;
    public final String displayName;
    public final UUID ownerUUID;
    public final BlockPos pos1;
    public final BlockPos pos2;

    public ClaimData(int claimId, String displayName, UUID ownerUUID, BlockPos pos1, BlockPos pos2)
    {
        this.claimId = claimId;
        this.displayName = displayName;
        this.ownerUUID = ownerUUID;
        this.pos1 = pos1;
        this.pos2 = pos2;
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
