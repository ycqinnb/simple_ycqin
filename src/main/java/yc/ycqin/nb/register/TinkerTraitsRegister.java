package yc.ycqin.nb.register;


import slimeknights.mantle.client.book.repository.FileRepository;
import slimeknights.tconstruct.library.TinkerRegistry;
import slimeknights.tconstruct.library.book.TinkerBook;
import yc.ycqin.nb.client.book.BookTransformerAppendModifiers;
import yc.ycqin.nb.common.trait.*;
import yc.ycqin.nb.proxy.CommonProxy;

public class TinkerTraitsRegister {
    public static final TraitReduceAdaptation  traitReduceAdaptation = new TraitReduceAdaptation();
    public static final TraitMinDamage traitMinDamage = new TraitMinDamage();
    public static final TraitrooterStrengthen traitrooterStrengthen = new TraitrooterStrengthen();
    public static final TraitdispatcherStrengthen traitdispatcherStrengthen = new TraitdispatcherStrengthen();
    public static final TraitbeckonStrengthen traitbeckonStrengthen = new TraitbeckonStrengthen();
    public static final TraitVirus traitVirus = new TraitVirus();
    public TinkerTraitsRegister() {
        TinkerRegistry.addTrait(traitVirus);
        TinkerRegistry.addTrait(traitMinDamage);
        TinkerRegistry.addTrait(traitReduceAdaptation);
        TinkerRegistry.addTrait(traitbeckonStrengthen);
        TinkerRegistry.addTrait(traitdispatcherStrengthen);
        TinkerRegistry.addTrait(traitrooterStrengthen);
        addItems();
    }
    public void addItems(){
        traitMinDamage.addItem(CommonProxy.getStackFromName("srparasites","living_core",1),1,1);
        traitbeckonStrengthen.addItem(CommonProxy.getStackFromName("srparasites","beckon_drop",1),1,1);
        traitdispatcherStrengthen.addItem(CommonProxy.getStackFromName("srparasites","dispatcher_drop",1),1,1);
        traitrooterStrengthen.addItem(ItemsRegister.ROOTERDROP,1,1);
        traitVirus.addItem(CommonProxy.getStackFromName("srparasites","lurecomponent4",1),1,1);
        traitReduceAdaptation.addItem(CommonProxy.getStackFromName("srparasites","infectious_blade_fragment",1),1,1);
    }
    public static void addbook(){
        TinkerBook.INSTANCE.addTransformer(new BookTransformerAppendModifiers(new FileRepository("tconstruct:book")));
    }

}