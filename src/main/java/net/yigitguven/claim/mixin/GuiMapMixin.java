package net.yigitguven.claim.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.yigitguven.claim.network.RequestClaimPayload;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xaero.map.gui.GuiMap;
import xaero.map.gui.MapTileSelection;
import xaero.map.gui.dropdown.rightclick.RightClickOption;

import java.util.ArrayList;

@Mixin(value = GuiMap.class, remap = false)
public abstract class GuiMapMixin extends net.minecraft.client.gui.screens.Screen
{
    protected GuiMapMixin(net.minecraft.network.chat.Component title) { super(title); }

    @Shadow
    private MapTileSelection mapTileSelection;

    @Inject(method = "getRightClickOptions", at = @At("RETURN"))
    private void claim$addOptions(CallbackInfoReturnable<ArrayList<RightClickOption>> cir)
    {
        ArrayList<RightClickOption> options = cir.getReturnValue();
        if (options == null || mapTileSelection == null) return;

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
                    Minecraft.getInstance().getConnection().send(new RequestClaimPayload(p1, p2));
                }
            }
        });
    }
}
