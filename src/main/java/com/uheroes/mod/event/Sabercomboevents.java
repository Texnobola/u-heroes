package com.uheroes.mod.event;

import com.uheroes.mod.UHeroesMod;
import com.uheroes.mod.client.animation.SaberAttackData;
import com.uheroes.mod.core.flux.FluxCapability;
import com.uheroes.mod.heroes.nanotech.weapon.LaserSwordItem;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * SERVER-SIDE combo tracker.
 *
 * Mirrors the client combo index independently on the server so we can
 * apply flux costs and damage multipliers for attacks 8-10.
 *
 * Combo resets after COMBO_RESET_MS of inactivity (tracked via System time
 * since server ticks can vary).
 */
@Mod.EventBusSubscriber(modid = UHeroesMod.MOD_ID)
public class SaberComboEvents {

    private static final int TOTAL_ATTACKS   = SaberAttackData.ATTACKS.length; // 10
    private static final long COMBO_RESET_MS = 2000L; // 2 seconds

    // Per-player tracking would need a Map — using single player for simplicity.
    // For multiplayer, key by player UUID.
    private static final java.util.Map<java.util.UUID, int[]>  serverComboIndex =
            new java.util.HashMap<>();
    private static final java.util.Map<java.util.UUID, Long>   lastAttackTime   =
            new java.util.HashMap<>();

    @SubscribeEvent
    public static void onAttackEntity(AttackEntityEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.level().isClientSide()) return;
        if (!(player.getMainHandItem().getItem() instanceof LaserSwordItem)) return;

        java.util.UUID id  = player.getUUID();
        long           now = System.currentTimeMillis();

        // Reset combo if too long since last attack
        long last = lastAttackTime.getOrDefault(id, 0L);
        if (now - last > COMBO_RESET_MS) {
            serverComboIndex.put(id, new int[]{0});
        }
        lastAttackTime.put(id, now);

        int[] indexBox = serverComboIndex.computeIfAbsent(id, k -> new int[]{0});
        int   index    = indexBox[0];

        SaberAttackData attack = SaberAttackData.ATTACKS[index];

        // Advance for next attack
        indexBox[0] = (index + 1) % TOTAL_ATTACKS;

        // Apply flux cost (attacks 8-10)
        if (attack.fluxCost > 0) {
            boolean hadFlux = FluxCapability.consume(player, attack.fluxCost);
            if (!hadFlux) return; // not enough flux — no bonus damage
        }

        // Apply damage multiplier via LivingHurtEvent is cleaner, but
        // we can't easily inject here. Instead we store the pending multiplier
        // and apply it in a companion LivingHurtEvent below.
        pendingMultiplier.put(id, attack.damageMultiplier);
        pendingAttackName.put(id, attack.name);
    }

    // Pending multipliers set by onAttackEntity, consumed by onLivingHurt
    private static final java.util.Map<java.util.UUID, Float>  pendingMultiplier =
            new java.util.HashMap<>();
    private static final java.util.Map<java.util.UUID, String> pendingAttackName =
            new java.util.HashMap<>();

    @SubscribeEvent
    public static void onLivingHurt(net.minecraftforge.event.entity.living.LivingHurtEvent event) {
        if (!(event.getSource().getEntity() instanceof Player player)) return;
        if (player.level().isClientSide()) return;

        java.util.UUID id = player.getUUID();
        Float mult = pendingMultiplier.remove(id);
        if (mult == null || mult == 1.0f) return;

        event.setAmount(event.getAmount() * mult);

        String name = pendingAttackName.remove(id);

        // Spawn particles for powerful attacks (8-10)
        if (player.level() instanceof ServerLevel serverLevel) {
            int particleCount = mult > 2.0f ? 20 : mult > 1.5f ? 12 : 8;
            var particleType  = mult > 2.0f ? ParticleTypes.ENCHANTED_HIT : ParticleTypes.CRIT;
            for (int i = 0; i < particleCount; i++) {
                double ox = (player.getRandom().nextDouble() - 0.5) * 0.6;
                double oy = (player.getRandom().nextDouble() - 0.5) * 0.6;
                double oz = (player.getRandom().nextDouble() - 0.5) * 0.6;
                serverLevel.sendParticles(particleType,
                    event.getEntity().getX() + ox,
                    event.getEntity().getY() + 1.0 + oy,
                    event.getEntity().getZ() + oz,
                    1, 0, 0, 0, 0.2);
            }
        }
    }
}