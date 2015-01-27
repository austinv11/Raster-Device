package mrtjp.rasterdevice;

import cpw.mods.fml.common.registry.GameData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_RESCALE_NORMAL;

public class RasterJob
{
    private int texW;
    private int texH;

    private String saveDir = "ItemRenders";

    private List<Item> blacklist = new ArrayList<Item>();
    private List<Item> whitelist = new ArrayList<Item>();

    public RasterJob(int w, int h)
    {
        texW = w;
        texH = h;
    }

    public RasterJob addToBlackList(Item item)
    {
        blacklist.add(item);
        return this;
    }

    public RasterJob addToWhiteList(Item item)
    {
        whitelist.add(item);
        return this;
    }

    public RasterJob raster()
    {
        Minecraft mc = Minecraft.getMinecraft();

        File folder = new File(mc.mcDataDir, saveDir+" x"+texH);
        int count = 2;
        while (folder.exists()) folder = new File(mc.mcDataDir, saveDir+" x"+texH+"_"+(count++));
        folder.mkdirs();

        pushInitial();

        RenderItem r = new RenderItem();
        long start = System.currentTimeMillis();

        try
        {
            @SuppressWarnings("unchecked")
            Iterator<Item> it = GameData.getItemRegistry().iterator();

            if (!RasterDeviceMod.instance.MOD_ID_TO_RASTER.equalsIgnoreCase("all"))
                System.out.println("Rastering items from mod id(s): "+RasterDeviceMod.instance.MOD_ID_TO_RASTER);

            while (it.hasNext())
            {
                Item item = it.next();
                if (RasterDeviceMod.instance.MOD_ID_TO_RASTER.equalsIgnoreCase("all") || Arrays.asList(RasterDeviceMod.instance.MOD_ID_TO_RASTER.split(",")).contains(Item.itemRegistry.getNameForObject(item).split(":")[0]))
                {
                    if (item == null) continue;
                    if (!isItemAllowed(item)) continue;

                    ArrayList<ItemStack> sub = new ArrayList<ItemStack>();
                    item.getSubItems(item, CreativeTabs.tabAllSearch, sub);
                    for (ItemStack stack : sub)
                    {

                        System.out.println("Rastering "+item.getUnlocalizedName()+":"+stack.getItemDamage()+" ["+getName(stack)+"]");

                        try
                        {
                            if (doRaster(r, stack))
                                drawRasterToFile(folder, getName(stack));
                        }
                        catch (Throwable t)
                        {
                            System.err.println("RENDER FALIED: "+getName(stack));
                            t.printStackTrace();
                        }

                        clearCanvas();
                    }
                }
            }
        }
        catch (Throwable t) {}

        popInitial();
        System.out.println("Raster Job took " + (System.currentTimeMillis() - start) + " milliseconds.");

        return this;
    }

    private void pushInitial()
    {
        glPushMatrix();
        glPushAttrib(GL_ALL_ATTRIB_BITS);
        glViewport(0, 0, texW, texH);

        clearCanvas();

        glDisable(GL11.GL_TEXTURE_2D);
        glDisable(GL_BLEND);
        glEnable(GL_DEPTH_TEST);
        RenderHelper.disableStandardItemLighting();
        glPopAttrib();

        float scale = (float)Math.max(texW, texH)/32f;
        glScalef(scale, scale, scale);

        RenderHelper.enableGUIStandardItemLighting();
        glEnable(GL_RESCALE_NORMAL);
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_ALPHA_TEST);
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 0.0F, 0.0F);
    }

    private void popInitial()
    {
        glPopMatrix();
    }

    private void clearCanvas()
    {
        glClearColor(0.0F, 0.0F, 0.0F, 0.0F);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);
    }

    private boolean doRaster(RenderItem r, ItemStack stack)
    {
        boolean done = true;

        glPushAttrib(GL_ALL_ATTRIB_BITS);
        glPushMatrix();

        try
        {
            try {Tessellator.instance.draw();} catch (Throwable t2) {}

            Minecraft mc = Minecraft.getMinecraft();
            r.renderItemAndEffectIntoGUI(mc.fontRenderer, mc.renderEngine, stack, 0, 0);
            r.renderItemOverlayIntoGUI(mc.fontRenderer, mc.renderEngine, stack, 0, 0, "");
        }
        catch (Throwable t)
        {
            done = false;
        }

        glPopMatrix();
        glPopAttrib();
        return done;
    }

    private void drawRasterToFile(File folder, String name)
    {
        int bufferSize = texW * texH *4;

        ByteBuffer data = BufferUtils.createByteBuffer(bufferSize);
        glReadPixels(0, Minecraft.getMinecraft().displayHeight-texH, texW, texH, GL_RGBA, GL_UNSIGNED_BYTE, data);

        name = legalizeFileName(name);
        File image = new File(folder, name+".png");
        int count = 2;
        while (image.exists()) image = new File(folder, name+" ("+(count++)+")"+".png");

        BufferedImage png = new BufferedImage(texW, texH, BufferedImage.TYPE_INT_ARGB);

        int x = 0;
        int y = 0;

        while(data.hasRemaining())
        {
            png.setRGB(x, texH -(y+1), (data.get()&0xFF)<<16 | (data.get()&0xFF)<<8 | (data.get()&0xFF) | (data.get()&0xFF)<<24);
            //png.setRGB(x, texH -(y+1), new ColourRGBA(data.get(), data.get(), data.get(), data.get()).argb());
            if (++x == texW) { x = 0; y++; }
        }

        try
        {
            ImageIO.write(png, "png", image);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private String getName(ItemStack stack)
    {
        String name = stack.getDisplayName();
        if (name.isEmpty()) name = stack.getDisplayName().replaceAll("^tile\\.", "");
        if (name.isEmpty()) name = stack.getUnlocalizedName()+ ":" + stack.getItemDamage();
        return name;
    }

    private boolean isItemAllowed(Item item)
    {
        if (blacklist.isEmpty() && whitelist.isEmpty()) return true;
        if (blacklist.contains(item)) return false;
        if (whitelist.contains(item)) return true;
        return false;
    }

    private String legalizeFileName(String name)
    {
        String dir = "";
        if (name == null || name.isEmpty()) dir = "null";
        else dir = name.replaceAll("[\\\\/:*?\"<>|]", "_");
        return dir;
    }
}
