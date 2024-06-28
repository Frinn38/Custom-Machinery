package fr.frinn.custommachinery.client.screen.creation.tabs;

import com.google.common.collect.ImmutableList;
import fr.frinn.custommachinery.api.crafting.ProcessorType;
import fr.frinn.custommachinery.client.screen.creation.MachineEditScreen;
import fr.frinn.custommachinery.client.screen.widget.ComponentEditBox;
import fr.frinn.custommachinery.common.init.Registration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.GridLayout.RowHelper;
import net.minecraft.network.chat.Component;

public class BaseInfoTab extends MachineEditTab {

    public BaseInfoTab(MachineEditScreen parent) {
        super(Component.translatable("custommachinery.gui.creation.tab.base_info"), parent);
        final Font font = this.parent.mc.font;

        //Each row must be the same amount of columns as defined here
        RowHelper row = this.layout.rowSpacing(8).createRowHelper(2);
        row.defaultCellSetting().paddingHorizontal(0);

        //Id (1rst row)
        row.addChild(new StringWidget(Component.translatable("custommachinery.gui.creation.base_info.id").append(Component.literal(this.parent.getBuilder().getLocation().getId().toString())), Minecraft.getInstance().font), 2, row.newCellSettings().alignHorizontallyCenter());

        //Name (2nd row)
        row.addChild(new StringWidget(Component.translatable("custommachinery.gui.creation.base_info.name"), font), row.newCellSettings().alignVerticallyMiddle());
        ComponentEditBox nameEdit = new ComponentEditBox(0, 0, 100, 20, Component.literal("name"));
        nameEdit.setHint(Component.literal("name"));
        nameEdit.setComponent(this.parent.getBuilder().getName());
        nameEdit.setComponentResponder(name -> {
            this.parent.setChanged();
            this.parent.getBuilder().setName(name);
        });
        row.addChild(nameEdit);

        //Processor (3rd row)
        row.addChild(new StringWidget(Component.translatable("custommachinery.gui.creation.base_info.processor"), font), row.newCellSettings().alignVerticallyMiddle());
        row.addChild(CycleButton.<ProcessorType<?>>builder(processor -> Component.literal(processor.getId().getPath()))
                .withValues(ImmutableList.copyOf(Registration.PROCESSOR_REGISTRY))
                .withInitialValue(Registration.MACHINE_PROCESSOR.get())
                .displayOnlyValue()
                .create(0, 0, 100, 20, Component.literal("Machine processor"), (button, processor) -> {
                    this.parent.getBuilder().setProcessor(processor);
                    this.parent.setChanged();
                }),
                row.newCellSettings().alignHorizontallyRight()
        );
    }
}
