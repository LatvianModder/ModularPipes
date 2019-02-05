package com.latmod.mods.modularpipes.item.module.fluid;

import com.latmod.mods.modularpipes.item.module.SidePipeModule;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import javax.annotation.Nullable;

/**
 * @author LatvianModder
 */
public class ModuleFluidHandler extends SidePipeModule implements IFluidHandler
{
	@Override
	public IFluidTankProperties[] getTankProperties()
	{
		return new IFluidTankProperties[0];
	}

	@Override
	public int fill(FluidStack resource, boolean doFill)
	{
		return 0;
	}

	@Nullable
	@Override
	public FluidStack drain(FluidStack resource, boolean doDrain)
	{
		return null;
	}

	@Nullable
	@Override
	public FluidStack drain(int maxDrain, boolean doDrain)
	{
		return null;
	}
}