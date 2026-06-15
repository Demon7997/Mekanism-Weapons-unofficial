/*/package mekanism.weapons.client.gui;

import mekanism.api.EnumColor;
import mekanism.client.gui.GuiMekanism;
import mekanism.client.gui.button.GuiDisableableButton;
import mekanism.client.gui.element.GuiElementScreen;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.GuiScrollList;
import mekanism.common.Mekanism;
import mekanism.common.frequency.FrequencyManager;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.weapons.common.inventory.container.ContainerMagnetizer;
import mekanism.weapons.common.item.ItemMagnetizer;
import mekanism.weapons.common.network.MekaWeaponsPacketHandler;
import mekanism.weapons.common.network.PacketMagnetizer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@SideOnly(Side.CLIENT)
public class GuiMagnetizer extends GuiMekanism {

    private EntityPlayer player;
    private EnumHand hand;
    private GuiDisableableButton publicButton;
    private GuiDisableableButton privateButton;
    private GuiDisableableButton setButton;
    private GuiDisableableButton deleteButton;
    private GuiDisableableButton checkboxButton;
    private GuiScrollList scrollList;
    private GuiTextField frequencyField;
    private boolean privateMode;
    private int yStart = 14;
    private List<String> freqNames = new ArrayList<>();
    private ResourceLocation resource;
    private ItemStack heldItem;

    public GuiMagnetizer(EntityPlayer playerIn, EnumHand handIn) {
    super(new ContainerMagnetizer(playerIn.inventory));
    this.player = playerIn; // Assegna il parametro al campo
    this.hand = handIn;     // Assegna il parametro al campo
    this.heldItem = playerIn.getHeldItem(handIn);
        this.heldItem = player.getHeldItem(hand); // INIZIALIZZAZIONE CORRETTA
        
        if (ItemDataUtils.hasData(heldItem, "frequency")) {
            privateMode = !ItemDataUtils.getBoolean(heldItem, "publicFreq");
        }
        this.resource = MekanismUtils.getResource(ResourceType.GUI, "GuiQuantumEntangloporter.png");
        this.ySize = 166;
    }

    @Override
    public void initGui() {
        super.initGui();
        buttonList.clear();
        
        buttonList.add(publicButton = new GuiDisableableButton(0, guiLeft + 27, guiTop + yStart, 60, 20, LangUtils.localize("gui.public")));
        buttonList.add(privateButton = new GuiDisableableButton(1, guiLeft + 89, guiTop + yStart, 60, 20, LangUtils.localize("gui.private")));
        buttonList.add(setButton = new GuiDisableableButton(2, guiLeft + 27, guiTop + yStart + 113, 60, 20, LangUtils.localize("gui.set")));
        buttonList.add(deleteButton = new GuiDisableableButton(3, guiLeft + 89, guiTop + yStart + 113, 60, 20, LangUtils.localize("gui.delete")));
        
        frequencyField = new GuiTextField(4, fontRenderer, 49, 113, 98, 11);
        frequencyField.setMaxStringLength(FrequencyManager.MAX_FREQ_LENGTH);
        frequencyField.setEnableBackgroundDrawing(false);
        frequencyField.setTextColor(0xFFFFFF);
        
        buttonList.add(checkboxButton = new GuiDisableableButton(5, guiLeft + 137, guiTop + yStart + 98, 11, 11).with(GuiDisableableButton.ImageOverlay.CHECKMARK));

        addGuiElement(scrollList = new GuiScrollList(this, resource, 28, 37, 120, 4));
        addGuiElement(new GuiElementScreen(this, resource, 27, 36, 122, 42).isFrame());
        addGuiElement(new GuiInnerScreen(this, resource, 48, 111, 101, 13));

        buttonList.add(new GuiDisableableButton(10, guiLeft + 61, guiTop + 145, 18, 18, "T").with(GuiDisableableButton.ImageOverlay.MAIN));
        buttonList.add(new GuiDisableableButton(11, guiLeft + 81, guiTop + 145, 18, 18, "B").with(GuiDisableableButton.ImageOverlay.MAIN));

        updateButtons();
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        // Spostiamo la chiamata al super dopo i controlli di sicurezza o facciamola con cautela
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);

        // Verifica di sicurezza per fontRenderer
        if (this.fontRenderer == null) {
            return; 
        }

        mekanism.client.render.MekanismRenderer.resetColor();
        String title = "Magnetizer";
        // Usa this.fontRenderer per chiarezza
        this.fontRenderer.drawString(title, (xSize / 2) - (this.fontRenderer.getStringWidth(title) / 2), 5, 0x404040);

        ItemStack itemStack = mc.player.getHeldItem(hand);
        String currentFreq = "None";
        if (ItemDataUtils.hasData(itemStack, "frequency")) {
            currentFreq = mekanism.common.frequency.Frequency.Identity.load(ItemDataUtils.getCompound(itemStack, "frequency")).name;
        }

        this.fontRenderer.drawString("Freq:", 27, 81, 0x404040);
        this.fontRenderer.drawString(" " + (currentFreq.equals("None") ? EnumColor.RED : EnumColor.BRIGHT_GREEN) + currentFreq, 27 + this.fontRenderer.getStringWidth("Freq:"), 81, 0xFFFFFF);
        this.fontRenderer.drawString("Owner: " + EnumColor.BRIGHT_GREEN + mc.player.getName(), 27, 91, 0x404040);
        this.fontRenderer.drawString("Security: " + (privateMode ? EnumColor.DARK_RED + "Private" : EnumColor.BRIGHT_GREEN + "Public"), 27, 101, 0x404040);
        this.fontRenderer.drawString("Set:", 27, 114, 0x404040);

        if (frequencyField != null) {
            frequencyField.drawTextBox();
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float p, int x, int y) {
        mc.renderEngine.bindTexture(resource);
        mekanism.client.render.MekanismRenderer.resetColor();
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
        super.drawGuiContainerBackgroundLayer(p, x, y);
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        frequencyField.updateCursorCounter();
        updateButtons();
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int button) throws IOException {
        super.mouseClicked(mouseX, mouseY, button);
        frequencyField.mouseClicked(mouseX - guiLeft, mouseY - guiTop, button);
        updateButtons();
    }

    @Override
    protected void keyTyped(char c, int i) throws IOException {
        if (frequencyField.isFocused()) {
            if (i == Keyboard.KEY_ESCAPE) {
                frequencyField.setFocused(false);
            } else if (i == Keyboard.KEY_RETURN) {
                setFrequency(frequencyField.getText());
                frequencyField.setText("");
            } else {
                frequencyField.textboxKeyTyped(c, i);
                updateButtons();
            }
        } else {
            super.keyTyped(c, i);
        }
    }

    @Override
    protected void actionPerformed(GuiButton guibutton) throws IOException {
        super.actionPerformed(guibutton);
        if (guibutton.id == 0) privateMode = false;
        else if (guibutton.id == 1) privateMode = true;
        else if (guibutton.id == 2) {
            int selection = scrollList.getSelection();
            if (selection != -1) setFrequency(freqNames.get(selection));
        } else if (guibutton.id == 3) {
            int selection = scrollList.getSelection();
            if (selection != -1) {
                MekaWeaponsPacketHandler.netHandler.sendToServer(new PacketMagnetizer(1, freqNames.get(selection), !privateMode));
                scrollList.clearSelection();
            }
        } else if (guibutton.id == 5) {
            setFrequency(frequencyField.getText());
            frequencyField.setText("");
        }
        else if (guibutton.id == 10) {
            ItemStack stack = player.getHeldItem(hand);
            boolean state = !((ItemMagnetizer)stack.getItem()).isRenderEnabled(stack, "tana");
            MekaWeaponsPacketHandler.netHandler.sendToServer(new PacketMagnetizer("tana", state));
        } else if (guibutton.id == 11) {
            ItemStack stack = player.getHeldItem(hand);
            boolean state = !((ItemMagnetizer)stack.getItem()).isRenderEnabled(stack, "bow");
            MekaWeaponsPacketHandler.netHandler.sendToServer(new PacketMagnetizer("bow", state));
        }
        updateButtons();
    }

    private void setFrequency(String name) {
        if (name.isEmpty()) return;

        ItemStack stack = player.getHeldItem(hand);
        if (!stack.isEmpty() && stack.getItem() instanceof ItemMagnetizer) {
            ItemMagnetizer item = (ItemMagnetizer) stack.getItem();
        
            item.setFrequencyCustom(stack, name, !privateMode);
        }

        MekaWeaponsPacketHandler.netHandler.sendToServer(new PacketMagnetizer(0, name, !privateMode));
    
        updateButtons();
    }

    public void updateButtons() {
        freqNames.clear();
        List<String> text = new ArrayList<>();
        FrequencyManager manager = privateMode ? Mekanism.privateEntangloporters.get(player.getUniqueID()) : Mekanism.publicEntangloporters;
        if (manager != null) {
            manager.getFrequencies().forEach(freq -> {
                freqNames.add(freq.name);
                text.add(freq.name + (privateMode ? "" : " (" + (freq.clientOwner != null ? freq.clientOwner : player.getName()) + ")"));
            });
        }
        scrollList.setText(text);
        if (publicButton != null) publicButton.enabled = privateMode;
        if (privateButton != null) privateButton.enabled = !privateMode;
        
        if (setButton != null) setButton.enabled = scrollList.hasSelection();
        if (deleteButton != null) deleteButton.enabled = scrollList.hasSelection();
        if (checkboxButton != null) checkboxButton.enabled = !frequencyField.getText().isEmpty();
    }

    @Override
    protected ResourceLocation getGuiLocation() {
        return resource;
    }
}
*/
