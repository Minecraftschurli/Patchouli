package vazkii.patchouli.markdown;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JavaOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.vladsch.flexmark.util.ast.Node;
import org.junit.jupiter.api.Test;
import vazkii.patchouli.markdown.parser.MarkdownParser;
import vazkii.patchouli.markdown.parser.frontmatter.YamlFrontMatterBlock;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class MarkdownTest {

    @SuppressWarnings("unchecked")
    @Test
    public void test() throws IOException {
        InputStream input = MarkdownTest.class.getResourceAsStream("/markdown.md");
        Node parsed = MarkdownParser.parse(input);
        Object data = assertInstanceOf(YamlFrontMatterBlock.class, parsed.getFirstChild()).getData();
        DataResult.Success<TestObject> dataResult = assertInstanceOf(DataResult.Success.class, TestObject.CODEC.parse(JavaOps.INSTANCE, data));
        assertEquals(new TestObject(List.of("value 1", "value 2"), new Inner(1, "test value", true, List.of("test"))), dataResult.value());
        // todo test rest of parsing
    }

    public record TestObject(List<String> list, Inner object) {
        public static final Codec<TestObject> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                Codec.STRING.listOf().fieldOf("list").forGetter(TestObject::list),
                Inner.CODEC.fieldOf("object").forGetter(TestObject::object)
        ).apply(inst, TestObject::new));
    }

    public record Inner(int key1, String key2, boolean key3, List<String> key4) {
        public static final Codec<Inner> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                Codec.INT.fieldOf("key1").forGetter(Inner::key1),
                Codec.STRING.fieldOf("key2").forGetter(Inner::key2),
                Codec.BOOL.fieldOf("key3").forGetter(Inner::key3),
                Codec.STRING.listOf().fieldOf("key4").forGetter(Inner::key4)
        ).apply(inst, Inner::new));
    }
}
