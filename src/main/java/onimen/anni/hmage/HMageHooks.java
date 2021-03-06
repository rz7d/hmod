package onimen.anni.hmage;

import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.network.play.server.SPacketParticles;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import onimen.anni.hmage.event.DrawWorldBackgroundEvent;
import onimen.anni.hmage.event.GetLocationCapeEvent;
import onimen.anni.hmage.event.PlayParticleEvent;

public class HMageHooks {

  public static boolean onDrawWorldBackground() {
    return MinecraftForge.EVENT_BUS.post(new DrawWorldBackgroundEvent());
  }

  public static ResourceLocation onGetLocationCape(AbstractClientPlayer player) {
    GetLocationCapeEvent event = new GetLocationCapeEvent(player);
    MinecraftForge.EVENT_BUS.post(event);
    return event.getCapeLocation();
  }

  public static boolean onhandleParticles(SPacketParticles particle) {
    PlayParticleEvent event = new PlayParticleEvent(particle);
    MinecraftForge.EVENT_BUS.post(event);
    return event.isCanceled();
  }

}
