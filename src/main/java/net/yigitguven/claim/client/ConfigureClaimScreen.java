package net.yigitguven.claim.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;
import net.yigitguven.claim.core.ClaimData;
import net.yigitguven.claim.core.ClientClaimManager;
import net.yigitguven.claim.network.UpdateClaimMetadataPayload;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class ConfigureClaimScreen extends Screen
{
    private final Screen lastScreen;
    private final ClaimData claim;
    private final int originalColor;
    
    private EditBox nameBox;
    private EditBox descBox;
    private int selectedColor;
    private ClaimData.PermissionMode permissionMode;
    private List<UUID> trustedPlayers;
    
    private float rotation = 0;
    
    private static final int[] PRESET_VALUES = {
        0xFF55FF7D, 0xFF5555FF, 0xFFFF5555, 0xFFFFFF55, 
        0xFFAA00AA, 0xFF55FFFF, 0xFFFFAA00, 0xFFFFFFFF
    };
    
    // The VISUAL colors the user wants to see on the UI buttons
    private static final int[] PRESET_DISPLAY_COLORS = {
        0xFF00FF00, 0xFFFF0000, 0xFF00008B, 0xFF00FFFF, 
        0xFF800080, 0xFFFFFF00, 0xFF0000FF, 0xFFFFFFFF
    };

    public ConfigureClaimScreen(Screen lastScreen, ClaimData claim)
    {
        super(Component.literal("Configure Claim"));
        this.lastScreen = lastScreen;
        this.claim = claim;
        this.permissionMode = claim.permissionMode;
        this.trustedPlayers = new ArrayList<>(claim.trustedPlayers);
        this.selectedColor = claim.color;
        this.originalColor = claim.color;
    }

    @Override
    protected void init()
    {
        int leftWidth = this.width / 2;
        int rightStart = leftWidth + 20;
        int inputWidth = Math.min(160, this.width / 2 - 40);

        // Name field
        this.nameBox = new EditBox(this.font, rightStart, 38, inputWidth, 18, Component.literal("Name"));
        this.nameBox.setValue(claim.displayName);
        this.addRenderableWidget(this.nameBox);

        // Description field
        this.descBox = new EditBox(this.font, rightStart, 72, inputWidth, 18, Component.literal("Description"));
        this.descBox.setValue(claim.description != null ? claim.description : "");
        this.addRenderableWidget(this.descBox);

        // Permission Toggle
        this.addRenderableWidget(Button.builder(Component.literal("Access: " + permissionMode.name()), (btn) -> {
            permissionMode = (permissionMode == ClaimData.PermissionMode.PRIVATE) ? ClaimData.PermissionMode.PUBLIC : ClaimData.PermissionMode.PRIVATE;
            btn.setMessage(Component.literal("Access: " + permissionMode.name()));
        }).bounds(rightStart, 102, inputWidth, 18).build());

        // Color Selection
        for (int i = 0; i < PRESET_VALUES.length; i++) {
            final int actualValue = PRESET_VALUES[i];
            int bx = rightStart + (i % 4) * (inputWidth / 4 + 2);
            int by = 135 + (i / 4) * 22;
            this.addRenderableWidget(Button.builder(Component.literal(""), (btn) -> {
                selectedColor = actualValue;
                claim.color = actualValue; // Live update preview
            }).bounds(bx, by, inputWidth / 4 - 2, 18).build());
        }

        // Trusted Players Management
        int trustedY = 190;
        this.addRenderableWidget(Button.builder(Component.literal("Add Player"), (btn) -> {
            openOnlinePlayerSelector();
        }).bounds(rightStart, trustedY, inputWidth, 18).build());

        // Save / Cancel at bottom
        int buttonY = this.height - 30;
        this.addRenderableWidget(Button.builder(Component.literal("Save Changes"), (btn) -> {
            saveAndExit();
        }).bounds(this.width / 2 - 105, buttonY, 100, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("Cancel"), (btn) -> {
            claim.color = originalColor;
            minecraft.setScreen(lastScreen);
        }).bounds(this.width / 2 + 5, buttonY, 100, 20).build());
    }

    private void openOnlinePlayerSelector() {
        if (minecraft.getConnection() == null) return;
        List<net.minecraft.client.multiplayer.PlayerInfo> online = new ArrayList<>(minecraft.getConnection().getOnlinePlayers());
        List<net.minecraft.client.multiplayer.PlayerInfo> candidates = online.stream()
                .filter(p -> !trustedPlayers.contains(p.getProfile().getId()) && !p.getProfile().getId().equals(minecraft.player.getUUID()))
                .collect(Collectors.toList());

        if (candidates.isEmpty()) return;
        trustedPlayers.add(candidates.get(0).getProfile().getId());
    }

    private void saveAndExit()
    {
        String newName = nameBox.getValue();
        String newDesc = descBox.getValue();
        if (!newName.isEmpty())
        {
            PacketDistributor.sendToServer(new UpdateClaimMetadataPayload(
                claim.claimId, newName, newDesc, permissionMode, selectedColor, trustedPlayers
            ));
            ClientClaimManager.updateClaimOptimistically(claim.claimId, newName, newDesc, permissionMode, selectedColor, trustedPlayers);
            minecraft.setScreen(new ClaimListScreen());
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick)
    {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        
        rotation += partialTick * 1.0f;
        int previewX = this.width / 4;
        int previewY = this.height / 2 - 10;
        float previewScale = Math.min(this.width / 6.0f, this.height / 3.5f);
        
        // Find the visual color for the border based on the selected internal value
        int borderDisplayColor = 0xFFFFFFFF;
        for (int i = 0; i < PRESET_VALUES.length; i++) {
            if (PRESET_VALUES[i] == selectedColor) {
                borderDisplayColor = PRESET_DISPLAY_COLORS[i];
                break;
            }
        }
        
        guiGraphics.fill(20, 30, this.width / 2 - 10, this.height - 50, 0x40000000);
        // Use the display color for the UI border
        guiGraphics.renderOutline(20, 30, this.width / 2 - 30, this.height - 80, borderDisplayColor);

        ClaimRenderHelper.renderClaimPreview(guiGraphics, claim, previewX, previewY, rotation, previewScale, true);

        int rightStart = this.width / 2 + 20;
        guiGraphics.drawString(this.font, "Name", rightStart, 28, 0xFFAAAAAA);
        guiGraphics.drawString(this.font, "Description", rightStart, 62, 0xFFAAAAAA);
        guiGraphics.drawString(this.font, "Permissions", rightStart, 92, 0xFFAAAAAA);
        guiGraphics.drawString(this.font, "Color", rightStart, 125, 0xFFAAAAAA);
        
        guiGraphics.drawString(this.font, "Trusted (" + trustedPlayers.size() + ")", rightStart, 180, 0xFFAAAAAA);
        
        int ty = 210;
        for (int i = 0; i < Math.min(trustedPlayers.size(), 2); i++) {
            UUID id = trustedPlayers.get(i);
            String name = "Unknown";
            if (minecraft.getConnection() != null) {
                net.minecraft.client.multiplayer.PlayerInfo info = minecraft.getConnection().getPlayerInfo(id);
                if (info != null) name = info.getProfile().getName();
            }
            guiGraphics.drawString(this.font, "• " + name, rightStart + 5, ty, 0xFFCCCCCC);
            ty += 11;
        }
        if (trustedPlayers.size() > 2) {
            guiGraphics.drawString(this.font, "...+" + (trustedPlayers.size() - 2) + " more", rightStart + 5, ty, 0xFF888888);
        }

        super.render(guiGraphics, mouseX, mouseY, partialTick);

        int inputWidth = Math.min(160, this.width / 2 - 40);
        for (int i = 0; i < PRESET_VALUES.length; i++) {
            int displayColor = PRESET_DISPLAY_COLORS[i];
            int actualValue = PRESET_VALUES[i];
            int bx = rightStart + (i % 4) * (inputWidth / 4 + 2);
            int by = 135 + (i / 4) * 22;
            
            guiGraphics.fill(bx + 3, by + 3, bx + (inputWidth / 4 - 5), by + 15, displayColor);
            
            if (actualValue == selectedColor) {
                guiGraphics.renderOutline(bx + 1, by + 1, inputWidth / 4 - 4, 16, 0xFFFFFFFF);
            }
        }

        guiGraphics.drawCenteredString(this.font, "Config: " + claim.displayName, this.width / 2, 15, 0xFFFFFFFF);
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.fillGradient(0, 0, this.width, this.height, 0xC0101010, 0xD0101010);
    }
}
