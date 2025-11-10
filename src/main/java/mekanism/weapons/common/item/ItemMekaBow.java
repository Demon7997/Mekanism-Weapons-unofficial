package mekanism.weapons.common.item;

import mekanism.api.EnumColor;
import mekanism.api.energy.IEnergizedItem;
import mekanism.api.gear.IModule;
import mekanism.client.MekKeyHandler;
import mekanism.client.MekanismKeyHandler;
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
import mekanism.weapons.common.module.ModuleGravityDampenerUnit;
import mekanism.weapons.common.module.ModuleWeaponAttackAmplificationUnit;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumAction;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.stats.StatList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import mekanism.common.item.interfaces.IModeItem;
import mekanism.common.lib.radial.IGenericRadialModeItem;
import mekanism.api.radial.RadialData;
import mekanism.api.radial.mode.IRadialMode;
import mekanism.api.radial.mode.NestedRadialMode;
import mekanism.common.lib.radial.data.NestingRadialData;
import mekanism.common.content.gear.Module;
import net.minecraft.util.text.ITextComponent;
import org.jetbrains.annotations.NotNull;
import net.minecraft.item.ItemBow;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;


public class ItemMekaBow extends ItemBow implements IEnergizedItem, IModuleContainerItem, IModeItem, IGenericRadialModeItem {

    public ItemMekaBow() {
        super();
        setCreativeTab(MekanismWeaponsItems.tabMekanismWeapons);
        setMaxStackSize(1);

        // Sovrascriviamo le animazioni vanilla. Ora funzionerà.
this.addPropertyOverride(new ResourceLocation("pulling"), (stack, world, entity) -> 
        entity != null && entity.isHandActive() && entity.getActiveItemStack() == stack ? 1.0F : 0.0F
    );

    this.addPropertyOverride(new ResourceLocation("pull"), (stack, world, entity) -> {
        if (entity == null || entity.getActiveItemStack().getItem() != this) {
            return 0.0F;
        }

        // L'ANIMAZIONE LEGGE L'NBT. ESATTAMENTE COME IL SERVER.
        float drawTimeNeeded = 20.0F;
        if (stack.hasTagCompound() && stack.getTagCompound().hasKey("mekData", 10)) {
            NBTTagCompound modules = stack.getTagCompound().getCompoundTag("mekData").getCompoundTag("modules");
            if (modules.hasKey("draw_speed_unit", 10)) {
                NBTTagCompound drawSpeedData = modules.getCompoundTag("draw_speed_unit");
                if (drawSpeedData.getBoolean("enabled")) {
                    int levelOrdinal = drawSpeedData.getInteger("draw_speed_level");
                    if (levelOrdinal > 0) {
                        drawTimeNeeded = 20 - (levelOrdinal * 5);
                    }
                }
            }
        }

        float charge = (float)(stack.getMaxItemUseDuration() - entity.getItemInUseCount());
        return charge / drawTimeNeeded;
    });
}
    @Override
    public EnumAction getItemUseAction(ItemStack stack) { return EnumAction.BOW; }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) { return 72000; }


@Nonnull
    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, @Nonnull EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
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
    int ticksInUse = getMaxItemUseDuration(stack) - count;

    // Controlliamo se il modulo Draw Speed è installato.
    IModule<ModuleDrawSpeedUnit> drawSpeedModule = getModule(stack, MekanismWeaponsModules.DRAW_SPEED_UNIT);
    boolean hasDrawSpeed = drawSpeedModule != null && drawSpeedModule.isEnabled();

    // Prendiamo il tempo di carica necessario. Se il modulo c'è, lo usiamo, altrimenti 20.
    int drawTimeNeeded = hasDrawSpeed ? drawSpeedModule.getCustomInstance().getDrawTicks() : 20;

    // --- LOGICA ---
    
    // CASO 1: Auto-Fire è attivo.
    if (isModuleEnabled(stack, MekanismWeaponsModules.AUTO_FIRE_UNIT)) {
        if (ticksInUse >= drawTimeNeeded) {
            player.stopActiveHand(); // Rilascia appena carico.
        }
        return; // Fine.
    }

    // CASO 2: Tiro Manuale, MA con il modulo Draw Speed.
    // L'animazione sembra un "auto-fire" perché l'arco si carica e poi "scatta".
    // Questo è il comportamento che cerchiamo per dare la sensazione di velocità.
    // Quando la carica è completa, fermiamo l'azione per scoccare la freccia.
    if (hasDrawSpeed) {
        if (ticksInUse >= drawTimeNeeded) {
            // Forziamo il rilascio. Questo farà scattare onPlayerStoppedUsing.
            player.stopActiveHand(); 
        }
    }
    
    // CASO 3: Arco base senza moduli.
    // In questo caso, non facciamo nulla. Il giocatore decide quando rilasciare.
    // onUsingTick non interviene.
}

@Override
public void onPlayerStoppedUsing(ItemStack stack, World worldIn, EntityLivingBase entityLiving, int timeLeft) {
    if (!(entityLiving instanceof EntityPlayer)) return;
    EntityPlayer player = (EntityPlayer) entityLiving;
    
    int charge = this.getMaxItemUseDuration(stack) - timeLeft;
    if (charge <= 0) return;

    // ... (tutta la logica per controllare munizioni, energia, ecc. va bene com'era) ...

    float drawTimeNeeded = (float) getDrawTicks(stack); // Prendiamo il tempo corretto
    float powerRatio = (float) charge / drawTimeNeeded;

    if (powerRatio > 1.0F) powerRatio = 1.0F; // Limitiamo la potenza al 100%
    
    boolean useEnergyArrows = isModuleEnabled(stack, MekanismWeaponsModules.ENERGY_ARROWS_UNIT);
    ItemStack ammoStack = findAmmo(player);
    if (ammoStack.isEmpty() && !useEnergyArrows && !player.capabilities.isCreativeMode) return;
    
    if (powerRatio > 1.0F) powerRatio = 1.0F;
    if (powerRatio < 0.1F) return;
    
    float finalPower = (powerRatio * powerRatio + powerRatio * 2.0F) / 3.0F;
    
    // --- Logica Server (sparo e consumo energia) ---
    if (!worldIn.isRemote) {
        double totalEnergyNeeded = MekanismWeaponsConfig.mekaBowEnergyUsage;
        IModule<ModuleWeaponAttackAmplificationUnit> damageModule = getModule(stack, MekanismWeaponsModules.WEAPON_ATTACK_AMPLIFICATION_UNIT);
        if (damageModule != null && damageModule.isEnabled()) totalEnergyNeeded += MekanismWeaponsConfig.attackAmplificationEnergyUsage * damageModule.getInstalledCount();
        if (isModuleEnabled(stack, MekanismWeaponsModules.AUTO_FIRE_UNIT)) totalEnergyNeeded += MekanismWeaponsConfig.mekaBowAutofireEnergyUsage;
        IModule<ModuleDrawSpeedUnit> drawSpeedModule = getModule(stack, MekanismWeaponsModules.DRAW_SPEED_UNIT);
        if (drawSpeedModule != null && drawSpeedModule.isEnabled()) totalEnergyNeeded += MekanismWeaponsConfig.mekaBowDrawSpeedUsage * drawSpeedModule.getInstalledCount();
        if (isModuleEnabled(stack, MekanismWeaponsModules.GRAVITY_DAMPENER_UNIT)) totalEnergyNeeded += MekanismWeaponsConfig.mekaBowGravityDampenerUsage;
        if (useEnergyArrows) totalEnergyNeeded += MekanismWeaponsConfig.mekaBowEnergyArrowUsage;

        if (getEnergy(stack) < totalEnergyNeeded && !player.capabilities.isCreativeMode) return;
        if (!player.capabilities.isCreativeMode) setEnergy(stack, getEnergy(stack) - totalEnergyNeeded);

        EntityMekaArrow arrow = new EntityMekaArrow(worldIn, player);
        float finalDamage = MekanismWeaponsConfig.mekaBowBaseDamage;
        if (damageModule != null && damageModule.isEnabled()) finalDamage *= damageModule.getCustomInstance().getDamageBonus(damageModule);
        arrow.setDamage(finalDamage);
        arrow.shoot(player, player.rotationPitch, player.rotationYaw, 0.0F, finalPower * 3.0F, 1.0F);
        
        if (powerRatio >= 1.0F) arrow.setIsCritical(true);
        
        if (useEnergyArrows || player.capabilities.isCreativeMode) {
            arrow.pickupStatus = EntityArrow.PickupStatus.CREATIVE_ONLY;
        } else {
            ammoStack.shrink(1);
            if (ammoStack.isEmpty()) player.inventory.deleteStack(ammoStack);
        }
        
        worldIn.spawnEntity(arrow);
    }
    
    // Suono e statistiche vengono gestiti da entrambi i lati
    worldIn.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_ARROW_SHOOT, SoundCategory.PLAYERS, 1.0F, 1.0F / (itemRand.nextFloat() * 0.4F + 1.2F) + powerRatio * 0.5F);
    player.addStat(StatList.getObjectUseStats(this));
}
    
    // Il metodo helper rimane utile per la logica di Auto-Fire
    public int getDrawTicks(ItemStack stack) {
        IModule<ModuleDrawSpeedUnit> drawSpeedModule = getModule(stack, MekanismWeaponsModules.DRAW_SPEED_UNIT);
        if (drawSpeedModule != null && drawSpeedModule.isEnabled()) {
            return drawSpeedModule.getCustomInstance().getDrawTicks();
        }
        return 20;
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
}
