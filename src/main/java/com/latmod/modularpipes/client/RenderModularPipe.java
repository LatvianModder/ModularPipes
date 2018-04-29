package com.latmod.modularpipes.client;

import com.feed_the_beast.ftblib.lib.client.ClientUtils;
import com.feed_the_beast.ftblib.lib.math.MathUtils;
import com.latmod.modularpipes.block.BlockPipeBase;
import com.latmod.modularpipes.tile.TileModularPipe;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import org.lwjgl.opengl.GL11;

/**
 * @author LatvianModder
 */
public class RenderModularPipe extends TileEntitySpecialRenderer<TileModularPipe>
{
	public static TextureAtlasSprite PIPE_ERROR;

	@Override
	public void render(TileModularPipe pipe, double x, double y, double z, float partialTicks, int destroyStage, float alpha)
	{
		if (pipe.isInvalid())
		{
			return;
		}

		GlStateManager.pushMatrix();
		GlStateManager.enableRescaleNormal();
		GlStateManager.translate(x, y, z);
		GlStateManager.color(1F, 1F, 1F, 1F);
		GlStateManager.enableDepth();
		GlStateManager.depthFunc(GL11.GL_LEQUAL);
		GlStateManager.depthMask(false);
		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		setLightmapDisabled(true);
		GlStateManager.enableLighting();
		GlStateManager.enableCull();
		TextureAtlasSprite sprite;

		if (pipe.hasError())
		{
			GlStateManager.enableBlend();
			GlStateManager.disableAlpha();
			sprite = PIPE_ERROR;
		}
		else
		{
			GlStateManager.disableBlend();
			GlStateManager.enableAlpha();
			sprite = pipe.tier.getSprite();
		}

		ClientUtils.MC.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();

		double s0 = (BlockPipeBase.SIZE - 0.03D) / 16D;
		double s1 = 1D - s0;

		double minU = sprite.getInterpolatedU(BlockPipeBase.SIZE);
		double minV = sprite.getInterpolatedV(BlockPipeBase.SIZE);
		double maxU = sprite.getInterpolatedU(16D - BlockPipeBase.SIZE);
		double maxV = sprite.getInterpolatedV(16D - BlockPipeBase.SIZE);
		int alphai = 255;

		if (destroyStage < 0)
		{
			alphai = (int) (alpha * 255);
		}

		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);
		buffer.pos(s0, s0, s0).tex(minU, minV).color(255, 255, 255, alphai).normal(0, -1, 0).endVertex();
		buffer.pos(s1, s0, s0).tex(maxU, minV).color(255, 255, 255, alphai).normal(0, -1, 0).endVertex();
		buffer.pos(s1, s0, s1).tex(maxU, maxV).color(255, 255, 255, alphai).normal(0, -1, 0).endVertex();
		buffer.pos(s0, s0, s1).tex(minU, maxV).color(255, 255, 255, alphai).normal(0, -1, 0).endVertex();
		buffer.pos(s0, s1, s0).tex(minU, minV).color(255, 255, 255, alphai).normal(0, 1, 0).endVertex();
		buffer.pos(s0, s1, s1).tex(minU, maxV).color(255, 255, 255, alphai).normal(0, 1, 0).endVertex();
		buffer.pos(s1, s1, s1).tex(maxU, maxV).color(255, 255, 255, alphai).normal(0, 1, 0).endVertex();
		buffer.pos(s1, s1, s0).tex(maxU, minV).color(255, 255, 255, alphai).normal(0, 1, 0).endVertex();
		buffer.pos(s0, s0, s0).tex(maxU, maxV).color(255, 255, 255, alphai).normal(0, 0, -1).endVertex();
		buffer.pos(s0, s1, s0).tex(maxU, minV).color(255, 255, 255, alphai).normal(0, 0, -1).endVertex();
		buffer.pos(s1, s1, s0).tex(minU, minV).color(255, 255, 255, alphai).normal(0, 0, -1).endVertex();
		buffer.pos(s1, s0, s0).tex(minU, maxV).color(255, 255, 255, alphai).normal(0, 0, -1).endVertex();
		buffer.pos(s0, s0, s1).tex(minU, maxV).color(255, 255, 255, alphai).normal(0, 0, 1).endVertex();
		buffer.pos(s1, s0, s1).tex(maxU, maxV).color(255, 255, 255, alphai).normal(0, 0, 1).endVertex();
		buffer.pos(s1, s1, s1).tex(maxU, minV).color(255, 255, 255, alphai).normal(0, 0, 1).endVertex();
		buffer.pos(s0, s1, s1).tex(minU, minV).color(255, 255, 255, alphai).normal(0, 0, 1).endVertex();
		buffer.pos(s0, s0, s0).tex(minU, maxV).color(255, 255, 255, alphai).normal(-1, 0, 0).endVertex();
		buffer.pos(s0, s0, s1).tex(maxU, maxV).color(255, 255, 255, alphai).normal(-1, 0, 0).endVertex();
		buffer.pos(s0, s1, s1).tex(maxU, minV).color(255, 255, 255, alphai).normal(-1, 0, 0).endVertex();
		buffer.pos(s0, s1, s0).tex(minU, minV).color(255, 255, 255, alphai).normal(-1, 0, 0).endVertex();
		buffer.pos(s1, s0, s0).tex(maxU, maxV).color(255, 255, 255, alphai).normal(1, 0, 0).endVertex();
		buffer.pos(s1, s1, s0).tex(maxU, minV).color(255, 255, 255, alphai).normal(1, 0, 0).endVertex();
		buffer.pos(s1, s1, s1).tex(minU, minV).color(255, 255, 255, alphai).normal(1, 0, 0).endVertex();
		buffer.pos(s1, s0, s1).tex(minU, maxV).color(255, 255, 255, alphai).normal(1, 0, 0).endVertex();
		tessellator.draw();

		GlStateManager.depthMask(true);
		GlStateManager.enableLighting();
		setLightmapDisabled(false);

		for (int i = 0; i < 6; i++)
		{
			if (pipe.modules[i].hasModule() && !pipe.modules[i].getItemStack().isEmpty())
			{
				EnumFacing facing = EnumFacing.VALUES[i];
				GlStateManager.pushMatrix();
				GlStateManager.translate(0.5D + facing.getFrontOffsetX() * 0.46975D, 0.5D + facing.getFrontOffsetY() * 0.46975D, 0.5D + facing.getFrontOffsetZ() * 0.46975D);
				//GlStateManager.rotate(180F, 0F, 0F, 1F);
				GlStateManager.rotate(MathUtils.ROTATION_Y[i], 0F, 1F, 0F);
				GlStateManager.rotate(MathUtils.ROTATION_X[i], 1F, 0F, 0F);
				GlStateManager.scale(0.5D, 0.5D, 1D);
				ClientUtils.MC.getRenderItem().renderItem(pipe.modules[i].getItemStack(), ItemCameraTransforms.TransformType.FIXED);
				GlStateManager.popMatrix();
			}
		}

		GlStateManager.disableBlend();
		GlStateManager.enableAlpha();
		GlStateManager.disableRescaleNormal();
		GlStateManager.color(1F, 1F, 1F, 1F);
		GlStateManager.popMatrix();
	}
}