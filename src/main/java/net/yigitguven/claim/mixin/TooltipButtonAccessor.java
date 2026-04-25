package net.yigitguven.claim.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import xaero.lib.client.gui.widget.Tooltip;
import xaero.map.gui.TooltipButton;

import java.util.function.Supplier;

@Mixin(value = TooltipButton.class, remap = false)
public interface TooltipButtonAccessor
{
    @Accessor("tooltipSupplier")
    void setTooltipSupplier(Supplier<Tooltip> supplier);
}
