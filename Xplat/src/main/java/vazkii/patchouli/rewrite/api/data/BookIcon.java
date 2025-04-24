package vazkii.patchouli.rewrite.api.data;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public sealed interface BookIcon {
    Codec<BookIcon> CODEC = Codec.either(ItemStack.CODEC, ResourceLocation.CODEC).xmap(
            e -> e.map(Stack::new, Image::new),
            i -> switch (i) {
                case Image(ResourceLocation image) -> Either.right(image);
                case Stack(ItemStack stack) -> Either.left(stack);
            });

    record Stack(ItemStack stack) implements BookIcon {}
    record Image(ResourceLocation image) implements BookIcon {}
}
