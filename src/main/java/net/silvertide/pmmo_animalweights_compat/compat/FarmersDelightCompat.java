package net.silvertide.pmmo_animalweights_compat.compat;

import net.minecraft.world.item.ItemStack;
import vectorwing.farmersdelight.common.item.KnifeItem;

// Touched only when Farmer's Delight is loaded. Keep all references to FD classes
// (e.g. KnifeItem) inside this class so the JVM doesn't try to resolve them when
// FD is absent.
public final class FarmersDelightCompat {
    private FarmersDelightCompat() {}

    public static boolean isKnife(ItemStack stack) {
        return stack != null && stack.getItem() instanceof KnifeItem;
    }
}
