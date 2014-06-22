package mrtjp.rasterdevice;

import java.util.ArrayDeque;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.Side;

public class RasterTickHandler
{
    private static ArrayDeque<RasterJob> queue = new ArrayDeque<RasterJob>();

    public static void addRasterJob(RasterJob job)
    {
        queue.addLast(job);
    }

    @SubscribeEvent
    public void tickEnd(TickEvent.PlayerTickEvent event)
    {
        if (event.phase == TickEvent.Phase.END && event.side == Side.CLIENT)
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
    }
}
