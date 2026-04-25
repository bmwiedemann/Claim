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
                },
                (buf) -> new ClaimData(
                        buf.readInt(),
                        buf.readUtf(),
                        buf.readUUID(),
                        buf.readBlockPos(),
                        buf.readBlockPos()
                )
        );
    }
}
