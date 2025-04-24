package vazkii.patchouli.rewrite.api;

import com.mojang.serialization.DataResult;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.IOException;
import java.util.Map;

public interface Loader<TRaw, T> {
    boolean handles(ResourceLocation id, String extension);

    TRaw read(ResourceLocation id, String extension, Resource resource, ResourceManager resourceManager) throws IOException;

    DataResult<Map<ResourceLocation, T>> parse(ResourceLocation id, TRaw data, HolderLookup.Provider registries);
}
