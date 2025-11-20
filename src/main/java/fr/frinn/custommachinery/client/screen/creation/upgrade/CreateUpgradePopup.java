package fr.frinn.custommachinery.client.screen.creation.upgrade;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.client.screen.BaseScreen;
import fr.frinn.custommachinery.client.screen.popup.InfoPopup;
import fr.frinn.custommachinery.client.screen.popup.PopupScreen;
import fr.frinn.custommachinery.client.screen.widget.ItemSelectionButton;
import fr.frinn.custommachinery.common.machine.MachineLocation;
import fr.frinn.custommachinery.common.network.CAddUpgradePacket;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.toasts.TutorialToast;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public class CreateUpgradePopup extends PopupScreen {

    private Button create;
    private Button cancel;
    private EditBox id;
    private ItemSelectionButton item;
    private CycleButton<MachineLocation.Loader> loader;

    protected CreateUpgradePopup(BaseScreen parent) {
        super(parent, 128, 151);
    }

    public void create() {
        PacketDistributor.sendToServer(new CAddUpgradePacket(this.id.getValue(), this.item.getItem(), this.loader.getValue() == MachineLocation.Loader.KUBEJS));
        this.parent.closePopup(this);
        if(this.loader.getValue() == MachineLocation.Loader.DEFAULT)
            this.parent.openPopup(new InfoPopup(this.parent, 144, 96).text(Component.translatable("custommachinery.gui.creation.upgrade.popup.success.description")));
        else if(this.loader.getValue() == MachineLocation.Loader.KUBEJS)
            Minecraft.getInstance().getTutorial().addTimedToast(new TutorialToast(TutorialToast.Icons.MOUSE, Component.translatable("custommachinery.gui.creation.upgrade.popup.success"), null, false), 50);
    }

    @Override
    protected void init() {
        super.init();
        GridLayout layout = new GridLayout(this.x, this.y).rowSpacing(5);
        GridLayout.RowHelper row = layout.createRowHelper(2);
        LayoutSettings center = row.newCellSettings().alignHorizontallyCenter();

        //Title
        row.addChild(new StringWidget(this.xSize, 10, Component.translatable("custommachinery.gui.creation.upgrade.popup.create"), this.font), 2, row.newCellSettings().alignHorizontallyCenter().paddingTop(5));

        //Id
        this.id = row.addChild(new EditBox(this.font, this.x + 10, this.y + 20, this.xSize - 20, 20, Component.literal("upgrade_id")), 2, center);
        this.id.setFilter(s -> {
            if(s.contains(":"))
                return ResourceLocation.tryParse(s) != null;
            for(char c : s.toCharArray())
                if(!ResourceLocation.validPathChar(c))
                    return false;
            return true;
        });
        this.id.setHint(Component.literal("upgrade_id"));
        this.id.setTooltip(Tooltip.create(Component.translatable("custommachinery.gui.creation.upgrade.popup.id.tooltip")));

        //Item
        this.item = row.addChild(new ItemSelectionButton(this.parent, 0, 0, 20, 20), 2, center);

        //Loader
        CycleButton.Builder<MachineLocation.Loader> builder = CycleButton.builder(MachineLocation.Loader::getTranslatedName).withValues(MachineLocation.Loader.DEFAULT).withInitialValue(MachineLocation.Loader.DEFAULT).displayOnlyValue();
        if(ModList.get().isLoaded("kubejs"))
            builder.withValues(MachineLocation.Loader.DEFAULT, MachineLocation.Loader.KUBEJS).withInitialValue(MachineLocation.Loader.KUBEJS);
        builder.withTooltip(loader -> Tooltip.create(Component.translatable("custommachinery.gui.creation.popup.create.loader." + loader.name().toLowerCase(Locale.ROOT))));
        this.loader = row.addChild(builder.create(0, 0, this.xSize - 20, 20, Component.empty()), 2, center);

        //Create
        this.create = row.addChild(Button.builder(Component.translatable("custommachinery.gui.creation.create").withStyle(ChatFormatting.GREEN), button -> this.create()).bounds(0, 0, 50, 20).build(), center);

        //Cancel
        this.cancel = row.addChild(Button.builder(Component.translatable("custommachinery.gui.popup.cancel").withStyle(ChatFormatting.DARK_RED), button -> this.parent.closePopup(this)).bounds(0, 0, 50, 20).build(), center);

        layout.arrangeElements();
        layout.visitWidgets(this::addRenderableWidget);
        this.ySize = layout.getHeight() + 5;
    }

    @Nullable
    public Component canCreate() {
        if(this.id.getValue().isEmpty())
            return Component.translatable("custommachinery.gui.creation.popup.error.id");
        ResourceLocation id = this.id.getValue().contains(":") ? ResourceLocation.tryParse(this.id.getValue()) : CustomMachinery.rl(this.id.getValue());
        if(id == null)
            return Component.translatable("custommachinery.gui.creation.popup.error.invalid");
        if(CustomMachinery.UPGRADES.getAllUpgrades().keySet().stream().anyMatch(loc -> loc.id().equals(id)))
            return Component.translatable("custommachinery.gui.creation.upgrade.popup.duplicate");
        if(this.item.getItem() == Items.AIR)
            return Component.translatable("custommachinery.gui.creation.upgrade.popup.item");
        return null;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.render(graphics, mouseX, mouseY, partialTicks);
        Component error = this.canCreate();
        this.create.active = error == null;
        this.create.setTooltip(error == null ? null : Tooltip.create(error));
    }
}
