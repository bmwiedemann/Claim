package net.yigitguven.claim.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.yigitguven.claim.Claim;

import java.util.UUID;

public record RenameClaimPayload(int claimId, String newName) implements CustomPacketPayload
{
    public static final Type<RenameClaimPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Claim.MODID, "rename_claim"));

    public static final StreamCodec<FriendlyByteBuf, RenameClaimPayload> CODEC = StreamCodec.composite(
            net.minecraft.network.codec.ByteBufCodecs.VAR_INT, RenameClaimPayload::claimId,
            net.minecraft.network.codec.ByteBufCodecs.STRING_UTF8, RenameClaimPayload::newName,
            RenameClaimPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
