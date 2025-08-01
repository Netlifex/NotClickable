package com.example.notclickable;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.joml.Vector3f;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Mod("notclickable")
public class NotClickableMod {
    public static final String MODID = "notclickable";

    // Карта: позиции блоков оставшееся время подсветки тд. и тп. плак плак
    private final Map<BlockPos, Integer> particlesToRender = new HashMap<>();

    public NotClickableMod() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        dispatcher.register(Commands.literal("notclickable")

                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                        .executes(context -> {
                            BlockPos pos = BlockPosArgument.getLoadedBlockPos(context, "pos");
                            ServerLevel level = context.getSource().getLevel();
                            NotClickableData data = NotClickableData.get(level);
                            data.add(pos);
                            context.getSource().sendSuccess(
                                    () -> Component.translatable("commands.notclickable.set_true",
                                            pos.getX(), pos.getY(), pos.getZ()), true);
                            return 1;
                        })
                        .then(Commands.argument("state", BoolArgumentType.bool())
                                .executes(context -> {
                                    BlockPos pos = BlockPosArgument.getLoadedBlockPos(context, "pos");
                                    boolean state = BoolArgumentType.getBool(context, "state");
                                    ServerLevel level = context.getSource().getLevel();
                                    NotClickableData data = NotClickableData.get(level);
                                    if (state) {
                                        data.add(pos);
                                        context.getSource().sendSuccess(
                                                () -> Component.translatable("commands.notclickable.set_true",
                                                        pos.getX(), pos.getY(), pos.getZ()), true);
                                    } else {
                                        data.remove(pos);
                                        context.getSource().sendSuccess(
                                                () -> Component.translatable("commands.notclickable.set_false",
                                                        pos.getX(), pos.getY(), pos.getZ()), true);
                                    }
                                    return 1;
                                })
                        )
                )

                .then(Commands.argument("state", BoolArgumentType.bool())
                        .executes(context -> {
                            ServerLevel level = context.getSource().getLevel();

                            HitResult hit = context.getSource().getPlayerOrException().pick(20, 0, false);
                            if (hit.getType() != HitResult.Type.BLOCK) {
                                context.getSource().sendFailure(Component.translatable("commands.notclickable.no_block"));
                                return 0;
                            }

                            BlockPos pos = ((BlockHitResult) hit).getBlockPos();
                            boolean state = BoolArgumentType.getBool(context, "state");

                            NotClickableData data = NotClickableData.get(level);
                            if (state) {
                                data.add(pos);
                                context.getSource().sendSuccess(
                                        () -> Component.translatable("commands.notclickable.set_true",
                                                pos.getX(), pos.getY(), pos.getZ()), true);
                            } else {
                                data.remove(pos);
                                context.getSource().sendSuccess(
                                        () -> Component.translatable("commands.notclickable.set_false",
                                                pos.getX(), pos.getY(), pos.getZ()), true);
                            }
                            return 1;
                        })
                )

                .then(Commands.literal("list")
                        .executes(context -> {
                            ServerLevel level = context.getSource().getLevel();
                            NotClickableData data = NotClickableData.get(level);

                            if (data.getPositions().isEmpty()) {
                                context.getSource().sendSuccess(
                                        () -> Component.translatable("commands.notclickable.list_empty"), false);
                            } else {
                                context.getSource().sendSuccess(
                                        () -> Component.translatable("commands.notclickable.list_header"), false);

                                for (BlockPos pos : data.getPositions()) {
                                    String coords = pos.getX() + " " + pos.getY() + " " + pos.getZ();

                                    Component clickable = Component.literal(" → " + coords)
                                            .withStyle(style -> style
                                                    .withColor(net.minecraft.ChatFormatting.AQUA)
                                                    .withClickEvent(new net.minecraft.network.chat.ClickEvent(
                                                            net.minecraft.network.chat.ClickEvent.Action.RUN_COMMAND,
                                                            "/tp @s " + coords
                                                    ))
                                                    .withHoverEvent(new net.minecraft.network.chat.HoverEvent(
                                                            net.minecraft.network.chat.HoverEvent.Action.SHOW_TEXT,
                                                            Component.translatable("commands.notclickable.list_hover")
                                                    ))
                                            );

                                    context.getSource().sendSuccess(() -> clickable, false);

                                    // (100 тиков = 5 секунд ведь так? я не помню)
                                    particlesToRender.put(pos.immutable(), 100);
                                }
                            }
                            return 1;
                        })
                )
        );
    }

    @SubscribeEvent
    public void onPlayerInteract(PlayerInteractEvent.RightClickBlock event) {
        if (!event.getLevel().isClientSide() && event.getLevel() instanceof ServerLevel serverLevel) {
            BlockPos pos = event.getPos();
            NotClickableData data = NotClickableData.get(serverLevel);
            if (data.contains(pos)) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || particlesToRender.isEmpty()) return;

        Iterator<Map.Entry<BlockPos, Integer>> iterator = particlesToRender.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<BlockPos, Integer> entry = iterator.next();
            BlockPos pos = entry.getKey();
            int ticks = entry.getValue();

            if (ticks <= 0) {
                iterator.remove();
                continue;
            }

            entry.setValue(ticks - 1);

            // Показываем частицу людишкам
            for (ServerPlayer player : ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers()) {
                ServerLevel level = (ServerLevel) player.level();

                spawnBlockEdgeParticles(level, pos, player);

            }
        }
    }
    private static final DustParticleOptions STATIC_PARTICLE = new DustParticleOptions(
            new Vector3f(0.0F, 1.0F, 1.0F), // цвет бирюзовый
            0.5F // размер частицы
    );

    private void spawnBlockEdgeParticles(ServerLevel level, BlockPos pos, ServerPlayer player) {
        double step = 0.1;

        for (double dx = 0; dx <= 1; dx += step) {
            level.sendParticles(player, STATIC_PARTICLE, true, pos.getX() + dx, pos.getY(), pos.getZ(), 1, 0, 0, 0, 0);
            level.sendParticles(player, STATIC_PARTICLE, true, pos.getX() + dx, pos.getY(), pos.getZ() + 1, 1, 0, 0, 0, 0);
            level.sendParticles(player, STATIC_PARTICLE, true, pos.getX() + dx, pos.getY() + 1, pos.getZ(), 1, 0, 0, 0, 0);
            level.sendParticles(player, STATIC_PARTICLE, true, pos.getX() + dx, pos.getY() + 1, pos.getZ() + 1, 1, 0, 0, 0, 0);
        }

        for (double dy = 0; dy <= 1; dy += step) {
            level.sendParticles(player, STATIC_PARTICLE, true, pos.getX(), pos.getY() + dy, pos.getZ(), 1, 0, 0, 0, 0);
            level.sendParticles(player, STATIC_PARTICLE, true, pos.getX() + 1, pos.getY() + dy, pos.getZ(), 1, 0, 0, 0, 0);
            level.sendParticles(player, STATIC_PARTICLE, true, pos.getX(), pos.getY() + dy, pos.getZ() + 1, 1, 0, 0, 0, 0);
            level.sendParticles(player, STATIC_PARTICLE, true, pos.getX() + 1, pos.getY() + dy, pos.getZ() + 1, 1, 0, 0, 0, 0);
        }

        for (double dz = 0; dz <= 1; dz += step) {
            level.sendParticles(player, STATIC_PARTICLE, true, pos.getX(), pos.getY(), pos.getZ() + dz, 1, 0, 0, 0, 0);
            level.sendParticles(player, STATIC_PARTICLE, true, pos.getX() + 1, pos.getY(), pos.getZ() + dz, 1, 0, 0, 0, 0);
            level.sendParticles(player, STATIC_PARTICLE, true, pos.getX(), pos.getY() + 1, pos.getZ() + dz, 1, 0, 0, 0, 0);
            level.sendParticles(player, STATIC_PARTICLE, true, pos.getX() + 1, pos.getY() + 1, pos.getZ() + dz, 1, 0, 0, 0, 0);
        }
    }

}
