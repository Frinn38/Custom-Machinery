package fr.frinn.custommachinery.client.screen.creation.upgrade;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.client.ClientHandler;
import fr.frinn.custommachinery.client.screen.BaseScreen;
import fr.frinn.custommachinery.client.screen.creation.upgrade.UpgradeListWidget.UpgradeEntry;
import fr.frinn.custommachinery.client.screen.widget.ListWidget;
import fr.frinn.custommachinery.common.config.CMConfig;
import fr.frinn.custommachinery.common.machine.CustomMachine;
import fr.frinn.custommachinery.common.upgrade.MachineUpgrade;
import fr.frinn.custommachinery.common.upgrade.UpgradeLocation;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

import java.nio.file.attribute.FileTime;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class UpgradeListWidget extends ListWidget<UpgradeEntry> {

    public String search = "";

    public UpgradeListWidget(int x, int y, int width, int height, int itemHeight) {
        super(x, y, width, height, itemHeight, Component.empty());
        this.setRenderSelection();
    }

    public void setFilterSearch(String search) {
        this.search = search;
    }

    public void reload() {
        this.clear();
        CustomMachinery.UPGRADES.getAllUpgrades().forEach((location, upgrade) -> {
            if(this.search.isEmpty()
                    || location.id().toString().contains(this.search)
                    || upgrade.item().getName(upgrade.item().getDefaultInstance()).getString().contains(this.search)
                    || BuiltInRegistries.ITEM.getKey(upgrade.item()).toString().contains(this.search)
                    || upgrade.machines().stream().anyMatch(id -> id.getPath().contains(this.search)))
                this.addEntry(new UpgradeEntry(location, upgrade));
        });
        this.sort();
    }

    public void sort() {
        this.sort(switch (CMConfig.CONFIG.sortUpgradeList.get()) {
            case ITEM -> Comparator.comparing(entry -> BuiltInRegistries.ITEM.getKey(entry.getUpgrade().item()));
            case MACHINE -> Comparator.comparing(entry -> entry.getUpgrade().machines().isEmpty() ? "zzz" : entry.getUpgrade().machines().getFirst().toString());
            case NEWEST -> Comparator.<UpgradeEntry, FileTime>comparing(entry -> entry.getLocation().modified()).reversed();
            case OLDEST -> Comparator.comparing(entry -> entry.getLocation().modified());
        });
    }

    public static class UpgradeEntry extends Entry {

        private final Minecraft mc = Minecraft.getInstance();
        private final UpgradeLocation location;
        private final MachineUpgrade upgrade;

        public UpgradeEntry(UpgradeLocation location, MachineUpgrade upgrade) {
            this.location = location;
            this.upgrade = upgrade;
        }

        public UpgradeLocation getLocation() {
            return this.location;
        }

        public MachineUpgrade getUpgrade() {
            return this.upgrade;
        }

        @Override
        protected void render(GuiGraphics graphics, int index, int x, int y, int width, int height, int mouseX, int mouseY, float partialTicks) {
            //Item
            graphics.renderItem(this.upgrade.item().getDefaultInstance(), x + 2, y + height / 2 - 8);

            //Machines
            MutableComponent machines = Component.empty();
            for(Iterator<ResourceLocation> iterator = this.upgrade.machines().iterator(); iterator.hasNext();) {
                machines.append(CustomMachinery.MACHINES.getOrDefault(iterator.next(), CustomMachine.DUMMY).getName());
                if(iterator.hasNext())
                    machines.append(", ");
            }
            ClientHandler.renderScrollingStringNoShadow(graphics, this.mc.font, machines, x + 20, x + width - 20, y + height / 2 - this.mc.font.lineHeight / 2 - 6, 0);

            //Id
            BaseScreen.drawScaledString(graphics, this.mc.font, Component.literal(this.location.id().toString()).withStyle(ChatFormatting.DARK_GRAY), x + 20, y + height / 2 - this.mc.font.lineHeight / 2 + 2, 0.8f, 0, false);

            //Loader
            BaseScreen.drawScaledString(graphics, this.mc.font, this.location.loader().getTranslatedName().withStyle(ChatFormatting.ITALIC), x + 20, y + height / 2 - this.mc.font.lineHeight / 2 + 9, 0.7f, 0, false);

            //Creation time
            if(this.location.created().toMillis() != 0) {
                String creationTime = new SimpleDateFormat("dd/MM/yy HH:mm").format(this.location.created().toMillis());
                Component creation = Component.translatable("custommachinery.gui.creation.time.created", creationTime).withStyle(ChatFormatting.DARK_GRAY);
                BaseScreen.drawScaledString(graphics, this.mc.font, creation, x + width - this.mc.font.width(creation) / 2 - 10, y + height / 2 - this.mc.font.lineHeight / 2 + 2, 0.5f, 0, false);
            }

            //Modification time
            if(this.location.modified().toMillis() != 0) {
                String modificationTime = new SimpleDateFormat("dd/MM/yy HH:mm").format(this.location.modified().toMillis());
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
