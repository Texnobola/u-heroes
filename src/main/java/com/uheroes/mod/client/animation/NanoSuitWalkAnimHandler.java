package com.uheroes.mod.client.animation;

import com.uheroes.mod.UHeroesMod;
import com.uheroes.mod.heroes.nanotech.armor.NanoSuitHandler;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationAccess;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Registers and drives {@link NanoSuitWalkAnimation} every client tick.
 *
 * <p>The animation activates when:
 * <ol>
 *   <li>Player wears the full Nano Suit</li>
 *   <li>Player is moving (deltaMovement length > threshold)</li>
 * </ol>
 *
 * <p>Layer priority 20 — below the saber attack animation (25) so attacks
 * properly override the walk pose.
 */
@Mod.EventBusSubscriber(modid = UHeroesMod.MOD_ID, value = Dist.CLIENT)
public class NanoSuitWalkAnimHandler {

    private static LocalPlayer lastRegisteredPlayer = null;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;

        if (player == null) {
            lastRegisteredPlayer = null;
            NanoSuitWalkAnimation.INSTANCE.setActive(false);
            return;
        }

        // Register animation layer once per player instance
        if (player != lastRegisteredPlayer) {
            try {
                PlayerAnimationAccess.getPlayerAnimLayer(player)
                    .addAnimLayer(20, NanoSuitWalkAnimation.INSTANCE);
                lastRegisteredPlayer = player;
            } catch (Exception e) {
                UHeroesMod.LOGGER.warn("[U-Heroes] Could not register walk anim: {}", e.getMessage());
                return;
            }
        }

        // Activate when wearing full suit and moving
        boolean wearing = NanoSuitHandler.isWearingFullNanoSuit(player);
        double speed = player.getDeltaMovement().horizontalDistance();
        boolean moving = speed > 0.02;

        NanoSuitWalkAnimation.INSTANCE.setActive(wearing && moving);
        NanoSuitWalkAnimation.INSTANCE.tick((float) speed);
    }
}