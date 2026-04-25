package net.yigitguven.claim.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.yigitguven.claim.Claim;

public record OpenClaimListPayload() implements CustomPacketPayload
{
    public static final Type<OpenClaimListPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Claim.MODID, "open_claim_list"));

    public static final StreamCodec<FriendlyByteBuf, OpenClaimListPayload> CODEC = StreamCodec.unit(new OpenClaimListPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
