package net.yigitguven.claim.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.yigitguven.claim.Claim;

public record RequestClaimPayload(BlockPos pos1, BlockPos pos2) implements CustomPacketPayload {
    public static final Type<RequestClaimPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Claim.MODID, "request_claim"));

    public static final StreamCodec<FriendlyByteBuf, RequestClaimPayload> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> {
                buf.writeBlockPos(payload.pos1);
                buf.writeBlockPos(payload.pos2);
            },
            (buf) -> new RequestClaimPayload(buf.readBlockPos(), buf.readBlockPos())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
