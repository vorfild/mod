package com.warfield.tankmod;

import com.warfield.tankmod.entity.TankEntity;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {

    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(TankMod.MODID);

    /**
     * Предмет-спавнер танка. ПКМ на земле — появляется Пантера перед игроком.
     * В Фазе 6 он будет заменён полноценным крафтовым рецептом с промежуточными деталями.
     */
    public static final DeferredItem<Item> TANK_SPAWNER = ITEMS.registerItem(
            "tank_spawner",
            props -> new Item(props) {
                @Override
                public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
                    if (!level.isClientSide()) {
                        TankEntity tank = new TankEntity(ModEntities.TANK.get(), level);
                        // Спавним в 3 блоках перед игроком по его направлению
                        double yawRad = Math.toRadians(player.getYRot());
                        tank.setPos(
                                player.getX() - Math.sin(yawRad) * 3,
                                player.getY(),
                                player.getZ() + Math.cos(yawRad) * 3
                        );
                        tank.setYRot(player.getYRot());
                        level.addFreshEntity(tank);
                        if (!player.isCreative()) player.getItemInHand(hand).shrink(1);
                    }
                    return InteractionResultHolder.sidedSuccess(
                            player.getItemInHand(hand), level.isClientSide());
                }
            }
    );

    /** Снаряд-расходник: загружается в танк через ПКМ. */
    public static final DeferredItem<Item> TANK_SHELL = ITEMS.registerItem(
            "tank_shell",
            props -> new Item(props.stacksTo(16))
    );

    /** Промежуточный компонент: стальной ствол орудия (3 iron_block в ряд). */
    public static final DeferredItem<Item> TANK_BARREL = ITEMS.registerItem(
            "tank_barrel",
            props -> new Item(props.stacksTo(1))
    );

    /** Промежуточный компонент: бронекорпус (8 iron_block в форме «О»). */
    public static final DeferredItem<Item> TANK_HULL = ITEMS.registerItem(
            "tank_hull",
            props -> new Item(props.stacksTo(1))
    );
}
