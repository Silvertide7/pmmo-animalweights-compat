package net.silvertide.pmmo_animalweights_compat;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.silvertide.pmmo_animalweights_compat.config.ServerConfigs;
import org.slf4j.Logger;

@Mod(PMMOAnimalWeights.MODID)
public class PMMOAnimalWeights {
    public static final String MODID = "pmmo_animalweights_compat";
    public static final Logger LOGGER = LogUtils.getLogger();

    public PMMOAnimalWeights(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.SERVER, ServerConfigs.SPEC, String.format("%s-server.toml", MODID));
    }
}
