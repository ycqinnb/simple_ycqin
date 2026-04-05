package yc.ycqin.nb.register;


import c4.conarm.lib.ArmoryRegistry;
import c4.conarm.lib.book.ArmoryBook;
import c4.conarm.lib.events.ArmoryEvent;
import c4.conarm.lib.utils.ConstructUtils;
import c4.conarm.lib.utils.RecipeMatchHolder;
import slimeknights.mantle.client.book.repository.FileRepository;
import slimeknights.tconstruct.library.TinkerRegistry;
import slimeknights.tconstruct.library.book.TinkerBook;
import yc.ycqin.nb.client.book.BookTransformerAppendArmorModifiers;
import yc.ycqin.nb.client.book.BookTransformerAppendModifiers;
import yc.ycqin.nb.common.trait.*;
import yc.ycqin.nb.common.trait.armorTrait.TraitAdaptation;
import yc.ycqin.nb.common.trait.armorTrait.TraitMinDamageProtect;
import yc.ycqin.nb.proxy.CommonProxy;

public class TinkerTraitsRegister {
    public static TraitReduceAdaptation  traitReduceAdaptation;
    public static TraitMinDamage traitMinDamage;
    public static TraitrooterStrengthen traitrooterStrengthen;
    public static TraitdispatcherStrengthen traitdispatcherStrengthen;
    public static TraitbeckonStrengthen traitbeckonStrengthen;
    public static TraitVirus traitVirus;
    public static TraitMinDamageProtect traitMinDamageProtect;
    public static TraitAdaptation traitAdaptation;
    public TinkerTraitsRegister() {
        traitMinDamage = new TraitMinDamage();
        traitVirus = new TraitVirus();
        traitReduceAdaptation = new TraitReduceAdaptation();
        traitdispatcherStrengthen = new TraitdispatcherStrengthen();
        traitrooterStrengthen = new TraitrooterStrengthen();
        traitbeckonStrengthen = new TraitbeckonStrengthen();

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
        if(CommonProxy.isTCArmorLoaded) ArmoryBook.INSTANCE.addTransformer(new BookTransformerAppendArmorModifiers(new FileRepository("conarm:book")));

    }
    public void regTCArmor(){
        traitMinDamageProtect = new TraitMinDamageProtect();
        traitAdaptation = new TraitAdaptation();
        if (CommonProxy.isTCArmorLoaded){
            RecipeMatchHolder.addItem(traitMinDamageProtect,CommonProxy.getStackFromName("srparasites","living_core",1),1,1);
            RecipeMatchHolder.addItem(traitAdaptation,CommonProxy.getStackFromName("srparasites","vile_shell",1),1,1);
        }
    }

}