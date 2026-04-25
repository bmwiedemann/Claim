package net.yigitguven.claim.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.yigitguven.claim.core.ClaimData;
import net.yigitguven.claim.core.ClientClaimManager;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import com.mojang.blaze3d.platform.Lighting;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ClaimListScreen extends Screen
{
    private List<ClaimData> playerClaims = new ArrayList<>();
    private List<Button> renameButtons = new ArrayList<>();
    private java.util.Map<Integer, CachedTerrain> terrainCache = new java.util.HashMap<>();
    private float rotation = 0;
    private double scrollAmount = 0;
    
    private static final net.minecraft.resources.ResourceLocation PLACEHOLDER = net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("claim", "textures/gui/placeholder.png");

    private record CachedTerrain(net.minecraft.client.renderer.RenderType renderType, int vertexCount) {}

    public ClaimListScreen()
    {
        super(Component.literal("My Claims"));
    }

    @Override
    protected void init()
    {
        UUID playerUUID = minecraft.player.getUUID();
        playerClaims = ClientClaimManager.getClaims().stream()
                .filter(c -> c.ownerUUID.equals(playerUUID))
                .toList();

        this.clearWidgets();
        renameButtons.clear();
        // Clear terrain cache to refresh blocks
        terrainCache.clear();

        for (ClaimData claim : playerClaims)
        {
            Button btn = Button.builder(Component.literal("Rename"), (b) -> openRenameDialog(claim))
                    .bounds(0, 0, 60, 20)
                    .build();
            this.addRenderableWidget(btn);
            renameButtons.add(btn);
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick)
    {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        
        if (playerClaims.isEmpty())
        {
            guiGraphics.drawCenteredString(this.font, "No claims found.", this.width / 2, this.height / 2, 0xFFFFFF);
        }
        else
        {
            rotation += partialTick * 1.5f;
            
            int columns = 4;
            int spacingX = this.width / (columns + 1);
            int spacingY = spacingX + 40; 
            float scale = spacingX * 0.45f;
            
            int startX = (this.width - (columns - 1) * spacingX) / 2;
            int startY = 80;

            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(0, -scrollAmount, 0);

            for (int i = 0; i < playerClaims.size(); i++)
            {
                ClaimData claim = playerClaims.get(i);
                int x = startX + (i % columns) * spacingX;
                int y = startY + (i / columns) * spacingY;

                Button btn = renameButtons.get(i);
                btn.setX(x - btn.getWidth() / 2);
                btn.setY((int)(y + scale + 10 - scrollAmount));
                btn.visible = btn.getY() + btn.getHeight() > 40 && btn.getY() < this.height - 10;

                boolean isHovered = mouseX >= x - spacingX/2 && mouseX <= x + spacingX/2 && 
                                   mouseY >= y - spacingX/2 - scrollAmount && mouseY <= y + spacingX/2 - scrollAmount;

                if (isHovered)
                {
                    guiGraphics.renderOutline(x - (int)scale - 5, y - (int)scale - 5, (int)scale * 2 + 10, (int)scale * 2 + 10, 0xFFFFFFFF);
                }

                renderCached3DClaim(guiGraphics, claim, x, y, rotation, scale);
            }
            
            guiGraphics.pose().popPose();
            super.render(guiGraphics, mouseX, mouseY, partialTick);
            
            // Render tooltips
            for (int i = 0; i < playerClaims.size(); i++)
            {
                int x = startX + (i % columns) * spacingX;
                int y = startY + (i / columns) * spacingY;
                if (mouseX >= x - spacingX/2 && mouseX <= x + spacingX/2 && 
                    mouseY >= y - spacingX/2 - scrollAmount && mouseY <= y + spacingX/2 - scrollAmount)
                {
                    guiGraphics.renderTooltip(this.font, Component.literal(playerClaims.get(i).displayName), mouseX, mouseY);
                }
            }

            guiGraphics.drawCenteredString(this.font, "My Claims (" + playerClaims.size() + ")", this.width / 2, 20, 0xFF55FF7D);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY)
    {
        scrollAmount = Math.max(0, scrollAmount - scrollY * 20);
        int columns = 4;
        int spacingY = this.width / (columns + 1) + 40;
        int rows = (int) Math.ceil(playerClaims.size() / (double) columns);
        int totalHeight = 80 + rows * spacingY;
        scrollAmount = Math.min(scrollAmount, Math.max(0, totalHeight - this.height + 50));
        return true;
    }

    private void openRenameDialog(ClaimData claim)
    {
        minecraft.setScreen(new RenameClaimScreen(this, claim));
    }

    private void renderCached3DClaim(GuiGraphics guiGraphics, ClaimData claim, int x, int y, float rot, float scale)
    {
        if (!areChunksLoaded(claim))
        {
            // Render placeholder fallback
            Lighting.setupForFlatItems();
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            
            int s = (int)scale * 2;
            int startX = x - s/2;
            int startY = y - s/2;
            float f = s / 128.0f; // Scale factor for 128x128 coordinates
            int bCol = 0xFFFFFFFF;

            // Draw white backing rects for specific placeholder regions
            guiGraphics.fill((int)(startX + 16*f), (int)(startY + 0*f), (int)(startX + 63*f), (int)(startY + 71*f), bCol);
            guiGraphics.fill((int)(startX + 16*f), (int)(startY + 72*f), (int)(startX + 39*f), (int)(startY + 127*f), bCol);
            guiGraphics.fill((int)(startX + 64*f), (int)(startY + 8*f), (int)(startX + 71*f), (int)(startY + 87*f), bCol);
            guiGraphics.fill((int)(startX + 72*f), (int)(startY + 16*f), (int)(startX + 111*f), (int)(startY + 39*f), bCol);
            guiGraphics.fill((int)(startX + 72*f), (int)(startY + 40*f), (int)(startX + 103*f), (int)(startY + 47*f), bCol);
            guiGraphics.fill((int)(startX + 72*f), (int)(startY + 48*f), (int)(startX + 95*f), (int)(startY + 63*f), bCol);
            guiGraphics.fill((int)(startX + 72*f), (int)(startY + 64*f), (int)(startX + 103*f), (int)(startY + 71*f), bCol);
            guiGraphics.fill((int)(startX + 72*f), (int)(startY + 73*f), (int)(startX + 111*f), (int)(startY + 87*f), bCol);

            // Sampling the full texture by matching the texture size parameter to the target size
            guiGraphics.blit(PLACEHOLDER, startX, startY, 0, 0, s, s, s, s);
            return;
        }

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(x, y, 300);
        
        // Setup lighting for the 3D model
        Lighting.setupFor3DItems();
        
        renderProjectorPreview(guiGraphics, claim, rot, scale);

        Lighting.setupForFlatItems();
        guiGraphics.pose().popPose();
    }

    private boolean areChunksLoaded(ClaimData claim)
    {
        net.minecraft.client.multiplayer.ClientLevel level = minecraft.level;
        if (level == null) return false;
        
        // Check if the center of the claim area is loaded
        int centerX = (claim.pos1.getX() + claim.pos2.getX()) / 2;
        int centerZ = (claim.pos1.getZ() + claim.pos2.getZ()) / 2;
        return level.getChunkSource().hasChunk(centerX >> 4, centerZ >> 4);
    }

    private void renderProjectorPreview(GuiGraphics guiGraphics, ClaimData claim, float rot, float scale)
    {
        net.minecraft.client.multiplayer.ClientLevel level = minecraft.level;
        if (level == null) return;

        int minX = Math.min(claim.pos1.getX(), claim.pos2.getX());
        int maxX = Math.max(claim.pos1.getX(), claim.pos2.getX());
        int minZ = Math.min(claim.pos1.getZ(), claim.pos2.getZ());
        int maxZ = Math.max(claim.pos1.getZ(), claim.pos2.getZ());

        int sizeX = maxX - minX + 1;
        int sizeZ = maxZ - minZ + 1;

        // Find global highest block in the claim to anchor the "projector"
        int globalMaxH = -64;
        int sampleStep = Math.max(1, Math.max(sizeX, sizeZ) / 16);
        for (int i = 0; i < sizeX; i += sampleStep) {
            for (int j = 0; j < sizeZ; j += sampleStep) {
                int h = level.getHeight(Heightmap.Types.WORLD_SURFACE, minX + i, minZ + j) - 1;
                if (h > globalMaxH) globalMaxH = h;
            }
        }

        // Calculate fit scale - account for both horizontal size and the 16-block depth
        float diag = (float) Math.sqrt(sizeX * sizeX + sizeZ * sizeZ + 16 * 16);
        float fitScale = (scale * 1.6f) / Math.max(diag, 20f);

        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(fitScale, -fitScale, fitScale);
        
        Quaternionf quaternion = new Quaternionf()
                .rotateX((float) Math.toRadians(25))
                .rotateY((float) Math.toRadians(rot));
        guiGraphics.pose().mulPose(quaternion);
        
        guiGraphics.pose().translate(-sizeX / 2.0f, 0, -sizeZ / 2.0f);

        BlockRenderDispatcher dispatcher = minecraft.getBlockRenderer();
        MultiBufferSource.BufferSource bufferSource = minecraft.renderBuffers().bufferSource();

        int step = Math.max(1, Math.max(sizeX, sizeZ) / 40);
        
        for (int i = 0; i < sizeX; i += step)
        {
            for (int j = 0; j < sizeZ; j += step)
            {
                int worldX = minX + i;
                int worldZ = minZ + j;
                
                // Render from globalMaxH down to globalMaxH - 16
                for (int y = globalMaxH; y > globalMaxH - 16; y--) {
                    BlockPos pos = new BlockPos(worldX, y, worldZ);
                    BlockState state = level.getBlockState(pos);
                    if (state.isAir()) continue;

                    guiGraphics.pose().pushPose();
                    guiGraphics.pose().translate(i, y - globalMaxH, j); // Relative to global max
                    dispatcher.renderSingleBlock(state, guiGraphics.pose(), bufferSource, 15728880, OverlayTexture.NO_OVERLAY);
                    guiGraphics.pose().popPose();
                }
            }
        }
        
        bufferSource.endBatch();
        guiGraphics.pose().popPose();
    }

    @Override
    public boolean isPauseScreen()
    {
        return false;
    }
    
    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.fillGradient(0, 0, this.width, this.height, 0xC0101010, 0xD0101010);
    }
}
