package vazkii.patchouli.rewrite.api.data;

import net.minecraft.resources.ResourceLocation;

public record BookData(ResourceLocation id) {
    public static class Builder {
        private final ResourceLocation id;

        public Builder(ResourceLocation id) {
            this.id = id;
        }
        
        public BookData build() {
            return new BookData(id);
        }
    }
}
