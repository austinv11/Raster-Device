package mrtjp.rasterdevice;

import net.minecraftforge.common.MinecraftForge;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = "RasterDevice", useMetadata = true)
public class RasterDevice
{
    /** Items **/
    public static ItemRasterDevice rasterDevice;
    
    @Instance("RasterDevice")
    public static RasterDevice instance;

    @SidedProxy(clientSide = "mrtjp.rasterdevice.ProxyC", serverSide = "mrtjp.rasterdevice.Proxy")
    public static Proxy proxy;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        Configs.init(event);
        proxy.preInit();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event)
    {
        MinecraftForge.EVENT_BUS.register(instance);
        proxy.init();
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event)
    {
        proxy.postInit();
        FMLCommonHandler.instance().bus().register(new RasterTickHandler());
    }
}
