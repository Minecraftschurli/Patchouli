package vazkii.patchouli.markdown.parser.customcomponent;

import com.vladsch.flexmark.parser.InlineParser;
import com.vladsch.flexmark.parser.InlineParserExtension;
import com.vladsch.flexmark.parser.InlineParserExtensionFactory;
import com.vladsch.flexmark.parser.LightInlineParser;
import com.vladsch.flexmark.util.sequence.BasedSequence;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CustomComponentInlineParser implements InlineParserExtension {
    private static final Pattern COMPONENT_PATTERN = Pattern.compile("\\[([a-z0-9_.-]+:[a-z0-9/._-]+)\\s*(.*?)]");
    private static final Pattern ATTRIBUTE_PATTERN = Pattern.compile("(\\w+)=\"(.*?)\"");

    @Override
    public void finalizeDocument(@NotNull InlineParser inlineParser) {}

    @Override
    public void finalizeBlock(@NotNull InlineParser inlineParser) {}

    @Override
    public boolean parse(@NotNull LightInlineParser inlineParser) {
        BasedSequence[] groups = inlineParser.matchWithGroups(COMPONENT_PATTERN);
        if (groups == null || groups.length != 3 || groups[1].isBlank()) return false;
        ResourceLocation type = ResourceLocation.tryParse(groups[1].unescape());
        if (type == null) return false;
        Map<String, String> attributes = parseAttributes(groups[2]);

        inlineParser.appendNode(new CustomComponentNode(type, attributes, groups[0]));
        return true;
    }

    private Map<String, String> parseAttributes(BasedSequence attrString) {
        Map<String, String> attributes = new HashMap<>();
        Matcher attrMatcher = ATTRIBUTE_PATTERN.matcher(attrString);
        while (attrMatcher.find()) {
            attributes.put(attrMatcher.group(1), attrMatcher.group(2));
        }
        return attributes;
    }

    public static class Factory implements InlineParserExtensionFactory {

        @Override
        public @NotNull InlineParserExtension apply(@NotNull LightInlineParser inlineParser) {
            return new CustomComponentInlineParser();
        }

        @Override
        public @NotNull CharSequence getCharacters() {
            return "[";
        }

        @Override
        public boolean affectsGlobalScope() {
            return false;
        }

        @Override
        public @Nullable Set<Class<?>> getAfterDependents() {
            return Set.of();
        }

        @Override
        public @Nullable Set<Class<?>> getBeforeDependents() {
            return Set.of();
        }
    }

}
