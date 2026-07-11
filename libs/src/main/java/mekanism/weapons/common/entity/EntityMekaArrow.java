package mekanism.weapons.common.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class EntityMekaArrow extends EntityArrow {

    private boolean noGravity = false;

    public EntityMekaArrow(World worldIn) { super(worldIn); }
    public EntityMekaArrow(World worldIn, EntityLivingBase shooter) { super(worldIn, shooter); }
    
    @Override
    public void onUpdate() {
        super.onUpdate();
        if (this.noGravity && !this.inGround) {
            this.motionY += 0.05000000074505806D;
        }
    }

    @Override
    protected void onHit(RayTraceResult raytraceResult) {
        Entity entity = raytraceResult.entityHit;

        if (entity != null) {
            float flatDamage = (float) this.getDamage();
            DamageSource damagesource = DamageSource.causeArrowDamage(this, this.shootingEntity == null ? this : this.shootingEntity);

            if (entity.attackEntityFrom(damagesource, flatDamage)) {
                this.playSound(SoundEvents.ENTITY_ARROW_HIT, 1.0F, 1.2F / (this.rand.nextFloat() * 0.2F + 0.9F));
                this.setDead();
            } else {
                this.motionX *= -0.1D;
                this.motionY *= -0.1D;
                this.motionZ *= -0.1D;
                this.rotationYaw += 180.0F;
                this.prevRotationYaw += 180.0F;
            }
        } else {
            super.onHit(raytraceResult);
        }
    }
    
    @Override
    public void setIsCritical(boolean critical) {
    }

    public void setNoGravity(boolean noGravity) { this.noGravity = noGravity; }
    @Override protected ItemStack getArrowStack() { return ItemStack.EMPTY; }
    @Override public void writeEntityToNBT(NBTTagCompound compound) { super.writeEntityToNBT(compound); compound.setBoolean("noGravity", this.noGravity); }
    @Override public void readEntityFromNBT(NBTTagCompound compound) { super.readEntityFromNBT(compound); this.noGravity = compound.getBoolean("noGravity"); }
}
