package onimen.anni.hmage;

import java.io.File;
import java.util.Map;
import java.util.UUID;

import org.apache.logging.log4j.Logger;

import com.google.common.collect.Maps;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.GameSettings.Options;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.MovementInputFromOptions;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.GuiScreenEvent.InitGuiEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import onimen.anni.hmage.cape.GlobalPlayerUseCapeManager;
import onimen.anni.hmage.cape.SPPlayerUseCape;
import onimen.anni.hmage.command.DebugCommand;
import onimen.anni.hmage.command.NameCommand;
import onimen.anni.hmage.event.GetLocationCapeEvent;
import onimen.anni.hmage.event.PlayParticleEvent;
import onimen.anni.hmage.gui.GuiAnniServers;
import onimen.anni.hmage.gui.GuiSettings;
import onimen.anni.hmage.module.CustomGuiBackground;
import onimen.anni.hmage.module.FixedFOV;
import onimen.anni.hmage.module.InterfaceModule;
import onimen.anni.hmage.module.RecipeBookRemover;
import onimen.anni.hmage.module.hud.AcroJumpHUD;
import onimen.anni.hmage.module.hud.ArmorDurabilityHUD;
import onimen.anni.hmage.module.hud.ArrowCounterHUD;
import onimen.anni.hmage.module.hud.CpsCounterHUD;
import onimen.anni.hmage.module.hud.InterfaceHUD;
import onimen.anni.hmage.module.hud.KillCounterHUD;
import onimen.anni.hmage.module.hud.NexusDamageHUD;
import onimen.anni.hmage.module.hud.StatusEffectHUD;
import onimen.anni.hmage.observer.AnniChatReciveExecutor;
import onimen.anni.hmage.observer.AnniObserverMap;
import onimen.anni.hmage.observer.killeffect.AnniKillEffectManager;
import onimen.anni.hmage.transformer.HurtingArmorInjector;
import onimen.anni.hmage.util.CustomMovementInput;
import onimen.anni.hmage.util.GuiScreenUtils;
import onimen.anni.hmage.util.ShotbowUtils;
import onimen.anni.hmage.util.scheduler.SyncTaskQueue;

@Mod(modid = HMage.MODID, name = HMage.NAME, version = HMage.VERSION)
public class HMage {
  public static final String MODID = "hmage";
  public static final String NAME = "HMage";
  public static final String VERSION = "1.0.1";
  public static Logger logger;

  public static HMage INSTANCE;
  private Minecraft mc;

  private CustomMovementInput customMovementInput;

  private static Map<String, InterfaceModule> moduleMap = Maps.newLinkedHashMap();
  private static Map<String, InterfaceHUD> hudMap = Maps.newLinkedHashMap();

  public static AnniObserverMap anniObserverMap;
  public static File modConfigurationDirectory;

  public void registerModule(InterfaceModule module) {
    if (module == null) { return; }

    MinecraftForge.EVENT_BUS.register(module);

    moduleMap.put(module.getName(), module);
    if (module instanceof InterfaceHUD) {
      hudMap.put(module.getName(), (InterfaceHUD) module);
    }
  }

  public static Map<String, InterfaceModule> getModuleMap() {
    return moduleMap;
  }

  public static Map<String, InterfaceHUD> getHUDMap() {
    return hudMap;
  }

  public HMage() {
    INSTANCE = this;
  }

  @SideOnly(Side.CLIENT)
  @EventHandler
  public void preInit(FMLPreInitializationEvent event) {
    MinecraftForge.EVENT_BUS.register(this);
    logger = event.getModLog();
    Preferences.load(event);
    modConfigurationDirectory = event.getModConfigurationDirectory();
    anniObserverMap = AnniObserverMap.getInstance();

    //明るさの設定を変更
    Options options = GameSettings.Options.GAMMA;
    options.setValueMax(15);
  }

  @SideOnly(Side.CLIENT)
  @EventHandler
  public void init(FMLInitializationEvent event) {

    this.mc = Minecraft.getMinecraft();

    //Register Modules
    this.registerModule(new RecipeBookRemover());
    this.registerModule(new FixedFOV());
    this.registerModule(new CustomGuiBackground());

    //HUD
    this.registerModule(new ArrowCounterHUD());
    this.registerModule(new StatusEffectHUD());
    this.registerModule(new ArmorDurabilityHUD());
    this.registerModule(new CpsCounterHUD());
    this.registerModule(new AcroJumpHUD());
    this.registerModule(new KillCounterHUD());
    this.registerModule(new NexusDamageHUD());

    ClientCommandHandler.instance.registerCommand(new DebugCommand());
    ClientCommandHandler.instance.registerCommand(new NameCommand());
    ClientRegistry.registerKeyBinding(Preferences.openSettingsKey);

    RenderManager renderManager = this.mc.getRenderManager();
    HurtingArmorInjector.replaceSkinMap(renderManager);
    //Capeをロード
    GlobalPlayerUseCapeManager.loadCape();

    //Anni用スレッド開始
    AnniChatReciveExecutor.startThread();

    customMovementInput = new CustomMovementInput(Minecraft.getMinecraft().gameSettings);
  }

  @SubscribeEvent
  public void onClientTick(ClientTickEvent event) {
    if (mc == null)
      return;

    if (mc.player != null && customMovementInput != null) {
      //If player uses vanilla MovementInput. use togglesneak input
      if (mc.player.movementInput instanceof MovementInputFromOptions)
        mc.player.movementInput = customMovementInput;
    }

    if (anniObserverMap.getAnniObserver() != null) {
      anniObserverMap.getAnniObserver().onClientTick(event);
    }
  }

  @SubscribeEvent
  public void onKeyInput(KeyInputEvent event) {

    if (Preferences.openSettingsKey.isPressed()) {
      if (mc.currentScreen == null) {
        //mc.displayGuiScreen(new CapeSetting());
        mc.displayGuiScreen(new GuiSettings(getModuleMap()));
      }
    }
  }

  @SubscribeEvent
  public void onRenderGameOverlay(RenderGameOverlayEvent.Post event) {

    if (!Preferences.enabled)
      return;

    if (mc.gameSettings.showDebugInfo)
      return;

    if (event.getType() == ElementType.TEXT) {
      if (mc.currentScreen == null) {
        for (InterfaceHUD item : hudMap.values()) {
          if (item.isEnable())
            item.drawItem(mc);
        }
      }
    }
  }

  @SubscribeEvent
  public void onInitGuiEvent(InitGuiEvent event) {
    if (!(event.getGui() instanceof GuiChest)) { return; }
    GuiChest gui = (GuiChest) event.getGui();

    if (gui instanceof GuiAnniServers) { return; }

    IInventory chestInventory = GuiScreenUtils.getChestInventory(gui);

    if (chestInventory == null) { return; }

    ITextComponent chestDisplayName = chestInventory.getDisplayName();
    if (chestDisplayName.getFormattedText().startsWith(GuiScreenUtils.SELEC_SERVER)) {
      mc.displayGuiScreen(new GuiAnniServers(mc.player.inventory, chestInventory));
    }
  }

  @SubscribeEvent
  public void onRecieveChat(ClientChatReceivedEvent event) {
    if (anniObserverMap.getAnniObserver() != null) {
      anniObserverMap.getAnniObserver().onRecieveChat(event);
    }
  }

  @SideOnly(Side.CLIENT)
  @SubscribeEvent
  public void onClientTickEvent(TickEvent.ClientTickEvent event) {
    Runnable nextTask;
    while ((nextTask = SyncTaskQueue.getNextTask()) != null) {
      nextTask.run();
    }
  }

  @SideOnly(Side.CLIENT)
  @SubscribeEvent
  public void onPlayParticleEvent(PlayParticleEvent event) {
    if (ShotbowUtils.isShotbow(Minecraft.getMinecraft())) {
      AnniKillEffectManager.getInstance().executeKillEffect(event.getParticle());
    }
  }

  @SubscribeEvent
  public void onGetLocationCape(GetLocationCapeEvent event) {

    //capeのリソースを取得
    UUID uniqueID = event.getPlayer().getUniqueID();
    boolean isMe = uniqueID.equals(Minecraft.getMinecraft().player.getUniqueID());
    ResourceLocation locationCape = isMe ? SPPlayerUseCape.getResourceLocation()
        : GlobalPlayerUseCapeManager.getCapeResource(uniqueID);

    event.setCapeLocation(locationCape);
  }
}
