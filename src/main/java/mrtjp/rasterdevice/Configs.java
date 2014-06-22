package mrtjp.rasterdevice;

import java.io.File;

import net.minecraftforge.common.config.Configuration;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

public class Configs
{
    protected static File _configFolder;
    protected static File _configFile;

    /** Constants **/
    public static final String modName = "Raster Device";
    public static final String version = "@VERSION@";
    public static final String buildnumber = "@BUILD_NUMBER@";

    /** Settings **/

    
    /** Render **/


    public static void init(FMLPreInitializationEvent event)
    {
        _configFolder = event.getModConfigurationDirectory();
        _configFile = new File(_configFolder.getAbsolutePath() + "/RasterDevice.cfg");
        loadPropertiesFromFile(_configFile);
    }

    public static void loadPropertiesFromFile(File file)
    {
        Configuration localConfig = new Configuration(file);
        localConfig.load();
        localConfig.save();
    }
}