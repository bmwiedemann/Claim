package net.yigitguven.claim.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.yigitguven.claim.Claim;
import net.yigitguven.claim.core.ClaimData;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record UpdateClaimMetadataPayload(int claimId, String name, String description, ClaimData.PermissionMode mode, int color, List<UUID> trustedPlayers) implements CustomPacketPayload
{
    public static final Type<UpdateClaimMetadataPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Claim.MODID, "update_claim_metadata"));

    public static final StreamCodec<RegistryFriendlyByteBuf, UpdateClaimMetadataPayload> STREAM_CODEC = StreamCodec.of(
            (buf, data) -> {
                buf.writeInt(data.claimId);
                buf.writeUtf(data.name);
                buf.writeUtf(data.description);
                buf.writeEnum(data.mode);
                buf.writeInt(data.color);
                buf.writeInt(data.trustedPlayers.size());
                for (UUID uuid : data.trustedPlayers) {
                    buf.writeUUID(uuid);
                }
            },
            (buf) -> {
                int id = buf.readInt();
                String name = buf.readUtf();
                String desc = buf.readUtf();
                ClaimData.PermissionMode mode = buf.readEnum(ClaimData.PermissionMode.class);
                int color = buf.readInt();
                int size = buf.readInt();
                List<UUID> trusted = new ArrayList<>(size);
                for (int i = 0; i < size; i++) {
                    trusted.add(buf.readUUID());
                }
                return new UpdateClaimMetadataPayload(id, name, desc, mode, color, trusted);
            }
    );

    @Override
    public Type<? extends CustomPacketPayload> type()
    {
        return TYPE;
    }
}
