package vazkii.patchouli.client.book.gui.button;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

import vazkii.patchouli.client.base.ClientTicker;
import vazkii.patchouli.client.base.PersistentData;
import vazkii.patchouli.client.book.gui.GuiBook;

public class GuiButtonBookEye extends GuiButtonBook {

	public GuiButtonBookEye(GuiBook parent, int x, int y, Button.IPressable onPress) {
		super(parent, x, y, 308, 31, 11, 11, onPress,
				new TranslationTextComponent("patchouli.gui.lexicon.button.visualize"),
				new TranslationTextComponent("patchouli.gui.lexicon.button.visualize.info").func_240699_a_(TextFormatting.GRAY));
	}

	@Override
	public void renderButton(MatrixStack ms, int mouseX, int mouseY, float partialTicks) {
		super.renderButton(ms, mouseX, mouseY, partialTicks);

		if (!PersistentData.data.clickedVisualize && (ClientTicker.ticksInGame) % 20 < 10) {
			parent.getMinecraft().fontRenderer.drawStringWithShadow(ms, "!", x, y, 0xFF3333);
		}
	}

}
