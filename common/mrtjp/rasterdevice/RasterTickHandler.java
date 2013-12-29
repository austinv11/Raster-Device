package mrtjp.rasterdevice;

import java.util.ArrayDeque;
import java.util.EnumSet;

import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;

public class RasterTickHandler implements ITickHandler
{
    private static ArrayDeque<RasterJob> queue = new ArrayDeque<RasterJob>();
    
    public static void addRasterJob(RasterJob job)
    {
        queue.addLast(job);
    }
    
    @Override
    public void tickStart(EnumSet<TickType> type, Object... tickData)
    {   
    }

    @Override
    public void tickEnd(EnumSet<TickType> type, Object... tickData)
    {
        if (!queue.isEmpty())
        {
            RasterJob r = queue.poll();
            
            if (r.pushWindowSize())
            {
                queue.addFirst(r);
                return; // So minecraft can run a tick and adjust the display.
            }
            
            r.raster();
            r.popWindowSize();
        }
    }

    @Override
    public EnumSet<TickType> ticks()
    {
        return EnumSet.of(TickType.CLIENT);
    }

    @Override
    public String getLabel()
    {
        return "item rastering";
    }
}
