package vazkii.patchouli.rewrite.api.data;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;
import java.util.Optional;

public record BookCategory(
        Component name,
        Component description,
        Optional<ResourceLocation> parent,
        String addedBy,
        ImmutableMap<ResourceLocation, BookCategory> children,
        ImmutableMap<ResourceLocation, BookEntry> entries
) {
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        public static final MapCodec<Builder> MAP_CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                ComponentSerialization.CODEC.fieldOf("name").forGetter(Builder::getName),
                ComponentSerialization.CODEC.fieldOf("description").forGetter(Builder::getDescription),
                ResourceLocation.CODEC.optionalFieldOf("parent").forGetter(Builder::getParent)
        ).apply(inst, Builder::new));
        public static final Codec<Builder> CODEC = MAP_CODEC.codec();

        private final ImmutableMap.Builder<ResourceLocation, BookEntry> entries = ImmutableMap.builder();
        private final ImmutableMap.Builder<ResourceLocation, BookCategory> children = ImmutableMap.builder();
        private Component name;
        private Component description;
        private Optional<ResourceLocation> parent = Optional.empty();

        private Builder() {}

        private Builder(Component name, Component description, Optional<ResourceLocation> parent) {
            this.name = name;
            this.description = description;
            this.parent = parent;
        }

        public Builder addEntry(ResourceLocation id, BookEntry entry) {
            this.entries.put(id, entry);
            return this;
        }

        public Builder addEntries(Map<ResourceLocation, BookEntry> entries) {
            this.entries.putAll(entries);
            return this;
        }

        public Builder addChild(ResourceLocation id, BookCategory category) {
            this.children.put(id, category);
            return this;
        }

        public Builder addChildren(Map<ResourceLocation, BookCategory> children) {
            this.children.putAll(children);
            return this;
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

        public Component getName() {
            return name;
        }

        public Builder withDescription(Component description) {
            this.description = description;
            return this;
        }

        public Builder withDescription(String description) {
            return this.withDescription(Component.literal(description));
        }

        public Builder withDescriptionTranslation(String translationKey) {
            return this.withDescription(Component.translatable(translationKey));
        }

        public Builder withDescriptionTranslation(String translationKey, String fallback) {
            return this.withDescription(Component.translatableWithFallback(translationKey, fallback));
        }

        public Component getDescription() {
            return description;
        }

        public Builder withParent(Optional<ResourceLocation> parent) {
            this.parent = parent;
            return this;
        }

        public Builder withParent(ResourceLocation parent) {
            return this.withParent(Optional.of(parent));
        }

        public Optional<ResourceLocation> getParent() {
            return this.parent;
        }

        public BookCategory build(String addedBy) {
            return new BookCategory(this.name, this.description, this.parent, addedBy, this.children.build(), this.entries.build());
        }
    }
}
