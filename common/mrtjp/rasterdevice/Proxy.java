package mrtjp.rasterdevice;

public class Proxy
{
    public void preInit()
    {
        
    }
    
    public void init()
    {
        RasterDevice.rasterDevice = new ItemRasterDevice(Configs.item_RasterDevice);
    }
    
    public void postInit()
    {
        
    }
}
