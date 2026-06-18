package com.warfield.tankmod.entity;

import com.warfield.tankmod.Config;
import com.warfield.tankmod.ModItems;
import com.warfield.tankmod.TankMod;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animatable.manager.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * Сущность управляемого танка «Пантера».
 *
 * Базовый класс — Entity (не LivingEntity), чтобы исключить ванильный AI,
 * систему атаки, зелья и т.д. Здоровье отслеживается через SynchedEntityData.
 * Аналог AbstractBoat / AbstractMinecart.
 */
public class TankEntity extends Entity implements GeoEntity {

    // ─── Синхронизируемые данные (автоматически реплицируются клиентам) ───

    /** Угол поворота башни, градусы (фаза 2) */
    private static final EntityDataAccessor<Float> DATA_TURRET_YAW =
            SynchedEntityData.defineId(TankEntity.class, EntityDataSerializers.FLOAT);

    /** Количество заряженных снарядов (фаза 3) */
    private static final EntityDataAccessor<Integer> DATA_LOADED_SHELLS =
            SynchedEntityData.defineId(TankEntity.class, EntityDataSerializers.INT);

    /** Открыт ли люк (фаза 5) */
    private static final EntityDataAccessor<Boolean> DATA_HATCH_OPEN =
            SynchedEntityData.defineId(TankEntity.class, EntityDataSerializers.BOOLEAN);

    /** Текущее здоровье танка */
    private static final EntityDataAccessor<Float> DATA_HEALTH =
            SynchedEntityData.defineId(TankEntity.class, EntityDataSerializers.FLOAT);

    /** Тики до конца перезарядки */
    private static final EntityDataAccessor<Integer> DATA_RELOAD_TIMER =
            SynchedEntityData.defineId(TankEntity.class, EntityDataSerializers.INT);

    /** Максимальный боезапас (реплицируется на клиент для HUD). */
    private static final EntityDataAccessor<Integer> DATA_MAX_SHELLS =
            SynchedEntityData.defineId(TankEntity.class, EntityDataSerializers.INT);

    // ─── Локальные поля движения (только сервер) ───

    /** Текущая скорость вдоль оси танка (блоков/тик) */
    private float speed = 0f;

    /** Последний ввод от клиента-водителя */
    private boolean inputFwd, inputBack, inputLeft, inputRight;

    // GeckoLib: кэш анимации (по одному экземпляру на объект)
    private final AnimatableInstanceCache animCache = GeckoLibUtil.createInstanceCache(this);

    public TankEntity(EntityType<?> type, Level level) {
        super(type, level);
        // Позволяет другим сущностям "упираться" в хитбокс танка
        this.noCulling = true;
    }

    // ─────────────────────────────────────────────────────────────────────
    // SynchedEntityData — новый Builder-паттерн 1.21
    // ─────────────────────────────────────────────────────────────────────

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_TURRET_YAW,    0.0f);
        builder.define(DATA_LOADED_SHELLS, 0);
        builder.define(DATA_HATCH_OPEN,    false);
        builder.define(DATA_HEALTH,        (float) Config.TANK_HEALTH.get());
        builder.define(DATA_RELOAD_TIMER,  0);
        builder.define(DATA_MAX_SHELLS,    Config.SHELL_CAPACITY.get());
    }

    // ─────────────────────────────────────────────────────────────────────
    // Посадка / выход
    // ─────────────────────────────────────────────────────────────────────

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (level().isClientSide()) return InteractionResult.sidedSuccess(true);

        ItemStack heldItem = player.getItemInHand(hand);

        // ПКМ с tank_shell → погрузить снаряд
        if (heldItem.is(ModItems.TANK_SHELL.get())) {
            int loaded   = entityData.get(DATA_LOADED_SHELLS);
            int capacity = entityData.get(DATA_MAX_SHELLS);
            if (loaded < capacity) {
                entityData.set(DATA_LOADED_SHELLS, loaded + 1);
                if (!player.isCreative()) heldItem.shrink(1);
                player.displayClientMessage(
                        Component.translatable("tankmod.ammo", loaded + 1, capacity), true);
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.FAIL;
        }

        // ПКМ без снаряда → сесть в танк (если не занято)
        if (!isVehicle()) {
            player.startRiding(this);
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    /** Размещаем пассажира внутри башни, чуть выше середины корпуса. */
    @Override
    protected void positionRider(Entity passenger, MoveFunction moveFunction) {
        if (!hasPassenger(passenger)) return;
        // Смещение: по центру X/Z, Y = верхушка корпуса (~1.1 блока от земли)
        double rYaw = Math.toRadians(getYRot());
        double px = getX() - Math.sin(rYaw) * 0.1;
        double py = getY() + 1.1;
        double pz = getZ() + Math.cos(rYaw) * 0.1;
        moveFunction.accept(passenger, px, py, pz);
        // Пассажир смотрит туда же, куда башня (в фазе 2 — куда башня, пока — корпус)
        passenger.setYRot(passenger.getYRot());
    }

    /** Пассажир выходит по Shift — стандартный механизм Minecraft. */
    @Override
    public boolean isPickable() { return !isVehicle(); }

    // ─────────────────────────────────────────────────────────────────────
    // Игровой цикл
    // ─────────────────────────────────────────────────────────────────────

    @Override
    public void tick() {
        super.tick();

        if (!level().isClientSide()) {
            serverTick();
        }
    }

    private void serverTick() {
        // Перезарядка (фаза 3 — таймер уже готов)
        int reloadTimer = entityData.get(DATA_RELOAD_TIMER);
        if (reloadTimer > 0) {
            entityData.set(DATA_RELOAD_TIMER, reloadTimer - 1);
        }

        // Движение: рассчитываем только если есть пассажир
        if (isVehicle()) {
            applyMovementPhysics();
        } else {
            // Трение без водителя
            speed *= 0.85f;
        }

        // Применяем движение
        double yawRad = Math.toRadians(getYRot());
        double dx = -Math.sin(yawRad) * speed;
        double dz =  Math.cos(yawRad) * speed;

        // Гравитация
        Vec3 motion = getDeltaMovement();
        if (!onGround()) {
            setDeltaMovement(dx, motion.y - 0.08, dz);
        } else {
            setDeltaMovement(dx, Math.max(motion.y, 0), dz);
        }

        move(MoverType.SELF, getDeltaMovement());

        // Горизонтальное затухание после move()
        Vec3 cur = getDeltaMovement();
        setDeltaMovement(cur.x * 0.98, cur.y, cur.z * 0.98);
    }

    /**
     * Физика движения танка: инерция разгона/торможения, ограничение поворота.
     * Вызывается только если есть пассажир (ввод уже записан в inputFwd/Back/Left/Right).
     */
    private void applyMovementPhysics() {
        double maxSpeed = Config.MAX_SPEED.get();
        float accel   = 0.012f;   // разгон (блоков/тик²)
        float turnRate = 1.8f;    // максимальный поворот за тик (градусов)

        if (inputFwd) {
            speed = (float) Math.min(speed + accel, maxSpeed);
        } else if (inputBack) {
            speed = (float) Math.max(speed - accel, -maxSpeed * 0.5);
        } else {
            // Инертное замедление
            speed *= 0.92f;
            if (Math.abs(speed) < 0.001f) speed = 0f;
        }

        // Поворот активен только при движении (иначе танк разворачивался бы на месте)
        if (Math.abs(speed) > 0.005f) {
            float dir = speed > 0 ? 1f : -1f;
            if (inputLeft)  setYRot(getYRot() - turnRate * dir);
            if (inputRight) setYRot(getYRot() + turnRate * dir);
        }
    }

    /**
     * Принимаем ввод от клиента (вызывается обработчиком TankInputPacket на сервере).
     * Оптимизация: пакет шлётся только при ИЗМЕНЕНИИ нажатых клавиш, не каждый тик.
     */
    public void setInput(boolean fwd, boolean back, boolean left, boolean right) {
        this.inputFwd   = fwd;
        this.inputBack  = back;
        this.inputLeft  = left;
        this.inputRight = right;
    }

    // ─────────────────────────────────────────────────────────────────────
    // Урон — избирательная неуязвимость (фаза 4 добавит фильтрацию)
    // ─────────────────────────────────────────────────────────────────────

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (level().isClientSide() || isRemoved()) return false;

        // Фаза 4 добавит фильтрацию: только IS_EXPLOSION и TANK_SHELL пройдут.
        // Пока принимаем любой урон для отладки.
        float health = entityData.get(DATA_HEALTH) - amount;
        if (health <= 0) {
            // Танк уничтожен
            TankMod.LOGGER.info("[TankMod] Танк уничтожен ({}).", getId());
            remove(RemovalReason.KILLED);
        } else {
            entityData.set(DATA_HEALTH, health);
        }
        return true;
    }

    // ─────────────────────────────────────────────────────────────────────
    // Геттеры/сеттеры для SynchedEntityData
    // ─────────────────────────────────────────────────────────────────────

    public float getTurretYaw()    { return entityData.get(DATA_TURRET_YAW); }
    public void  setTurretYaw(float yaw) { entityData.set(DATA_TURRET_YAW, yaw); }

    public int  getLoadedShells()  { return entityData.get(DATA_LOADED_SHELLS); }
    public void setLoadedShells(int n) { entityData.set(DATA_LOADED_SHELLS, n); }

    public int  getMaxShells()     { return entityData.get(DATA_MAX_SHELLS); }

    public boolean isHatchOpen()   { return entityData.get(DATA_HATCH_OPEN); }
    public void    setHatchOpen(boolean open) { entityData.set(DATA_HATCH_OPEN, open); }

    public float getTankHealth()   { return entityData.get(DATA_HEALTH); }

    public int  getReloadTimer()   { return entityData.get(DATA_RELOAD_TIMER); }
    public void setReloadTimer(int t)  { entityData.set(DATA_RELOAD_TIMER, t); }

    public float getSpeed()        { return speed; }

    /**
     * Выстрел снарядом (вызывается из сервера по TankShootPacket).
     * Проверяет условия: боезапас > 0, перезарядка завершена.
     */
    public void shoot() {
        if (level().isClientSide()) return;
        int loaded = entityData.get(DATA_LOADED_SHELLS);
        if (loaded <= 0 || entityData.get(DATA_RELOAD_TIMER) > 0) return;

        TankShellEntity shell = new TankShellEntity(this, level());
        level().addFreshEntity(shell);

        entityData.set(DATA_LOADED_SHELLS, loaded - 1);
        entityData.set(DATA_RELOAD_TIMER, Config.RELOAD_TICKS.get());
    }

    // ─────────────────────────────────────────────────────────────────────
    // NBT — сохранение/загрузка состояния при перезагрузке мира
    // ─────────────────────────────────────────────────────────────────────

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        entityData.set(DATA_HEALTH,        tag.contains("TankHealth")   ? tag.getFloat("TankHealth")   : (float) Config.TANK_HEALTH.get());
        entityData.set(DATA_LOADED_SHELLS, tag.contains("LoadedShells") ? tag.getInt("LoadedShells")   : 0);
        entityData.set(DATA_HATCH_OPEN,    tag.contains("HatchOpen")    ? tag.getBoolean("HatchOpen")  : false);
        entityData.set(DATA_TURRET_YAW,    tag.contains("TurretYaw")    ? tag.getFloat("TurretYaw")    : 0f);
        speed = tag.contains("Speed") ? tag.getFloat("Speed") : 0f;
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putFloat("TankHealth",   entityData.get(DATA_HEALTH));
        tag.putInt("LoadedShells",   entityData.get(DATA_LOADED_SHELLS));
        tag.putBoolean("HatchOpen",  entityData.get(DATA_HATCH_OPEN));
        tag.putFloat("TurretYaw",    entityData.get(DATA_TURRET_YAW));
        tag.putFloat("Speed",        speed);
    }

    // ─────────────────────────────────────────────────────────────────────
    // Обязательные методы Entity
    // ─────────────────────────────────────────────────────────────────────

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket(ServerEntity serverEntity) {
        // Стандартный пакет добавления сущности; NeoForge регистрирует его за нас
        return new ClientboundAddEntityPacket(this, serverEntity);
    }

    /** Танк не блокирует взаимодействие с блоками под собой. */
    @Override
    public boolean canBeCollidedWith() { return true; }

    // ─────────────────────────────────────────────────────────────────────
    // GeckoLib — анимации
    // ─────────────────────────────────────────────────────────────────────

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        // Фаза 1: базовый контроллер без анимаций (холостой).
        // Фаза 2: поворот башни добавляется в TankModel.setCustomAnimations().
        // TODO Фаза N: анимация гусениц — скорость → параметр animation_speed.
        controllers.add(new AnimationController<>(this, "idle", state -> PlayState.STOP));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return animCache;
    }
}
