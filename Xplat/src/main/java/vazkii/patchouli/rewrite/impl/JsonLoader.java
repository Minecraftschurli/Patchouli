package vazkii.patchouli.rewrite.impl;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import vazkii.patchouli.rewrite.api.Loader;

import java.io.IOException;
import java.io.Reader;
import java.util.Map;

public class JsonLoader<T> implements Loader<JsonElement, T> {
    private final Gson gson;
    private final Codec<T> codec;

    public JsonLoader(Gson gson, Codec<T> codec) {
        this.gson = gson;
        this.codec = codec;
    }

    @Override
    public boolean handles(ResourceLocation id, String extension) {
        return "json".equals(extension);
    }

    @Override
    public JsonElement read(ResourceLocation id, String extension, Resource resource, ResourceManager resourceManager) throws IOException {
        try(Reader reader = resource.openAsReader()) {
            return GsonHelper.fromJson(gson, reader, JsonElement.class);
        }
    }

    @Override
    public DataResult<Map<ResourceLocation, T>> parse(ResourceLocation location, JsonElement data, HolderLookup.Provider registries) {
        return this.codec.parse(registries.createSerializationContext(JsonOps.INSTANCE), data).map(it -> Map.of(location, it));
    }
}
