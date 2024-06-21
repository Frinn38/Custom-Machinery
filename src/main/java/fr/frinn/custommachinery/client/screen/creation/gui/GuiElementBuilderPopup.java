package fr.frinn.custommachinery.client.screen.creation.gui;

import fr.frinn.custommachinery.api.component.IMachineComponentTemplate;
import fr.frinn.custommachinery.api.guielement.IGuiElement;
import fr.frinn.custommachinery.client.screen.BaseScreen;
import fr.frinn.custommachinery.client.screen.creation.MachineEditScreen;
import fr.frinn.custommachinery.client.screen.popup.PopupScreen;
import fr.frinn.custommachinery.client.screen.widget.SuggestedEditBox;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.GridLayout.RowHelper;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public abstract class GuiElementBuilderPopup<T extends IGuiElement> extends PopupScreen {

    public final MutableProperties properties;
    @Nullable
    public final T baseElement;
    private final Consumer<T> onFinish;

    private Button confirm;

    public GuiElementBuilderPopup(BaseScreen parent, MutableProperties properties, @Nullable T from, Consumer<T> onFinish) {
        super(parent, 256, 196);
        this.properties = properties;
        this.baseElement = from;
        this.onFinish = onFinish;
    }

    public abstract T makeElement();

    public abstract void addWidgets(RowHelper row);

    //Empty for allowing creation, error message for denying
    public Component canCreate() {
        return Component.empty();
    }

    private void save() {
        this.onFinish.accept(this.makeElement());
        this.parent.closePopup(this);
    }

    public void addId(RowHelper row) {
        row.addChild(new StringWidget(Component.translatable("custommachinery.gui.creation.gui.id"), this.font));
        SuggestedEditBox id = row.addChild(new SuggestedEditBox(this.font, 0, 0, 100, 20, Component.translatable("custommachinery.gui.creation.gui.id"), 5));
        id.setResponder(this.properties::setId);
        id.setValue(this.properties.getId());
        if(this.parent instanceof MachineEditScreen editScreen)
            id.addSuggestions(editScreen.getBuilder().getComponents().stream().map(IMachineComponentTemplate::getId).filter(s -> !s.isEmpty()).toList());
    }

    public void addPriority(RowHelper row) {
        row.addChild(new StringWidget(Component.translatable("custommachinery.gui.creation.gui.priority"), this.font));
        EditBox priority = row.addChild(new EditBox(this.font, 0, 0, 100, 20, Component.translatable("custommachinery.gui.creation.gui.priority")));
        priority.setResponder(value -> this.properties.setPriority(Integer.parseInt(value)));
        priority.setFilter(value -> {
            try {
                Integer.parseInt(value);
                return true;
            } catch (NumberFormatException ignored) {
                return false;
            }
        });
        priority.setValue("" + this.properties.getPriority());
    }

    public void addTexture(RowHelper row, Component title, Consumer<ResourceLocation> responder, @Nullable ResourceLocation baseTexture) {
        row.addChild(new StringWidget(title, this.font));
        SuggestedEditBox texture = row.addChild(new SuggestedEditBox(this.font, 0, 0, 100, 20, title, 5));
        texture.setMaxLength(Integer.MAX_VALUE);
        texture.setResponder(s -> responder.accept(s.isEmpty() ? null : ResourceLocation.tryParse(s)));
        texture.setValue(baseTexture == null ? "" : baseTexture.toString());
        texture.hideSuggestions();
        texture.addSuggestions(Minecraft.getInstance().getResourceManager().listResources("textures", id -> true).keySet().stream().map(ResourceLocation::toString).toList());
        texture.setFilter(s -> ResourceLocation.tryParse(s) != null);
    }

    @Override
    protected void init() {
        super.init();
        GridLayout layout = new GridLayout(this.x, this.y);
        layout.defaultCellSetting().paddingTop(5).paddingHorizontal(5);
        RowHelper row = layout.createRowHelper(2);
        LayoutSettings center = row.newCellSettings().alignHorizontallyCenter();

        row.addChild(new StringWidget(this.xSize, this.font.lineHeight, Component.translatable("custommachinery.gui.creation.gui.edit"), this.font), 2, center);
        this.addWidgets(row);
        this.confirm = row.addChild(Button.builder(Component.translatable("custommachinery.gui.popup.confirm").withStyle(ChatFormatting.GREEN), button -> this.save()).size(50, 20).build(), center);
        row.addChild(Button.builder(Component.translatable("custommachinery.gui.popup.cancel").withStyle(ChatFormatting.RED), button -> this.parent.closePopup(this)).size(50, 20).build(), center);

        layout.arrangeElements();
        this.ySize = layout.getHeight() + 10;
        layout.visitWidgets(this::addRenderableWidget);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.render(graphics, mouseX, mouseY, partialTicks);
        Component canCreate = this.canCreate();
        if(canCreate.getString().isEmpty())
            this.confirm.active = true;
        else {
            this.confirm.active = false;
            if(this.confirm.isHovered())
                graphics.renderTooltip(this.font, canCreate, mouseX, mouseY);
        }
    }
}
