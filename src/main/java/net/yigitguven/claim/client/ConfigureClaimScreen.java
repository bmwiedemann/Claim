package net.yigitguven.claim.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;
import net.yigitguven.claim.core.ClaimData;
import net.yigitguven.claim.network.UpdateClaimMetadataPayload;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ConfigureClaimScreen extends Screen
{
    private final Screen lastScreen;
    private final ClaimData claim;
    
    private EditBox nameBox;
    private EditBox descBox;
    private EditBox colorBox;
    private ClaimData.PermissionMode permissionMode;
    private List<UUID> trustedPlayers;
    
    private float rotation = 0;

    public ConfigureClaimScreen(Screen lastScreen, ClaimData claim)
    {
        super(Component.literal("Configure Claim"));
        this.lastScreen = lastScreen;
        this.claim = claim;
        this.permissionMode = claim.permissionMode;
        this.trustedPlayers = new ArrayList<>(claim.trustedPlayers);
    }

    @Override
    protected void init()
    {
        int leftWidth = this.width / 2;
        int rightStart = leftWidth + 20;
        int inputWidth = 160;

        // Name field
        this.nameBox = new EditBox(this.font, rightStart, 60, inputWidth, 20, Component.literal("Name"));
        this.nameBox.setValue(claim.displayName);
        this.addRenderableWidget(this.nameBox);

        // Description field
        this.descBox = new EditBox(this.font, rightStart, 100, inputWidth, 20, Component.literal("Description"));
        this.descBox.setValue(claim.description != null ? claim.description : "");
        this.addRenderableWidget(this.descBox);

        // Permission Toggle
        this.addRenderableWidget(Button.builder(Component.literal("Access: " + permissionMode.name()), (btn) -> {
            permissionMode = (permissionMode == ClaimData.PermissionMode.PRIVATE) ? ClaimData.PermissionMode.PUBLIC : ClaimData.PermissionMode.PRIVATE;
            btn.setMessage(Component.literal("Access: " + permissionMode.name()));
        }).bounds(rightStart, 140, inputWidth, 20).build());

        // Color Hex
        this.colorBox = new EditBox(this.font, rightStart, 180, inputWidth, 20, Component.literal("Color (Hex)"));
        this.colorBox.setValue(String.format("%08X", claim.color));
        this.addRenderableWidget(this.colorBox);

        // Save / Cancel at bottom
        int buttonY = this.height - 40;
        this.addRenderableWidget(Button.builder(Component.literal("Save Changes"), (btn) -> {
            saveAndExit();
        }).bounds(this.width / 2 - 110, buttonY, 100, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("Cancel"), (btn) -> {
            minecraft.setScreen(lastScreen);
        }).bounds(this.width / 2 + 10, buttonY, 100, 20).build());
    }

    private void saveAndExit()
    {
        String newName = nameBox.getValue();
        String newDesc = descBox.getValue();
        int newColor;
        try {
            newColor = (int) Long.parseLong(colorBox.getValue(), 16);
        } catch (NumberFormatException e) {
            newColor = claim.color;
        }

        if (!newName.isEmpty())
        {
            PacketDistributor.sendToServer(new UpdateClaimMetadataPayload(
                claim.claimId, newName, newDesc, permissionMode, newColor, trustedPlayers
            ));
            
            // Optimistic update
            claim.displayName = newName;
            claim.description = newDesc;
            claim.permissionMode = permissionMode;
            claim.color = newColor;
            claim.trustedPlayers = trustedPlayers;
            
            // Reload the list screen to show changes
            minecraft.setScreen(new ClaimListScreen());
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick)
    {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        
        // Render 3D Preview on the left
        rotation += partialTick * 1.0f;
        int previewX = this.width / 4;
        int previewY = this.height / 2 - 20;
        float previewScale = this.width / 6.0f;
        
        // Draw a nice dark backing for the preview
        guiGraphics.fill(20, 40, this.width / 2 - 10, this.height - 60, 0x40000000);
        guiGraphics.renderOutline(20, 40, this.width / 2 - 30, this.height - 100, 0xFF55FF7D);

        ClaimRenderHelper.renderClaimPreview(guiGraphics, claim, previewX, previewY, rotation, previewScale, true);

        // Render Labels
        guiGraphics.drawString(this.font, "Display Name", this.width / 2 + 20, 48, 0xFFAAAAAA);
        guiGraphics.drawString(this.font, "Description", this.width / 2 + 20, 88, 0xFFAAAAAA);
        guiGraphics.drawString(this.font, "Default Permissions", this.width / 2 + 20, 128, 0xFFAAAAAA);
        guiGraphics.drawString(this.font, "Claim Color (Hex ARGB)", this.width / 2 + 20, 168, 0xFFAAAAAA);

        super.render(guiGraphics, mouseX, mouseY, partialTick);
        
        guiGraphics.drawCenteredString(this.font, "Configuring: " + claim.displayName, this.width / 2, 20, 0xFFFFFFFF);
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.fillGradient(0, 0, this.width, this.height, 0xC0101010, 0xD0101010);
    }
}
