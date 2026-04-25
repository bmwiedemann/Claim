package net.yigitguven.claim.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.yigitguven.claim.network.RequestClaimPayload;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xaero.map.gui.GuiMap;
import xaero.map.gui.MapTileSelection;
import xaero.map.gui.dropdown.rightclick.RightClickOption;

import java.util.ArrayList;

@Mixin(value = GuiMap.class, remap = false)
public abstract class GuiMapMixin extends net.minecraft.client.gui.screens.Screen
{
    protected GuiMapMixin(net.minecraft.network.chat.Component title) { super(title); }

    @Override
    public void removed()
    {
        net.yigitguven.claim.integration.XaeroMapState.CURRENT_GUI = null;
        super.removed();
    }

    @Shadow
    private MapTileSelection mapTileSelection;

    @Shadow
    private net.minecraft.client.gui.components.Button claimsButton;

    @Inject(method = "onClaimsButton", at = @At("TAIL"))
    private void claim$onClaimsButton(CallbackInfo ci)
    {
        net.yigitguven.claim.integration.XaeroMapIntegration.refresh();
    }


    @Inject(method = "init", at = @At("TAIL"))
    private void claim$init(CallbackInfo ci)
    {
        net.yigitguven.claim.integration.XaeroMapState.CURRENT_GUI = (GuiMap) (Object) this;
        if (xaero.map.WorldMap.settings != null)
        {
            xaero.map.WorldMap.settings.updateRegionCacheHashCode();
        }
        if (this.claimsButton != null)
        {
            this.claimsButton.visible = true;
            this.claimsButton.active = true;
        }
    }

    @Inject(method = "getRightClickOptions", at = @At("RETURN"))
    private void claim$addOptions(CallbackInfoReturnable<ArrayList<RightClickOption>> cir)
    {
        ArrayList<RightClickOption> options = cir.getReturnValue();
        if (options == null || mapTileSelection == null) return;

        // Check if claims are enabled in Xaero's settings
        if (!net.yigitguven.claim.integration.XaeroMapIntegration.isEnabled()) return;

        options.add(new RightClickOption("Claim Selection", options.size(), (GuiMap) (Object) this)
        {
            @Override
            public void onAction(Screen screen)
            {
                int left = mapTileSelection.getLeft();
                int top = mapTileSelection.getTop();
                int right = mapTileSelection.getRight();
                int bottom = mapTileSelection.getBottom();

                // Convert chunk coordinates to block coordinates
                BlockPos p1 = new BlockPos(left << 4, -64, top << 4);
                BlockPos p2 = new BlockPos((right << 4) + 15, 320, (bottom << 4) + 15);

                if (Minecraft.getInstance().getConnection() != null)
                {
                    Minecraft.getInstance().getConnection().send(new net.yigitguven.claim.network.RequestClaimPayload(p1, p2));
                }
            }
        });

        // Add Unclaim Selection option
        java.util.List<Integer> selectedClaimIds = new java.util.ArrayList<>();
        int left = mapTileSelection.getLeft();
        int top = mapTileSelection.getTop();
        int right = mapTileSelection.getRight();
        int bottom = mapTileSelection.getBottom();
        java.util.UUID playerUUID = Minecraft.getInstance().player != null ? Minecraft.getInstance().player.getUUID() : null;

        if (playerUUID != null)
        {
            for (net.yigitguven.claim.core.ClaimData claim : net.yigitguven.claim.core.ClientClaimManager.getClaims())
            {
                if (claim.ownerUUID.equals(playerUUID))
                {
                    int cMinX = Math.min(claim.pos1.getX(), claim.pos2.getX()) >> 4;
                    int cMinZ = Math.min(claim.pos1.getZ(), claim.pos2.getZ()) >> 4;
                    int cMaxX = Math.max(claim.pos1.getX(), claim.pos2.getX()) >> 4;
                    int cMaxZ = Math.max(claim.pos1.getZ(), claim.pos2.getZ()) >> 4;

                    if (cMaxX >= left && cMinX <= right && cMaxZ >= top && cMinZ <= bottom)
                    {
                        selectedClaimIds.add(claim.claimId);
                    }
                }
            }
        }

        if (!selectedClaimIds.isEmpty())
        {
            options.add(new RightClickOption("Unclaim Selection", options.size(), (GuiMap) (Object) this)
            {
                @Override
                public void onAction(Screen screen)
                {
                    Minecraft.getInstance().setScreen(new net.minecraft.client.gui.screens.ConfirmScreen((result) -> {
                        if (result) {
                            Minecraft.getInstance().setScreen(new net.minecraft.client.gui.screens.ConfirmScreen((result2) -> {
                                if (result2 && Minecraft.getInstance().getConnection() != null) {
                                    Minecraft.getInstance().getConnection().send(new net.yigitguven.claim.network.RequestUnclaimPayload(selectedClaimIds));
                                }
                                Minecraft.getInstance().setScreen(screen);
                            }, net.minecraft.network.chat.Component.literal("Are you REALLY sure?"), net.minecraft.network.chat.Component.literal("This will unclaim " + selectedClaimIds.size() + " claim(s).")));
                        } else {
                            Minecraft.getInstance().setScreen(screen);
                        }
                    }, net.minecraft.network.chat.Component.literal("Unclaim Selection"), net.minecraft.network.chat.Component.literal("Are you sure you want to unclaim " + selectedClaimIds.size() + " claim(s)?")));
                }
            });
        }
    }
}
