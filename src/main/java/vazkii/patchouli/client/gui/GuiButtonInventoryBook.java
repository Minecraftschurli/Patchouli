package vazkii.patchouli.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;

import vazkii.patchouli.client.RenderHelper;
import vazkii.patchouli.client.book.BookContents;
import vazkii.patchouli.client.book.EntryDisplayState;
import vazkii.patchouli.client.book.gui.GuiBook;
import vazkii.patchouli.common.base.Patchouli;
import vazkii.patchouli.common.book.Book;

public class GuiButtonInventoryBook extends Button {

	private Book book;

	public GuiButtonInventoryBook(Book book, int x, int y) {
		super(x, y, 20, 20, StringTextComponent.EMPTY, (b) -> {
			BookContents contents = book.contents;
			contents.openLexiconGui(contents.getCurrentGui(), false);
		});
		this.book = book;
	}

	@Override
	public void renderButton(MatrixStack ms, int mouseX, int mouseY, float pticks) {
		Minecraft mc = Minecraft.getInstance();
		mc.getTextureManager().bindTexture(new ResourceLocation(Patchouli.MOD_ID, "textures/gui/inventory_button.png"));
		RenderSystem.color3f(1F, 1F, 1F);

		boolean hovered = mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
		AbstractGui.blit(ms, x, y, (hovered ? 20 : 0), 0, width, height, 64, 64);

		ItemStack stack = book.getBookItem();
		RenderHelper.renderItemStackInGui(ms, stack, x + 2, y + 2);

		EntryDisplayState readState = book.contents.getReadState();
		if (readState.hasIcon && readState.showInInventory) {
			GuiBook.drawMarking(ms, book, x, y, 0, readState);
		}
	}

	public Book getBook() {
		return book;
	}

}
