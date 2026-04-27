package net.yigitguven.claim.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.yigitguven.claim.core.ClaimData;
import net.yigitguven.claim.core.ClientClaimManager;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ClaimListScreen extends Screen
{
    private List<ClaimData> playerClaims = new ArrayList<>();
    private List<Button> configButtons = new ArrayList<>();
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

        this.clearWidgets();
        configButtons.clear();

        for (ClaimData claim : playerClaims)
        {
            Button btn = Button.builder(Component.literal("Configure"), (b) -> openConfigureScreen(claim))
                    .bounds(0, 0, 70, 20)
                    .build();
            this.addRenderableWidget(btn);
            configButtons.add(btn);
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

                Button btn = configButtons.get(i);
                btn.setX(x - btn.getWidth() / 2);
                btn.setY((int)(y + scale + 10 - scrollAmount));
                btn.visible = btn.getY() + btn.getHeight() > 40 && btn.getY() < this.height - 10;

                boolean isHovered = mouseX >= x - spacingX/2 && mouseX <= x + spacingX/2 && 
                                   mouseY >= y - spacingX/2 - scrollAmount && mouseY <= y + spacingX/2 - scrollAmount;

                if (isHovered)
                {
                    guiGraphics.renderOutline(x - (int)scale - 5, y - (int)scale - 5, (int)scale * 2 + 10, (int)scale * 2 + 10, 0xFFFFFFFF);
                }

                ClaimRenderHelper.renderClaimPreview(guiGraphics, claim, x, y, rotation, scale, false);
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

    private void openConfigureScreen(ClaimData claim)
    {
        minecraft.setScreen(new ConfigureClaimScreen(this, claim));
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
