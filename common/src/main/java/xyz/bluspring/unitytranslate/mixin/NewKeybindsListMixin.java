package xyz.bluspring.unitytranslate.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.screens.controls.KeyBindsList;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import xyz.bluspring.unitytranslate.UnityTranslate;

@Pseudo
@Mixin(targets = "com.blamejared.controlling.client.NewKeyBindsList")
public class NewKeybindsListMixin {
    @Dynamic
    @WrapOperation(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyMapping;getCategory()Ljava/lang/String;"))
    private String ut$removeUnityEntries(KeyMapping mapping, Operation<String> original) {
        var category = original.call(mapping);

        if (category.equals("UnityTranslate") && UnityTranslate.IS_UNITY_SERVER && !UnityTranslate.Companion.getConfig().getClient().getEnabled())
            return category + ".hidden";

        return category;
    }
}
