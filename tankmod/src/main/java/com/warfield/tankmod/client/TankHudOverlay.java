package com.warfield.tankmod.client;

import com.warfield.tankmod.Config;
import com.warfield.tankmod.TankMod;
import com.warfield.tankmod.entity.TankEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

/**
 * Фаза 7: кокпит-HUD танка.
 *
 * Рисуется поверх всего GUI (RenderGuiEvent.Post):
 *  - тёмная рамка по краям экрана (имитация смотрового люка)
 *  - прицельная сетка с дальномером; тёмный фон под крестиком перекрывает ванильный
 *  - полоса HP (нижний-левый угол)
 *  - счётчик снарядов / полоса перезарядки (нижний-правый)
 *  - скорость (нижний-центр)
 */
@EventBusSubscriber(modid = TankMod.MODID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class TankHudOverlay {

    private static final int FRAME_COLOR  = 0xEE0D0D0D;
    private static final int SCOPE_COLOR  = 0xCC22EE22;
    private static final int TEXT_COLOR   = 0xFFCCFFCC;
    private static final int HP_COLOR     = 0xCC44CC44;
    private static final int AMMO_COLOR   = 0xCCBBAA22;
    private static final int RELOAD_COLOR = 0xCCCCCC00;

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || !(mc.player.getVehicle() instanceof TankEntity tank)) return;

        GuiGraphics g = event.getGuiGraphics();
        int w = mc.getWindow().getGuiScaledWidth();
        int h = mc.getWindow().getGuiScaledHeight();

        // ── Рамка кокпита ────────────────────────────────────────────────
        int bx = w / 5;
        int by = h / 6;
        g.fill(0,        0,   bx,      h,  FRAME_COLOR);
        g.fill(w - bx,   0,   w,       h,  FRAME_COLOR);
        g.fill(bx,       0,   w - bx,  by, FRAME_COLOR);
        g.fill(bx,  h - by,   w - bx,  h,  FRAME_COLOR);

        // ── Прицельная сетка ─────────────────────────────────────────────
        int cx = w / 2, cy = h / 2;
        int arm = 18, gap = 5, thick = 1;

        // Тёмный фон в центре перекрывает ванильный белый крест MC
        g.fill(cx - 8, cy - 8, cx + 8, cy + 8, 0xFF111111);

        // горизонтальные плечи
        g.fill(cx - arm - gap, cy - thick, cx - gap,       cy + thick, SCOPE_COLOR);
        g.fill(cx + gap,       cy - thick, cx + arm + gap, cy + thick, SCOPE_COLOR);
        // вертикальные плечи
        g.fill(cx - thick, cy - arm - gap, cx + thick, cy - gap,       SCOPE_COLOR);
        g.fill(cx - thick, cy + gap,       cx + thick, cy + arm + gap, SCOPE_COLOR);
        // центральная точка
        g.fill(cx - 2, cy - 2, cx + 2, cy + 2, SCOPE_COLOR);

        // ── Дальномерные штрихи ───────────────────────────────────────────
        for (int i = -3; i <= 3; i++) {
            if (i == 0) continue;
            int lx = cx + i * 24;
            int lh = (Math.abs(i) == 1) ? 5 : 3;
            g.fill(lx - thick, cy - lh, lx + thick, cy + lh, SCOPE_COLOR);
        }
        g.fill(cx - 80, cy, cx - gap - arm - 4, cy + thick, 0x66228822);
        g.fill(cx + gap + arm + 4, cy, cx + 80, cy + thick, 0x66228822);

        // ── Полоса HP (нижний-левый) ──────────────────────────────────────
        float hp    = tank.getTankHealth();
        float maxHp = Config.TANK_HEALTH.get().floatValue();
        int barW = 120, barH = 8;
        int hpX = bx + 8, hpY = h - by - barH - 22;
        drawBar(g, hpX, hpY, barW, barH, hp / maxHp, HP_COLOR);
        g.drawString(mc.font,
                "HP  " + (int) hp + " / " + (int) maxHp,
                hpX, hpY - 11, TEXT_COLOR, false);

        // ── Снаряды / перезарядка (нижний-правый) ────────────────────────
        int shells    = tank.getLoadedShells();
        int maxShells = tank.getMaxShells();
        int reload    = tank.getReloadTimer();
        int maxReload = Config.RELOAD_TICKS.get();
        int amX = w - bx - barW - 8, amY = hpY;
        if (reload > 0) {
            float progress = 1f - (float) reload / maxReload;
            drawBar(g, amX, amY, barW, barH, progress, RELOAD_COLOR);
            int secsLeft = (int) Math.ceil(reload / 20.0);
            g.drawString(mc.font,
                    "Перезарядка  " + secsLeft + "с",
                    amX, amY - 11, 0xFFFFEE44, false);
        } else {
            drawBar(g, amX, amY, barW, barH, (float) shells / maxShells, AMMO_COLOR);
            g.drawString(mc.font,
                    "Снаряды  " + shells + " / " + maxShells,
                    amX, amY - 11, TEXT_COLOR, false);
        }

        // ── Скорость (нижний-центр) ───────────────────────────────────────
        float speedKmh = tank.getSpeed() * 20 * 3.6f;
        String speedStr = String.format("%+.0f км/ч", speedKmh);
        int strW = mc.font.width(speedStr);
        g.drawString(mc.font, speedStr, (w - strW) / 2, h - by - 22, TEXT_COLOR, false);
    }

    private static void drawBar(GuiGraphics g, int x, int y, int w, int h,
                                 float fill, int color) {
        g.fill(x - 1,          y - 1, x + w + 1, y + h + 1, 0xAA000000);
        g.fill(x, y, x + (int)(w * Math.min(fill, 1f)), y + h, color);
    }
}
