package net.yigitguven.claim.client;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.yigitguven.claim.core.ClaimData;
import org.joml.Quaternionf;

public class ClaimRenderHelper {
    public static final ResourceLocation PLACEHOLDER = ResourceLocation.fromNamespaceAndPath("claim", "textures/gui/placeholder.png");

    public static void renderClaimPreview(GuiGraphics guiGraphics, ClaimData claim, int x, int y, float rotation, float scale, boolean isFullSize) {
        Minecraft minecraft = Minecraft.getInstance();
        if (!areChunksLoaded(claim)) {
            renderPlaceholder(guiGraphics, x, y, scale);
            return;
        }

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(x, y, 300);
        Lighting.setupFor3DItems();
        
        renderProjector(guiGraphics, claim, rotation, scale, isFullSize);

        Lighting.setupForFlatItems();
        guiGraphics.pose().popPose();
    }

    private static void renderPlaceholder(GuiGraphics guiGraphics, int x, int y, float scale) {
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0, 0, 10);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        Lighting.setupForFlatItems();
        
        int s = (int)scale * 2;
        int startX = x - s/2;
        int startY = y - s/2;
        float f = s / 128.0f;
        int bCol = 0xFFFFFFFF;
        float margin = 1.0f;

        // Draw white backing rects
        guiGraphics.fill((int)(startX + (16+margin)*f), (int)(startY + (0+margin)*f), (int)(startX + (63-margin)*f), (int)(startY + (71-margin)*f), bCol);
        guiGraphics.fill((int)(startX + (16+margin)*f), (int)(startY + (72+margin)*f), (int)(startX + (39-margin)*f), (int)(startY + (127-margin)*f), bCol);
        guiGraphics.fill((int)(startX + (64+margin)*f), (int)(startY + (8+margin)*f), (int)(startX + (71-margin)*f), (int)(startY + (87-margin)*f), bCol);
        guiGraphics.fill((int)(startX + (72+margin)*f), (int)(startY + (16+margin)*f), (int)(startX + (111-margin)*f), (int)(startY + (39-margin)*f), bCol);
        guiGraphics.fill((int)(startX + (72+margin)*f), (int)(startY + (40+margin)*f), (int)(startX + (103-margin)*f), (int)(startY + (47-margin)*f), bCol);
        guiGraphics.fill((int)(startX + (72+margin)*f), (int)(startY + (48+margin)*f), (int)(startX + (95-margin)*f), (int)(startY + (63-margin)*f), bCol);
        guiGraphics.fill((int)(startX + (72+margin)*f), (int)(startY + (64+margin)*f), (int)(startX + (103-margin)*f), (int)(startY + (71-margin)*f), bCol);
        guiGraphics.fill((int)(startX + (72+margin)*f), (int)(startY + (73+margin)*f), (int)(startX + (111-margin)*f), (int)(startY + (87-margin)*f), bCol);

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        guiGraphics.blit(PLACEHOLDER, startX, startY, 0, 0, s, s, s, s);
        
        guiGraphics.pose().popPose();
    }

    private static void renderProjector(GuiGraphics guiGraphics, ClaimData claim, float rot, float scale, boolean isFullSize) {
        Minecraft minecraft = Minecraft.getInstance();
        net.minecraft.client.multiplayer.ClientLevel level = minecraft.level;
        if (level == null) return;

        int minX = Math.min(claim.pos1.getX(), claim.pos2.getX());
        int maxX = Math.max(claim.pos1.getX(), claim.pos2.getX());
        int minZ = Math.min(claim.pos1.getZ(), claim.pos2.getZ());
        int maxZ = Math.max(claim.pos1.getZ(), claim.pos2.getZ());
        int minY = Math.min(claim.pos1.getY(), claim.pos2.getY());
        int maxY = Math.max(claim.pos1.getY(), claim.pos2.getY());

        int sizeX = maxX - minX + 1;
        int sizeZ = maxZ - minZ + 1;

        int globalMaxH = minY;
        int sampleStep = Math.max(1, Math.max(sizeX, sizeZ) / 16);
        for (int i = 0; i < sizeX; i += sampleStep) {
            for (int j = 0; j < sizeZ; j += sampleStep) {
                for (int h = maxY; h >= minY; h--) {
                    if (!level.getBlockState(new BlockPos(minX + i, h, minZ + j)).isAir()) {
                        if (h > globalMaxH) globalMaxH = h;
                        break;
                    }
                }
            }
        }

        float diag = (float) Math.sqrt(sizeX * sizeX + sizeZ * sizeZ + 16 * 16);
        float fitScale = (scale * 1.75f) / Math.max(diag, 12f);

        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(fitScale, -fitScale, fitScale);
        
        Quaternionf quaternion = new Quaternionf()
                .rotateX((float) Math.toRadians(25))
                .rotateY((float) Math.toRadians(rot));
        guiGraphics.pose().mulPose(quaternion);
        
        guiGraphics.pose().translate(-sizeX / 2.0f, 0, -sizeZ / 2.0f);

        BlockRenderDispatcher dispatcher = minecraft.getBlockRenderer();
        MultiBufferSource.BufferSource bufferSource = minecraft.renderBuffers().bufferSource();

        int step = Math.max(1, Math.max(sizeX, sizeZ) / (isFullSize ? 64 : 40));
        
        for (int i = 0; i < sizeX; i += step) {
            for (int j = 0; j < sizeZ; j += step) {
                int worldX = minX + i;
                int worldZ = minZ + j;
                for (int y = globalMaxH; y >= minY && y > globalMaxH - 16; y--) {
                    BlockPos pos = new BlockPos(worldX, y, worldZ);
                    BlockState state = level.getBlockState(pos);
                    if (state.isAir()) continue;

                    guiGraphics.pose().pushPose();
                    guiGraphics.pose().translate(i, y - globalMaxH, j);
                    dispatcher.renderSingleBlock(state, guiGraphics.pose(), bufferSource, 15728880, OverlayTexture.NO_OVERLAY);
                    guiGraphics.pose().popPose();
                }
            }
        }
        
        bufferSource.endBatch();
        guiGraphics.pose().popPose();
    }

    private static boolean areChunksLoaded(ClaimData claim) {
        Minecraft minecraft = Minecraft.getInstance();
        net.minecraft.client.multiplayer.ClientLevel level = minecraft.level;
        if (level == null) return false;
        int centerX = (claim.pos1.getX() + claim.pos2.getX()) / 2;
        int centerZ = (claim.pos1.getZ() + claim.pos2.getZ()) / 2;
        return level.getChunkSource().hasChunk(centerX >> 4, centerZ >> 4);
    }
}
