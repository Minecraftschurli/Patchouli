package vazkii.patchouli.markdown.parser.frontmatter;

import com.vladsch.flexmark.util.ast.Block;
import com.vladsch.flexmark.util.sequence.BasedSequence;
import org.jetbrains.annotations.NotNull;

public class YamlFrontMatterBlock extends Block {
    Object data;

    public Object getData() {
        return this.data;
    }

    @Override
    public @NotNull BasedSequence[] getSegments() {
        return new BasedSequence[0];
    }
}
