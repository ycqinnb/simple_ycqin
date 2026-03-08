package yc.ycqin.nb.common.blade.sa;

import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import mods.flammpfeil.slashblade.named.event.LoadEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class SaLoader {
    @SubscribeEvent
    public void InitSA(LoadEvent.InitEvent event) {
        ItemSlashBlade.specialAttacks.put(114, new OrbBoomSA());
    }
}