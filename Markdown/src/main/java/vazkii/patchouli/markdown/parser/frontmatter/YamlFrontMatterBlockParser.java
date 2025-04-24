package vazkii.patchouli.markdown.parser.frontmatter;

import com.vladsch.flexmark.parser.block.*;
import com.vladsch.flexmark.util.ast.Block;
import com.vladsch.flexmark.util.data.DataHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.Yaml;

import java.util.Set;
import java.util.regex.Pattern;

public class YamlFrontMatterBlockParser extends AbstractBlockParser {
    private static final Pattern REGEX_BEGIN = Pattern.compile("^-{3}(\\s.*)?");
    private static final Pattern REGEX_END = Pattern.compile("^(-{3}|\\.{3})(\\s.*)?");

    private final Yaml yamlParser;
    private final YamlFrontMatterBlock block = new YamlFrontMatterBlock();
    private final StringBuilder content = new StringBuilder();

    private YamlFrontMatterBlockParser(Yaml yamlParser) {
        this.yamlParser = yamlParser;
    }


    @Override
    public Block getBlock() {
        return block;
    }

    @Override
    public BlockContinue tryContinue(ParserState state) {
        final CharSequence line = state.getLine();

        if (REGEX_END.matcher(line).matches()) {
            this.block.data = yamlParser.load(this.content.toString());
            return BlockContinue.finished();
        }

        this.content.append(line).append('\n');
        return BlockContinue.atIndex(state.getIndex());
    }

    @Override
    public void closeBlock(ParserState state) {}

    public static class Factory implements CustomBlockParserFactory {

        @Override
        public @NotNull BlockParserFactory apply(@NotNull DataHolder options) {
            return new BlockFactory(options);
        }

        @Override
        public @Nullable Set<Class<?>> getAfterDependents() {
            return Set.of();
        }

        @Override
        public @Nullable Set<Class<?>> getBeforeDependents() {
            return Set.of();
        }

        @Override
        public boolean affectsGlobalScope() {
            return false;
        }
    }

    private static class BlockFactory extends AbstractBlockParserFactory {
        private final Yaml yamlParser;

        BlockFactory(DataHolder options) {
            super(options);
            this.yamlParser = new Yaml();
        }

        @Override
        public BlockStart tryStart(ParserState state, MatchedBlockParser matchedBlockParser) {
            CharSequence line = state.getLine();
            BlockParser parentParser = matchedBlockParser.getBlockParser();
            // check whether this line is the first line of whole document or not
            if (parentParser.getBlock() != null && parentParser.getBlock().getFirstChild() == null && REGEX_BEGIN.matcher(line).matches()) {
                return BlockStart.of(new YamlFrontMatterBlockParser(yamlParser)).atIndex(state.getNextNonSpaceIndex());
            }

            return BlockStart.none();
        }
    }
}
