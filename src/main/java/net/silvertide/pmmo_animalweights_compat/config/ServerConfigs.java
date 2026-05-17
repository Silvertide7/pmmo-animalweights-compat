package net.silvertide.pmmo_animalweights_compat.config;

import harmonised.pmmo.api.enums.EventType;
import net.neoforged.neoforge.common.ModConfigSpec;

public class ServerConfigs {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.IntValue ANIMAL_BREED_COOLDOWN_SECONDS;
    public static final ModConfigSpec.DoubleValue FARMERS_DELIGHT_KNIFE_KILL_BONUS;
    public static final ModConfigSpec.EnumValue<EventType> KILL_EVENT_TYPE;
    public static final ModConfigSpec.EnumValue<EventType> BREED_EVENT_TYPE;

    static {
        BUILDER.push("Animal XP");

        BUILDER.comment("Which PMMO EventType to look up on the animal entity when awarding XP for killing it. Defaults to KILL.");
        KILL_EVENT_TYPE = BUILDER.defineEnum("killEventType", EventType.KILL);

        BUILDER.comment("Which PMMO EventType to look up on the parent entity when awarding XP for breeding. Defaults to BREED.");
        BREED_EVENT_TYPE = BUILDER.defineEnum("breedEventType", EventType.BREED);

        BUILDER.pop();

        BUILDER.push("Animal Breeding");

        BUILDER.comment("Cooldown in seconds before two animals can breed again after producing a baby. Vanilla default is 300 (5 minutes).");
        ANIMAL_BREED_COOLDOWN_SECONDS = BUILDER.defineInRange("animalBreedCooldownSeconds", 1200, 0, 86400);

        BUILDER.pop();

        BUILDER.push("Farmers Delight Integration");

        BUILDER.comment("Multiplier applied to kill XP when the animal is killed with a Farmer's Delight knife. Only used if Farmer's Delight is installed. Set to 1.0 to disable the bonus.");
        FARMERS_DELIGHT_KNIFE_KILL_BONUS = BUILDER.defineInRange("farmersDelightKnifeKillBonus", 1.15, 1.0, 10.0);

        BUILDER.pop();

        SPEC = BUILDER.build();
    }
}
