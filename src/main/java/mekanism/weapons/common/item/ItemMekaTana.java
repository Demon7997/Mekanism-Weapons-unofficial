package mekanism.weapons.common.item;

import com.google.common.collect.Multimap;
import mekanism.api.EnumColor;
import mekanism.api.gear.IModule;
import mekanism.api.radial.RadialData;
import mekanism.api.radial.mode.IRadialMode;
import mekanism.api.radial.mode.NestedRadialMode;
import mekanism.client.MekKeyHandler;
import mekanism.client.MekanismKeyHandler;
import mekanism.common.MekanismModules;
import mekanism.common.content.gear.IModuleContainerItem;
import mekanism.common.content.gear.Module;
import mekanism.common.content.gear.mekatool.ModuleTeleportationUnit;
import mekanism.common.content.gear.shared.ModuleEnergyUnit;
import mekanism.common.item.ItemEnergized;
import mekanism.common.item.interfaces.IModeItem;
import mekanism.common.lib.radial.IGenericRadialModeItem;
import mekanism.common.lib.radial.data.NestingRadialData;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.weapons.MekanismWeapons;
import mekanism.weapons.MekanismWeaponsItems;
import mekanism.weapons.MekanismWeaponsModules;
import mekanism.weapons.config.MekanismWeaponsConfig;
import mekanism.weapons.common.module.ModuleWeaponAttackAmplificationUnit;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class ItemMekaTana extends ItemEnergized implements IModuleContainerItem, IModeItem, IGenericRadialModeItem {

    public ItemMekaTana() {
        // Chiama il costruttore di ItemEnergized con la capacità di base dalla config
        super(MekanismWeaponsConfig.mekaTanaBaseEnergyCapacity);
        setCreativeTab(MekanismWeaponsItems.tabMekanismWeapons);
        setMaxStackSize(1);
        setRarity(EnumRarity.EPIC);
    }

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
    
    // Restituiamo la rarità più alta trovata (o quella di base se non ci sono moduli più rari).
    return rarity;
}

    // Traduce la logica IEnergizedItem nel sistema standard di Forge (Capabilities)
    // Questo è il pezzo fondamentale che fa comunicare la Tana con il Cubo di Energia
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable NBTTagCompound nbt) {
    // Chiama il metodo initCapabilities della classe genitore (ItemEnergized),
    // che sa già come gestire l'energia correttamente per questa versione di Mekanism.
    return super.initCapabilities(stack, nbt);
    }

    // Metodi energetici che danno priorità ai moduli
    @Override
    public double getMaxEnergy(ItemStack stack) {
        IModule<ModuleEnergyUnit> module = getModule(stack, MekanismModules.ENERGY_UNIT);
        return module == null ? super.getMaxEnergy(stack) : module.getCustomInstance().getEnergyCapacity(module);
    }

    @Override
    public double getMaxTransfer(ItemStack stack) {
        IModule<ModuleEnergyUnit> module = getModule(stack, MekanismModules.ENERGY_UNIT);
        // Se c'è il modulo usa quello, altrimenti usa il rate base della config
        return module != null ? module.getCustomInstance().getChargeRate(module) : MekanismWeaponsConfig.mekaTanaBaseChargeRate;
    }

    // --- Logica di Combattimento (Tuo codice originale, corretto) ---
    @Override
    public Multimap<String, AttributeModifier> getAttributeModifiers(EntityEquipmentSlot slot, ItemStack stack) {
    Multimap<String, AttributeModifier> multimap = super.getAttributeModifiers(slot, stack);
    multimap.clear(); // Partiamo da zero

    if (slot == EntityEquipmentSlot.MAINHAND) {
        double damage;
        double energyCost = MekanismWeaponsConfig.mekaTanaEnergyUsage;
        double baseDamageWithEnergy = MekanismWeaponsConfig.mekaTanaBaseDamage; // Questo è 51
        double baseDamageWithoutEnergy = 20D; // Questo è 21

        IModule<ModuleWeaponAttackAmplificationUnit> damageModule = getModule(stack, MekanismWeaponsModules.WEAPON_ATTACK_AMPLIFICATION_UNIT);
        
        // Controlliamo se il modulo è installato, attivo E NON su OFF
        if (damageModule != null && damageModule.isEnabled()) {
            ModuleWeaponAttackAmplificationUnit customInstance = damageModule.getCustomInstance();
            ModuleWeaponAttackAmplificationUnit.DamageMode mode = customInstance.getDamageMode();

            // REGOLA 1: Se il modulo è su OFF, il danno è 1.
            if (mode == ModuleWeaponAttackAmplificationUnit.DamageMode.OFF) {
                damage = 0;
            } else {
                // Se il modulo è su LOW o superiore...
                energyCost += customInstance.getEnergyCost(damageModule);
                // Controlliamo se abbiamo abbastanza energia
                if (getEnergy(stack) >= energyCost) {
                    // CON ENERGIA: il danno è quello base (51) moltiplicato per il bonus
                    damage = baseDamageWithEnergy * mode.getMultiplier();
                } else {
                    // SENZA ENERGIA: il danno è 21
                    damage = baseDamageWithoutEnergy;
                }
            }
        } else {
            // Se non c'è nessun modulo di danno...
            // Controlliamo l'energia per decidere tra 51 e 21
            if (getEnergy(stack) >= energyCost) {
                damage = baseDamageWithEnergy;
            } else {
                damage = baseDamageWithoutEnergy;
            }
        }
        
        multimap.put(SharedMonsterAttributes.ATTACK_DAMAGE.getName(), new AttributeModifier(ItemSword.ATTACK_DAMAGE_MODIFIER, "Weapon modifier", damage, 0));
        multimap.put(SharedMonsterAttributes.ATTACK_SPEED.getName(), new AttributeModifier(ItemSword.ATTACK_SPEED_MODIFIER, "Weapon speed modifier", MekanismWeaponsConfig.mekaTanaAttackSpeed, 0));
    }
    
    return multimap;
    }
    
    @Override
    public boolean hitEntity(ItemStack stack, EntityLivingBase target, EntityLivingBase attacker) {
        double energyCost = MekanismWeaponsConfig.mekaTanaEnergyUsage;
        IModule<ModuleWeaponAttackAmplificationUnit> module = getModule(stack, MekanismWeaponsModules.WEAPON_ATTACK_AMPLIFICATION_UNIT);
        
        if (module != null && module.isEnabled()) {
            energyCost += module.getCustomInstance().getEnergyCost(module);
        }

        // Se chi attacca è un player in Creative, NON consumare energia
        if (attacker instanceof EntityPlayer && ((EntityPlayer) attacker).capabilities.isCreativeMode) {
            return true;
        }

        if (getEnergy(stack) >= energyCost) {
            setEnergy(stack, getEnergy(stack) - energyCost);
        }
        return true;
    }

    // --- Logica del Teletrasporto (Tuo codice originale) ---
    @NotNull
    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, @NotNull EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        IModule<ModuleTeleportationUnit> module = getModule(stack, MekanismModules.TELEPORTATION_UNIT);
        
        if (module != null && module.isEnabled()) {
            RayTraceResult pos = MekanismUtils.rayTrace(world, player, MekanismWeaponsConfig.mekaTanaMaxTeleportReach);
            if (pos != null && pos.typeOfHit == RayTraceResult.Type.BLOCK) {
                double distance = player.getDistanceSq(pos.getBlockPos());
                double energyCost = MekanismWeaponsConfig.mekaTanaTeleportEnergyUsage * (distance / 10D);
                
                // Se sei in creative o hai abbastanza energia
                if (player.capabilities.isCreativeMode || getEnergy(stack) >= energyCost) {
                    BlockPos blockPos = pos.getBlockPos();
                    if (world.isAirBlock(blockPos.up()) && world.isAirBlock(blockPos.up(2))) {
                        if (!world.isRemote) {
                            if (player.isRiding()) player.dismountRidingEntity();
                            player.setPositionAndUpdate(blockPos.getX() + 0.5, blockPos.getY() + 1.5, blockPos.getZ() + 0.5);
                            player.fallDistance = 0.0F;
                            
                            // Consuma energia SOLO se NON sei in creative
                            if (!player.capabilities.isCreativeMode) {
                                setEnergy(stack, getEnergy(stack) - energyCost);
                            }
                            
                            world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_ENDERMEN_TELEPORT, SoundCategory.PLAYERS, 1.0F, 1.0F);
                        }
                        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
                    }
                }
            }
        }
        return new ActionResult<>(EnumActionResult.PASS, stack);
    }
    
    // --- Metodi di Visualizzazione e Tooltip (ereditati o corretti) ---
    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        if (MekKeyHandler.getIsKeyPressed(MekanismKeyHandler.sneakKey)) {
            addModuleDetails(stack, tooltip);
        } else {
            tooltip.add(EnumColor.AQUA + LangUtils.localize("tooltip.storedEnergy") + ": " + EnumColor.GREY + MekanismUtils.getEnergyDisplay(getEnergy(stack), getMaxEnergy(stack)));
            tooltip.add(LangUtils.localize("tooltip.hold") + " " + EnumColor.INDIGO + GameSettings.getKeyDisplayString(MekanismKeyHandler.sneakKey.getKeyCode()) + EnumColor.GREY + " " + LangUtils.localize("tooltip.forDetails") + ".");
        }
    }
    
    @Override
    public boolean isEnchantable(ItemStack stack) {
        return MekanismWeaponsConfig.mekaTanaEnchantments;
    }

    @Override
    public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
        return isEnchantable(stack);
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
        return new NestingRadialData(new ResourceLocation(MekanismWeapons.MODID, "meka_tana_root"), nestedModes);
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
    // QUESTO DEVE APPARIRE IN CHAT APPENA CLICCHI
    if (!player.world.isRemote) {
        player.sendMessage(new TextComponentString("DEBUG SPADA: Ricevuto clic per " + radialData.getIdentifier().toString()));
    }

    for (mekanism.common.content.gear.Module<?> module : getModules(stack)) {
        if (module.getCustomInstance() instanceof ModuleWeaponAttackAmplificationUnit) {
            ModuleWeaponAttackAmplificationUnit custom = (ModuleWeaponAttackAmplificationUnit) module.getCustomInstance();
            if (custom.setModeCustom(player, stack, radialData, (IRadialMode) mode)) {
                return;
            }
        }
    }
}

    @Override
    public void getSubItems(net.minecraft.creativetab.CreativeTabs tabs, net.minecraft.util.NonNullList<ItemStack> list) {
        if (!isInCreativeTab(tabs)) return;

        // 1. Tana Base (Vuota e Scarica)
        list.add(new ItemStack(this));

        // 2. Tana Solo Carica (Senza moduli)
        ItemStack chargedOnly = new ItemStack(this);
        setEnergy(chargedOnly, getMaxEnergy(chargedOnly));
        list.add(chargedOnly);

        // 3. Tana Full Modded (Moduli + Energia)
        ItemStack fullStack = new ItemStack(this);
        setAllModule(fullStack); 
        setEnergy(fullStack, getMaxEnergy(fullStack));
        list.add(fullStack);
    }

    @Override
    public void setAllModule(ItemStack stack) {
        // Array dei moduli compatibili con la MekaTana
        mekanism.api.gear.ModuleData<?>[] modules = {
            MekanismModules.ENERGY_UNIT,               // Capacità energetica
            MekanismModules.TELEPORTATION_UNIT,        // Teletrasporto (Tasto Destro)
            MekanismWeaponsModules.WEAPON_ATTACK_AMPLIFICATION_UNIT // Danno aumentato
        };

        for (mekanism.api.gear.ModuleData<?> type : modules) {
            // Aggiungiamo il modulo all'item
            addModule(stack, type);
        
            // Recuperiamo l'istanza per configurarla al massimo
            mekanism.api.gear.IModule<?> iModule = getModule(stack, type);
            if (iModule instanceof mekanism.common.content.gear.Module) {
                mekanism.common.content.gear.Module instance = (mekanism.common.content.gear.Module) iModule;
            
                // Impostiamo il numero massimo di stack (es. 4 unità di danno)
                instance.setInstalledCount(type.getMaxStackSize());
            
                // Salviamo lo stato attivando il modulo (Lambda vuota come richiesto dalla tua versione)
                instance.save(() -> {}); 
            }
        }
    }
}
