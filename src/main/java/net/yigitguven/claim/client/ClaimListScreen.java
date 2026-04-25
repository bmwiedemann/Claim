package net.yigitguven.claim.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
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
    private float rotation = 0;
    private double scrollAmount = 0;

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
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick)
    {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        if (playerClaims.isEmpty())
        {
            guiGraphics.drawCenteredString(this.font, "No claims found.", this.width / 2, this.height / 2, 0xFFFFFF);
        }
        else
        {
            rotation += partialTick * 1.5f;
            
            int columns = 5;
            int spacingX = this.width / (columns + 1);
            int spacingY = spacingX + 20; // Extra space for name/button
            float scale = spacingX * 0.35f;
            
            int startX = (this.width - (columns - 1) * spacingX) / 2;
            int startY = 80;

            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(0, -scrollAmount, 0);

            for (int i = 0; i < playerClaims.size(); i++)
            {
                ClaimData claim = playerClaims.get(i);
                int x = startX + (i % columns) * spacingX;
                int y = startY + (i / columns) * spacingY;

                boolean isHovered = mouseX >= x - spacingX/2 && mouseX <= x + spacingX/2 && 
                                   mouseY >= y - spacingX/2 - scrollAmount && mouseY <= y + spacingX/2 - scrollAmount;

                if (isHovered)
                {
                    // Draw nice white border
                    guiGraphics.renderOutline(x - (int)scale - 5, y - (int)scale - 5, (int)scale * 2 + 10, (int)scale * 2 + 10, 0xFFFFFFFF);
                    guiGraphics.renderTooltip(this.font, Component.literal(claim.displayName), mouseX, (int)(mouseY + scrollAmount));
                }

                render3DClaim(guiGraphics, claim, x, y, rotation, scale);
                
                // Render Edit Button below
                int btnW = 40;
                int btnH = 12;
                int btnX = x - btnW/2;
                int btnY = y + (int)scale + 5;
                
                guiGraphics.fill(btnX, btnY, btnX + btnW, btnY + btnH, isHovered ? 0xFF55FF7D : 0xFF333333);
                guiGraphics.drawCenteredString(this.font, "Rename", x, btnY + 2, 0xFFFFFF);
            }
            
            guiGraphics.pose().popPose();
            guiGraphics.drawCenteredString(this.font, "My Claims (" + playerClaims.size() + ")", this.width / 2, 20, 0xFF55FF7D);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button)
    {
        if (button == 0) // Left click
        {
            int columns = 5;
            int spacingX = this.width / (columns + 1);
            int spacingY = spacingX + 20;
            int startX = (this.width - (columns - 1) * spacingX) / 2;
            int startY = 80;

            for (int i = 0; i < playerClaims.size(); i++)
            {
                int x = startX + (i % columns) * spacingX;
                int y = startY + (i / columns) * spacingY;
                
                float scale = spacingX * 0.35f;
                int btnW = 40;
                int btnH = 12;
                int btnX = x - btnW/2;
                int btnY = (int)(y + scale + 5 - scrollAmount);

                if (mouseX >= btnX && mouseX <= btnX + btnW && mouseY >= btnY && mouseY <= btnY + btnH)
                {
                    openRenameDialog(playerClaims.get(i));
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void openRenameDialog(ClaimData claim)
    {
        minecraft.setScreen(new RenameClaimScreen(this, claim));
    }

    private void render3DClaim(GuiGraphics guiGraphics, ClaimData claim, int x, int y, float rot, float scale)
    {
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(x, y, 100);
        guiGraphics.pose().scale(scale, scale, scale);
        
        Quaternionf quaternion = new Quaternionf()
                .rotateX((float) Math.toRadians(30))
                .rotateY((float) Math.toRadians(rot));
        guiGraphics.pose().mulPose(quaternion);

        renderMiniatureTerrain(guiGraphics, claim);

        guiGraphics.pose().popPose();
    }

    private void renderMiniatureTerrain(GuiGraphics guiGraphics, ClaimData claim)
    {
        net.minecraft.client.multiplayer.ClientLevel level = minecraft.level;
        if (level == null) return;

        int minX = Math.min(claim.pos1.getX(), claim.pos2.getX());
        int maxX = Math.max(claim.pos1.getX(), claim.pos2.getX());
        int minZ = Math.min(claim.pos1.getZ(), claim.pos2.getZ());
        int maxZ = Math.max(claim.pos1.getZ(), claim.pos2.getZ());

        int sizeX = maxX - minX + 1;
        int sizeZ = maxZ - minZ + 1;
        
        int stepX = Math.max(1, sizeX / 16);
        int stepZ = Math.max(1, sizeZ / 16);
        
        float miniScale = 1.8f / Math.max(sizeX, sizeZ);
        guiGraphics.pose().scale(miniScale, miniScale, miniScale);
        guiGraphics.pose().translate(-sizeX / 2.0f, 0, -sizeZ / 2.0f);

        for (int sx = 0; sx < sizeX; sx += stepX)
        {
            for (int sz = 0; sz < sizeZ; sz += stepZ)
            {
                int worldX = minX + sx;
                int worldZ = minZ + sz;
                
                net.minecraft.core.BlockPos topPos = new net.minecraft.core.BlockPos(worldX, level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE, worldX, worldZ) - 1, worldZ);
                net.minecraft.world.level.block.state.BlockState state = level.getBlockState(topPos);
                int color = state.getMapColor(level, topPos).col | 0xFF000000;

                guiGraphics.pose().pushPose();
                guiGraphics.pose().translate(sx, 0, sz);
                renderSmallBlock(guiGraphics, color);
                guiGraphics.pose().popPose();
            }
        }
    }

    private void renderSmallBlock(GuiGraphics guiGraphics, int color)
    {
        // Improved shading for clearer 3D look
        int a = (color >> 24) & 0xFF;
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;
        
        // Front
        guiGraphics.fill(0, 0, 1, 1, color);
        
        // Top (Significantly lighter)
        int topColor = (a << 24) | (Math.min(r + 60, 255) << 16) | (Math.min(g + 60, 255) << 8) | Math.min(b + 60, 255);
        guiGraphics.pose().pushPose();
        guiGraphics.pose().mulPose(com.mojang.math.Axis.XP.rotationDegrees(-90));
        guiGraphics.fill(0, 0, 1, 1, topColor);
        guiGraphics.pose().popPose();

        // Right (Significantly darker)
        int rightColor = (a << 24) | (Math.max(r - 60, 0) << 16) | (Math.max(g - 60, 0) << 8) | Math.max(b - 60, 0);
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(1, 0, 0);
        guiGraphics.pose().mulPose(com.mojang.math.Axis.YP.rotationDegrees(90));
        guiGraphics.fill(-1, 0, 0, 1, rightColor);
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
