package fr.frinn.custommachinery.client.screen;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import fr.frinn.custommachinery.client.screen.widget.EnumButton;
import fr.frinn.custommachinery.client.screen.widget.ToogleTextFieldWidget;
import fr.frinn.custommachinery.common.data.MachineAppearance;
import fr.frinn.custommachinery.common.data.builder.CustomMachineBuilder;
import fr.frinn.custommachinery.common.init.CustomMachineItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Arrays;
import java.util.Locale;

public class MachineAppearanceScreen extends MachineCreationTabScreen {

    private EnumButton<MachineAppearance.AppearanceType> appearanceTypeButton;
    private ToogleTextFieldWidget modelPrompt;
    private ToogleTextFieldWidget blockPrompt;
    private ToogleTextFieldWidget blockStatePrompt;

    public MachineAppearanceScreen(MachineCreationScreen parent, CustomMachineBuilder machine) {
        super(new StringTextComponent("Machine Appearance"), parent, machine);
    }

    @Override
    protected void init() {
        super.init();
        this.appearanceTypeButton = this.addButton(new EnumButton<>(
                this.xPos + 128 - 75,
                this.yPos + 10,
                150,
                20,
                (button) -> this.machine.getAppearance().setType(((EnumButton<MachineAppearance.AppearanceType>)button).getValue()),
                (button, matrix, mouseX, mouseY) -> this.renderTooltip(matrix, mouseX, mouseY),
                (value) -> new TranslationTextComponent("custommachinery.gui.machineappearance.appearance").appendString(" : ").appendSibling(new TranslationTextComponent("custommachinery.gui.machineappearance.type." + value.toString().toLowerCase(Locale.ENGLISH))),
                Arrays.asList(MachineAppearance.AppearanceType.values()),
                this.machine.getAppearance().getType()
        ));
        this.modelPrompt = this.addListener(new ToogleTextFieldWidget(
                this.font,
                this.xPos + 128 - 75,
                this.yPos + 35,
                150,
                20,
                StringTextComponent.EMPTY,
                widget -> this.machine.getAppearance().getType() == MachineAppearance.AppearanceType.MODEL
        ));
        this.modelPrompt.setMaxStringLength(100);
        this.modelPrompt.setText(this.machine.getAppearance().getModel() == null ? "" : this.machine.getAppearance().getModel().toString());
        this.modelPrompt.setSelectionPos(0);
        this.modelPrompt.setResponder(s -> this.machine.getAppearance().setModel(new ResourceLocation(s)));
        this.blockPrompt = this.addListener(new ToogleTextFieldWidget(
                this.font,
                this.xPos + 128 - 75,
                this.yPos + 35,
                150,
                20,
                StringTextComponent.EMPTY,
                widget -> this.machine.getAppearance().getType() == MachineAppearance.AppearanceType.BLOCK
        ));
        this.blockPrompt.setMaxStringLength(100);
        this.blockPrompt.setText(this.machine.getAppearance().getBlock() == null ? "" : this.machine.getAppearance().getBlock().getRegistryName().toString());
        this.blockPrompt.setSelectionPos(0);
        this.blockPrompt.setResponder(s -> this.machine.getAppearance().setBlock(ForgeRegistries.BLOCKS.getValue(new ResourceLocation(s))));
        this.blockStatePrompt = this.addListener(new ToogleTextFieldWidget(
                this.font,
                this.xPos + 128 - 75,
                this.yPos + 35,
                150,
                20,
                StringTextComponent.EMPTY,
                widget -> this.machine.getAppearance().getType() == MachineAppearance.AppearanceType.BLOCKSTATE
        ));
        this.blockStatePrompt.setMaxStringLength(100);
        this.blockStatePrompt.setText(this.machine.getAppearance().getBlockState() == null ? "" : this.machine.getAppearance().getBlockState().toString());
        this.blockStatePrompt.setSelectionPos(0);
        this.blockStatePrompt.setResponder(s -> this.machine.getAppearance().setBlockState(new ModelResourceLocation(s)));
    }

    @SuppressWarnings("deprecation")
    @ParametersAreNonnullByDefault
    @Override
    public void render(MatrixStack matrix, int mouseX, int mouseY, float partialTicks) {
        switch (this.machine.getAppearance().getType()) {
            case MODEL:
                this.font.drawString(matrix, new TranslationTextComponent("custommachinery.gui.machineappearance.type.model").getString() + ":", this.xPos + 8, this.yPos + 40, 0);
                break;
            case BLOCK:
                this.font.drawString(matrix, new TranslationTextComponent("custommachinery.gui.machineappearance.type.block").getString() + ":", this.xPos + 8, this.yPos + 40, 0);
                break;
            case BLOCKSTATE:
                matrix.push();
                matrix.translate(this.xPos + 5, this.yPos + 41, 0);
                matrix.scale(0.85F, 0.85F, 0.85F);
                this.font.drawString(matrix, new TranslationTextComponent("custommachinery.gui.machineappearance.type.blockstate").getString() + ":", 0, 0, 0);
                matrix.pop();
                break;
        }

        this.modelPrompt.render(matrix, mouseX, mouseY, partialTicks);
        this.blockPrompt.render(matrix, mouseX, mouseY, partialTicks);
        this.blockStatePrompt.render(matrix, mouseX, mouseY, partialTicks);

        ItemStack item = CustomMachineItem.makeMachineItem(this.machine.getLocation().getId());

        int scale = 2;
        int x = this.xPos + 128 - 8 * scale;
        int y = this.yPos + 83 - 8 * scale;

        RenderSystem.translated(x, y, 0);
        RenderSystem.scaled(scale, scale, scale);
        Minecraft.getInstance().getItemRenderer().renderItemAndEffectIntoGUI(item, 0, 0);
        RenderSystem.scaled(1 / (double)scale, 1 / (double)scale, 1 / (double)scale);
        RenderSystem.translated(-x, -y, 0);

        super.render(matrix, mouseX, mouseY, partialTicks);
    }

    private void renderTooltip(MatrixStack matrix, int mouseX, int mouseY) {

    }
}
