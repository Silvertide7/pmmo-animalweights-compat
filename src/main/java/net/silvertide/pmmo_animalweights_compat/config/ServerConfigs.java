package net.silvertide.pmmo_animalweights_compat.config;

import harmonised.pmmo.api.enums.EventType;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

public class ServerConfigs {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.IntValue ANIMAL_BREED_COOLDOWN_SECONDS;
    public static final ModConfigSpec.DoubleValue FARMERS_DELIGHT_KNIFE_KILL_BONUS;
    public static final ModConfigSpec.EnumValue<EventType> KILL_EVENT_TYPE;
    public static final ModConfigSpec.EnumValue<EventType> BREED_EVENT_TYPE;
    public static final ModConfigSpec.EnumValue<EventType> FEED_EVENT_TYPE;
    public static final ModConfigSpec.DoubleValue FEED_XP_MULTIPLIER;
    public static final ModConfigSpec.ConfigValue<List<? extends Number>> KILL_MULTIPLIERS;
    public static final ModConfigSpec.ConfigValue<List<? extends Number>> BREED_MULTIPLIERS;
    public static final ModConfigSpec.ConfigValue<String> FEED_WEIGHT_SKILL;
    public static final ModConfigSpec.DoubleValue FEED_WEIGHT_CHANCE_PER_LEVEL;
    public static final ModConfigSpec.DoubleValue FEED_WEIGHT_MAX_CHANCE;
    public static final ModConfigSpec.IntValue FEED_WEIGHT_MAX_WEIGHT;

    static {
        BUILDER.push("Animal XP");

        BUILDER.comment("Which PMMO EventType to look up on the animal entity when awarding XP for killing it. Defaults to DEATH.");
        KILL_EVENT_TYPE = BUILDER.defineEnum("deathEventType", EventType.DEATH);

        BUILDER.comment("Which PMMO EventType to look up on the parent entity when awarding XP for breeding. Defaults to BREED.");
        BREED_EVENT_TYPE = BUILDER.defineEnum("breedEventType", EventType.BREED);

        BUILDER.comment("Which PMMO EventType to look up on the animal entity when awarding XP for feeding it into love mode. Defaults to TAMING.");
        FEED_EVENT_TYPE = BUILDER.defineEnum("feedEventType", EventType.TAMING);

        BUILDER.comment("Flat multiplier applied to the entity's PMMO award map for the feedEventType when a player feeds an animal into love mode.");
        FEED_XP_MULTIPLIER = BUILDER.defineInRange("feedXpMultiplier", 0.2, 0.0, 100.0);

        BUILDER.comment(
                "Kill XP multiplier indexed by animal weight (0-8). List should have exactly 9 entries.",
                "Index 0 = weight 0, index 8 = weight 8. Weights below 2 award no XP by default.",
                "Multiplier is applied to the entity's PMMO award map for the killEventType.");
        KILL_MULTIPLIERS = BUILDER.defineList("killMultipliers",
                List.of(0.0, 0.0, 1.5, 2.5, 4.0, 6.0, 9.0, 12.0, 17.0),
                obj -> obj instanceof Number n && n.doubleValue() >= 0.0);

        BUILDER.comment(
                "Breed XP multiplier indexed by the average weight of the two parents (0-8). List should have exactly 9 entries.",
                "Multiplier is applied to the parent's PMMO award map for the breedEventType.");
        BREED_MULTIPLIERS = BUILDER.defineList("breedMultipliers",
                List.of(0.0, 0.0, 1.0, 1.5, 2.0, 2.5, 3.0, 5.0, 7.0),
                obj -> obj instanceof Number n && n.doubleValue() >= 0.0);

        BUILDER.pop();

        BUILDER.push("Animal Breeding");

        BUILDER.comment("Cooldown in seconds before two animals can breed again after producing a baby. Vanilla default is 300 (5 minutes).");
        ANIMAL_BREED_COOLDOWN_SECONDS = BUILDER.defineInRange("animalBreedCooldownSeconds", 300, 0, 86400);

        BUILDER.pop();

        BUILDER.push("Feed Weight Gain");

        BUILDER.comment("PMMO skill checked when a player feeds an animal into love mode. Higher levels increase the chance the animal gains 1 weight.");
        FEED_WEIGHT_SKILL = BUILDER.define("feedWeightSkill", "taming");

        BUILDER.comment("Probability added per skill level for the animal to gain 1 weight when fed into love mode. 0.01 = +1% per level.");
        FEED_WEIGHT_CHANCE_PER_LEVEL = BUILDER.defineInRange("feedWeightChancePerLevel", 0.01, 0.0, 1.0);

        BUILDER.comment("Maximum probability cap regardless of skill level.");
        FEED_WEIGHT_MAX_CHANCE = BUILDER.defineInRange("feedWeightMaxChance", 0.5, 0.0, 1.0);

        BUILDER.comment("Maximum weight an animal can reach via feeding. Vanilla weights are clamped 0-8.");
        FEED_WEIGHT_MAX_WEIGHT = BUILDER.defineInRange("feedWeightMaxWeight", 8, 0, 8);

        BUILDER.pop();

        BUILDER.push("Farmers Delight Integration");

        BUILDER.comment("Multiplier applied to kill XP when the animal is killed with a Farmer's Delight knife. Only used if Farmer's Delight is installed. Set to 1.0 to disable the bonus.");
        FARMERS_DELIGHT_KNIFE_KILL_BONUS = BUILDER.defineInRange("farmersDelightKnifeKillBonus", 1.15, 1.0, 10.0);

        BUILDER.pop();

        SPEC = BUILDER.build();
    }

    // Returns the multiplier at `weight`, or 0.0 if the configured list is shorter than expected.
    public static double multiplierFor(List<? extends Number> list, int weight) {
        return (weight >= 0 && weight < list.size()) ? list.get(weight).doubleValue() : 0.0;
    }
}
