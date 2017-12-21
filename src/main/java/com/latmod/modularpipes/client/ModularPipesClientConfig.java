package com.latmod.modularpipes.client;

import com.feed_the_beast.ftblib.lib.gui.GuiLang;
import com.latmod.modularpipes.ModularPipes;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

/**
 * @author LatvianModder
 */
@Mod.EventBusSubscriber(modid = ModularPipes.MOD_ID, value = Side.CLIENT)
@Config(modid = ModularPipes.MOD_ID + "_client", category = "", name = "../local/client/" + ModularPipes.MOD_ID)
public class ModularPipesClientConfig
{
	@Config.LangKey(GuiLang.LANG_GENERAL)
	public static final General general = new General();

	public static class General
	{
		@Config.RangeDouble(min = 0, max = 1000)
		public double item_render_distance = 90D;

		public boolean item_particles = true;
	}

	public static void sync()
	{
		ConfigManager.sync(ModularPipes.MOD_ID + "_client", Config.Type.INSTANCE);
	}

	@SubscribeEvent
	public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event)
	{
		if (event.getModID().equals(ModularPipes.MOD_ID + "_client"))
		{
			sync();
		}
	}
}