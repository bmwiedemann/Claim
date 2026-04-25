package net.yigitguven.claim.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xaero.lib.client.gui.widget.Tooltip;
import xaero.map.gui.TooltipButton;

import java.util.function.Supplier;

@Mixin(value = TooltipButton.class, remap = false)
public abstract class TooltipButtonMixin
{
    @Shadow
    protected Supplier<Tooltip> tooltipSupplier;

    @Inject(method = "getXaero_tooltip", at = @At("HEAD"), cancellable = true)
    private void claim$getCustomTooltip(CallbackInfoReturnable<Supplier<Tooltip>> cir)
    {
        // Check if this is the claims button
        // We can't easily check from here without more context,
        // so we might want to set a flag on the button itself.
    }
}
