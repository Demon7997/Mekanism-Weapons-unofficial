package mekanism.weapons.client.gui;

import mekanism.weapons.common.inventory.container.MagnetizerContainer;
import mekanism.weapons.common.network.MekaWeaponsPacketHandler;
import mekanism.weapons.common.network.PacketMagnetizer;
import mekanism.api.EnumColor;
import mekanism.client.gui.GuiMekanism;
import mekanism.client.gui.button.GuiDisableableButton;
import mekanism.client.gui.element.GuiElementScreen;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.GuiScrollList;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.sound.SoundHandler;
import mekanism.common.Mekanism;
import mekanism.common.MekanismSounds;
import mekanism.common.frequency.Frequency;
import mekanism.common.frequency.FrequencyManager;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GuiMagnetizer extends GuiMekanism {
    private ItemStack itemStack;
    private MagnetizerContainer container;
    private GuiTextField frequencyField;
    private GuiScrollList scrollList;
    private GuiDisableableButton publicButton, privateButton, setButton, deleteButton, checkboxButton;
    private boolean privateMode;
    private List<String> freqCache = new ArrayList<>();
    
    private int yStart = 14;

    public GuiMagnetizer(InventoryPlayer inventory, EnumHand hand) {
        super(new MagnetizerContainer(inventory, hand));
        this.container = (MagnetizerContainer) inventorySlots;
        this.itemStack = container.getStack();
        
        ResourceLocation res = MekanismUtils.getResource(ResourceType.GUI, "GuiBlank.png");
        
        addGuiElement(scrollList = new GuiScrollList(this, res, 28, 37, 120, 4));
        addGuiElement(new GuiElementScreen(this, res, 27, 36, 122, 42).isFrame());
        addGuiElement(new GuiInnerScreen(this, res, 48, 111, 101, 13));
        
        ySize = 175; 
    }

    @Override
    public void initGui() {
        super.initGui();
        buttonList.add(publicButton = new GuiDisableableButton(0, guiLeft + 27, guiTop + yStart, 60, 20, LangUtils.localize("gui.public")));
        buttonList.add(privateButton = new GuiDisableableButton(1, guiLeft + 89, guiTop + yStart, 60, 20, LangUtils.localize("gui.private")));
        buttonList.add(setButton = new GuiDisableableButton(2, guiLeft + 27, guiTop + yStart + 113, 60, 20, LangUtils.localize("gui.set")));
        buttonList.add(deleteButton = new GuiDisableableButton(3, guiLeft + 89, guiTop + yStart + 113, 60, 20, LangUtils.localize("gui.delete")));
        
        frequencyField = new GuiTextField(4, fontRenderer, guiLeft + 50, guiTop + yStart + 99, 98, 11);
        frequencyField.setMaxStringLength(FrequencyManager.MAX_FREQ_LENGTH);
        frequencyField.setEnableBackgroundDrawing(false);
        
        buttonList.add(checkboxButton = new GuiDisableableButton(5, guiLeft + 137, guiTop + yStart + 98, 11, 11).with(GuiDisableableButton.ImageOverlay.CHECKMARK));
        
        updateButtons();
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        String title = LangUtils.localize("gui.mekaweapons.magnetizer"); fontRenderer.drawString(title, (xSize / 2) - (fontRenderer.getStringWidth(title) / 2), 4, 0x404040);

        String freqName = EnumColor.DARK_RED + LangUtils.localize("gui.none");
        String ownerName = EnumColor.DARK_RED + LangUtils.localize("gui.none");
        String securityStr = EnumColor.DARK_RED + LangUtils.localize("gui.none");

        if (ItemDataUtils.hasData(itemStack, "frequency")) {
            NBTTagCompound tag = ItemDataUtils.getCompound(itemStack, "frequency");
            freqName = tag.getString("name");
            boolean isPublic = tag.getBoolean("publicFreq");
            securityStr = isPublic ? EnumColor.BRIGHT_GREEN + LangUtils.localize("gui.public") : EnumColor.DARK_RED + LangUtils.localize("gui.private");
            ownerName = EnumColor.BRIGHT_GREEN + getOwnerUsername();
        }

        int xPos = 27;
        int yStart = 14;

        fontRenderer.drawString(LangUtils.localize("gui.freq") + ":", xPos, yStart + 67, 0x404040);
        fontRenderer.drawString(" " + freqName, xPos + fontRenderer.getStringWidth(LangUtils.localize("gui.freq") + ":"), yStart + 67, 0x797979);

        fontRenderer.drawString(LangUtils.localize("gui.owner") + ":", xPos, yStart + 77, 0x404040);
        fontRenderer.drawString(" " + ownerName, xPos + fontRenderer.getStringWidth(LangUtils.localize("gui.owner") + ":"), yStart + 77, 0x797979);

        fontRenderer.drawString(LangUtils.localize("gui.security") + ":", xPos, yStart + 87, 0x404040);
        fontRenderer.drawString(" " + securityStr, xPos + fontRenderer.getStringWidth(LangUtils.localize("gui.security") + ":"), yStart + 87, 0x797979);

        renderScaledText(LangUtils.localize("gui.set") + ":", xPos, yStart + 100, 0x404040, 20);

        renderText(LangUtils.localize("gui.on"), 78.5F, 150.5F);
        renderText(LangUtils.localize("gui.off"), 78.5F, 159.5F);
        renderText(LangUtils.localize("gui.on"), 98.5F, 150.5F);
        renderText(LangUtils.localize("gui.off"), 98.5F, 159.5F);

        int xAxis = mouseX - guiLeft;
        int yAxis = mouseY - guiTop;
        if (katanaButtonInBounds(xAxis, yAxis)) {
            displayTooltip(LangUtils.localize("gui.mekaweapons.toggle_katana"), xAxis, yAxis);
        } else if (bowButtonInBounds(xAxis, yAxis)) {
            displayTooltip(LangUtils.localize("gui.mekaweapons.toggle_bow"), xAxis, yAxis);
        }

        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTick, int mouseX, int mouseY) {
        super.drawGuiContainerBackgroundLayer(partialTick, mouseX, mouseY);
    
        frequencyField.drawTextBox();

        MekanismRenderer.resetColor();
        mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.SWITCH, "switch_icon.png"));
    
        renderSwitch(71, 150, "render_tana");
        renderSwitch(91, 150, "render_bow");
    }

    private void renderSwitch(int x, int y, String nbtKey) {
        boolean active = ItemDataUtils.getBoolean(itemStack, nbtKey);
        drawTexturedModalRect(guiLeft + x, guiTop + y, 0, active ? 0 : 8, 15, 8);
        drawTexturedModalRect(guiLeft + x, guiTop + y + 9, 0, !active ? 0 : 8, 15, 8);
    }

    private void renderOnPfText(String text, float x, float y) {
        int textWidth = fontRenderer.getStringWidth(text);
        float centerX = (x - (textWidth / 2F) * 0.5F);
        float yAdd = 4 - (0.5F * 8) / 2F;
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y + yAdd, 0);
        GlStateManager.scale(0.5F, 0.5F, 0.5F);
        fontRenderer.drawString(text, (x - centerX) * -1, 0, 0x101010, false);
        GlStateManager.popMatrix();
        MekanismRenderer.resetColor();
    }

    protected boolean katanaButtonInBounds(int xAxis, int yAxis) { 
        return xAxis >= 71 && xAxis <= 86 && yAxis >= 150 && yAxis <= 168; 
    }

    protected boolean bowButtonInBounds(int xAxis, int yAxis) { 
        return xAxis >= 91 && xAxis <= 106 && yAxis >= 150 && yAxis <= 168; 
    }

    @Override
    protected void mouseClicked(int x, int y, int button) throws IOException {
        super.mouseClicked(x, y, button);
        frequencyField.mouseClicked(x, y, button);
        int xAxis = x - guiLeft;
        int yAxis = y - guiTop;

        String key = null;
        if (katanaButtonInBounds(xAxis, yAxis)) key = "render_tana";
        else if (bowButtonInBounds(xAxis, yAxis)) key = "render_bow";

        if (key != null) {
            SoundHandler.playSound(MekanismSounds.BEEP2);
            boolean newState = !ItemDataUtils.getBoolean(itemStack, key);
            ItemDataUtils.setBoolean(itemStack, key, newState);
            MekaWeaponsPacketHandler.netHandler.sendToServer(new PacketMagnetizer(key, newState));
        }
    }

    @Override
    protected void actionPerformed(GuiButton guibutton) throws IOException {
        super.actionPerformed(guibutton);

        if (guibutton.id == publicButton.id) {
            privateMode = false;
        } else if (guibutton.id == privateButton.id) {
            privateMode = true;
        } else if (guibutton.id == setButton.id && scrollList.hasSelection()) {
            String freq = freqCache.get(scrollList.getSelection());
        
            net.minecraft.nbt.NBTTagCompound tag = new net.minecraft.nbt.NBTTagCompound();
            tag.setString("name", freq);
            tag.setBoolean("publicFreq", !privateMode);
            ItemDataUtils.setCompound(itemStack, "frequency", tag);
        
            if (!ItemDataUtils.hasData(itemStack, "ownerUUID")) {
                ItemDataUtils.setString(itemStack, "ownerUUID", mc.player.getUniqueID().toString());
            }

            MekaWeaponsPacketHandler.netHandler.sendToServer(new PacketMagnetizer(0, freq, !privateMode));
        
        } else if (guibutton.id == deleteButton.id && scrollList.hasSelection()) {
            String freq = freqCache.get(scrollList.getSelection());
        
            if (ItemDataUtils.hasData(itemStack, "frequency")) {
                if (ItemDataUtils.getCompound(itemStack, "frequency").getString("name").equals(freq)) {
                    ItemDataUtils.removeData(itemStack, "frequency");
                }
            }
        
            MekaWeaponsPacketHandler.netHandler.sendToServer(new PacketMagnetizer(1, freq, !privateMode));
        
        } else if (guibutton.id == checkboxButton.id) {
            String freq = frequencyField.getText();
            if (!freq.isEmpty()) {
                net.minecraft.nbt.NBTTagCompound tag = new net.minecraft.nbt.NBTTagCompound();
                tag.setString("name", freq);
                tag.setBoolean("publicFreq", !privateMode);
                ItemDataUtils.setCompound(itemStack, "frequency", tag);

                if (!ItemDataUtils.hasData(itemStack, "ownerUUID")) {
                    ItemDataUtils.setString(itemStack, "ownerUUID", mc.player.getUniqueID().toString());
                }

                MekaWeaponsPacketHandler.netHandler.sendToServer(new PacketMagnetizer(0, freq, !privateMode));
                frequencyField.setText("");
            }
        }
        updateButtons();
    }

    public void updateButtons() {
    freqCache.clear();
    List<String> displayList = new ArrayList<>();
    
    FrequencyManager manager = privateMode ? Mekanism.privateEntangloporters.get(mc.player.getUniqueID()) : Mekanism.publicEntangloporters;
    
    if (manager != null) {
        for (Frequency freq : manager.getFrequencies()) {
            freqCache.add(freq.name);
            
            if (!privateMode) {
                String displayName = freq.clientOwner;
                
                if (displayName == null || displayName.isEmpty()) {
                    if (freq.ownerUUID != null && freq.ownerUUID.equals(mc.player.getUniqueID())) {
                        displayName = mc.player.getName();
                    } else {
                        displayName = "Unknown";
                    }
                }
                
                displayList.add(freq.name + " (" + displayName + ")");
            } else {
                displayList.add(freq.name);
            }
        }
    }
        
        scrollList.setText(displayList);
        
        publicButton.enabled = privateMode;
        privateButton.enabled = !privateMode;
        setButton.enabled = scrollList.hasSelection();
        deleteButton.enabled = scrollList.hasSelection();
        checkboxButton.enabled = !frequencyField.getText().isEmpty();
    }

    private String getOwnerUsername() {
        if (ItemDataUtils.hasData(itemStack, "ownerUUID")) {
            try {
                String uuidStr = ItemDataUtils.getString(itemStack, "ownerUUID");
                String username = MekanismUtils.getLastKnownUsername(java.util.UUID.fromString(uuidStr));
                return username != null ? username : mc.player.getName();
            } catch (Exception e) {
                return mc.player.getName();
            }
        }
        return mc.player.getName();
    }

    @Override public void updateScreen() { super.updateScreen(); updateButtons(); frequencyField.updateCursorCounter(); }
    @Override protected void keyTyped(char c, int i) throws IOException {
        if (!frequencyField.isFocused() || i == Keyboard.KEY_ESCAPE) super.keyTyped(c, i);
        if (i == Keyboard.KEY_RETURN && frequencyField.isFocused()) {
            MekaWeaponsPacketHandler.netHandler.sendToServer(new PacketMagnetizer(0, frequencyField.getText(), !privateMode));
            frequencyField.setText("");
        }
        if (Character.isDigit(c) || Character.isLetter(c) || isTextboxKey(c, i) || FrequencyManager.SPECIAL_CHARS.contains(c)) frequencyField.textboxKeyTyped(c, i);
        updateButtons();
    }

        private void renderText(String text, float x, float y) {
        int textWidth = fontRenderer.getStringWidth(text);
        float yAdd = 4 - (0.5F * 8) / 2F; 
    
        GlStateManager.pushMatrix();
        GlStateManager.scale(0.5F, 0.5F, 0.5F);
    
        fontRenderer.drawString(text, (int)(x * 2 - textWidth / 2), (int)((y + yAdd) * 2), 0x101010, false);
    
        GlStateManager.popMatrix();
        MekanismRenderer.resetColor();
    }
}
