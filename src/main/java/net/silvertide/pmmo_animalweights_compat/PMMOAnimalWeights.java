package net.silvertide.pmmo_animalweights_compat;

import com.mojang.logging.LogUtils;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.silvertide.pmmo_animalweights_compat.config.ServerConfigs;
import org.slf4j.Logger;

@Mod(PMMOAnimalWeights.MODID)
public class PMMOAnimalWeights {
    public static final String MODID = "pmmo_animalweights_compat";
    public static final Logger LOGGER = LogUtils.getLogger();

    public PMMOAnimalWeights(FMLJavaModLoadingContext context) {
        context.registerConfig(ModConfig.Type.SERVER, ServerConfigs.SPEC, String.format("%s-server.toml", MODID));
    }
}
