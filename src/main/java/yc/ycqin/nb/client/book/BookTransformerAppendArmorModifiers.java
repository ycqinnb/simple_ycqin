package yc.ycqin.nb.client.book;

import slimeknights.mantle.client.book.data.BookData;
import slimeknights.mantle.client.book.data.PageData;
import slimeknights.mantle.client.book.data.SectionData;
import slimeknights.mantle.client.book.repository.BookRepository;
import slimeknights.tconstruct.library.book.content.ContentListing;
import slimeknights.tconstruct.library.book.sectiontransformer.SectionTransformer;
import slimeknights.tconstruct.library.modifiers.Modifier;
import yc.ycqin.nb.register.TinkerTraitsRegister;

import java.util.ArrayList;
import java.util.List;

public class BookTransformerAppendArmorModifiers extends SectionTransformer {
    private final BookRepository source;

    public BookTransformerAppendArmorModifiers(BookRepository source) {
        super("modifiers");
        this.source = source;
    }

    @Override
    public void transform(BookData book, SectionData section) {
        ContentListing listing = (ContentListing)((PageData)section.pages.get(0)).content;

        List<Modifier> modifiers = new ArrayList<>();
        modifiers.add(TinkerTraitsRegister.traitMinDamageProtect);
        modifiers.add(TinkerTraitsRegister.traitAdaptation);

        for (Modifier mod : modifiers) {
            PageData page = new PageData();
            page.source = this.source;
            page.parent = section;
            page.type = "armormodifier";
            page.data = "modifiers/" + mod.identifier + ".json";
            section.pages.add(page);
            page.load();

            listing.addEntry(mod.getLocalizedName(), page);
        }
    }
}
