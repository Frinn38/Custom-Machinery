package fr.frinn.custommachinery.client.screen.creation;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.client.screen.BaseScreen;
import fr.frinn.custommachinery.client.screen.creation.MachineListWidget.MachineEntry;
import fr.frinn.custommachinery.client.screen.widget.ListWidget;
import fr.frinn.custommachinery.common.config.CMConfig;
import fr.frinn.custommachinery.common.init.CustomMachineItem;
import fr.frinn.custommachinery.common.machine.CustomMachine;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;

import java.nio.file.attribute.FileTime;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MachineListWidget extends ListWidget<MachineEntry> {

    private final MachineCreationScreen parent;

    public MachineListWidget(MachineCreationScreen parent, int x, int y, int width, int height, int itemHeight) {
        super(x, y, width, height, itemHeight, Component.empty());
        this.parent = parent;
        this.setRenderSelection();
    }

    public void reload() {
        this.clear();
        CustomMachinery.MACHINES.values().forEach(machine -> this.addEntry(new MachineEntry(machine)));
        this.sort();
    }

    public void sort() {
        this.sort(switch (CMConfig.CONFIG.sortMachineList.get()) {
            case A_Z -> Comparator.comparing(entry -> entry.getMachine().getName().getString());
            case Z_A -> Comparator.<MachineEntry, String>comparing(entry -> entry.getMachine().getName().getString()).reversed();
            case NEWEST -> Comparator.<MachineEntry, FileTime>comparing(entry -> entry.getMachine().getLocation().modified()).reversed();
            case OLDEST -> Comparator.comparing(entry -> entry.getMachine().getLocation().modified());
        });
    }

    public static class MachineEntry extends Entry {

        private final Minecraft mc = Minecraft.getInstance();
        private final CustomMachine machine;

        public MachineEntry(CustomMachine machine) {
            this.machine = machine;
        }

        public CustomMachine getMachine() {
            return this.machine;
        }

        @Override
        public void render(GuiGraphics graphics, int index, int x, int y, int width, int height, int mouseX, int mouseY, float partialTick) {
            //Item
            graphics.renderItem(CustomMachineItem.makeMachineItem(this.machine.getId()), x + 2, y + height / 2 - 8);
            //Name
            graphics.drawString(this.mc.font, this.machine.getName(), x + 20, y + height / 2 - this.mc.font.lineHeight / 2 - 6, 0, false);
            //Id
            BaseScreen.drawScaledString(graphics, this.mc.font, Component.literal(this.machine.getId().toString()).withStyle(ChatFormatting.DARK_GRAY), x + 20, y + height / 2 - this.mc.font.lineHeight / 2 + 2, 0.8f, 0, false);
            //Loader
            BaseScreen.drawScaledString(graphics, this.mc.font, this.machine.getLocation().loader().getTranslatedName().withStyle(ChatFormatting.ITALIC), x + 20, y + height / 2 - this.mc.font.lineHeight / 2 + 9, 0.7f, 0, false);
            //Creation time
            if(this.machine.getLocation().created().toMillis() != 0) {
                String creationTime = new SimpleDateFormat("dd/MM/yy HH:mm").format(this.machine.getLocation().created().toMillis());
                Component creation = Component.translatable("custommachinery.gui.creation.time.created", creationTime).withStyle(ChatFormatting.DARK_GRAY);
                BaseScreen.drawScaledString(graphics, this.mc.font, creation, x + width - this.mc.font.width(creation) / 2 - 10, y + height / 2 - this.mc.font.lineHeight / 2 + 2, 0.5f, 0, false);
            }
            //Modification time
            if(this.machine.getLocation().modified().toMillis() != 0) {
                String modificationTime = new SimpleDateFormat("dd/MM/yy HH:mm").format(this.machine.getLocation().modified().toMillis());
                Component modification = Component.translatable("custommachinery.gui.creation.time.modified", modificationTime).withStyle(ChatFormatting.DARK_GRAY);
                BaseScreen.drawScaledString(graphics, this.mc.font, modification, x + width - this.mc.font.width(modification) / 2 - 11, y + height / 2 - this.mc.font.lineHeight / 2 + 9, 0.5f, 0, false);
            }
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return Collections.emptyList();
        }
    }
}
