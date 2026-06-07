package me.purpleeast.mods.ppe_essentials;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class PpeEvents {
    private static final Map<UUID, Integer> BACK_NOTICE_TICKS = new HashMap<>();
    private static final Map<UUID, Integer> FIRST_JOIN_NOTICE_TICKS = new HashMap<>();

    public static void register() {
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            if (entity instanceof ServerPlayer player) {
                PpePlayerData.get(player.server).setDeathBack(player.getUUID(), PpeLocation.of(player));
            }
        });
        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, damageSource, amount) ->
                !(entity instanceof ServerPlayer player && PpePlayerData.get(player.server).isGodEnabled(player.getUUID()))
        );
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            if (PpePlayerData.get(newPlayer.server).markBackNoticeShown(newPlayer.getUUID())) {
                BACK_NOTICE_TICKS.put(newPlayer.getUUID(), newPlayer.server.getTickCount() + 10);
            }
        });
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayer player = handler.player;
            if (PpeConfig.firstJoinNotice() && !PpePlayerData.get(server).hasFirstJoinNoticeShown(player.getUUID())) {
                FIRST_JOIN_NOTICE_TICKS.put(player.getUUID(), server.getTickCount() + 40);
            }
        });
        ServerTickEvents.END_SERVER_TICK.register(PpeEvents::onServerTick);
    }

    public static void clearNoticeQueues() {
        BACK_NOTICE_TICKS.clear();
        FIRST_JOIN_NOTICE_TICKS.clear();
    }

    private static void onServerTick(MinecraftServer server) {
        Iterator<Map.Entry<UUID, Integer>> backIterator = BACK_NOTICE_TICKS.entrySet().iterator();
        while (backIterator.hasNext()) {
            Map.Entry<UUID, Integer> entry = backIterator.next();
            if (entry.getValue() > server.getTickCount()) {
                continue;
            }

            backIterator.remove();
            ServerPlayer player = server.getPlayerList().getPlayer(entry.getKey());
            if (player != null) {
                player.playNotifySound(SoundEvents.CHICKEN_EGG, SoundSource.PLAYERS, 1.0F, 1.2F);
                player.sendSystemMessage(PpeLang.prefixedComponent(player, "ppe_essentials.back.notice")
                        .withStyle(Style.EMPTY
                                .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/back"))
                                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, PpeLang.component(player, "ppe_essentials.back.notice.tooltip")))));
            }
        }

        Iterator<Map.Entry<UUID, Integer>> joinIterator = FIRST_JOIN_NOTICE_TICKS.entrySet().iterator();
        while (joinIterator.hasNext()) {
            Map.Entry<UUID, Integer> entry = joinIterator.next();
            if (entry.getValue() > server.getTickCount()) {
                continue;
            }

            joinIterator.remove();
            ServerPlayer player = server.getPlayerList().getPlayer(entry.getKey());
            if (player != null && PpePlayerData.get(server).markFirstJoinNoticeShown(player.getUUID())) {
                player.playNotifySound(SoundEvents.CHICKEN_EGG, SoundSource.PLAYERS, 1.0F, 1.2F);
                player.sendSystemMessage(PpeLang.prefixedComponent(player, "ppe_essentials.first_join.notice")
                        .withStyle(Style.EMPTY
                                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/ppe-ess help"))
                                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, PpeLang.component(player, "ppe_essentials.first_join.notice.tooltip")))));
            }
        }
    }
}
