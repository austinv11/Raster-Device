package mrtjp.rasterdevice;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

public class RenderUtils
{
    public static void changeTexture(String texture)
    {
        changeTexture(new ResourceLocation(texture));
    }

    public static void changeTexture(ResourceLocation texture)
    {
        Minecraft.getMinecraft().renderEngine.bindTexture(texture);
    }

}
