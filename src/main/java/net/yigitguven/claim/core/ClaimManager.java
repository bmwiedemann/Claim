package net.yigitguven.claim.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.yigitguven.claim.Claim;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ClaimManager
{
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final List<ClaimData> claims = new ArrayList<>();
    private static int nextClaimId = 0;

    public static boolean addClaim(ServerLevel level, String displayName, UUID ownerUUID, BlockPos pos1, BlockPos pos2)
    {
        BlockPos[] normalized = getNormalizedPositions(level, pos1, pos2);
        pos1 = normalized[0];
        pos2 = normalized[1];

        if (isAreaClaimed(pos1, pos2))
        {
            return false;
        }

        String ownerName = "Unknown";
        ServerPlayer player = level.getServer().getPlayerList().getPlayer(ownerUUID);
        if (player != null) ownerName = player.getScoreboardName();

        String name = net.yigitguven.claim.config.ModConfig.DEFAULT_CLAIM_NAME.get().replace("{player}", ownerName);
        String desc = net.yigitguven.claim.config.ModConfig.DEFAULT_CLAIM_DESCRIPTION.get();
        ClaimData.PermissionMode mode = net.yigitguven.claim.config.ModConfig.DEFAULT_CLAIM_PERMISSIONS.get();
        int color = net.yigitguven.claim.config.ModConfig.DEFAULT_CLAIM_COLOR.get();

        claims.add(new ClaimData(nextClaimId++, name, ownerUUID, pos1, pos2, new ArrayList<>(), mode, System.currentTimeMillis(), color, desc));
        save(level);
        
        // Sync to all players
        for (ServerPlayer player : level.getServer().getPlayerList().getPlayers())
        {
            Claim.syncClaims(player);
        }
        return true;
    }

    public static boolean isAreaClaimed(BlockPos pos1, BlockPos pos2)
    {
        for (ClaimData claim : claims)
        {
            if (intersects(pos1, pos2, claim.pos1, claim.pos2))
            {
                return true;
            }
        }
        return false;
    }

    private static boolean intersects(BlockPos a1, BlockPos a2, BlockPos b1, BlockPos b2)
    {
        return Math.min(a1.getX(), a2.getX()) <= Math.max(b1.getX(), b2.getX()) &&
               Math.max(a1.getX(), a2.getX()) >= Math.min(b1.getX(), b2.getX()) &&
               Math.min(a1.getY(), a2.getY()) <= Math.max(b1.getY(), b2.getY()) &&
               Math.max(a1.getY(), a2.getY()) >= Math.min(b1.getY(), b2.getY()) &&
               Math.min(a1.getZ(), a2.getZ()) <= Math.max(b1.getZ(), b2.getZ()) &&
               Math.max(a1.getZ(), a2.getZ()) >= Math.min(b1.getZ(), b2.getZ());
    }

    public static long calculateVolume(BlockPos p1, BlockPos p2)
    {
        long x = Math.abs(p1.getX() - p2.getX()) + 1;
        long y = Math.abs(p1.getY() - p2.getY()) + 1;
        long z = Math.abs(p1.getZ() - p2.getZ()) + 1;
        return x * y * z;
    }

    public static long calculateVolume(ServerLevel level, BlockPos p1, BlockPos p2)
    {
        BlockPos[] normalized = getNormalizedPositions(level, p1, p2);
        return calculateVolume(normalized[0], normalized[1]);
    }

    public static BlockPos[] getNormalizedPositions(ServerLevel level, BlockPos p1, BlockPos p2)
    {
        if (net.yigitguven.claim.config.ModConfig.CLAIM_ALL_Y.get())
        {
            return new BlockPos[]{
                new BlockPos(p1.getX(), level.getMinBuildHeight(), p1.getZ()),
                new BlockPos(p2.getX(), level.getMaxBuildHeight(), p2.getZ())
            };
        }
        return new BlockPos[]{p1, p2};
    }

    public static void removeClaims(ServerLevel level, List<Integer> claimIds, UUID playerUUID)
    {
        claims.removeIf(claim -> claimIds.contains(claim.claimId) && claim.ownerUUID.equals(playerUUID));
        save(level);
        for (ServerPlayer player : level.getServer().getPlayerList().getPlayers())
        {
            Claim.syncClaims(player);
        }
    }

    public static boolean removeClaim(ServerLevel level, int claimId)
    {
        boolean removed = claims.removeIf(claim -> claim.claimId == claimId);
        if (removed)
        {
            save(level);
            for (ServerPlayer player : level.getServer().getPlayerList().getPlayers())
            {
                Claim.syncClaims(player);
            }
        }
        return removed;
    }

    public static void clearPlayerClaims(ServerLevel level, UUID playerUUID)
    {
        claims.removeIf(claim -> claim.ownerUUID.equals(playerUUID));
        save(level);
        for (ServerPlayer player : level.getServer().getPlayerList().getPlayers())
        {
            Claim.syncClaims(player);
        }
    }

    public static ClaimData getClaimById(int claimId)
    {
        for (ClaimData claim : claims)
        {
            if (claim.claimId == claimId)
            {
                return claim;
            }
        }
        return null;
    }

    public static boolean setVisitPos(ServerLevel level, int claimId, UUID playerUUID, BlockPos visitPos, boolean bypass)
    {
        for (ClaimData claim : claims)
        {
            if (claim.claimId == claimId)
            {
                if (!bypass && !claim.ownerUUID.equals(playerUUID))
                {
                    return false;
                }

                claim.visitPos = visitPos;
                save(level);
                for (ServerPlayer player : level.getServer().getPlayerList().getPlayers())
                {
                    Claim.syncClaims(player);
                }
                return true;
            }
        }
        return false;
    }

    public static BlockPos getVisitPos(ServerLevel level, ClaimData claim)
    {
        if (claim.visitPos != null && isInside(claim.visitPos, claim.pos1, claim.pos2) && isSafeStandingPos(level, claim.visitPos))
        {
            return claim.visitPos;
        }
        return findSafeClaimCenterPos(level, claim);
    }

    public static boolean hasRequiredLandPermit(ServerPlayer player)
    {
        if (!net.yigitguven.claim.config.ModConfig.REQUIRE_LAND_PERMIT.get())
        {
            return true;
        }

        Item permitItem = resolvePermitItem();
        if (permitItem == null)
        {
            // Do not block claiming if the configured id is invalid; fail open with warning.
            return true;
        }

        int required = net.yigitguven.claim.config.ModConfig.LAND_PERMIT_AMOUNT.get();
        int total = 0;
        for (ItemStack stack : player.getInventory().items)
        {
            if (stack.getItem() == permitItem)
            {
                total += stack.getCount();
                if (total >= required)
                {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean consumeLandPermit(ServerPlayer player)
    {
        if (!net.yigitguven.claim.config.ModConfig.REQUIRE_LAND_PERMIT.get() || !net.yigitguven.claim.config.ModConfig.CONSUME_LAND_PERMIT_ON_USE.get())
        {
            return true;
        }

        Item permitItem = resolvePermitItem();
        if (permitItem == null)
        {
            return true;
        }

        int required = net.yigitguven.claim.config.ModConfig.LAND_PERMIT_AMOUNT.get();
        int remaining = required;
        for (ItemStack stack : player.getInventory().items)
        {
            if (stack.getItem() != permitItem)
            {
                continue;
            }

            int toTake = Math.min(stack.getCount(), remaining);
            stack.shrink(toTake);
            remaining -= toTake;
            if (remaining <= 0)
            {
                return true;
            }
        }

        LOGGER.warn("Failed to consume enough permit items for player {}. Required={}, Remaining={}", player.getScoreboardName(), required, remaining);
        return false;
    }

    private static Item resolvePermitItem()
    {
        String itemId = net.yigitguven.claim.config.ModConfig.LAND_PERMIT_ITEM.get();
        ResourceLocation key = ResourceLocation.tryParse(itemId);
        if (key == null)
        {
            LOGGER.warn("Invalid land permit item id in config: {}", itemId);
            return null;
        }
        if (!BuiltInRegistries.ITEM.containsKey(key))
        {
            LOGGER.warn("Configured land permit item does not exist: {}", itemId);
            return null;
        }
        return BuiltInRegistries.ITEM.get(key);
    }

    private static BlockPos findSafeClaimCenterPos(ServerLevel level, ClaimData claim)
    {
        int minX = Math.min(claim.pos1.getX(), claim.pos2.getX());
        int maxX = Math.max(claim.pos1.getX(), claim.pos2.getX());
        int minZ = Math.min(claim.pos1.getZ(), claim.pos2.getZ());
        int maxZ = Math.max(claim.pos1.getZ(), claim.pos2.getZ());
        int centerX = (minX + maxX) / 2;
        int centerZ = (minZ + maxZ) / 2;

        int surfaceY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, centerX, centerZ) + 1;
        int minY = level.getMinBuildHeight() + 1;
        int maxY = level.getMaxBuildHeight() - 2;

        int startY = Math.max(minY, Math.min(maxY, surfaceY));

        for (int y = startY; y <= maxY; y++)
        {
            BlockPos candidate = new BlockPos(centerX, y, centerZ);
            if (isInside(candidate, claim.pos1, claim.pos2) && isSafeStandingPos(level, candidate))
            {
                return candidate;
            }
        }

        for (int y = startY - 1; y >= minY; y--)
        {
            BlockPos candidate = new BlockPos(centerX, y, centerZ);
            if (isInside(candidate, claim.pos1, claim.pos2) && isSafeStandingPos(level, candidate))
            {
                return candidate;
            }
        }

        return new BlockPos(centerX, Math.max(minY, startY), centerZ);
    }

    private static boolean isSafeStandingPos(ServerLevel level, BlockPos feetPos)
    {
        int minY = level.getMinBuildHeight() + 1;
        int maxY = level.getMaxBuildHeight() - 2;
        if (feetPos.getY() < minY || feetPos.getY() > maxY)
        {
            return false;
        }

        BlockPos headPos = feetPos.above();
        BlockPos belowPos = feetPos.below();

        BlockState feet = level.getBlockState(feetPos);
        BlockState head = level.getBlockState(headPos);
        BlockState below = level.getBlockState(belowPos);

        if (!feet.getCollisionShape(level, feetPos).isEmpty() || !head.getCollisionShape(level, headPos).isEmpty())
        {
            return false;
        }

        if (below.isAir() || !below.blocksMotion())
        {
            return false;
        }

        return !level.getFluidState(feetPos).is(FluidTags.LAVA)
                && !level.getFluidState(headPos).is(FluidTags.LAVA)
                && !level.getFluidState(belowPos).is(FluidTags.LAVA);
    }

    public static void updateClaimMetadata(ServerLevel level, int claimId, String newName, String description, ClaimData.PermissionMode mode, int color, List<UUID> trustedPlayers, UUID ownerUUID)
    {
        for (ClaimData claim : claims)
        {
            if (claim.claimId == claimId && claim.ownerUUID.equals(ownerUUID))
            {
                claim.displayName = newName;
                claim.description = description;
                claim.permissionMode = mode;
                claim.color = color;
                claim.trustedPlayers = new ArrayList<>(trustedPlayers);
                
                save(level);
                for (ServerPlayer player : level.getServer().getPlayerList().getPlayers())
                {
                    Claim.syncClaims(player);
                }
                break;
            }
        }
    }

    public static ClaimData getClaimAt(BlockPos pos)
    {
        for (ClaimData claim : claims)
        {
            if (isInside(pos, claim.pos1, claim.pos2))
            {
                return claim;
            }
        }
        return null;
    }

    private static boolean isInside(BlockPos pos, BlockPos p1, BlockPos p2)
    {
        return pos.getX() >= Math.min(p1.getX(), p2.getX()) && pos.getX() <= Math.max(p1.getX(), p2.getX()) &&
               pos.getY() >= Math.min(p1.getY(), p2.getY()) && pos.getY() <= Math.max(p1.getY(), p2.getY()) &&
               pos.getZ() >= Math.min(p1.getZ(), p2.getZ()) && pos.getZ() <= Math.max(p1.getZ(), p2.getZ());
    }

    public static List<ClaimData> getClaims()
    {
        return new ArrayList<>(claims);
    }

    public static int getClaimCount(UUID playerUUID)
    {
        return (int) claims.stream().filter(claim -> claim.ownerUUID.equals(playerUUID)).count();
    }

    public static long getTotalClaimedBlocks(UUID playerUUID)
    {
        return claims.stream()
                .filter(claim -> claim.ownerUUID.equals(playerUUID))
                .mapToLong(ClaimData::getBlockCount)
                .sum();
    }

    public static void load(ServerLevel level)
    {
        File file = getSaveFile(level);
        if (!file.exists()) return;

        try (FileReader reader = new FileReader(file)) {
            Type listType = new TypeToken<ArrayList<ClaimData>>() {}.getType();
            List<ClaimData> loadedClaims = GSON.fromJson(reader, listType);
            if (loadedClaims != null) {
                claims.clear();
                claims.addAll(loadedClaims);
                nextClaimId = claims.stream().mapToInt(c -> c.claimId).max().orElse(-1) + 1;
            }
        } catch (IOException e) {
            LOGGER.error("Failed to load claims", e);
        }
    }

    public static void save(ServerLevel level)
    {
        File file = getSaveFile(level);
        file.getParentFile().mkdirs();

        try (FileWriter writer = new FileWriter(file)) {
            GSON.toJson(claims, writer);
        } catch (IOException e) {
            LOGGER.error("Failed to save claims", e);
        }
    }

    private static File getSaveFile(ServerLevel level)
    {
        return level.getServer().getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT).resolve("data/claim/claims.json").toFile();
    }
}
