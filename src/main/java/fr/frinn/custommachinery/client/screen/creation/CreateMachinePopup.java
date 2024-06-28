package fr.frinn.custommachinery.client.screen.creation;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.client.screen.BaseScreen;
import fr.frinn.custommachinery.client.screen.popup.InfoPopup;
import fr.frinn.custommachinery.client.screen.popup.PopupScreen;
import fr.frinn.custommachinery.client.screen.widget.ComponentEditBox;
import fr.frinn.custommachinery.common.machine.MachineLocation.Loader;
import fr.frinn.custommachinery.common.network.CAddMachinePacket;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.toasts.TutorialToast;
import net.minecraft.client.gui.components.toasts.TutorialToast.Icons;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Locale;

public class CreateMachinePopup extends PopupScreen {

    private Button create;
    private Button cancel;
    private EditBox id;
    private ComponentEditBox name;
    private CycleButton<Loader> loader;

    protected CreateMachinePopup(BaseScreen parent) {
        super(parent, 128, 121);
    }

    public void create() {
        PacketDistributor.sendToServer(new CAddMachinePacket(this.id.getValue(), this.name.getComponent(), this.loader.getValue() == Loader.KUBEJS));
        this.parent.closePopup(this);
        if(this.loader.getValue() == Loader.DEFAULT)
            this.parent.openPopup(new InfoPopup(this.parent, 144, 96).text(Component.translatable("custommachinery.gui.creation.popup.create.success.description")));
        else if(this.loader.getValue() == Loader.KUBEJS)
            Minecraft.getInstance().getTutorial().addTimedToast(new TutorialToast(Icons.MOUSE, Component.translatable("custommachinery.gui.creation.popup.create.success"), null, false), 50);
    }

    @Override
    protected void init() {
        super.init();
        GridLayout layout = new GridLayout(this.x, this.y).rowSpacing(5);
        GridLayout.RowHelper row = layout.createRowHelper(2);
        LayoutSettings center = row.newCellSettings().alignHorizontallyCenter();
        row.addChild(new StringWidget(this.xSize, 10, Component.translatable("custommachinery.gui.creation.popup.create"), this.font), 2, row.newCellSettings().alignHorizontallyCenter().paddingTop(5));
        this.id = row.addChild(new EditBox(this.font, this.x + 10, this.y + 20, this.xSize - 20, 20, Component.literal("machine_id")), 2, center);
        this.id.setFilter(s -> {
            for(char c : s.toCharArray())
                if(!ResourceLocation.validPathChar(c))
                    return false;
            return true;
        });
        this.id.setHint(Component.literal("machine_id"));
        this.id.setTooltip(Tooltip.create(Component.translatable("custommachinery.gui.creation.popup.create.id.tooltip")));
        this.name = row.addChild(new ComponentEditBox(this.x + 10, this.y + 43, this.xSize - 20, 20, Component.literal("Machine name")), 2, center);
        this.name.setHint(Component.literal("Machine name"));
        this.name.setTooltip(Tooltip.create(Component.translatable("custommachinery.gui.creation.popup.create.name.tooltip")));
        CycleButton.Builder<Loader> builder = CycleButton.builder(Loader::getTranslatedName).withValues(Loader.DEFAULT).withInitialValue(Loader.DEFAULT).displayOnlyValue();
        if(ModList.get().isLoaded("kubejs"))
            builder.withValues(Loader.DEFAULT, Loader.KUBEJS);
        builder.withTooltip(loader -> Tooltip.create(Component.translatable("custommachinery.gui.creation.popup.create.loader." + loader.name().toLowerCase(Locale.ROOT))));
        this.loader = row.addChild(builder.create(0, 0, this.xSize - 20, 20, Component.empty()), 2, center);
        this.create = row.addChild(Button.builder(Component.translatable("custommachinery.gui.creation.create").withStyle(ChatFormatting.GREEN), button -> this.create()).bounds(0, 0, 50, 20).build(), center);
        this.cancel = row.addChild(Button.builder(Component.translatable("custommachinery.gui.popup.cancel").withStyle(ChatFormatting.DARK_RED), button -> this.parent.closePopup(this)).bounds(0, 0, 50, 20).build(), center);
        layout.arrangeElements();
        layout.visitWidgets(this::addRenderableWidget);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.render(graphics, mouseX, mouseY, partialTicks);
        this.create.active = !(this.id.getValue().isEmpty() && ResourceLocation.tryParse(CustomMachinery.MODID + ":" + this.id.getValue()) != null && this.name.getValue().isEmpty());
    }
}
