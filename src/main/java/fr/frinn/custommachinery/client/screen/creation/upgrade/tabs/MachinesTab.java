package fr.frinn.custommachinery.client.screen.creation.upgrade.tabs;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.client.screen.creation.upgrade.UpgradeEditScreen;
import fr.frinn.custommachinery.client.screen.widget.GridListWidget;
import fr.frinn.custommachinery.common.init.CustomMachineItem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class MachinesTab extends UpgradeEditTab {

    public MachinesTab(UpgradeEditScreen parent) {
        super(Component.translatable("custommachinery.gui.creation.upgrade.tab.machines"), parent);

        //Each row must be the same amount of columns as defined here
        GridLayout.RowHelper row = this.layout.rowSpacing(8).createRowHelper(1);
        row.defaultCellSetting().paddingHorizontal(0).paddingTop(10);
        LayoutSettings center = row.newCellSettings().alignHorizontallyCenter();

        //Title
        row.addChild(new StringWidget(Component.translatable("custommachinery.gui.creation.upgrade.machines.title"), Minecraft.getInstance().font), center);

        //List
        MachineSelectionList list = row.addChild(new MachineSelectionList(0, 0, 250, 200, id -> {
            if(parent.getBuilder().getMachines().contains(id))
                parent.getBuilder().getMachines().remove(id);
            else
                parent.getBuilder().getMachines().add(id);
            parent.setChanged();
        }), center);
        list.getAll().forEach(entry -> {
            if(parent.getBuilder().getMachines().contains(entry.machine))
                entry.selected = true;
        });
    }

    private static class MachineSelectionList extends GridListWidget<MachineSelectionList.MachineEntry> {

        public MachineSelectionList(int x, int y, int width, int height, Consumer<ResourceLocation> onClick) {
            super(x, y, width, height);
            CustomMachinery.MACHINES.keySet().forEach(id -> this.addEntry(new MachineEntry(id, onClick)));
        }

        private static class MachineEntry extends Entry {

            private static final Component SELECT = Component.translatable("custommachinery.gui.creation.upgrade.machines.select").withStyle(ChatFormatting.DARK_RED);
            private static final Component UNSELECT = Component.translatable("custommachinery.gui.creation.upgrade.machines.unselect").withStyle(ChatFormatting.DARK_RED);

            private final ResourceLocation machine;
            private final ItemStack machineStack;
            private final Consumer<ResourceLocation> onClick;
            private boolean selected = false;

            private MachineEntry(ResourceLocation machine, Consumer<ResourceLocation> onClick) {
                this.machine = machine;
                this.machineStack = CustomMachineItem.makeMachineItem(machine);
                this.onClick = onClick;
            }

            @Override
            public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
                if(this.selected)
                    graphics.fill(0, 0, 18, 18, FastColor.ARGB32.color(255, 255, 0, 0));
                graphics.renderItem(this.machineStack, 1, 1);
            }

            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                this.selected = !this.selected;
                this.onClick.accept(this.machine);
                return true;
            }

            @Override
            public List<Component> getTooltips() {
                List<Component> tooltips = this.machineStack.getTooltipLines(Item.TooltipContext.EMPTY, Minecraft.getInstance().player, TooltipFlag.NORMAL);
                if(this.selected)
                    tooltips.add(UNSELECT);
                else
                    tooltips.add(SELECT);
                return tooltips;
            }

            @Override
            public List<? extends GuiEventListener> children() {
                return Collections.emptyList();
            }
        }
    }
}
