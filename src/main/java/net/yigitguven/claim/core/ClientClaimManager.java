package net.yigitguven.claim.core;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ClientClaimManager
{
    private static List<ClaimData> claims = new ArrayList<>();
    private static int availableBlocks = 0;

    public static void setClaims(List<ClaimData> newClaims)
    {
        claims = new ArrayList<>(newClaims);
    }

    public static List<ClaimData> getClaims()
    {
        return claims;
    }

    public static void setAvailableBlocks(int blocks) {
        availableBlocks = blocks;
    }

    public static int getAvailableBlocks() {
        return availableBlocks;
    }

    public static void updateClaimOptimistically(int id, String name, String desc, ClaimData.PermissionMode mode, int color, List<UUID> trusted)
    {
        for (ClaimData claim : claims)
        {
            if (claim.claimId == id)
            {
                claim.displayName = name;
                claim.description = desc;
                claim.permissionMode = mode;
                claim.color = color;
                claim.trustedPlayers = new ArrayList<>(trusted);
                break;
            }
        }
    }
}
