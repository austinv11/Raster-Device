package mrtjp.rasterdevice;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_RESCALE_NORMAL;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.OpenGLException;

import codechicken.lib.colour.ColourRGBA;

public class RasterJob
{    
    private int textureWidth;
    private int textureHeight;
    
    private String saveDir = "raster";

    private List<Integer> blacklist = new ArrayList<Integer>();
    private List<Integer> whitelist = new ArrayList<Integer>();
    
    private int savedWidth = 854;
    private int savedHeight = 480;
    
    public RasterJob setSize(int w, int h)
    {
        textureWidth = w;
        textureHeight = h;
        return this;
    }
    
    public RasterJob addToBlackList(int id)
    {
        blacklist.add(id);
        return this;
    }
    
    public RasterJob addToWhiteList(int id)
    {
        whitelist.add(id);
        return this;
    }
    
    public RasterJob setSaveDir(String dir)
    {
        if (dir == null || dir.isEmpty())
            return this;
        
        saveDir = legalizeFileName(dir);

        return this;
    }
    
    public boolean pushWindowSize()
    {
        Minecraft mc = Minecraft.getMinecraft();

        int oldW = mc.displayWidth;
        int oldH = mc.displayHeight;
        
        int newW = Math.max(1024, textureWidth);
        int newH = textureHeight;

        if (resize(newW, newH))
        {
            savedWidth = oldW;
            savedHeight = oldH;
            return true;
        }
        
        return false;
    }
    
    public boolean popWindowSize()
    {
        return resize(savedWidth, savedHeight);
    }
    
    private boolean resize(int w, int h)
    {
        Minecraft mc = Minecraft.getMinecraft();
        
        if (mc.displayWidth != w || mc.displayHeight != h)
        {
            try
            {
                Display.setDisplayMode(new DisplayMode(w, h));
            }
            catch (LWJGLException e)
            {
                e.printStackTrace();
            }
            
            mc.displayWidth = w <= 0 ? 1 : w;
            mc.displayHeight = h <= 0 ? 1 : h;

            if (mc.currentScreen != null)
            {
                ScaledResolution scaledresolution = new ScaledResolution(mc.gameSettings, w, h);
                int k = scaledresolution.getScaledWidth();
                int l = scaledresolution.getScaledHeight();
                mc.currentScreen.setWorldAndResolution(mc, k, l);
            }
            
            return true;
        }

        return false;
    }
    
    public RasterJob raster()
    {
        Minecraft mc = Minecraft.getMinecraft();
        
        File folder = new File(mc.mcDataDir, saveDir + " x" + textureHeight);
        int count = 2;
        while (folder.exists())
            folder = new File(mc.mcDataDir, saveDir + " x" + textureHeight + "_" + (count++));
        folder.mkdirs();
        
        throwIfError();
        setupCanvas();

        RenderItem r = new RenderItem();
        long start = System.currentTimeMillis();

        try
        {
            for (int i = 0; i < Item.itemsList.length; i++)
            {
                Item item = Item.itemsList[i];
                if (item == null)
                    continue;
                
                if (!isIDAllowed(i))
                    continue;
                
                ArrayList<ItemStack> sub = new ArrayList<ItemStack>();
                item.getSubItems(i, CreativeTabs.tabAllSearch, sub);
                
                for (ItemStack stack : sub)
                {
                    System.out.println("Rastering " + i + ":" + stack.getItemDamage() + " [" + getName(stack) + "]");
                    
                    try
                    {
                        if (doRaster(r, stack))
                            drawRasterToFile(folder, getName(stack));
                    }
                    catch (OpenGLException e)
                    {
                        if (e.getMessage().equals("Stack overflow (1283)"))
                        {
                            setupCanvas();
                            if (doRaster(r, stack))
                                drawRasterToFile(folder, getName(stack));
                        }
                        else
                            throw e;
                    }
                    
                    clearCanvas();
                }
            }
        }
        catch (Throwable t)
        {
        }
        
        return this;
    }
    
    private boolean doRaster(RenderItem r, ItemStack stack)
    {
        boolean done = true;
        
        glPushAttrib(GL_ALL_ATTRIB_BITS);
        glPushMatrix();
        throwIfError();

        try
        {
            Minecraft mc = Minecraft.getMinecraft();
            r.renderItemAndEffectIntoGUI(mc.fontRenderer, mc.renderEngine, stack, 0, 0);
            r.renderItemOverlayIntoGUI(mc.fontRenderer, mc.renderEngine, stack, 0, 0, "");
            throwIfError();
        }
        catch (Throwable t)
        {
            Tessellator.instance = new Tessellator();
            
            System.err.println("RENDER FALIED: " + getName(stack));
            t.printStackTrace();
            done = false;
        }
        
        glPopMatrix();
        glPopAttrib();
        throwIfError();
        return done;
    }
    
    private void drawRasterToFile(File folder, String name)
    {
        int bufferSize = textureWidth * textureHeight * 4;
        
        ByteBuffer data = BufferUtils.createByteBuffer(bufferSize);
        glReadPixels(0, 0, textureWidth, textureHeight, GL_RGBA, GL_UNSIGNED_BYTE, data);

        throwIfError();

        name = legalizeFileName(name);
        File image = new File(folder, name + ".png");
        int count = 2;
        while (image.exists())
            image = new File(folder, name + " (" + (count++) + ")" + ".png");

        BufferedImage png = new BufferedImage(textureWidth, textureHeight, BufferedImage.TYPE_INT_ARGB);
        
        int x = 0;
        int y = 0;
        
        while(data.hasRemaining())
        {
            png.setRGB(x, textureHeight - (y + 1), new ColourRGBA(data.get()&0xFF, data.get()&0xFF, data.get()&0xFF, data.get()&0xFF).argb());
            
            if (++x == textureWidth)
            {
                x = 0;
                y++;
            }
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
        if (name.isEmpty())
            name = stack.getDisplayName().replaceAll("^tile\\.", "");

        if (name.isEmpty())
            name = stack.itemID + ":" + stack.getItemDamage();

        return name;
    }
    
    private boolean isIDAllowed(int id)
    {
        if (blacklist.isEmpty() && whitelist.isEmpty())
            return true;

        if (blacklist.contains(Integer.valueOf(id)))
            return false;
        
        if (whitelist.contains(Integer.valueOf(id)))
            return true;
        
        return false;
    }

    private void setupCanvas()
    {
        glPushAttrib(GL_ALL_ATTRIB_BITS);
        glViewport(0, 0, textureWidth, textureHeight);
        throwIfError();

        clearCanvas();

        glDisable(GL11.GL_TEXTURE_2D);
        glDisable(GL_BLEND);
        glEnable(GL_DEPTH_TEST);
        RenderHelper.disableStandardItemLighting();
        throwIfError();

        glPopAttrib();
        throwIfError();

        float factor = (float) Math.max(textureWidth, textureHeight);        
        float scale = Math.min((32.0f*factor)/512.0f, 16.0f);

        glScalef(scale, scale, scale);

        throwIfError();

        RenderHelper.enableGUIStandardItemLighting();
        glEnable(GL_RESCALE_NORMAL);
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 0.0F, 0.0F);
        throwIfError();
    }

    private void clearCanvas()
    {
        glClearColor(0.0F, 0.0F, 0.0F, 0.0F);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);
        throwIfError();
    }

    private void throwIfError()
    {
        final int e = glGetError();
        if (e != GL_NO_ERROR)
            throw new OpenGLException(e);
    }

    private String legalizeFileName(String name)
    {
        String dir = "";
        if (name == null || name.isEmpty())
            dir = "null";
        else
            dir = name.replaceAll("[\\\\/:*?\"<>|]", "_");

        return dir;
    }
}
