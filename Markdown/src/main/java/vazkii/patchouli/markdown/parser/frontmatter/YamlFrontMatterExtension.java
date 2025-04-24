package vazkii.patchouli.markdown.parser.frontmatter;

import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.data.MutableDataHolder;
import com.vladsch.flexmark.util.misc.Extension;

public class YamlFrontMatterExtension implements Parser.ParserExtension {
    public static Extension create() {
        return new YamlFrontMatterExtension();
    }

    @Override
    public void parserOptions(MutableDataHolder options) {}

    @Override
    public void extend(Parser.Builder parserBuilder) {
        parserBuilder.customBlockParserFactory(new YamlFrontMatterBlockParser.Factory());
    }
}
