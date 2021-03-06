package onimen.anni.hmage.module;

import java.util.Iterator;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiButtonImage;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class RecipeBookRemover extends AbstractModule {

  @Override
  public String getId() {
    return "hmage.module.hide-recipebook";
  }

  @SubscribeEvent
  public void onInitGuiEvent(GuiScreenEvent.InitGuiEvent event) {
    if (!canBehaivor()) { return; }

    if (event.getGui() instanceof GuiInventory) {
      for (Iterator<GuiButton> itr = event.getButtonList().iterator(); itr.hasNext();) {
        GuiButton guiButton = itr.next();

        if (guiButton instanceof GuiButtonImage) {
          itr.remove();
        }
      }
    }
  }

}
