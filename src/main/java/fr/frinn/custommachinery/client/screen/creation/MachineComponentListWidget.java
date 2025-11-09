package fr.frinn.custommachinery.client.screen.creation;

import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import fr.frinn.custommachinery.api.component.IMachineComponentTemplate;
import fr.frinn.custommachinery.client.screen.creation.MachineComponentListWidget.MachineComponentEntry;
import fr.frinn.custommachinery.client.screen.creation.component.IMachineComponentBuilder;
import fr.frinn.custommachinery.client.screen.creation.component.MachineComponentBuilderRegistry;
import fr.frinn.custommachinery.client.screen.creation.tabs.ComponentTab;
import fr.frinn.custommachinery.client.screen.widget.ListWidget;
import fr.frinn.custommachinery.common.machine.builder.CustomMachineBuilder;
import fr.frinn.custommachinery.common.util.Utils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class MachineComponentListWidget extends ListWidget<MachineComponentEntry> {

    private final ComponentTab tab;

    public MachineComponentListWidget(int x, int y, int width, int height, int itemHeight, ComponentTab tab) {
        super(x, y, width, height, itemHeight, Component.empty());
        this.tab = tab;
        this.setRenderSelection();
    }

    public void setup(CustomMachineBuilder builder) {
        this.clear();
        for(IMachineComponentTemplate<?> template : builder.getComponents()) {
            IMachineComponentBuilder<?, ?> componentBuilder = MachineComponentBuilderRegistry.getBuilder(template.getType());
            if(componentBuilder != null)
                this.addEntry(new MachineComponentEntry(template, componentBuilder));
        }
        this.sort(Comparator.comparing(entry -> entry.getTemplate().getType().getId().toString() + ":" + entry.getTemplate().getId()));
        this.tab.setupButtons();
    }

    @Override
    public void setSelected(@Nullable MachineComponentListWidget.MachineComponentEntry selected) {
        super.setSelected(selected);
        this.tab.setupButtons();
    }

    @Override
    public void addEntry(MachineComponentEntry entry) {
        super.addEntry(entry);
    }

    public class MachineComponentEntry extends Entry {

        private IMachineComponentTemplate<?> template;
        private IMachineComponentBuilder<?, ?> builder;

        public MachineComponentEntry(IMachineComponentTemplate<?> template, IMachineComponentBuilder<?, ?> builder) {
            this.template = template;
            this.builder = builder;
        }

        public IMachineComponentTemplate<?> getTemplate() {
            return this.template;
        }

        public void setTemplate(IMachineComponentTemplate<?> template) {
            IMachineComponentBuilder<?, ?> builder = MachineComponentBuilderRegistry.getBuilder(template.getType());
            if(builder != null) {
                MachineComponentListWidget.this.tab.parent.getBuilder().getComponents().remove(this.template);
                this.template = template;
                this.builder = builder;
                MachineComponentListWidget.this.tab.parent.getBuilder().getComponents().add(this.template);
            }
        }

        public MachineComponentEntry copy() {
            return IMachineComponentTemplate.CODEC.encodeStart(JsonOps.INSTANCE, this.template).mapOrElse(json -> {
                if(json instanceof JsonObject templateJson && templateJson.has("id") && templateJson.get("id").isJsonPrimitive() && templateJson.getAsJsonPrimitive("id").isString()) {
                    String id = templateJson.get("id").getAsString();
                    AtomicReference<String> copyId = new AtomicReference<>(Utils.incrementLastNumber(id));
                    //Check if there isn't another component with this id.
                    while(MachineComponentListWidget.this.getEntries().stream().anyMatch(entry -> {
                        if(entry == this || entry.builder.type() != this.builder.type())
                            return false;
                        return entry.getTemplate().getId().equals(copyId.get());
                    })) {
                        copyId.set(Utils.incrementLastNumber(copyId.get()));
                    }
                    templateJson.addProperty("id", copyId.get());
                    IMachineComponentTemplate<?> copyTemplate = IMachineComponentTemplate.CODEC.read(JsonOps.INSTANCE, templateJson).getOrThrow();
                    return new MachineComponentEntry(copyTemplate, MachineComponentBuilderRegistry.getBuilder(copyTemplate.getType()));
                }
                throw new IllegalStateException("Trying to copy machine component without id: " + json);
            }, error -> {throw new IllegalStateException("Error while encoding machine component to json to copy: " + error.message());});
        }

        @SuppressWarnings({"unchecked", "rawtypes"})
        @Override
        public void render(GuiGraphics graphics, int index, int x, int y, int width, int height, int mouseX, int mouseY, float partialTick) {
            ((IMachineComponentBuilder)this.builder).render(graphics, x, y, width, height, this.template);
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return Collections.emptyList();
        }
    }
}
