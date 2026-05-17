package net.silvertide.pmmo_animalweights_compat.compat;

import net.minecraft.world.item.ItemStack;
import vectorwing.farmersdelight.common.item.KnifeItem;

public final class FarmersDelightCompat {
    private FarmersDelightCompat() {}

    public static boolean isKnife(ItemStack stack) {
        return stack != null && stack.getItem() instanceof KnifeItem;
    }
}
