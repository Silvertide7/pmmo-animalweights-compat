package net.silvertide.pmmo_animalweights_compat.events;


import com.leclowndu93150.animalweights.WeightAttachment;
import harmonised.pmmo.api.APIUtils;
import harmonised.pmmo.core.Core;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.BabyEntitySpawnEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.silvertide.pmmo_animalweights_compat.PMMOAnimalWeights;
import net.silvertide.pmmo_animalweights_compat.compat.FarmersDelightCompat;
import net.silvertide.pmmo_animalweights_compat.config.ServerConfigs;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mod.EventBusSubscriber(modid = PMMOAnimalWeights.MODID)
public class AnimalWeightEvents {

    private static final boolean FD_LOADED = ModList.get().isLoaded("farmersdelight");

    @SubscribeEvent
    public static void onAnimalDeath(LivingDeathEvent livingDeathEvent) {
        if(!(livingDeathEvent.getEntity() instanceof Animal animal)) return;
        if(!(livingDeathEvent.getSource().getEntity() instanceof ServerPlayer killer)) return;

        int weight = WeightAttachment.getWeight(animal);
        double base = ServerConfigs.multiplierFor(ServerConfigs.KILL_MULTIPLIERS.get(), weight);
        if (base <= 0.0) return;

        // 1.20.1 has no DamageSource#getWeaponItem(); fall back to the direct entity's main hand.
        Entity direct = livingDeathEvent.getSource().getDirectEntity();
        ItemStack weapon = (direct instanceof LivingEntity le) ? le.getMainHandItem() : ItemStack.EMPTY;
        double knifeBonus = (FD_LOADED && FarmersDelightCompat.isKnife(weapon))
                ? ServerConfigs.FARMERS_DELIGHT_KNIFE_KILL_BONUS.get()
                : 1.0;
        double multiplier = base * knifeBonus;

        Map<String, Long> scaled = new HashMap<>();
        APIUtils.getXpAwardMap(animal, ServerConfigs.KILL_EVENT_TYPE.get(), LogicalSide.SERVER, killer)
                .forEach((skill, xp) -> {
                    long s = Math.round(xp * multiplier);
                    if (s > 0) scaled.put(skill, s);
                });
        if (!scaled.isEmpty()) Core.get(LogicalSide.SERVER).awardXP(List.of(killer), scaled);
    }

    @SubscribeEvent
    public static void onAnimalFed(PlayerInteractEvent.EntityInteract event) {
        if (event.getLevel().isClientSide()) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!(event.getTarget() instanceof Animal animal)) return;

        ItemStack stack = event.getItemStack();
        if (!animal.isFood(stack)) return;
        if (animal.getAge() != 0) return;
        if (!animal.canFallInLove()) return;

        MinecraftServer server = player.server;
        if (server == null) return;

        server.tell(new TickTask(server.getTickCount() + 1, () -> {
            if (!animal.isAlive() || !animal.isInLove()) return;
            if (animal.getLoveCause() != player) return;

            double feedMultiplier = ServerConfigs.FEED_XP_MULTIPLIER.get();
            if (feedMultiplier > 0.0) {
                Map<String, Long> scaled = new HashMap<>();
                APIUtils.getXpAwardMap(animal, ServerConfigs.FEED_EVENT_TYPE.get(), LogicalSide.SERVER, player)
                        .forEach((skill, xp) -> {
                            long s = Math.round(xp * feedMultiplier);
                            if (s > 0) scaled.put(skill, s);
                        });
                if (!scaled.isEmpty()) Core.get(LogicalSide.SERVER).awardXP(List.of(player), scaled);
            }

            int maxWeight = ServerConfigs.FEED_WEIGHT_MAX_WEIGHT.get();
            int currentWeight = WeightAttachment.getWeight(animal);
            if (currentWeight >= maxWeight) return;

            long level = APIUtils.getLevel(ServerConfigs.FEED_WEIGHT_SKILL.get(), player);
            double chance = Math.min(
                    ServerConfigs.FEED_WEIGHT_MAX_CHANCE.get(),
                    level * ServerConfigs.FEED_WEIGHT_CHANCE_PER_LEVEL.get());
            if (chance <= 0.0) return;

            if (animal.getRandom().nextDouble() < chance) {
                WeightAttachment.setWeight(animal, currentWeight + 1);
            }
        }));
    }

    @SubscribeEvent
    public static void onAnimalBorn(BabyEntitySpawnEvent babyEntitySpawnEvent) {
        if(!(babyEntitySpawnEvent.getCausedByPlayer() instanceof ServerPlayer player)) return;
        if(!(babyEntitySpawnEvent.getParentA() instanceof Animal parentA)) return;
        if(!(babyEntitySpawnEvent.getParentB() instanceof Animal parentB)) return;

        int weightA = WeightAttachment.getWeight(parentA);
        int weightB = WeightAttachment.getWeight(parentB);

        MinecraftServer server = player.server;
        if (server == null) return;
        int breedCooldownTicks = ServerConfigs.ANIMAL_BREED_COOLDOWN_SECONDS.get() * 20;

        if(breedCooldownTicks != 6000) {
            server.tell(new TickTask(server.getTickCount() + 1, () -> {
                parentA.setAge(breedCooldownTicks);
                parentB.setAge(breedCooldownTicks);
            }));
        }

        int averageWeight = (weightA + weightB) / 2;
        double multiplier = ServerConfigs.multiplierFor(ServerConfigs.BREED_MULTIPLIERS.get(), averageWeight);
        if (multiplier <= 0.0) return;

        Map<String, Long> scaled = new HashMap<>();
        APIUtils.getXpAwardMap(parentA, ServerConfigs.BREED_EVENT_TYPE.get(), LogicalSide.SERVER, player)
                .forEach((skill, xp) -> {
                    long s = Math.round(xp * multiplier);
                    if (s > 0) scaled.put(skill, s);
                });
        if (!scaled.isEmpty()) Core.get(LogicalSide.SERVER).awardXP(List.of(player), scaled);
    }
}
