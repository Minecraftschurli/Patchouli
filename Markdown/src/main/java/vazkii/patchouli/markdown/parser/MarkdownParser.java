package vazkii.patchouli.markdown.parser;

import com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughExtension;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import vazkii.patchouli.markdown.parser.customcomponent.CustomComponentExtension;
import vazkii.patchouli.markdown.parser.frontmatter.YamlFrontMatterExtension;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;

public abstract class MarkdownParser {
    private static final Parser PARSER = Parser
            .builder()
            .extensions(List.of(
                    YamlFrontMatterExtension.create(),
                    TablesExtension.create(),
                    StrikethroughExtension.create(),
                    CustomComponentExtension.create()
            ))
            .build();

    private MarkdownParser() { throw new IllegalAccessError("Utility class"); }

    public static Node parse(String markdown) {
        return PARSER.parse(markdown);
    }

    public static Node parse(InputStream stream) throws IOException {
        try (Reader reader = new InputStreamReader(stream)) {
            return parse(reader);
        }
    }

    public static Node parse(Reader reader) throws IOException {
        return PARSER.parseReader(reader);
    }
}
