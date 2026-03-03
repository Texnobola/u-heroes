package com.uheroes.mod.core.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.uheroes.mod.UHeroesMod;
import com.uheroes.mod.core.flux.FluxCapability;
import com.uheroes.mod.core.network.FluxSyncPacket;
import com.uheroes.mod.core.network.ModNetwork;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Debug commands for Neural Flux system.
 */
@Mod.EventBusSubscriber(modid = UHeroesMod.MOD_ID)
public class FluxCommand {
    
    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        
        dispatcher.register(Commands.literal("uh_flux")
            .requires(source -> source.hasPermission(2))
            .then(Commands.literal("get")
                .executes(FluxCommand::getFlux))
            .then(Commands.literal("set")
                .then(Commands.argument("amount", IntegerArgumentType.integer(0))
                    .executes(FluxCommand::setFlux)))
            .then(Commands.literal("setmax")
                .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                    .executes(FluxCommand::setMaxFlux)))
            .then(Commands.literal("consume")
                .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                    .executes(FluxCommand::consumeFlux)))
            .then(Commands.literal("fill")
                .executes(FluxCommand::fillFlux))
            .then(Commands.literal("regen")
                .then(Commands.argument("rate", FloatArgumentType.floatArg(0.0f))
                    .executes(FluxCommand::setRegenRate)))
        );
    }
    
    private static int getFlux(CommandContext<CommandSourceStack> ctx) {
        if (!(ctx.getSource().getEntity() instanceof ServerPlayer player)) {
            return 0;
        }
        
        player.getCapability(FluxCapability.INSTANCE).ifPresent(flux -> {
            String overcharged = flux.isOvercharged() ? "§a✓" : "§c✗";
            player.sendSystemMessage(Component.translatable("command.u_heroes.flux.get",
                flux.getCurrentFlux(),
                flux.getMaxFlux(),
                flux.getRegenRate(),
                flux.getRegenCooldown(),
                overcharged
            ));
        });
        
        return 1;
    }
    
    private static int setFlux(CommandContext<CommandSourceStack> ctx) {
        if (!(ctx.getSource().getEntity() instanceof ServerPlayer player)) {
            return 0;
        }
        
        int amount = IntegerArgumentType.getInteger(ctx, "amount");
        
        player.getCapability(FluxCapability.INSTANCE).ifPresent(flux -> {
            flux.setCurrentFlux(amount);
            player.sendSystemMessage(Component.translatable("command.u_heroes.flux.set", amount));
            syncFlux(player, flux);
        });
        
        return 1;
    }
    
    private static int setMaxFlux(CommandContext<CommandSourceStack> ctx) {
        if (!(ctx.getSource().getEntity() instanceof ServerPlayer player)) {
            return 0;
        }
        
        int amount = IntegerArgumentType.getInteger(ctx, "amount");
        
        player.getCapability(FluxCapability.INSTANCE).ifPresent(flux -> {
            flux.setMaxFlux(amount);
            player.sendSystemMessage(Component.translatable("command.u_heroes.flux.setmax", amount));
            syncFlux(player, flux);
        });
        
        return 1;
    }
    
    private static int consumeFlux(CommandContext<CommandSourceStack> ctx) {
        if (!(ctx.getSource().getEntity() instanceof ServerPlayer player)) {
            return 0;
        }
        
        int amount = IntegerArgumentType.getInteger(ctx, "amount");
        
        player.getCapability(FluxCapability.INSTANCE).ifPresent(flux -> {
            if (flux.consumeFlux(amount)) {
                player.sendSystemMessage(Component.translatable("command.u_heroes.flux.consume_success",
                    amount, flux.getCurrentFlux()));
            } else {
                player.sendSystemMessage(Component.translatable("command.u_heroes.flux.consume_fail",
                    flux.getCurrentFlux(), amount));
            }
            syncFlux(player, flux);
        });
        
        return 1;
    }
    
    private static int fillFlux(CommandContext<CommandSourceStack> ctx) {
        if (!(ctx.getSource().getEntity() instanceof ServerPlayer player)) {
            return 0;
        }
        
        player.getCapability(FluxCapability.INSTANCE).ifPresent(flux -> {
            flux.setCurrentFlux(flux.getMaxFlux());
            player.sendSystemMessage(Component.translatable("command.u_heroes.flux.fill", flux.getMaxFlux()));
            syncFlux(player, flux);
        });
        
        return 1;
    }
    
    private static int setRegenRate(CommandContext<CommandSourceStack> ctx) {
        if (!(ctx.getSource().getEntity() instanceof ServerPlayer player)) {
            return 0;
        }
        
        float rate = FloatArgumentType.getFloat(ctx, "rate");
        
        player.getCapability(FluxCapability.INSTANCE).ifPresent(flux -> {
            flux.setRegenRate(rate);
            player.sendSystemMessage(Component.translatable("command.u_heroes.flux.regen_set", rate));
            syncFlux(player, flux);
        });
        
        return 1;
    }
    
    private static void syncFlux(ServerPlayer player, com.uheroes.mod.core.flux.NeuralFlux flux) {
        ModNetwork.sendToPlayer(
            new FluxSyncPacket(flux.getCurrentFlux(), flux.getMaxFlux(), flux.isOvercharged()),
            player
        );
    }
}
