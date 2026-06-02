package net.yigitguven.claim.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.yigitguven.claim.Claim;
import net.yigitguven.claim.core.ClaimData;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record ClaimSyncPayload(List<ClaimData> claims) implements CustomPacketPayload
{
    public static final Type<ClaimSyncPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Claim.MODID, "claim_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ClaimSyncPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.collection(ArrayList::new, ClaimDataCodec.STREAM_CODEC), ClaimSyncPayload::claims,
            ClaimSyncPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type()
    {
        return TYPE;
    }

    private static class ClaimDataCodec
    {
        public static final StreamCodec<RegistryFriendlyByteBuf, ClaimData> STREAM_CODEC = StreamCodec.of(
                (buf, data) -> {
                    buf.writeInt(data.claimId);
                    buf.writeUtf(data.displayName);
                    buf.writeUUID(data.ownerUUID);
                    buf.writeBlockPos(data.pos1);
                    buf.writeBlockPos(data.pos2);
                    
                    // Trusted players
                    buf.writeInt(data.trustedPlayers.size());
                    for (UUID uuid : data.trustedPlayers) {
                        buf.writeUUID(uuid);
                    }
                    
                    // Permission mode
                    buf.writeEnum(data.permissionMode);
                    
                    // Other metadata
                    buf.writeLong(data.createdAt);
                    buf.writeInt(data.color);
                    buf.writeUtf(data.description);
                    buf.writeBoolean(data.visitPos != null);
                    if (data.visitPos != null)
                    {
                        buf.writeBlockPos(data.visitPos);
                    }
                },
                (buf) -> {
                    int id = buf.readInt();
                    String name = buf.readUtf();
                    UUID owner = buf.readUUID();
                    BlockPos p1 = buf.readBlockPos();
                    BlockPos p2 = buf.readBlockPos();
                    
                    int trustSize = buf.readInt();
                    List<UUID> trusted = new ArrayList<>(trustSize);
                    for (int i = 0; i < trustSize; i++) {
                        trusted.add(buf.readUUID());
                    }
                    
                    ClaimData.PermissionMode mode = buf.readEnum(ClaimData.PermissionMode.class);
                    long created = buf.readLong();
                    int color = buf.readInt();
                    String desc = buf.readUtf();
                    BlockPos visitPos = null;
                    if (buf.readBoolean())
                    {
                        visitPos = buf.readBlockPos();
                    }
                    
                    return new ClaimData(id, name, owner, p1, p2, trusted, mode, created, color, desc, visitPos);
                }
        );
    }
}
