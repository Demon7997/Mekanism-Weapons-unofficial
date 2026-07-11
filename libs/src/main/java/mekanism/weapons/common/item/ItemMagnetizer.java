package mekanism.weapons.common.item;

import baubles.api.BaubleType;
import baubles.api.IBauble;
import mekanism.api.EnumColor;
import mekanism.api.energy.IStrictEnergyStorage;
import mekanism.common.Mekanism;
import mekanism.common.content.entangloporter.InventoryFrequency;
import mekanism.common.frequency.Frequency;
import mekanism.common.frequency.FrequencyManager;
import mekanism.common.item.ItemMekanism;
import mekanism.common.util.ChargeUtils;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.weapons.MekanismWeapons;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public class ItemMagnetizer extends ItemMekanism implements IBauble {

    public ItemMagnetizer() {
        super();
        setMaxStackSize(1);
        setRarity(EnumRarity.RARE);
    }

    @Override
    public BaubleType getBaubleType(ItemStack itemstack) { return BaubleType.TRINKET; }

    @Override
    public void onWornTick(ItemStack itemstack, EntityLivingBase player) {
        if (player instanceof EntityPlayer && !player.world.isRemote) {
            chargeInventory((EntityPlayer) player, itemstack);
        }
    }

    @Override
    public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean isSelected) {
        if (!world.isRemote && entity instanceof EntityPlayer) {
            chargeInventory((EntityPlayer) entity, stack);
        }
    }

    private void chargeInventory(EntityPlayer player, ItemStack magnetizer) {
        InventoryFrequency frequency = getInventoryFrequency(magnetizer, player.world);
        
        if (frequency != null && frequency.storedEnergy > 0) {
            IStrictEnergyStorage frequencyWrapper = new IStrictEnergyStorage() {
                @Override public double getEnergy() { return frequency.storedEnergy; }
                @Override public void setEnergy(double energy) { frequency.storedEnergy = energy; }
                @Override public double getMaxEnergy() { return Double.MAX_VALUE; }
            };

            for (ItemStack slot : player.inventory.armorInventory) {
                if (frequency.storedEnergy <= 0) break;
                ChargeUtils.charge(slot, frequencyWrapper);
            }

            for (ItemStack slot : player.inventory.mainInventory) {
                if (frequency.storedEnergy <= 0) break;
                if (slot == magnetizer) continue;
                ChargeUtils.charge(slot, frequencyWrapper);
            }
        }
    }

    public InventoryFrequency getInventoryFrequency(ItemStack stack, World world) {
        if (!ItemDataUtils.hasData(stack, "frequency")) return null;
        
        NBTTagCompound tag = ItemDataUtils.getCompound(stack, "frequency");
        String name = tag.getString("name");
        boolean isPublic = tag.getBoolean("publicFreq");
        UUID owner = getOwnerUUID(stack);
        
        FrequencyManager manager;
        if (isPublic) {
            manager = Mekanism.publicEntangloporters;
        } else {
            if (owner == null) return null;
            if (!Mekanism.privateEntangloporters.containsKey(owner)) {
                FrequencyManager newManager = new FrequencyManager(InventoryFrequency.class, InventoryFrequency.ENTANGLOPORTER, owner);
                Mekanism.privateEntangloporters.put(owner, newManager);
                newManager.createOrLoad(world);
            }
            manager = Mekanism.privateEntangloporters.get(owner);
        }

        if (manager != null) {
            for (Frequency f : manager.getFrequencies()) {
                if (f.name.equals(name)) return (InventoryFrequency) f;
            }
        }
        return null;
    }

    public UUID getOwnerUUID(ItemStack stack) {
        if (!ItemDataUtils.hasData(stack, "ownerUUID")) return null;
        try {
            return UUID.fromString(ItemDataUtils.getString(stack, "ownerUUID"));
        } catch (Exception e) { return null; }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag) {
        tooltip.add(EnumColor.ORANGE + LangUtils.localize("tooltip.mekaweapons.slot") + ": " + EnumColor.WHITE + "Magnetizer");
    
        String owner = getOwnerUsername(stack);
    
        if (owner.equals("No Owner")) {
            tooltip.add(EnumColor.DARK_RED + LangUtils.localize("gui.no_owner"));
        } else {
            tooltip.add(EnumColor.GREY + LangUtils.localize("gui.owner") + ": " + EnumColor.BRIGHT_GREEN + owner);
        }
    
        tooltip.add(LangUtils.localize("tooltip.mekaweapons.magnetizer.desc"));
    }

    private String getOwnerUsername(ItemStack stack) {
        if (ItemDataUtils.hasData(stack, "ownerUUID")) {
            try {
                String uuidStr = ItemDataUtils.getString(stack, "ownerUUID");
                String username = MekanismUtils.getLastKnownUsername(UUID.fromString(uuidStr));
                return username != null ? username : "No Owner";
            } catch (Exception e) {
                return "No Owner";
            }
        }
        return "No Owner";
    }

    @Nonnull
    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (getOwnerUUID(stack) == null) {
            ItemDataUtils.setString(stack, "ownerUUID", player.getUniqueID().toString());
        }
        if (!world.isRemote) {
            player.openGui(MekanismWeapons.instance, 100, world, hand.ordinal(), 0, 0);
        }
        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }

    public void setFrequencyCustom(ItemStack stack, String name, boolean isPublic) {
        if (name == null || name.isEmpty()) ItemDataUtils.removeData(stack, "frequency");
        else {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setString("name", name);
            tag.setBoolean("publicFreq", isPublic);
            ItemDataUtils.setCompound(stack, "frequency", tag);
        }
    }

    public void setRenderEnabled(ItemStack stack, String key, boolean state) {
        ItemDataUtils.setBoolean(stack, key, state);
    }

    public boolean isRenderEnabled(ItemStack stack, String type) {
        return ItemDataUtils.getBoolean(stack, "render_" + type);
    }
}
