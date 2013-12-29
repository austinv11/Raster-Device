package mrtjp.rasterdevice;

import java.util.LinkedList;
import java.util.List;

import mrtjp.rasterdevice.copied_utils.GhostGuiScreen;
import mrtjp.rasterdevice.copied_utils.WidgetButton.WidgetCheckBox;
import mrtjp.rasterdevice.copied_utils.WidgetButton.WidgetDotSelector;
import mrtjp.rasterdevice.copied_utils.WidgetButton.WidgetSimpleButton;

public class GuiRasterDevice extends GhostGuiScreen
{
    WidgetCheckBox x16 = new WidgetCheckBox(16, 10, false);
    WidgetCheckBox x32 = new WidgetCheckBox(16, 26, false);
    WidgetCheckBox x64 = new WidgetCheckBox(16, 42, false);
    WidgetCheckBox x128 = new WidgetCheckBox(16, 58, false);
    WidgetCheckBox x256 = new WidgetCheckBox(16, 74, false);
    WidgetCheckBox x512 = new WidgetCheckBox(16, 90, false);
    
    @Override
    public void addWidgets()
    {
        add(x16);
        add(x32);
        add(x64);
        add(x128);
        add(x256);
        add(x512);
        
        add(new WidgetSimpleButton(9, 100, 50, 15).setText("Raster").setActionCommand("raster"));
        
        add(new WidgetDotSelector(13, 130) {
            @Override
            public List<String> getOverlayText()
            {
                List<String> list = new LinkedList<String>();
                
                list.add("This device can rastered images of items to png files.");
                list.add("Select image resolutions and click raster.");
                list.add("Images will be saved to mcDir/raster_x(resolution)");
                list.add("NOTE:");
                list.add("- During raster, Minecraft may freeze.");
                list.add("- During raster, window will resize. Dont adjust!");
                list.add("- Before raster, disable fullscreen if enabled.");
                
                return list;
            }
        });
    }
    
    @Override
    public void drawBackground()
    {
        RenderUtils.changeTexture("rasterdevice:textures/gui/guirasterdevice.png");
        drawTexturedModalRect(0, 0, 0, 0, xSize, ySize);
    }
    
    @Override
    public void drawForeground()
    {
        for (int i = 0; i < 6; i++)
            fontRenderer.drawString("x" + (16<<i), 30, 7 + 16*i, 0xFFFFFF);
    }
    
    @Override
    public void actionPerformed(String actionCommand, Object... params)
    {
        if (actionCommand.equals("raster"))
        {
            if (x16.isChecked())
                RasterTickHandler.addRasterJob(createRaster().setSize(16, 16));
                
            if (x32.isChecked())
                RasterTickHandler.addRasterJob(createRaster().setSize(32, 32));
            
            if (x64.isChecked())
                RasterTickHandler.addRasterJob(createRaster().setSize(64, 64));
            
            if (x128.isChecked())
                RasterTickHandler.addRasterJob(createRaster().setSize(128, 128));
            
            if (x256.isChecked())
                RasterTickHandler.addRasterJob(createRaster().setSize(256, 256));
            
            if (x512.isChecked())
                RasterTickHandler.addRasterJob(createRaster().setSize(512, 512));
        }
    }
    
    private RasterJob createRaster()
    {
        RasterJob r = new RasterJob();
        return r;
    }
}
