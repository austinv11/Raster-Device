package mrtjp.rasterdevice;

public class Proxy
{
    public void preInit()
    {
        
    }
    
    public void init()
    {
        RasterDevice.rasterDevice = new ItemRasterDevice();
    }
    
    public void postInit()
    {
        
    }
}
