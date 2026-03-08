package com.uheroes.mod.client.animation;

import com.uheroes.mod.UHeroesMod;
import com.uheroes.mod.heroes.nanotech.weapon.LaserSwordItem;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationAccess;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * CLIENT-SIDE combo tracker.
 *
 * Tracks which combo step the player is on (0–9) and fires the animation.
 * Combo resets after COMBO_RESET_TICKS of inactivity.
 *
 * How to detect left-click on CLIENT:
 *   Minecraft stores the attack cooldown in player.getAttackStrengthScale().
 *   When it resets from <1 back toward 1, an attack just happened.
 *   We compare the previous swing progress to detect the moment of click.
 *
 * Server-side damage / flux cost is handled in SaberComboEvents.java.
 */
@Mod.EventBusSubscriber(modid = UHeroesMod.MOD_ID, value = Dist.CLIENT)
public class SaberComboHandler {

    private static LocalPlayer lastRegisteredPlayer = null;

    // How many ticks of no attack before combo resets to 0
    private static final int COMBO_RESET_TICKS = 40;

    private static int comboIndex       = 0;
    private static int ticksSinceAttack = COMBO_RESET_TICKS;

    // Track last swing progress to detect the moment a click fires
    private static float lastSwingProgress = 1.0f;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            lastRegisteredPlayer = null;
            return;
        }

        // Register both animations on first encounter with this player
        if (player != lastRegisteredPlayer) {
            try {
                var stack = PlayerAnimationAccess.getPlayerAnimLayer(player);
                stack.addAnimLayer(10, SaberBlockAnimation.INSTANCE);
                stack.addAnimLayer(20, SaberComboAnimation.INSTANCE); // combo on top
                lastRegisteredPlayer = player;
            } catch (Exception e) {
                UHeroesMod.LOGGER.warn("[U-Heroes] Could not register animation layers: {}", e.getMessage());
                return;
            }
        }

        // Tick the combo animation
        SaberComboAnimation.INSTANCE.tick();

        // Only track combo when LaserSword is in main hand
        if (!(player.getMainHandItem().getItem() instanceof LaserSwordItem)) {
            lastSwingProgress = 1.0f;
            return;
        }

        // Detect attack: swing progress resets (goes from low → high suddenly)
        // player.getAttackStrengthScale(0) returns 0.0 right after swing, builds to 1.0
        float swingNow = player.getAttackStrengthScale(0f);
        boolean attackFired = (lastSwingProgress < 0.9f && swingNow > lastSwingProgress + 0.05f);
        lastSwingProgress = swingNow;

        if (attackFired) {
            // Reset combo index if too long since last attack
            if (ticksSinceAttack >= COMBO_RESET_TICKS) {
                comboIndex = 0;
            }
            ticksSinceAttack = 0;

            // Fire the animation for current combo step
            SaberComboAnimation.INSTANCE.startAttack(comboIndex);

            // Advance to next step (wraps back to 0 after 10)
            comboIndex = (comboIndex + 1) % SaberAttackData.ATTACKS.length;
        }

        // Count ticks since last attack
        if (ticksSinceAttack < COMBO_RESET_TICKS) {
            ticksSinceAttack++;
        }

        // Block animation toggle (sneak)
        boolean shouldBlock = player.isCrouching()
                && player.getMainHandItem().getItem() instanceof LaserSwordItem;
        SaberBlockAnimation.INSTANCE.setActive(shouldBlock);
    }
}