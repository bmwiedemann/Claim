package net.yigitguven.claim.integration;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.yigitguven.claim.core.ClaimData;
import net.yigitguven.claim.core.ClientClaimManager;
import xaero.map.highlight.ChunkHighlighter;

import java.util.List;
import java.util.UUID;

public class XaeroMapIntegration extends ChunkHighlighter
{
    private final int[] resultStore = new int[5];

    public XaeroMapIntegration()
    {
        super(true);
    }

    @Override
    public boolean chunkIsHighlit(ResourceKey<Level> dimension, int chunkX, int chunkZ)
    {
        for (ClaimData claim : ClientClaimManager.getClaims())
        {
            int minX = Math.min(claim.pos1.getX(), claim.pos2.getX()) >> 4;
            int minZ = Math.min(claim.pos1.getZ(), claim.pos2.getZ()) >> 4;
            int maxX = Math.max(claim.pos1.getX(), claim.pos2.getX()) >> 4;
            int maxZ = Math.max(claim.pos1.getZ(), claim.pos2.getZ()) >> 4;

            if (chunkX >= minX && chunkX <= maxX && chunkZ >= minZ && chunkZ <= maxZ)
            {
                System.out.println("[Claim] chunkIsHighlit true for " + chunkX + ", " + chunkZ);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean regionHasHighlights(ResourceKey<Level> dimension, int regionX, int regionZ)
    {
        return true;
    }

    private static int refreshCounter = 0;

    @Override
    public int calculateRegionHash(ResourceKey<Level> dimension, int regionX, int regionZ)
    {
        return refreshCounter;
    }

    public static void refresh()
    {
        refreshCounter++;
        System.out.println("[Claim] Requesting Xaero Map refresh... Counter: " + refreshCounter);
        try
        {
            if (xaero.map.WorldMap.settings != null)
            {
                xaero.map.WorldMap.settings.updateRegionCacheHashCode();
            }
            
            xaero.map.WorldMapSession session = xaero.map.WorldMapSession.getCurrentSession();
            if (session != null && session.getMapProcessor() != null)
            {
                xaero.map.MapProcessor processor = session.getMapProcessor();
                for (ClaimData claim : ClientClaimManager.getClaims())
                {
                    int regionX = Math.min(claim.pos1.getX(), claim.pos2.getX()) >> 9;
                    int regionZ = Math.min(claim.pos1.getZ(), claim.pos2.getZ()) >> 9;
                    int maxRegionX = Math.max(claim.pos1.getX(), claim.pos2.getX()) >> 9;
                    int maxRegionZ = Math.max(claim.pos1.getZ(), claim.pos2.getZ()) >> 9;
                    
                    for (int rx = regionX; rx <= maxRegionX; rx++)
                    {
                        for (int rz = regionZ; rz <= maxRegionZ; rz++)
                        {
                            xaero.map.region.MapRegion region = processor.getLeafMapRegion(rx, rz, processor.getCurrentCaveLayer(), false);
                            if (region != null)
                            {
                                region.requestRefresh(processor);
                            }
                        }
                    }
                }
            }
            
            // If the map GUI is open, toggle claims display to force an immediate redraw
            if (net.yigitguven.claim.integration.XaeroMapState.CURRENT_GUI != null)
            {
                net.yigitguven.claim.integration.XaeroMapState.CURRENT_GUI.onClaimsButton(null);
                net.yigitguven.claim.integration.XaeroMapState.CURRENT_GUI.onClaimsButton(null);
            }
        }
        catch (Throwable e)
        {
            System.err.println("[Claim] Failed to refresh Xaero Map: " + e.getMessage());
        }
    }

    @Override
    protected int[] getColors(ResourceKey<Level> dimension, int chunkX, int chunkZ)
    {
        System.out.println("[Claim] getColors for " + chunkX + ", " + chunkZ);

        ClaimData foundClaim = null;
        for (ClaimData claim : ClientClaimManager.getClaims())
        {
            int minX = Math.min(claim.pos1.getX(), claim.pos2.getX()) >> 4;
            int minZ = Math.min(claim.pos1.getZ(), claim.pos2.getZ()) >> 4;
            int maxX = Math.max(claim.pos1.getX(), claim.pos2.getX()) >> 4;
            int maxZ = Math.max(claim.pos1.getZ(), claim.pos2.getZ()) >> 4;

            if (chunkX >= minX && chunkX <= maxX && chunkZ >= minZ && chunkZ <= maxZ)
            {
                foundClaim = claim;
                break;
            }
        }

        if (foundClaim == null) return null;

        // Use the color that was working
        int color = 1442796919; // Green (0x55FF7D77)

        // Try to add red for others
        try {
            UUID playerUUID = Minecraft.getInstance().player != null ? Minecraft.getInstance().player.getUUID() : null;
            if (playerUUID != null && !playerUUID.equals(foundClaim.ownerUUID)) {
                // Red color with same alpha (0x55)
                color = 0x55FF0000;
            }
        } catch (Exception e) {}

        for (int i = 0; i < 5; i++)
        {
            resultStore[i] = color;
        }
        
        return resultStore;
    }

    @Override
    public Component getChunkHighlightSubtleTooltip(ResourceKey<Level> dimension, int chunkX, int chunkZ)
    {
        return null;
    }

    @Override
    public Component getChunkHighlightBluntTooltip(ResourceKey<Level> dimension, int chunkX, int chunkZ)
    {
        for (ClaimData claim : ClientClaimManager.getClaims())
        {
            int minX = Math.min(claim.pos1.getX(), claim.pos2.getX()) >> 4;
            int minZ = Math.min(claim.pos1.getZ(), claim.pos2.getZ()) >> 4;
            int maxX = Math.max(claim.pos1.getX(), claim.pos2.getX()) >> 4;
            int maxZ = Math.max(claim.pos1.getZ(), claim.pos2.getZ()) >> 4;

            if (chunkX >= minX && chunkX <= maxX && chunkZ >= minZ && chunkZ <= maxZ)
            {
                return Component.literal("Claim: " + claim.displayName);
            }
        }
        return null;
    }

    @Override
    public void addMinimapBlockHighlightTooltips(List<Component> tooltips, ResourceKey<Level> dimension, int x, int y, int z)
    {
        Component tooltip = getChunkHighlightBluntTooltip(dimension, x >> 4, z >> 4);
        if (tooltip != null)
        {
            tooltips.add(tooltip);
        }
    }
}
