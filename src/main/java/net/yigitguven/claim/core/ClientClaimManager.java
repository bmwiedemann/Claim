package net.yigitguven.claim.core;

import java.util.ArrayList;
import java.util.List;

public class ClientClaimManager
{
    private static List<ClaimData> claims = new ArrayList<>();

    public static void setClaims(List<ClaimData> newClaims)
    {
        claims = new ArrayList<>(newClaims);
    }

    public static List<ClaimData> getClaims()
    {
        return claims;
    }
}
