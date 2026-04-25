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
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            int s = (int)scale * 2;
            guiGraphics.blit(PLACEHOLDER, x - s/2, y - s/2, s, s, 0.0f, 0.0f, 128, 128, 128, 128);
            return;
        }

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(x, y, 200);
        guiGraphics.pose().scale(scale, scale, scale);
        
        Quaternionf quaternion = new Quaternionf()
                .rotateX((float) Math.toRadians(30))
                .rotateY((float) Math.toRadians(rot));
        guiGraphics.pose().mulPose(quaternion);

        renderOptimizedTerrain(guiGraphics, claim);

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

    private void renderOptimizedTerrain(GuiGraphics guiGraphics, ClaimData claim)
    {
        net.minecraft.client.multiplayer.ClientLevel level = minecraft.level;
        if (level == null) return;

        int minX = Math.min(claim.pos1.getX(), claim.pos2.getX());
        int maxX = Math.max(claim.pos1.getX(), claim.pos2.getX());
        int minZ = Math.min(claim.pos1.getZ(), claim.pos2.getZ());
        int maxZ = Math.max(claim.pos1.getZ(), claim.pos2.getZ());

        int sizeX = maxX - minX + 1;
        int sizeZ = maxZ - minZ + 1;
        
        int res = 48; // Higher resolution for better detail
        int stepX = Math.max(1, sizeX / res);
        int stepZ = Math.max(1, sizeZ / res);
        
        float miniScale = 2.0f / Math.max(sizeX, sizeZ);
        guiGraphics.pose().scale(miniScale, miniScale, miniScale);
        guiGraphics.pose().translate(-sizeX / 2.0f, 0, -sizeZ / 2.0f);

        com.mojang.blaze3d.vertex.Tesselator tesselator = com.mojang.blaze3d.vertex.Tesselator.getInstance();
        com.mojang.blaze3d.vertex.BufferBuilder buffer = tesselator.begin(com.mojang.blaze3d.vertex.VertexFormat.Mode.QUADS, com.mojang.blaze3d.vertex.DefaultVertexFormat.POSITION_COLOR);

        Matrix4f matrix = guiGraphics.pose().last().pose();

        // Sample heights first
        int[][] heights = new int[sizeX/stepX + 1][sizeZ/stepZ + 1];
        int[][] colors = new int[sizeX/stepX + 1][sizeZ/stepZ + 1];
        
        int minY = 256;
        for (int i = 0; i < sizeX/stepX; i++)
        {
            for (int j = 0; j < sizeZ/stepZ; j++)
            {
                int worldX = minX + i * stepX;
                int worldZ = minZ + j * stepZ;
                int h = level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE, worldX, worldZ) - 1;
                heights[i][j] = h;
                if (h < minY) minY = h;
                
                net.minecraft.core.BlockPos pos = new net.minecraft.core.BlockPos(worldX, h, worldZ);
                net.minecraft.world.level.block.state.BlockState state = level.getBlockState(pos);
                colors[i][j] = state.getMapColor(level, pos).col | 0xFF000000;
            }
        }

        // Render faces
        for (int i = 0; i < sizeX/stepX; i++)
        {
            for (int j = 0; j < sizeZ/stepZ; j++)
            {
                float x = i * stepX;
                float z = j * stepZ;
                float h = heights[i][j] - minY;
                int color = colors[i][j];

                // Top Face
                addFaceHorizontal(buffer, matrix, x, z, x + stepX, z + stepZ, h, color);

                // Check neighbors for sides (simulating 3D blocks)
                if (i < sizeX/stepX - 1) {
                    float nextH = heights[i+1][j] - minY;
                    if (h > nextH) {
                        // Right Side
                        int rightColor = darken(color, 40);
                        addFaceVerticalSide(buffer, matrix, x + stepX, nextH, z, x + stepX, h, z + stepZ, rightColor);
                    }
                }
                
                if (j < sizeZ/stepZ - 1) {
                    float nextH = heights[i][j+1] - minY;
                    if (h > nextH) {
                        // Front Side
                        int frontColor = darken(color, 20);
                        addFace(buffer, matrix, x, nextH, z + stepZ, x + stepX, h, z + stepZ, frontColor);
                    }
                }
            }
        }
        
        RenderSystem.setShader(net.minecraft.client.renderer.GameRenderer::getPositionColorShader);
        RenderSystem.enableDepthTest();
        com.mojang.blaze3d.vertex.BufferUploader.drawWithShader(buffer.buildOrThrow());
    }

    private int darken(int color, int amount) {
        int a = (color >> 24) & 0xFF;
        int r = Math.max(0, ((color >> 16) & 0xFF) - amount);
        int g = Math.max(0, ((color >> 8) & 0xFF) - amount);
        int b = Math.max(0, (color & 0xFF) - amount);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    private void addFace(com.mojang.blaze3d.vertex.BufferBuilder buffer, Matrix4f matrix, float x1, float y1, float z1, float x2, float y2, float z2, int color)
    {
        buffer.addVertex(matrix, x1, y2, z1).setColor(color);
        buffer.addVertex(matrix, x2, y2, z1).setColor(color);
        buffer.addVertex(matrix, x2, y1, z1).setColor(color);
        buffer.addVertex(matrix, x1, y1, z1).setColor(color);
    }

    private void addFaceHorizontal(com.mojang.blaze3d.vertex.BufferBuilder buffer, Matrix4f matrix, float x1, float z1, float x2, float z2, float y, int color)
    {
        buffer.addVertex(matrix, x1, y, z1).setColor(color);
        buffer.addVertex(matrix, x1, y, z2).setColor(color);
        buffer.addVertex(matrix, x2, y, z2).setColor(color);
        buffer.addVertex(matrix, x2, y, z1).setColor(color);
    }

    private void addFaceVerticalSide(com.mojang.blaze3d.vertex.BufferBuilder buffer, Matrix4f matrix, float x, float y1, float z1, float x2, float y2, float z2, int color)
    {
        buffer.addVertex(matrix, x, y1, z1).setColor(color);
        buffer.addVertex(matrix, x, y2, z1).setColor(color);
        buffer.addVertex(matrix, x, y2, z2).setColor(color);
        buffer.addVertex(matrix, x, y1, z2).setColor(color);
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
