package net.yigitguven.claim.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.yigitguven.claim.core.ClaimData;
import net.yigitguven.claim.network.RenameClaimPayload;

public class RenameClaimScreen extends Screen
{
    private final Screen lastScreen;
    private final ClaimData claim;
    private EditBox nameBox;

    public RenameClaimScreen(Screen lastScreen, ClaimData claim)
    {
        super(Component.literal("Rename Claim"));
        this.lastScreen = lastScreen;
        this.claim = claim;
    }

    @Override
    protected void init()
    {
        this.nameBox = new EditBox(this.font, this.width / 2 - 100, this.height / 2 - 20, 200, 20, Component.literal("Name"));
        this.nameBox.setValue(claim.displayName);
        this.addRenderableWidget(this.nameBox);

        this.addRenderableWidget(Button.builder(Component.literal("Save"), (btn) -> {
            String newName = this.nameBox.getValue();
            if (!newName.isEmpty())
            {
                net.neoforged.neoforge.network.PacketDistributor.sendToServer(new RenameClaimPayload(claim.claimId, newName));
                minecraft.setScreen(lastScreen);
            }
        }).bounds(this.width / 2 - 105, this.height / 2 + 10, 100, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("Cancel"), (btn) -> {
            minecraft.setScreen(lastScreen);
        }).bounds(this.width / 2 + 5, this.height / 2 + 10, 100, 20).build());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick)
    {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawCenteredString(this.font, "Rename Claim: " + claim.displayName, this.width / 2, this.height / 2 - 50, 0xFFFFFF);
    }
}
