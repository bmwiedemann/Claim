package net.yigitguven.claim.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.yigitguven.claim.Claim;

import java.util.ArrayList;
import java.util.List;

public record RequestUnclaimPayload(List<Integer> claimIds) implements CustomPacketPayload
{
    public static final Type<RequestUnclaimPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Claim.MODID, "request_unclaim"));

    public static final StreamCodec<FriendlyByteBuf, RequestUnclaimPayload> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> {
                buf.writeInt(payload.claimIds().size());
                for (int id : payload.claimIds()) buf.writeInt(id);
            },
            buf -> {
                int size = buf.readInt();
                List<Integer> ids = new ArrayList<>(size);
                for (int i = 0; i < size; i++) ids.add(buf.readInt());
                return new RequestUnclaimPayload(ids);
            }
    );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
