package xyz.bluspring.unitytranslate.mixin;

import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(SignBlockEntity.class)
public interface SignBlockEntityAccessor {
    @Invoker
    Component[] callGetMessages(boolean filtered);
}
