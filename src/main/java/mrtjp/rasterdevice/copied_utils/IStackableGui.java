package mrtjp.rasterdevice.copied_utils;

import net.minecraft.client.gui.GuiScreen;

public interface IStackableGui
{
    public GuiScreen getPreviousScreen();

    public void prepareReDisplay();
}
