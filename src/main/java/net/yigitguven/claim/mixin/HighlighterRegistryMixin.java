package net.yigitguven.claim.mixin;

import net.yigitguven.claim.integration.XaeroMapIntegration;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xaero.map.highlight.HighlighterRegistry;

@Mixin(value = HighlighterRegistry.class, remap = false)
public abstract class HighlighterRegistryMixin
{
    @Shadow
    public abstract void register(xaero.map.highlight.AbstractHighlighter provider);

    @Inject(method = "<init>", at = @At("TAIL"))
    private void claim$init(CallbackInfo ci)
    {
        this.register(new XaeroMapIntegration());
    }
}
