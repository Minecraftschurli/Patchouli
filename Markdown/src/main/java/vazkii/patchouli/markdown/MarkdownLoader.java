package vazkii.patchouli.markdown;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.JavaOps;
import com.vladsch.flexmark.util.ast.Node;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import vazkii.patchouli.markdown.parser.MarkdownParser;
import vazkii.patchouli.markdown.parser.frontmatter.YamlFrontMatterBlock;
import vazkii.patchouli.rewrite.api.Loader;
import vazkii.patchouli.rewrite.api.data.BookEntry;

import java.io.IOException;
import java.io.Reader;
import java.util.Map;

public class MarkdownLoader implements Loader<Node, BookEntry.Builder> {

    @Override
    public boolean handles(ResourceLocation id, String extension) {
        return "md".equals(extension);
    }

    @Override
    public Node read(ResourceLocation id, String extension, Resource resource, ResourceManager resourceManager) throws IOException {
        try (Reader reader = resource.openAsReader()) {
            return MarkdownParser.parse(reader);
        }
    }

    @Override
    public DataResult<Map<ResourceLocation, BookEntry.Builder>> parse(ResourceLocation id, Node data, HolderLookup.Provider registries) {
        Object metadataRaw = data.getFirstChild() instanceof YamlFrontMatterBlock frontMatter ? frontMatter.getData() : null;
        DataResult<BookEntry.Builder> builderDr = BookEntry.Builder.METADATA_CODEC.parse(JavaOps.INSTANCE, metadataRaw);
        // todo fill entry content from ast
        return builderDr.map(builder -> Map.of(id, builder));
    }
}
