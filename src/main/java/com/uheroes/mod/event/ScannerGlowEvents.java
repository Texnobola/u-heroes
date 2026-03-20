package com.uheroes.mod.event;

import com.uheroes.mod.UHeroesMod;
import com.uheroes.mod.client.hud.ScannerHUD;
import com.uheroes.mod.core.network.ScannerDataPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderNameTagEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber(modid = UHeroesMod.MOD_ID, value = Dist.CLIENT)
public class ScannerGlowEvents {

    /**
     * Make scanned entities glow cyan through walls.
     * Minecraft's built-in glowing system handles the through-wall rendering.
     */
    @SubscribeEvent
    public static void onRenderNameTag(RenderNameTagEvent event) {
        if (!ScannerHUD.scannerActive) return;
        // Name tag rendering is handled separately in ScannerHUD overlay
        // Suppress vanilla name tags for scanned entities to avoid clutter
    }

    /**
     * Called every client tick to apply/remove glowing status.
     * We use Entity.setGlowingTag() so vanilla handles the cyan outline through walls.
     */
    public static void tickGlow() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        List<ScannerDataPacket.TargetInfo> targets = ScannerHUD.getTargets();

        for (Entity entity : mc.level.entitiesForRendering()) {
            boolean shouldGlow = ScannerHUD.scannerActive &&
                targets.stream().anyMatch(t -> t.entityId == entity.getId());
            if (entity.isCurrentlyGlowing() != shouldGlow) {
                entity.setGlowingTag(shouldGlow);
            }
        }
    }
}