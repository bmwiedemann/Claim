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
            rotation += partialTick * 0.5f;
            
            int columns = 4;
            int spacing = 100;
            int startX = (this.width - (columns - 1) * spacing) / 2;
            int startY = 80;

            for (int i = 0; i < playerClaims.size(); i++)
            {
                ClaimData claim = playerClaims.get(i);
                int x = startX + (i % columns) * spacing;
                int y = startY + (i / columns) * spacing;

                render3DClaim(guiGraphics, claim, x, y, rotation, mouseX, mouseY);
            }
        }
    }

    private void render3DClaim(GuiGraphics guiGraphics, ClaimData claim, int x, int y, float rot, int mouseX, int mouseY)
    {
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(x, y, 100);
        guiGraphics.pose().scale(40, 40, 40);
        
        // Tilt for 3D effect
        Quaternionf quaternion = new Quaternionf()
                .rotateX((float) Math.toRadians(30))
                .rotateY((float) Math.toRadians(rot));
        guiGraphics.pose().mulPose(quaternion);

        // Render a simple wireframe cuboid or solid colored one
        renderBox(guiGraphics, 0xFF55FF7D);

        guiGraphics.pose().popPose();

        // Hover check
        if (mouseX >= x - 40 && mouseX <= x + 40 && mouseY >= y - 40 && mouseY <= y + 40)
        {
            guiGraphics.renderTooltip(this.font, Component.literal(claim.displayName), mouseX, mouseY);
        }
    }

    private void renderBox(GuiGraphics guiGraphics, int color)
    {
        // Draw 3 faces to simulate a 3D block
        // Base color components
        int a = (color >> 24) & 0xFF;
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;

        // Front Face (Normal)
        guiGraphics.fill(-10, -10, 10, 10, color);

        // Top Face (Lighter)
        int topColor = (a << 24) | (Math.min(r + 30, 255) << 16) | (Math.min(g + 30, 255) << 8) | Math.min(b + 30, 255);
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0, -10, 0);
        guiGraphics.pose().mulPose(com.mojang.math.Axis.XP.rotationDegrees(-90));
        guiGraphics.fill(-10, -10, 10, 10, topColor);
        guiGraphics.pose().popPose();

        // Right Face (Darker)
        int rightColor = (a << 24) | (Math.max(r - 30, 0) << 16) | (Math.max(g - 30, 0) << 8) | Math.max(b - 30, 0);
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(10, 0, 0);
        guiGraphics.pose().mulPose(com.mojang.math.Axis.YP.rotationDegrees(90));
        guiGraphics.fill(-10, -10, 10, 10, rightColor);
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
