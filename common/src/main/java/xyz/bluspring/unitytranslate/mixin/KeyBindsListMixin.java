package xyz.bluspring.unitytranslate.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.screens.controls.KeyBindsList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.bluspring.unitytranslate.UnityTranslate;

@Mixin(KeyBindsList.class)
public abstract class KeyBindsListMixin extends ContainerObjectSelectionList<KeyBindsList.Entry> {
    public KeyBindsListMixin(Minecraft minecraft, int width, int height, int y0, int y1, int itemHeight) {
        super(minecraft, width, height, y0, y1, itemHeight);
    }

    @WrapWithCondition(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/controls/KeyBindsList;addEntry(Lnet/minecraft/client/gui/components/AbstractSelectionList$Entry;)I"))
    private boolean ut$removeUnityEntries(KeyBindsList instance, AbstractSelectionList.Entry<?> entry, @Local KeyMapping mapping) {
        if (mapping.getCategory().equals("UnityTranslate") && UnityTranslate.IS_UNITY_SERVER && !UnityTranslate.Companion.getConfig().getClient().getEnabled())
            return false;

        return true;
    }
}
