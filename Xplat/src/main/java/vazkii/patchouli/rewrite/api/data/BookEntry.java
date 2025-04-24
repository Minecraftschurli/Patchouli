package vazkii.patchouli.rewrite.api.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

import java.util.function.Function;

public record BookEntry(
        Component name,
        BookIcon icon,
        ResourceLocation categoryId,
        // TODO content
        String addedBy
) {
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        public static final MapCodec<Builder> METADATA_MAP_CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                ComponentSerialization.CODEC.fieldOf("name").forGetter(Builder::getName),
                BookIcon.CODEC.fieldOf("icon").forGetter(Builder::getIcon),
                ResourceLocation.CODEC.fieldOf("category").forGetter(Builder::getCategoryId)
        ).apply(inst, Builder::new));
        public static final Codec<Builder> METADATA_CODEC = METADATA_MAP_CODEC.codec();
        public static final MapCodec<Builder> MAP_CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                METADATA_MAP_CODEC.forGetter(Function.identity())
        ).apply(inst, ((builder) -> builder)));
        public static final Codec<Builder> CODEC = MAP_CODEC.codec();

        private Component name;
        private BookIcon icon;
        private ResourceLocation categoryId;

        private Builder() {}

        private Builder(Component name, BookIcon icon, ResourceLocation categoryId) {
            this.name = name;
            this.icon = icon;
            this.categoryId = categoryId;
        }

        public Builder withName(Component name) {
            this.name = name;
            return this;
        }

        public Builder withName(String name) {
            return this.withName(Component.literal(name));
        }

        public Builder withNameTranslation(String translationKey) {
            return this.withName(Component.translatable(translationKey));
        }

        public Builder withNameTranslation(String translationKey, String fallback) {
            return this.withName(Component.translatableWithFallback(translationKey, fallback));
        }

        public Builder withIcon(BookIcon icon) {
            this.icon = icon;
            return this;
        }

        public Builder withIcon(ItemStack stack) {
            return this.withIcon(new BookIcon.Stack(stack));
        }

        public Builder withIcon(Holder<Item> item) {
            return this.withIcon(new BookIcon.Stack(new ItemStack(item)));
        }

        public Builder withIcon(ItemLike item) {
            return this.withIcon(new BookIcon.Stack(new ItemStack(item)));
        }

        public Builder withIcon(ResourceLocation image) {
            return this.withIcon(new BookIcon.Image(image));
        }

        public Builder withCategoryId(ResourceLocation categoryId) {
            this.categoryId = categoryId;
            return this;
        }

        public Component getName() {
            return name;
        }
        
        public BookIcon getIcon() {
            return icon;
        }

        public ResourceLocation getCategoryId() {
            return this.categoryId;
        }

        public BookEntry build(String addedBy) {
            return new BookEntry(this.name, this.icon, this.categoryId, addedBy);
        }
    }
}
