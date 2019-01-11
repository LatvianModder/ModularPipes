package com.latmod.modularpipes.client;

import com.latmod.modularpipes.ModularPipesCommon;
import net.minecraft.util.BlockRenderLayer;
import net.minecraftforge.client.MinecraftForgeClient;

/**
 * @author LatvianModder
 */
public class ModularPipesClient extends ModularPipesCommon
{
	@Override
	public int getPipeLightValue()
	{
		return MinecraftForgeClient.getRenderLayer() == BlockRenderLayer.TRANSLUCENT ? 15 : 0;
	}
}