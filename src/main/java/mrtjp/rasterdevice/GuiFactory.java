package mrtjp.rasterdevice;

import cpw.mods.fml.client.IModGuiFactory;
import cpw.mods.fml.client.config.GuiConfig;
import cpw.mods.fml.client.config.GuiUnicodeGlyphButton;
import cpw.mods.fml.client.config.IConfigElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;

import java.awt.*;
import java.util.ArrayList;
import java.util.Set;

import static cpw.mods.fml.client.config.GuiUtils.RESET_CHAR;

public class GuiFactory implements IModGuiFactory
{
    public static class FMLConfigGuiScreen extends GuiConfig
    {

        private GuiTextField text;

        public FMLConfigGuiScreen(GuiScreen parent)
        {
            super(parent, new ArrayList<IConfigElement>(), "FML", false, false,
                    "Render items into png files for use in blogs, wikis, etc.");
        }

        @Override
        protected void actionPerformed(GuiButton button)
        {
            super.actionPerformed(button);

            switch (button.id)
            {
                case 2222:new RasterJob(16, 16).raster(); break;
                case 2223:new RasterJob(32, 32).raster(); break;
                case 2224:new RasterJob(64, 64).raster(); break;
                case 2225:new RasterJob(128, 128).raster(); break;
                case 2226:new RasterJob(256, 256).raster(); break;
                case 3333:
                    new RasterJob(16, 16).raster();
                    new RasterJob(32, 32).raster();
                    new RasterJob(64, 64).raster();
                    new RasterJob(128, 128).raster();
                    new RasterJob(256, 256).raster();
                    break;
                default:
            }
        }

        @Override
        public void initGui()
        {
            super.initGui();

            int dy = 5;

            for (int i = 0; i < 5; i++)
            {
                int x = this.width/2-(65/2);
                int y = this.height/dy+i*25;
                GuiUnicodeGlyphButton b = new GuiUnicodeGlyphButton(2222+i, x, y, 80, 20,
                        "Render x"+(16<<i), RESET_CHAR, 2.0F);
                this.buttonList.add(b);
            }

            this.buttonList.add(new GuiUnicodeGlyphButton(3333, this.width/2-(65/2)+80,
                    this.height/dy+4*25, 80, 20, "Render all", RESET_CHAR, 2.0F));
            text = new GuiTextField(Minecraft.getMinecraft().fontRenderer, this.width/2-(65/2)+80,
                    this.height/dy+3*25, 80, 20);
            text.setText(RasterDeviceMod.instance.MOD_ID_TO_RASTER);
        }

        @Override
        protected void mouseClicked(int x, int y, int mouseEvent)
        {
            super.mouseClicked(x, y, mouseEvent);
            text.mouseClicked(x, y, mouseEvent);
        }

        @Override
        public void updateScreen()
        {
            super.updateScreen();
            text.updateCursorCounter();
        }

        @Override
        protected void keyTyped(char eventChar, int eventKey)
        {
            super.keyTyped(eventChar, eventKey);
            if (text.textboxKeyTyped(eventChar, eventKey))
                RasterDeviceMod.instance.MOD_ID_TO_RASTER = text.getText();
        }

        @Override
        public void drawScreen(int mouseX, int mouseY, float partialTicks)
        {
            super.drawScreen(mouseX, mouseY, partialTicks);
            Minecraft.getMinecraft().fontRenderer.drawString("Mod ID to Raster:", this.width/2-(65/2)+80,
                    this.height/5+(int)Math.floor(2.5*25), Color.WHITE.getRGB());
            text.drawTextBox();
        }
    }

    @Override
    public void initialize(Minecraft minecraftInstance)
    {
    }

    @Override
    public Class<? extends GuiScreen> mainConfigGuiClass()
    {
        return FMLConfigGuiScreen.class;
    }

    @Override
    public Set<RuntimeOptionCategoryElement> runtimeGuiCategories()
    {
        return null;
    }

    @Override
    public RuntimeOptionGuiHandler getHandlerFor(RuntimeOptionCategoryElement element)
    {
        return null;
    }
}
