package com.warfield.tankmod.entity;

import com.warfield.tankmod.Config;
import com.warfield.tankmod.ModEntities;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Снаряд 75mm KwK 42 — быстрый прямолинейный снаряд без гравитации.
 * Наносит урон и создаёт взрыв при попадании в блок или сущность.
 * Рендерится как вращающийся предмет tank_shell (клиент видит его мгновенно).
 */
public class TankShellEntity extends Projectile {

    /** Скорость снаряда (блоков/тик). 75mm KwK 42 ~925 м/с → при 1 тике = 0.05 сек → ~46 б/тик. */
    private static final float SPEED = 6.0f;

    public TankShellEntity(EntityType<TankShellEntity> type, Level level) {
        super(type, level);
    }

    /** Фабричный конструктор — вызывается на сервере при выстреле из TankEntity.shoot(). */
    public TankShellEntity(TankEntity shooter, Level level) {
        this(ModEntities.TANK_SHELL_ENTITY.get(), level);
        setOwner(shooter);

        // Направление: мировой угол башни (градусы, MC-конвенция)
        float yawRad = (float) Math.toRadians(shooter.getTurretYaw());
        double dx = -Math.sin(yawRad);
        double dz =  Math.cos(yawRad);

        // Позиция: дульный срез (~5 блоков вперёд от центра танка, высота ~1.7 блока)
        setPos(
            shooter.getX() + dx * 5.0,
            shooter.getY() + 1.7,
            shooter.getZ() + dz * 5.0
        );
        setYRot(shooter.getTurretYaw());
        setDeltaMovement(dx * SPEED, 0, dz * SPEED);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        // Снаряду не нужны синхронизированные данные — клиент видит только позицию
    }

    @Override
    public void tick() {
        super.tick();

        if (tickCount > 200) { discard(); return; }

        Vec3 pos  = position();
        Vec3 vel  = getDeltaMovement();
        Vec3 next = pos.add(vel);

        if (!level().isClientSide()) {
            // Коллизия с блоками
            HitResult blockHit = level().clip(new ClipContext(
                    pos, next, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));

            // Коллизия с сущностями (исключаем стрелка и его транспортное средство)
            Entity owner = getOwner();
            EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(
                    level(), this, pos, next,
                    getBoundingBox().expandTowards(vel).inflate(1.0),
                    e -> !e.isSpectator() && e.isPickable() && e != owner
            );

            HitResult finalHit = null;
            if (entityHit != null) {
                // Берём ближайшее попадание
                if (blockHit.getType() == HitResult.Type.MISS
                        || entityHit.getLocation().distanceToSqr(pos) <= blockHit.getLocation().distanceToSqr(pos)) {
                    finalHit = entityHit;
                } else {
                    finalHit = blockHit;
                }
            } else if (blockHit.getType() != HitResult.Type.MISS) {
                finalHit = blockHit;
            }

            if (finalHit != null) {
                onHit(finalHit);
                discard();
                return;
            }
        }

        setPos(next.x, next.y, next.z);
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        if (level().isClientSide()) return;

        // Прямой урон по цели
        DamageSource source = level().damageSources().source(
                ModDamageTypes.TANK_SHELL, this, getOwner());
        result.getEntity().hurt(source, (float)(double) Config.SHELL_DAMAGE.get());

        explodeAtSelf();
    }

    @Override
    protected void onHitBlock(net.minecraft.world.phys.BlockHitResult result) {
        if (level().isClientSide()) return;
        explodeAtSelf();
    }

    private void explodeAtSelf() {
        float radius = (float)(double) Config.EXPLOSION_RADIUS.get();
        Level.ExplosionInteraction interaction = Config.DESTROY_BLOCKS.get()
                ? Level.ExplosionInteraction.TNT
                : Level.ExplosionInteraction.NONE;
        level().explode(this, getX(), getY(), getZ(), radius, false, interaction);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket(ServerEntity serverEntity) {
        return new ClientboundAddEntityPacket(this, serverEntity);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {}

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {}
}
