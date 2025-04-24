package vazkii.patchouli.markdown.parser.customcomponent;

import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.sequence.BasedSequence;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class CustomComponentNode extends Node {
    private final ResourceLocation type;
    private final Map<String, String> attributes;

    public CustomComponentNode(ResourceLocation type, Map<String, String> attributes, BasedSequence chars) {
        super(chars);
        this.type = type;
        this.attributes = attributes;
    }

    public ResourceLocation getType() {
        return type;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    @Override
    public @NotNull BasedSequence[] getSegments() {
        return new BasedSequence[]{getChars()};
    }
}
