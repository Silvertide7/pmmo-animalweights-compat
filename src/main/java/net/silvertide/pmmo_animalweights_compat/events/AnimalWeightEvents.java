package net.silvertide.pmmo_animalweights_compat.events;


import com.leclowndu93150.animalweights.WeightAttachment;
import harmonised.pmmo.api.APIUtils;
import harmonised.pmmo.core.Core;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.LogicalSide;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.BabyEntitySpawnEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.silvertide.pmmo_animalweights_compat.PMMOAnimalWeights;
import net.silvertide.pmmo_animalweights_compat.compat.FarmersDelightCompat;
import net.silvertide.pmmo_animalweights_compat.config.ServerConfigs;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@EventBusSubscriber(modid = PMMOAnimalWeights.MODID)
public class AnimalWeightEvents {

    private static final boolean FD_LOADED = ModList.get().isLoaded("farmersdelight");

    private static final Map<Integer, Double> killMultiplier = Map.of(
            0, 0D,
            1, 0D,
            2, 1.5D,
            3, 2.5D,
            4, 4D,
            5, 6D,
            6, 9D,
            7, 12D,
            8, 17D
    );


    @SubscribeEvent()
    public static void onAnimalDeath(LivingDeathEvent livingDeathEvent) {
        if(!(livingDeathEvent.getEntity() instanceof Animal animal)) return;
        if(!(livingDeathEvent.getSource().getEntity() instanceof ServerPlayer killer)) return;

        int weight = Math.clamp(WeightAttachment.getWeight(animal), 0, 8);
        if(weight < 2) return;

        ItemStack weapon = livingDeathEvent.getSource().getWeaponItem();
        double knifeBonus = (FD_LOADED && FarmersDelightCompat.isKnife(weapon))
                ? ServerConfigs.FARMERS_DELIGHT_KNIFE_KILL_BONUS.get()
                : 1.0;
        double multiplier = killMultiplier.get(weight) * knifeBonus;

        Map<String, Long> scaled = new HashMap<>();
        APIUtils.getXpAwardMap(animal, ServerConfigs.KILL_EVENT_TYPE.get(), LogicalSide.SERVER, killer)
                .forEach((skill, xp) -> {
                    long s = Math.round(xp * multiplier);
                    if (s > 0) scaled.put(skill, s);
                });
        if (!scaled.isEmpty()) Core.get(LogicalSide.SERVER).awardXP(List.of(killer), scaled);
    }

    private static final Map<Integer, Double> breedMultiplier = Map.of(
            0, 0D,
            1, 0D,
            2, 1.0D,
            3, 1.5D,
            4, 2D,
            5, 2.5D,
            6, 3D,
            7, 5D,
            8, 7D
    );

    @SubscribeEvent()
    public static void onAnimalBorn(BabyEntitySpawnEvent babyEntitySpawnEvent) {
        if(!(babyEntitySpawnEvent.getCausedByPlayer() instanceof ServerPlayer player)) return;
        if(!(babyEntitySpawnEvent.getParentA() instanceof Animal parentA)) return;
        if(!(babyEntitySpawnEvent.getParentB() instanceof Animal parentB)) return;

        int weightA = WeightAttachment.getWeight(parentA);
        int weightB = WeightAttachment.getWeight(parentB);

        int averageWeight = Math.clamp((weightA + weightB) / 2, 0, 8);
        if (averageWeight < 2) return;
        double multiplier = breedMultiplier.get(averageWeight);

        Map<String, Long> scaled = new HashMap<>();
        APIUtils.getXpAwardMap(parentA, ServerConfigs.BREED_EVENT_TYPE.get(), LogicalSide.SERVER, player)
                .forEach((skill, xp) -> {
                    long s = Math.round(xp * multiplier);
                    if (s > 0) scaled.put(skill, s);
                });
        if (!scaled.isEmpty()) Core.get(LogicalSide.SERVER).awardXP(List.of(player), scaled);

        // Set the breed cooldown longer for animals
        MinecraftServer server = parentA.level().getServer();
        if (server == null) return;
        int breedCooldownTicks = ServerConfigs.ANIMAL_BREED_COOLDOWN_SECONDS.get() * 20;

        server.tell(new TickTask(server.getTickCount() + 1, () -> {
            parentA.setAge(breedCooldownTicks);
            parentB.setAge(breedCooldownTicks);
        }));
    }
}
