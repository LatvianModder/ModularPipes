package com.latmod.mods.modularpipes.block;

import com.latmod.mods.modularpipes.ModularPipes;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.registry.GameRegistry;

/**
 * @author LatvianModder
 */
@GameRegistry.ObjectHolder(ModularPipes.MOD_ID)
@Mod.EventBusSubscriber(modid = ModularPipes.MOD_ID)
public class ModularPipesBlocks
{
	public static final Block PIPE_TRANSPORT = Blocks.AIR;
	public static final Block PIPE_MODULAR_MK1 = Blocks.AIR;
	public static final Block PIPE_MODULAR_MK2 = Blocks.AIR;
	public static final Block PIPE_MODULAR_MK3 = Blocks.AIR;
	public static final Block TANK = Blocks.AIR;
	public static final Block MODULAR_STORAGE = Blocks.AIR;
	public static final Block MODULAR_TANK = Blocks.AIR;
}