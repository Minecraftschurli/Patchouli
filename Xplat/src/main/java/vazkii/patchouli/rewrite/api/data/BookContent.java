package vazkii.patchouli.rewrite.api.data;

import com.google.common.collect.ImmutableMap;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.CreativeModeTab;

import java.util.Map;

public record BookContent(
        Component title,
        Component subtitle,
        Component landingText,
        ResourceKey<CreativeModeTab> creativeTab,
        ResourceKey<SoundEvent> openSound,
        ResourceKey<SoundEvent> flipSound,
        ImmutableMap<ResourceLocation, BookCategory> categories,
        ImmutableMap<ResourceLocation, BookEntry> entries
) {
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final ImmutableMap.Builder<ResourceLocation, BookCategory> categories = ImmutableMap.builder();
        private final ImmutableMap.Builder<ResourceLocation, BookEntry> entries = ImmutableMap.builder();
        private Component title;
        private Component subtitle;
        private Component landingText;
        private ResourceKey<CreativeModeTab> creativeTab;
        private ResourceKey<SoundEvent> openSound;
        private ResourceKey<SoundEvent> flipSound;

        private Builder() {}

        public Builder addCategory(ResourceLocation id, BookCategory category) {
            this.categories.put(id, category);
            return this;
        }

        public Builder addEntry(ResourceLocation id, BookEntry entry) {
            this.entries.put(id, entry);
            return this;
        }

        public Builder addCategories(Map<ResourceLocation, BookCategory> categories) {
            this.categories.putAll(categories);
            return this;
        }

        public Builder addEntries(Map<ResourceLocation, BookEntry> entries) {
            this.entries.putAll(entries);
            return this;
        }
        
        public Builder title(Component title) {
            this.title = title;
            return this;
        }

        public Builder title(String title) {
            return this.title(Component.literal(title));
        }

        public Builder titleTranslation(String translationKey) {
            return this.title(Component.translatable(translationKey));
        }

        public Builder titleTranslation(String translationKey, String fallback) {
            return this.title(Component.translatableWithFallback(translationKey, fallback));
        }

        public Builder subtitle(Component subtitle) {
            this.subtitle = subtitle;
            return this;
        }

        public Builder subtitle(String subtitle) {
            return this.subtitle(Component.literal(subtitle));
        }

        public Builder subtitleTranslation(String translationKey) {
            return this.subtitle(Component.translatable(translationKey));
        }

        public Builder subtitleTranslation(String translationKey, String fallback) {
            return this.subtitle(Component.translatableWithFallback(translationKey, fallback));
        }
        
        public Builder landingText(Component landingText) {
            this.landingText = landingText;
            return this;
        }

        public Builder landingText(String landingText) {
            return this.landingText(Component.literal(landingText));
        }

        public Builder landingTextTranslation(String translationKey) {
            return this.landingText(Component.translatable(translationKey));
        }

        public Builder landingText(String translationKey, String fallback) {
            return this.landingText(Component.translatableWithFallback(translationKey, fallback));
        }

        public Builder creativeTab(ResourceKey<CreativeModeTab> creativeTab) {
            this.creativeTab = creativeTab;
            return this;
        }

        public Builder creativeTab(ResourceLocation creativeTab) {
            return this.creativeTab(ResourceKey.create(Registries.CREATIVE_MODE_TAB, creativeTab));
        }

        public Builder openSound(ResourceKey<SoundEvent> openSound) {
            this.openSound = openSound;
            return this;
        }

        public Builder openSound(ResourceLocation openSound) {
            return this.openSound(ResourceKey.create(Registries.SOUND_EVENT, openSound));
        }

        public Builder flipSound(ResourceKey<SoundEvent> flipSound) {
            this.flipSound = flipSound;
            return this;
        }

        public Builder flipSound(ResourceLocation flipSound) {
            return this.flipSound(ResourceKey.create(Registries.SOUND_EVENT, flipSound));
        }

        public BookContent build() {
            return new BookContent(
                    title,
                    subtitle,
                    landingText,
                    creativeTab,
                    openSound,
                    flipSound,
                    categories.build(),
                    entries.build()
            );
        }
    }
}
