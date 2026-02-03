package mekanism.weapons.common.item;

import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.EnumAction;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.stats.StatList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.item.ItemBow;
import mekanism.api.radial.RadialData;
import mekanism.api.radial.mode.IRadialMode;
import mekanism.api.radial.mode.NestedRadialMode;
import mekanism.api.EnumColor;
import mekanism.api.energy.IEnergizedItem;
import mekanism.api.gear.IModule;
import mekanism.client.MekKeyHandler;
import mekanism.client.MekanismKeyHandler;
import mekanism.common.item.interfaces.IModeItem;
import mekanism.common.lib.radial.IGenericRadialModeItem;
import mekanism.common.lib.radial.data.NestingRadialData;
import mekanism.common.content.gear.Module;
import mekanism.common.MekanismModules;
import mekanism.common.content.gear.IModuleContainerItem;
import mekanism.common.content.gear.shared.ModuleEnergyUnit;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.weapons.MekanismWeapons;
import mekanism.weapons.MekanismWeaponsItems;
import mekanism.weapons.MekanismWeaponsModules;
import mekanism.weapons.config.MekanismWeaponsConfig;
import mekanism.weapons.common.entity.EntityMekaArrow;
import mekanism.weapons.common.module.ModuleDrawSpeedUnit;
import mekanism.weapons.common.module.ModuleWeaponAttackAmplificationUnit;
import mekanism.weapons.common.module.ModuleArrowVelocityUnit;
import mekanism.weapons.common.module.ModuleCompoundArrowUnit;
import org.jetbrains.annotations.NotNull;
import com.google.common.collect.Multimap;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class ItemMekaBow extends ItemBow implements IEnergizedItem, IModuleContainerItem, IModeItem, IGenericRadialModeItem {

public ItemMekaBow() {
    super();
    setCreativeTab(MekanismWeaponsItems.tabMekanismWeapons);
    setMaxStackSize(1);

    this.addPropertyOverride(new ResourceLocation("pulling"), (stack, world, entity) -> 
        entity != null && entity.isHandActive() && entity.getActiveItemStack() == stack ? 1.0F : 0.0F
    );

    this.addPropertyOverride(new ResourceLocation("pull"), (stack, world, entity) -> {
        if (entity == null || entity.getActiveItemStack().getItem() != this) {
            return 0.0F;
        }
        
        // Quanto tempo hai tenuto premuto?
        float chargeTime = (float)(stack.getMaxItemUseDuration() - entity.getItemInUseCount());
        
        // Quanto veloce è l'arco?
        float multiplier = getDrawSpeedMultiplier(stack);
        
        // Calcoliamo il progresso.
        float progress = (chargeTime * multiplier) / 20.0F;
        
        if (progress > 1.0F) return 1.0F;
        return progress;
    });
}

// --- GESTIONE RARITÀ DINAMICA ---
    @Override
    public EnumRarity getRarity(ItemStack stack) {
        // Partiamo dalla rarità di base che vogliamo per l'arma
        EnumRarity rarity = EnumRarity.EPIC;
        
        // Controlliamo tutti i moduli installati
        for (IModule<?> module : getModules(stack)) {
            // Se un modulo ha una rarità più alta di quella attuale...
            if (module.getData().getRarity().ordinal() > rarity.ordinal()) {
                // ...aggiorniamo la rarità a quella del modulo.
                rarity = module.getData().getRarity();
            }
        }
        return rarity;
    }

@Override
    public String getItemStackDisplayName(ItemStack stack) {
        // Forza il colore ROSA/VIOLA (Mekanism Epic) direttamente nel nome.
        // Così sarà colorato ovunque, anche nella chat o nei tooltip degli altri oggetti.
        return EnumColor.PINK + super.getItemStackDisplayName(stack);
    }

    // --- LOGICA TOOLTIP (VISUALIZZAZIONE DANNO) ---
    @Override
      public Multimap<String, AttributeModifier> getAttributeModifiers(EntityEquipmentSlot slot, ItemStack stack) {
        Multimap<String, AttributeModifier> multimap = super.getAttributeModifiers(slot, stack);
        
        if (slot == EntityEquipmentSlot.MAINHAND) {
            multimap.clear(); 

            // Default: Danno "Scarico" (20 + 1 = 21)
            double damage = 20.0D; 
            
            IModule<ModuleWeaponAttackAmplificationUnit> damageModule = getModule(stack, MekanismWeaponsModules.WEAPON_ATTACK_AMPLIFICATION_UNIT);
            
            // C'è energia sufficiente?
            boolean hasEnergy = getEnergy(stack) >= MekanismWeaponsConfig.mekaBowEnergyUsage;

            if (damageModule != null && damageModule.isEnabled()) {
                double mult = damageModule.getCustomInstance().getDamageMode().getMultiplier();
                
                if (mult == 0) {
                    // CASO 1: Modulo OFF -> Danno 1 (0 + 1 base)
                    damage = 0.0D;
                } else if (hasEnergy) {
                    // CASO 2: Modulo ON + Energia -> Danno Pieno (50 * mult)
                    damage = MekanismWeaponsConfig.mekaBowBaseDamage * mult;
                } else {
                    // CASO 3: Modulo ON + No Energia -> Danno Scarico (21)
                    damage = 20.0D;
                }
            } else {
                // Senza modulo
                if (hasEnergy) {
                    // CASO 4: No Modulo + Energia -> Danno Base (50)
                    damage = MekanismWeaponsConfig.mekaBowBaseDamage;
                } else {
                    // CASO 5: No Modulo + No Energia -> Danno Scarico (21)
                    damage = 20.0D;
                }
            }
            
            multimap.put(SharedMonsterAttributes.ATTACK_DAMAGE.getName(), new AttributeModifier(ItemSword.ATTACK_DAMAGE_MODIFIER, "Weapon modifier", damage, 0));
        }
        
        return multimap;
    }

    @Override
    public EnumAction getItemUseAction(ItemStack stack) { return EnumAction.BOW; }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) { return 72000; }

@Nonnull
@Override
public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, @Nonnull EnumHand hand) {
    ItemStack stack = player.getHeldItem(hand);
    
    // AZZERA IL TIMER ALL'INIZIO DI OGNI UTILIZZO
    ItemDataUtils.setInt(stack, "charge_timer", 0);

    boolean hasAmmo = !findAmmo(player).isEmpty() || isModuleEnabled(stack, MekanismWeaponsModules.ENERGY_ARROWS_UNIT);
    if (!player.capabilities.isCreativeMode && !hasAmmo) {
        return new ActionResult<>(EnumActionResult.FAIL, stack);
    }
    player.setActiveHand(hand);
    return new ActionResult<>(EnumActionResult.SUCCESS, stack);
}

    @Override
     public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable NBTTagCompound nbt) {
     // Poiché non possiamo usare un helper, dobbiamo creare manualmente il provider.
      return new ICapabilityProvider() {
        // Creiamo un'istanza dell'interfaccia di Forge Energy
        private final net.minecraftforge.energy.IEnergyStorage energyStorage = new net.minecraftforge.energy.IEnergyStorage() {
            
            @Override 
            public int receiveEnergy(int maxReceive, boolean simulate) {
                // La logica di ricezione che avevamo già trovato, a prova di overflow
                if (!ItemMekaBow.this.canReceive(stack)) return 0;
                double energyNeeded = getMaxEnergy(stack) - getEnergy(stack);
                double toReceive = Math.min(maxReceive, Math.min(energyNeeded, Integer.MAX_VALUE));
                if (toReceive < 0) toReceive = 0;
                if (!simulate) {
                    setEnergy(stack, getEnergy(stack) + toReceive);
                }
                return (int) Math.round(toReceive);
            }

            @Override 
            public int extractEnergy(int maxExtract, boolean simulate) {
                // La logica di estrazione che avevamo già trovato, a prova di overflow
                if (!ItemMekaBow.this.canSend(stack)) return 0;
                double energyStored = getEnergy(stack);
                double toExtract = Math.min(maxExtract, Math.min(energyStored, Integer.MAX_VALUE));
                if (toExtract < 0) toExtract = 0;
                if (!simulate) {
                    setEnergy(stack, getEnergy(stack) - toExtract);
                }
                return (int) Math.round(toExtract);
            }
            
            @Override
            public int getEnergyStored() {
                // La versione a prova di overflow
                return (int) Math.min(Integer.MAX_VALUE, getEnergy(stack));
            }

            @Override
            public int getMaxEnergyStored() {
                // La versione a prova di overflow
                return (int) Math.min(Integer.MAX_VALUE, getMaxEnergy(stack));
            }

            @Override 
            public boolean canExtract() {
                // La versione corretta
                return ItemMekaBow.this.canSend(stack); 
            }

            @Override 
            public boolean canReceive() {
                return ItemMekaBow.this.canReceive(stack); 
            }
        };

        @Override
        public boolean hasCapability(@Nonnull net.minecraftforge.common.capabilities.Capability<?> capability, @Nullable net.minecraft.util.EnumFacing facing) {
            // Diciamo a Forge che forniamo la capacità energetica
            return capability == net.minecraftforge.energy.CapabilityEnergy.ENERGY;
        }

        @Nullable
        @Override
        public <T> T getCapability(@Nonnull net.minecraftforge.common.capabilities.Capability<T> capability, @Nullable net.minecraft.util.EnumFacing facing) {
            // Restituiamo la nostra implementazione di IEnergyStorage quando viene richiesta
            if (capability == net.minecraftforge.energy.CapabilityEnergy.ENERGY) {
                return net.minecraftforge.energy.CapabilityEnergy.ENERGY.cast(energyStorage);
            }
            return null;
        }
    };
}
    
    @Override public double getEnergy(ItemStack stack) { return ItemDataUtils.getDouble(stack, "energyStored"); }
    @Override public void setEnergy(ItemStack stack, double amount) { ItemDataUtils.setDouble(stack, "energyStored", Math.max(0, Math.min(amount, getMaxEnergy(stack)))); }
    @Override public double getMaxEnergy(ItemStack stack) {
        IModule<ModuleEnergyUnit> module = getModule(stack, MekanismModules.ENERGY_UNIT);
        return module != null ? module.getCustomInstance().getEnergyCapacity(module) : MekanismWeaponsConfig.mekaBowBaseEnergyCapacity;
    }
    @Override public double getMaxTransfer(ItemStack stack) {
        IModule<ModuleEnergyUnit> module = getModule(stack, MekanismModules.ENERGY_UNIT);
        return module != null ? module.getCustomInstance().getChargeRate(module) : MekanismWeaponsConfig.mekaBowBaseChargeRate;
    }
    @Override public boolean canReceive(ItemStack stack) { return true; }
    @Override public boolean canSend(ItemStack stack) { return true; }
    @Override public boolean showDurabilityBar(ItemStack stack) { return true; }
    @Override public double getDurabilityForDisplay(ItemStack stack) { return 1.0D - (getEnergy(stack) / getMaxEnergy(stack)); }
    @Override public int getRGBDurabilityForDisplay(ItemStack stack) { return MathHelper.hsvToRGB(Math.max(0.0F, (float) (getEnergy(stack) / getMaxEnergy(stack))) / 3.0F, 1.0F, 1.0F); }

        
@Override
public void onUsingTick(ItemStack stack, EntityLivingBase player, int count) {
    // Solo se Auto-Fire è attivo
    if (isModuleEnabled(stack, MekanismWeaponsModules.AUTO_FIRE_UNIT)) {
        int actualCharge = getMaxItemUseDuration(stack) - count;
        float multiplier = getDrawSpeedMultiplier(stack);
        
        // Calcoliamo se abbiamo raggiunto la carica "virtuale" di 20 (massimo vanilla)
        if ((actualCharge * multiplier) >= 20.0F) {
            player.stopActiveHand();
        }
    }
}

@Override
    public void onPlayerStoppedUsing(ItemStack stack, World worldIn, EntityLivingBase entityLiving, int timeLeft) {
        if (!(entityLiving instanceof EntityPlayer)) return;
        EntityPlayer player = (EntityPlayer) entityLiving;

        int actualCharge = this.getMaxItemUseDuration(stack) - timeLeft;
        float multiplier = getDrawSpeedMultiplier(stack);
        int effectiveCharge = (int) (actualCharge * multiplier);
        
        float velocity = ItemBow.getArrowVelocity(effectiveCharge);
        if ((double)velocity < 0.1D) return;

        // Recupero Moduli
        IModule<ModuleArrowVelocityUnit> velocityModule = getModule(stack, MekanismWeaponsModules.ARROW_VELOCITY_UNIT);
        IModule<ModuleCompoundArrowUnit> compoundModule = getModule(stack, MekanismWeaponsModules.COMPOUND_ARROW_UNIT);
        
        // --- 1. CONTROLLO GRAVITÀ ---
        boolean gravityDampener = isModuleEnabled(stack, MekanismWeaponsModules.GRAVITY_DAMPENER_UNIT);

        float velocityMult = 1.0F;
        if (velocityModule != null && velocityModule.isEnabled()) {
            velocityMult = velocityModule.getCustomInstance().getVelocityMultiplier(velocityModule);
        }

        int arrowCount = 1;
        if (compoundModule != null && compoundModule.isEnabled()) {
            arrowCount = compoundModule.getCustomInstance().getArrowCount();
        }

        boolean useEnergyArrows = isModuleEnabled(stack, MekanismWeaponsModules.ENERGY_ARROWS_UNIT);
        ItemStack ammoStack = findAmmo(player);

        if (!useEnergyArrows && !player.capabilities.isCreativeMode && ammoStack.isEmpty()) return;

        if (!worldIn.isRemote) {
            double totalEnergyNeeded = MekanismWeaponsConfig.mekaBowEnergyUsage;
            
            // Calcolo Consumi
            IModule<ModuleWeaponAttackAmplificationUnit> damageModule = getModule(stack, MekanismWeaponsModules.WEAPON_ATTACK_AMPLIFICATION_UNIT);
            if (damageModule != null && damageModule.isEnabled()) {
                totalEnergyNeeded += MekanismWeaponsConfig.attackAmplificationEnergyUsage * damageModule.getInstalledCount();
            }
            if (isModuleEnabled(stack, MekanismWeaponsModules.AUTO_FIRE_UNIT)) totalEnergyNeeded += MekanismWeaponsConfig.mekaBowAutofireEnergyUsage;
            IModule<ModuleDrawSpeedUnit> drawSpeedModule = getModule(stack, MekanismWeaponsModules.DRAW_SPEED_UNIT);
            if (drawSpeedModule != null && drawSpeedModule.isEnabled()) totalEnergyNeeded += MekanismWeaponsConfig.mekaBowDrawSpeedUsage * drawSpeedModule.getInstalledCount();
            
            // --- 2. CONSUMO ENERGIA GRAVITÀ ---
            if (gravityDampener) totalEnergyNeeded += MekanismWeaponsConfig.mekaBowGravityDampenerUsage;
            
            if (useEnergyArrows) {
                totalEnergyNeeded += MekanismWeaponsConfig.mekaBowEnergyArrowUsage;
                if (arrowCount > 1) totalEnergyNeeded += (MekanismWeaponsConfig.mekaBowEnergyArrowUsage * (arrowCount - 1));
            }
            if (velocityModule != null) totalEnergyNeeded += velocityModule.getCustomInstance().getEnergyCost(velocityModule);
            if (compoundModule != null) totalEnergyNeeded += compoundModule.getCustomInstance().getEnergyCost();

            if (getEnergy(stack) < totalEnergyNeeded && !player.capabilities.isCreativeMode) return;
            if (!player.capabilities.isCreativeMode) setEnergy(stack, getEnergy(stack) - totalEnergyNeeded);

            for (int i = 0; i < arrowCount; i++) {
                if (!useEnergyArrows && !player.capabilities.isCreativeMode) {
                    ammoStack = findAmmo(player);
                    if (ammoStack.isEmpty()) break;
                }

                EntityMekaArrow arrow = new EntityMekaArrow(worldIn, player);
                
                // --- 3. APPLICAZIONE GRAVITÀ ---
                if (gravityDampener) {
                    // Questo comando dice alla freccia di ignorare la fisica della caduta
                    arrow.setNoGravity(true);
                }

                // Danno (con fix OFF = 1)
                float finalDamage = (float)MekanismWeaponsConfig.mekaBowBaseDamage;
                if (damageModule != null && damageModule.isEnabled()) {
                    float modMult = damageModule.getCustomInstance().getDamageBonus(damageModule);
                    if (modMult == 0) finalDamage = 1.0F;
                    else finalDamage *= modMult;
                }
                arrow.setDamage(finalDamage);

                float spread = (arrowCount > 1) ? 3.0F : 1.0F; 
                arrow.shoot(player, player.rotationPitch, player.rotationYaw, 0.0F, velocity * 3.0F * velocityMult, spread);

                if (velocity >= 1.0F) arrow.setIsCritical(true);

                if (useEnergyArrows || player.capabilities.isCreativeMode) {
                    arrow.pickupStatus = EntityArrow.PickupStatus.CREATIVE_ONLY;
                } else {
                    ammoStack.shrink(1);
                    if (ammoStack.isEmpty()) player.inventory.deleteStack(ammoStack);
                }
                worldIn.spawnEntity(arrow);
            }
        }

        float pitch = 1.0F / (itemRand.nextFloat() * 0.4F + 1.2F) + velocity * 0.5F;
        if (velocityMult > 1.0F) pitch += (velocityMult * 0.1F);
        
        worldIn.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_ARROW_SHOOT, SoundCategory.PLAYERS, 1.0F, pitch);
        player.addStat(StatList.getObjectUseStats(this));
    }

    // Metodo per trovare le munizioni nell'inventario del giocatore
    @Override
    protected ItemStack findAmmo(EntityPlayer player) {
        if (isArrow(player.getHeldItem(EnumHand.OFF_HAND))) {
            return player.getHeldItem(EnumHand.OFF_HAND);
        } else if (isArrow(player.getHeldItem(EnumHand.MAIN_HAND))) {
            // Controlla che non sia l'arco stesso
            if (player.getHeldItem(EnumHand.MAIN_HAND).getItem() != this) {
                 return player.getHeldItem(EnumHand.MAIN_HAND);
            }
        }
        for (int i = 0; i < player.inventory.getSizeInventory(); ++i) {
            ItemStack itemstack = player.inventory.getStackInSlot(i);
            if (isArrow(itemstack)) {
                return itemstack;
            }
        }
        return ItemStack.EMPTY;
    }

    // Metodo di supporto per controllare se un ItemStack è una freccia
    protected boolean isArrow(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof net.minecraft.item.ItemArrow;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag) {
        if (MekKeyHandler.getIsKeyPressed(MekanismKeyHandler.sneakKey)) {
            addModuleDetails(stack, tooltip);
        } else {
            tooltip.add(EnumColor.AQUA + LangUtils.localize("tooltip.storedEnergy") + ": " + EnumColor.GREY + MekanismUtils.getEnergyDisplay(getEnergy(stack), getMaxEnergy(stack)));
            tooltip.add(LangUtils.localize("tooltip.hold") + " " + EnumColor.INDIGO + GameSettings.getKeyDisplayString(MekanismKeyHandler.sneakKey.getKeyCode()) + EnumColor.GREY + " " + LangUtils.localize("tooltip.forDetails") + ".");
        }
    }
      @Override
    public void changeMode(@NotNull EntityPlayer player, @NotNull ItemStack stack, int shift, @NotNull DisplayChange displayChange) {
        for (Module<?> module : getModules(stack)) {
            if (module.handlesModeChange()) {
                module.changeMode(player, stack, shift, displayChange);
                return;
            }
        }
    }

    @NotNull
    @Override
    public ITextComponent getScrollTextComponent(@NotNull ItemStack stack) {
        return getModules(stack).stream()
                .filter(Module::handlesModeChange)
                .findFirst()
                .map(module -> module.getModeScrollComponent(stack))
                .orElse(null);
    }
    
    @Nullable
    @Override
    public RadialData<?> getRadialData(ItemStack stack) {
        List<NestedRadialMode> nestedModes = new ArrayList<>();
        for (Module<?> module : getModules(stack)) {
            if (module.handlesRadialModeChange()) {
                module.addRadialModes(stack, nestedModes::add);
            }
        }
        if (nestedModes.isEmpty()) return null;
        if (nestedModes.size() == 1) return nestedModes.get(0).nestedData();
        return new NestingRadialData(new ResourceLocation(MekanismWeapons.MODID, "meka_bow_root"), nestedModes);
    }

    @Nullable
    @Override
    public <M extends IRadialMode> M getMode(ItemStack stack, RadialData<M> radialData) {
        for (Module<?> module : getModules(stack)) {
            if (module.handlesRadialModeChange()) {
                M mode = module.getMode(stack, radialData);
                if (mode != null) return mode;
            }
        }
        return null;
    }
    
    @Override
    public <M extends IRadialMode> void setMode(ItemStack stack, EntityPlayer player, RadialData<M> radialData, M mode) {
        for (Module<?> module : getModules(stack)) {
            if (module.handlesRadialModeChange() && module.setMode(player, stack, radialData, mode)) {
                return;
            }
        }

    }

    public float getDrawSpeedMultiplier(ItemStack stack) {
    // Valore base (1.0 = velocità vanilla)
    float multiplier = 1.0F;
    
    IModule<ModuleDrawSpeedUnit> module = getModule(stack, MekanismWeaponsModules.DRAW_SPEED_UNIT);
    if (module != null && module.isEnabled()) {
        // ORA USIAMO DIRETTAMENTE IL MOLTIPLICATORE TIPO INSANIUM
        // Non facciamo più calcoli strani con 20 / tick.
        multiplier = module.getCustomInstance().getDrawSpeedMultiplier();
    }
    
    return multiplier;
   }

   @Override
    public boolean isEnchantable(ItemStack stack) {
        return MekanismWeaponsConfig.mekaBowEnchantments;
    }

    @Override
    public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
        return isEnchantable(stack);
    }

    @Override
    public void getSubItems(@Nonnull CreativeTabs tabs, @Nonnull NonNullList<ItemStack> list) {
        if (!isInCreativeTab(tabs)) return;

        // 1. Arco scarico e senza moduli (Base)
        list.add(new ItemStack(this));

        // 2. Arco SOLO CARICO (Senza moduli)
        ItemStack chargedOnly = new ItemStack(this);
        // Impostiamo l'energia al massimo della capacità base (senza moduli energy)
        setEnergy(chargedOnly, getMaxEnergy(chargedOnly));
        list.add(chargedOnly);

        // 3. Arco FULL (Moduli al massimo + Energia al massimo)
        ItemStack fullStack = new ItemStack(this);
        setAllModule(fullStack); // Questo aggiunge i moduli
        // Ricarichiamo l'energia (getMaxEnergy ora restituirà il valore potenziato dai moduli)
        setEnergy(fullStack, getMaxEnergy(fullStack));
        list.add(fullStack);
    }

    @Override
    public void setAllModule(ItemStack stack) {
        mekanism.api.gear.ModuleData<?>[] modules = {
            MekanismModules.ENERGY_UNIT,
            MekanismWeaponsModules.WEAPON_ATTACK_AMPLIFICATION_UNIT,
            MekanismWeaponsModules.ARROW_VELOCITY_UNIT,
            MekanismWeaponsModules.AUTO_FIRE_UNIT,
            MekanismWeaponsModules.DRAW_SPEED_UNIT,
            MekanismWeaponsModules.COMPOUND_ARROW_UNIT,
            MekanismWeaponsModules.ENERGY_ARROWS_UNIT,
            MekanismWeaponsModules.GRAVITY_DAMPENER_UNIT
        };

        for (mekanism.api.gear.ModuleData<?> type : modules) {
            // 1. Aggiungiamo il modulo con il metodo che funziona sicuramente
            addModule(stack, type);
        
            // 2. Recuperiamo l'istanza come IModule (che abbiamo visto avere getCustomInstance o simili)
            mekanism.api.gear.IModule<?> iModule = getModule(stack, type);
        
            if (iModule instanceof mekanism.common.content.gear.Module) {
                // Facciamo il cast alla classe concreta per i setter
                mekanism.common.content.gear.Module instance = (mekanism.common.content.gear.Module) iModule;
            
                // Impostiamo il numero massimo
                instance.setInstalledCount(type.getMaxStackSize());
            
                // Invece di setEnabled, usiamo la gestione diretta del NBT di Mekanism
                // Il metodo save() con un Runnable vuoto forza la scrittura dei dati correnti (incluso enabled:true)
                instance.save(() -> {}); 
            }
        }
    }
}
