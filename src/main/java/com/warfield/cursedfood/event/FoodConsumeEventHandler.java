package com.warfield.cursedfood.event;

import com.warfield.cursedfood.Config;
import com.warfield.cursedfood.CursedFoodMod;
import com.warfield.cursedfood.item.ModItems;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.UserBanList;
import net.minecraft.server.players.UserBanListEntry;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;

/**
 * Обработчик события окончания использования предмета (еды).
 *
 * @EventBusSubscriber без явного bus= регистрирует на Bus.GAME (игровые события),
 * что правильно для LivingEntityUseItemEvent. Аннотация обрабатывает регистрацию
 * автоматически — NeoForge сканирует классы мода при загрузке.
 */
@EventBusSubscriber(modid = CursedFoodMod.MODID)
public class FoodConsumeEventHandler {

    /**
     * Срабатывает, когда живая сущность закончила использовать предмет
     * (например, полностью съела еду).
     *
     * Логика бана:
     *  - На выделенном сервере (dedicated) и при открытом LAN-мире: настоящий бан
     *    через UserBanList + кик. При следующей попытке зайти сервер откажет в доступе.
     *  - В одиночной игре без открытого мира: бан-лист есть, но смысл в нём нулевой —
     *    игрок просто создаст новый мир или удалит bans.json. Поэтому при ban_enabled=false
     *    делаем только кик. При ban_enabled=true запись всё равно добавляется в бан-лист
     *    (вдруг мир откроют по LAN позже), но сразу же следует кик.
     */
    @SubscribeEvent
    public static void onFinishUsingItem(LivingEntityUseItemEvent.Finish event) {
        // Фильтр 1: только наш предмет
        if (!event.getItem().is(ModItems.BANHAMMER_PIE.get())) return;

        // Фильтр 2: только ServerPlayer (исключает мобов и клиентскую копию игрока)
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        // Фильтр 3: только серверная сторона (дополнительная защита от двойного срабатывания)
        if (player.level().isClientSide()) return;

        MinecraftServer server = player.getServer();
        if (server == null) return; // теоретически невозможно, но не будем падать с NPE

        String reason = Config.BAN_REASON.get();
        Component disconnectMessage = Component.literal(reason);

        if (Config.BAN_ENABLED.get()) {
            // Добавляем игрока в постоянный бан-лист сервера.
            // null в created/expires означает: дата = прямо сейчас / бессрочно.
            UserBanList banList = server.getPlayerList().getBans();
            UserBanListEntry entry = new UserBanListEntry(
                    player.getGameProfile(),
                    null,          // created: null = now
                    "CursedFood",  // source
                    null,          // expires: null = permanent
                    reason
            );
            banList.add(entry);
            CursedFoodMod.LOGGER.info("[CursedFood] Игрок {} забанен: {}", player.getName().getString(), reason);
        } else {
            CursedFoodMod.LOGGER.info("[CursedFood] Игрок {} кикнут (ban_enabled=false): {}", player.getName().getString(), reason);
        }

        // Кик обязателен в любом случае: добавление в бан-лист само по себе
        // не разрывает уже установленное соединение.
        player.connection.disconnect(disconnectMessage);
    }
}
