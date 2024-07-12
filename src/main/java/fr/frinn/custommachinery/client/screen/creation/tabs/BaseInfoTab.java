package fr.frinn.custommachinery.client.screen.creation.tabs;

import com.google.common.collect.ImmutableList;
import fr.frinn.custommachinery.api.crafting.ProcessorType;
import fr.frinn.custommachinery.client.screen.creation.MachineEditScreen;
import fr.frinn.custommachinery.client.screen.widget.ComponentEditBox;
import fr.frinn.custommachinery.client.screen.widget.IntegerSlider;
import fr.frinn.custommachinery.common.crafting.craft.CraftProcessor;
import fr.frinn.custommachinery.common.crafting.machine.MachineProcessor.Template;
import fr.frinn.custommachinery.common.init.Registration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.GridLayout.RowHelper;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class BaseInfoTab extends MachineEditTab {

    private final List<AbstractWidget> processorWidgets = new ArrayList<>();
    private int coresAmount = 1;
    private int recipeCheckCooldown = 20;

    public BaseInfoTab(MachineEditScreen parent) {
        super(Component.translatable("custommachinery.gui.creation.tab.base_info"), parent);
        final Font font = this.parent.mc.font;

        if(this.parent.getBuilder().getProcessor() instanceof Template template) {
            this.coresAmount = template.amount();
            this.recipeCheckCooldown = template.recipeCheckCooldown();
        }

        //Each row must be the same amount of columns as defined here
        RowHelper row = this.layout.rowSpacing(8).createRowHelper(2);
        row.defaultCellSetting().paddingHorizontal(0);
        LayoutSettings middle = row.newCellSettings().alignVerticallyMiddle();
        LayoutSettings right = row.newCellSettings().alignHorizontallyRight();

        //Id (1rst row)
        row.addChild(new StringWidget(Component.translatable("custommachinery.gui.creation.base_info.id").append(Component.literal(this.parent.getBuilder().getLocation().getId().toString())), Minecraft.getInstance().font), 2, row.newCellSettings().alignHorizontallyCenter());

        //Name (2nd row)
        row.addChild(new StringWidget(Component.translatable("custommachinery.gui.creation.base_info.name"), font), middle);
        ComponentEditBox nameEdit = new ComponentEditBox(0, 0, 100, 20, Component.literal("name"));
        nameEdit.setHint(Component.literal("name"));
        nameEdit.setComponent(this.parent.getBuilder().getName());
        nameEdit.setComponentResponder(name -> {
            this.parent.setChanged();
            this.parent.getBuilder().setName(name);
        });
        row.addChild(nameEdit, right);

        //Processor (3rd row)
        row.addChild(new StringWidget(Component.translatable("custommachinery.gui.creation.base_info.processor"), font), middle);
        row.addChild(CycleButton.<ProcessorType<?>>builder(processor -> Component.literal(processor.getId().getPath()))
                .withValues(ImmutableList.copyOf(Registration.PROCESSOR_REGISTRY))
                .withInitialValue(Registration.MACHINE_PROCESSOR.get())
                .displayOnlyValue()
                .create(0, 0, 100, 20, Component.literal("Machine processor"), (button, processor) -> {
                    if(processor == this.parent.getBuilder().getProcessor().getType())
                        return;
                    if(processor == Registration.MACHINE_PROCESSOR.get())
                        this.parent.getBuilder().setProcessor(new Template(this.coresAmount, this.recipeCheckCooldown));
                    else if(processor == Registration.CRAFT_PROCESSOR.get())
                        this.parent.getBuilder().setProcessor(CraftProcessor.Template.DEFAULT);
                    this.parent.setChanged();
                    boolean visible = processor == Registration.MACHINE_PROCESSOR.get();
                    this.processorWidgets.forEach(widget -> widget.visible = visible);
                }),
                right
        );

        //Cores (4th row)
        this.processorWidgets.add(row.addChild(new StringWidget(Component.translatable("custommachinery.gui.creation.base_info.cores"), font), middle));
        IntegerSlider cores = row.addChild(IntegerSlider.builder().bounds(1, 16).defaultValue(this.coresAmount).setResponder(amount -> {
            if(this.parent.getBuilder().getProcessor() instanceof Template template) {
                this.coresAmount = amount;
                this.parent.getBuilder().setProcessor(new Template(amount, template.recipeCheckCooldown()));
                this.parent.setChanged();
            }
        }).displayOnlyValue().create(0, 0, 100, 20, Component.empty()), right);
        cores.setTooltip(Tooltip.create(Component.translatable("custommachinery.gui.creation.base_info.cores.tooltip")));
        this.processorWidgets.add(cores);

        //Check cooldown (5th row)
        this.processorWidgets.add(row.addChild(new StringWidget(Component.translatable("custommachinery.gui.creation.base_info.cooldown"), font), middle));
        IntegerSlider cooldown = row.addChild(IntegerSlider.builder().bounds(0, 200).defaultValue(this.recipeCheckCooldown).setResponder(recipeCheckCooldown -> {
            if(this.parent.getBuilder().getProcessor() instanceof  Template template) {
                this.recipeCheckCooldown = recipeCheckCooldown;
                this.parent.getBuilder().setProcessor(new Template(template.amount(), recipeCheckCooldown));
                this.parent.setChanged();
            }
        }).displayOnlyValue().create(0, 0, 100, 20, Component.empty()), right);
        cooldown.setTooltip(Tooltip.create(Component.translatable("custommachinery.gui.creation.base_info.cooldown.tooltip")));
        this.processorWidgets.add(cooldown);
    }
}
