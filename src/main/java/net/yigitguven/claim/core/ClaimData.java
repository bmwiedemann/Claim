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
}
