package vazkii.patchouli.rewrite.api.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

public record BookTemplate(
        // TODO template content
        String addedBy
) {
    public static class Builder {
        public static final MapCodec<Builder> MAP_CODEC = MapCodec.unit(Builder::new);
        public static final Codec<Builder> CODEC = MAP_CODEC.codec();

        private Builder() {}

        public BookTemplate build(String addedBy) {
            return new BookTemplate(addedBy);
        }
    }
}
