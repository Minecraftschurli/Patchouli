package vazkii.patchouli.markdown.parser.customcomponent;

import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.data.MutableDataHolder;

public class CustomComponentExtension implements Parser.ParserExtension {
    public static CustomComponentExtension create() {
        return new CustomComponentExtension();
    }

    @Override
    public void parserOptions(MutableDataHolder options) {}

    @Override
    public void extend(Parser.Builder parserBuilder) {
        parserBuilder.customInlineParserExtensionFactory(new CustomComponentInlineParser.Factory());
    }
}
