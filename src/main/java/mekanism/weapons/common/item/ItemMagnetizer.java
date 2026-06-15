package mekanism.weapons.common.item;

import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import baubles.api.BaubleType;
import baubles.api.IBauble;
import mekanism.api.EnumColor;
import mekanism.api.energy.IEnergizedItem;
import mekanism.common.Mekanism;
import mekanism.common.content.entangloporter.InventoryFrequency;
import mekanism.common.frequency.Frequency;
import mekanism.common.frequency.FrequencyManager;
import mekanism.common.item.ItemPortableTeleporter;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.LangUtils;
import mekanism.common.util.SecurityUtils;
import mekanism.weapons.MekanismWeapons;
import mekanism.weapons.MekanismWeaponsItems;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Optional.Interface(iface = "baubles.api.IBauble", modid = "baubles")
public class ItemMagnetizer extends ItemPortableTeleporter implements IBauble {

    public ItemMagnetizer() {
        super();
        setCreativeTab(MekanismWeaponsItems.tabMekanismWeapons);
    }

    @Override
    public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if (!world.isRemote && entity instanceof EntityPlayer) {
            chargeInventory(stack, (EntityPlayer) entity);
        }
    }

    @Override
    @Optional.Method(modid = "baubles")
    public void onWornTick(ItemStack stack, EntityLivingBase player) {
        if (!player.world.isRemote && player instanceof EntityPlayer) {
            chargeInventory(stack, (EntityPlayer) player);
        }
    }

    private void chargeInventory(ItemStack stack, EntityPlayer player) {
        Frequency.Identity ident = getFrequency(stack);
        if (ident == null) return;

        InventoryFrequency freq = null;
        FrequencyManager manager = ident.publicFreq ? Mekanism.publicEntangloporters : Mekanism.privateEntangloporters.get(getOwnerUUID(stack));
        
        if (manager != null) {
            for (Frequency f : manager.getFrequencies()) {
                if (f.name.equals(ident.name)) {
                    freq = (InventoryFrequency) f;
                    break;
                }
            }
        }

        if (freq == null || freq.storedEnergy <= 0) return;

        double rate = 256000;

        for (ItemStack invStack : player.inventory.mainInventory) {
            if (invStack.isEmpty() || invStack == stack) continue;
            chargeItemDirectly(freq, invStack, rate);
        }
        for (ItemStack armorStack : player.inventory.armorInventory) {
            if (armorStack.isEmpty()) continue;
            chargeItemDirectly(freq, armorStack, rate);
        }
    }

    private void chargeItemDirectly(InventoryFrequency freq, ItemStack stack, double rate) {
        if (stack.getItem() instanceof IEnergizedItem) {
            IEnergizedItem energized = (IEnergizedItem) stack.getItem();
            double needed = energized.getMaxEnergy(stack) - energized.getEnergy(stack);
            if (needed > 0) {
                double toTransfer = Math.min(Math.min(needed, rate), freq.storedEnergy);
                energized.setEnergy(stack, energized.getEnergy(stack) + toTransfer);
                freq.storedEnergy -= toTransfer;
            }
        } else if (stack.hasCapability(CapabilityEnergy.ENERGY, null)) {
            IEnergyStorage storage = stack.getCapability(CapabilityEnergy.ENERGY, null);
            if (storage != null && storage.canReceive()) {
                int toTransfer = (int) Math.min(rate, freq.storedEnergy);
                int accepted = storage.receiveEnergy(toTransfer, false);
                freq.storedEnergy -= accepted;
            }
        }
    }

    public void setFrequencyCustom(ItemStack stack, String name, boolean isPublic) {
        net.minecraft.nbt.NBTTagCompound compound = new net.minecraft.nbt.NBTTagCompound();
        compound.setString("name", name);
        compound.setBoolean("publicFreq", isPublic);
        ItemDataUtils.setCompound(stack, "frequency", compound);
    }

    public void setRenderEnabled(ItemStack stack, String weaponType, boolean state) {
        ItemDataUtils.setBoolean(stack, "render_" + weaponType, state);
    }

    public boolean isRenderEnabled(ItemStack stack, String weaponType) {
        if (!ItemDataUtils.hasData(stack, "render_" + weaponType)) return true;
        return ItemDataUtils.getBoolean(stack, "render_" + weaponType);
    }

    @Nonnull
    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        if (!world.isRemote) {
            if (getOwnerUUID(player.getHeldItem(hand)) == null) {
                setOwnerUUID(player.getHeldItem(hand), player.getUniqueID());
            }
            player.openGui(MekanismWeapons.instance, 100, world, hand.ordinal(), 0, 0);
        }
        return new ActionResult<>(EnumActionResult.SUCCESS, player.getHeldItem(hand));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag) {
        tooltip.add(SecurityUtils.getOwnerDisplay(net.minecraft.client.Minecraft.getMinecraft().player, 
                    mekanism.client.MekanismClient.clientUUIDMap.get(getOwnerUUID(stack))));
        
        Frequency.Identity ident = getFrequency(stack);
        if (ident != null) {
            tooltip.add(EnumColor.INDIGO + LangUtils.localize("gui.frequency") + ": " + EnumColor.GREY + ident.name);
            String modeStr = ident.publicFreq ? LangUtils.localize("gui.public") : LangUtils.localize("gui.private");
            tooltip.add(EnumColor.INDIGO + LangUtils.localize("gui.mode") + ": " + EnumColor.GREY + modeStr);
        }
        tooltip.add(EnumColor.PURPLE + "Slot: Magnetizer / Trinket");
    }

    @Override @Optional.Method(modid = "baubles") public BaubleType getBaubleType(ItemStack itemstack) { return BaubleType.TRINKET; }
}
