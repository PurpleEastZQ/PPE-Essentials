package me.purpleeast.mods.ppe_essential;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

@Mod(PpeEssential.MODID)
public class PpeEssential {
    public static final String MODID = "ppe_essential";
    public static final Logger LOGGER = LogUtils.getLogger();

    public PpeEssential(IEventBus modEventBus, ModContainer modContainer) {
        NeoForge.EVENT_BUS.register(PpeCommands.class);
        NeoForge.EVENT_BUS.register(PpeEvents.class);
        modContainer.registerConfig(ModConfig.Type.COMMON, PpeConfig.SPEC);
    }
}
