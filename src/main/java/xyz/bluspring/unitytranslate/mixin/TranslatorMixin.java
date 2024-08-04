package xyz.bluspring.unitytranslate.mixin;

import net.suuft.libretranslate.Translator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = Translator.class, remap = false)
public class TranslatorMixin {
    // Hard disable the printStackTrace, it spams console way too much.
    @Redirect(method = "translate(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;", at = @At(value = "INVOKE", target = "Ljava/lang/Exception;printStackTrace()V"))
    private static void unitytranslate$fuckOff(Exception instance) {

    }
}
