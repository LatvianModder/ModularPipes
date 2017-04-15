package com.latmod.modularpipes;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = ModularPipes.MOD_ID, name = "Modular Pipes", version = "0.0.0", useMetadata = true)
public class ModularPipes
{
    public static final String MOD_ID = "modularpipes";
    public static final Logger LOGGER = LogManager.getLogger("ModularPipes");

    @SidedProxy(serverSide = "com.latmod.modularpipes.ModularPipesCommon", clientSide = "com.latmod.modularpipes.client.ModularPipesClient")
    public static ModularPipesCommon PROXY;

    @EventHandler
    public void onPreInit(FMLPreInitializationEvent event)
    {
        PROXY.preInit();
    }

    @EventHandler
    public void onPostInit(FMLPostInitializationEvent event)
    {
        PROXY.postInit();
    }
}