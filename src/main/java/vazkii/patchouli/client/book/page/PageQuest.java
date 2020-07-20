package vazkii.patchouli.client.book.page;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import vazkii.patchouli.client.base.ClientAdvancements;
import vazkii.patchouli.client.base.PersistentData;
import vazkii.patchouli.client.base.PersistentData.DataHolder.BookData;
import vazkii.patchouli.client.book.BookEntry;
import vazkii.patchouli.client.book.gui.GuiBook;
import vazkii.patchouli.client.book.gui.GuiBookEntry;
import vazkii.patchouli.client.book.page.abstr.PageWithText;
import vazkii.patchouli.common.book.Book;

public class PageQuest extends PageWithText {

	ResourceLocation trigger;
	String title;

	transient boolean isManual;

	@Override
	public int getTextHeight() {
		return 22;
	}

	@Override
	public void build(BookEntry entry, int pageNum) {
		super.build(entry, pageNum);

		isManual = trigger == null;
	}

	public boolean isCompleted(Book book) {
		return isManual
				? PersistentData.data.getBookData(book).completedManualQuests.contains(entry.getId().toString())
				: trigger != null && ClientAdvancements.hasDone(trigger.toString());
	}

	@Override
	public void onDisplayed(GuiBookEntry parent, int left, int top) {
		super.onDisplayed(parent, left, top);

		if (isManual) {
			Button button = new Button(GuiBook.PAGE_WIDTH / 2 - 50, GuiBook.PAGE_HEIGHT - 35, 100, 20, StringTextComponent.EMPTY, this::questButtonClicked);
			addButton(button);
			updateButtonText(button);
		}
	}

	private void updateButtonText(Button button) {
		boolean completed = isCompleted(parent.book);
		ITextComponent s = new TranslationTextComponent(completed ? "patchouli.gui.lexicon.mark_incomplete" : "patchouli.gui.lexicon.mark_complete");
		button.setMessage(s);
	}

	protected void questButtonClicked(Button button) {
		String res = entry.getId().toString();
		BookData data = PersistentData.data.getBookData(parent.book);

		if (data.completedManualQuests.contains(res)) {
			data.completedManualQuests.remove(res);
		} else {
			data.completedManualQuests.add(res);
		}
		PersistentData.save();

		updateButtonText(button);
		entry.markReadStateDirty();
	}

	@Override
	public void render(MatrixStack ms, int mouseX, int mouseY, float pticks) {
		super.render(ms, mouseX, mouseY, pticks);

		parent.drawCenteredStringNoShadow(ms, title == null || title.isEmpty() ? I18n.format("patchouli.gui.lexicon.objective") : i18n(title), GuiBook.PAGE_WIDTH / 2, 0, book.headerColor);
		GuiBook.drawSeparator(ms, book, 0, 12);

		if (!isManual) {
			GuiBook.drawSeparator(ms, book, 0, GuiBook.PAGE_HEIGHT - 25);

			boolean completed = isCompleted(parent.book);
			String s = I18n.format(completed ? "patchouli.gui.lexicon.complete" : "patchouli.gui.lexicon.incomplete");
			int color = completed ? 0x008b1a : book.headerColor;

			parent.drawCenteredStringNoShadow(ms, s, GuiBook.PAGE_WIDTH / 2, GuiBook.PAGE_HEIGHT - 17, color);
		}

	}

}
