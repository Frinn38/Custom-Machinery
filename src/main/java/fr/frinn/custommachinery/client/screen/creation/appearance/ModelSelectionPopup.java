package fr.frinn.custommachinery.client.screen.creation.appearance;

import com.mojang.blaze3d.platform.Lighting;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.client.ClientHandler;
import fr.frinn.custommachinery.client.screen.BaseScreen;
import fr.frinn.custommachinery.client.screen.popup.PopupScreen;
import fr.frinn.custommachinery.client.screen.widget.SuggestedEditBox;
import fr.frinn.custommachinery.common.util.MachineModelLocation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.neoforged.neoforge.client.ChunkRenderTypeSet;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ModelSelectionPopup extends PopupScreen {

    private static final WidgetSprites EXIT_SPRITES = new WidgetSprites(CustomMachinery.rl("config/exit_button"), CustomMachinery.rl("config/exit_button_hovered"));

    private final Supplier<MachineModelLocation> supplier;
    private final Consumer<MachineModelLocation> consumer;
    private final boolean isBlock;

    private SuggestedEditBox box;
    private ModelSelectionList selectionList;
    private Checkbox blocks;
    private Checkbox items;
    private Checkbox models;

    public ModelSelectionPopup(BaseScreen parent, Supplier<MachineModelLocation> supplier, Consumer<MachineModelLocation> consumer, boolean isBlock) {
        super(parent, 200, 230);
        this.supplier = supplier;
        this.consumer = consumer;
        this.isBlock = isBlock;
    }

    public void refreshBoxSuggestions() {
        this.box.clearSuggestions();

        List<String> possibleSuggestions = new ArrayList<>();

        if(this.blocks.selected())
            possibleSuggestions.addAll(BuiltInRegistries.BLOCK.keySet().stream().map(ResourceLocation::toString).toList());

        if(this.items.selected())
            possibleSuggestions.addAll(BuiltInRegistries.ITEM.keySet().stream().map(ResourceLocation::toString).toList());

        if(this.models.selected())
            possibleSuggestions.addAll(ClientHandler.getAllModels().keySet().stream().map(ModelResourceLocation::toString).toList());

        this.box.addSuggestions(possibleSuggestions);
        this.sortList(possibleSuggestions);
    }

    public void sortList(List<String> possibleSuggestions) {
        String input = this.box.getValue();
        List<MachineModelLocation> suggestions = possibleSuggestions.stream().sorted(Comparator.comparingInt(s -> {
            if(s.equals(input))
                return -1000;
            else if(s.startsWith(input))
                return -100;
            else if(s.contains(input))
                return -10;
            int matchingChars = 0;
            for(char c : input.toCharArray())
                if(s.contains("" + c))
                    matchingChars++;
            return -matchingChars;
        })).limit(100).map(MachineModelLocation::of).toList();
        this.selectionList.setList(suggestions);
    }

    @Override
    protected void init() {
        super.init();

        //Exit
        this.addRenderableWidget(new ImageButton(this.x + 5, this.y + 5, 9, 9, EXIT_SPRITES, button -> this.parent.closePopup(this)));

        //Buttons
        this.blocks = this.addRenderableWidget(Checkbox.builder(Component.literal("Blocks"), this.font).pos(this.x + 5, this.y + 45).selected(this.isBlock).onValueChange((checkbox, value) -> ModelSelectionPopup.this.refreshBoxSuggestions()).build());
        this.items = this.addRenderableWidget(Checkbox.builder(Component.literal("Items"), this.font).pos(this.x + 65, this.y + 45).selected(!this.isBlock).onValueChange((checkbox, value) -> ModelSelectionPopup.this.refreshBoxSuggestions()).build());
        this.models = this.addRenderableWidget(Checkbox.builder(Component.literal("Models"), this.font).pos(this.x + 125, this.y + 45).selected(false).onValueChange((checkbox, value) -> ModelSelectionPopup.this.refreshBoxSuggestions()).build());

        //Search box
        this.box = this.addRenderableWidget(new SuggestedEditBox(Minecraft.getInstance().font, this.x + 5, this.y + 70, this.xSize - 10, 20, this.title, 5));
        this.box.setMaxLength(Integer.MAX_VALUE);
        this.box.setValue(supplier.get().toString());
        this.box.moveCursorToStart(false);

        //List
        this.selectionList = this.addRenderableWidget(new ModelSelectionList(this.x + 5, this.y + 90, this.xSize - 10, this.ySize - 95));
        this.selectionList.setResponder(this.consumer);
        this.box.setResponder(value -> this.sortList(this.box.getPossibleSuggestions()));
        this.refreshBoxSuggestions();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.render(graphics, mouseX, mouseY, partialTicks);
        renderModel(graphics, this.x + this.xSize / 2F - 8, this.y + 20, this.supplier.get(), 32F);
    }

    public static void renderModel(GuiGraphics graphics, float x, float y, MachineModelLocation loc, float scale) {
        BakedModel model = Minecraft.getInstance().getModelManager().getMissingModel();
        if(loc.getState() != null)
            model = Minecraft.getInstance().getBlockRenderer().getBlockModel(loc.getState());
        else if(loc.getItem() != null)
            model = Minecraft.getInstance().getItemRenderer().getModel(loc.getItem().getDefaultInstance(), Minecraft.getInstance().level, Minecraft.getInstance().player, 42);
        else if(loc.getLoc() != null && loc.getProperties() != null)
            model = Minecraft.getInstance().getModelManager().getModel(new ModelResourceLocation(loc.getLoc(), loc.getProperties()));
        else if(loc.getLoc() != null)
            model = ClientHandler.getAllModels().getOrDefault(ModelResourceLocation.standalone(loc.getLoc()), model);

        ChunkRenderTypeSet renderTypes = ChunkRenderTypeSet.all();

        graphics.pose().pushPose();
        graphics.pose().translate(x, y, 50);
        graphics.pose().scale(scale, scale, scale);
        model.applyTransform(ItemDisplayContext.GUI, graphics.pose(), false);
        if(loc.getState() != null) {
            graphics.pose().mulPose(new Quaternionf().fromAxisAngleDeg(0, 1, 0, 270));
            graphics.pose().mulPose(new Quaternionf().fromAxisAngleDeg(1, 0, 0, 180));
            renderTypes = model.getRenderTypes(loc.getState(), RandomSource.create(42L), ModelData.EMPTY);
            Lighting.setupFor3DItems();
        }
        graphics.pose().translate(-0.5F, -0.5F, -0.5);
        for(RenderType renderType : renderTypes)
            Minecraft.getInstance().getBlockRenderer().getModelRenderer().renderModel(graphics.pose().last(), graphics.bufferSource().getBuffer(renderType), loc.getState(), model, 1, 1, 1, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, ModelData.EMPTY, renderType);
        if(loc.getState() != null)
            Lighting.setupForFlatItems();
        graphics.pose().popPose();
    }

    public static class ModelSelectionList extends AbstractWidget {

        private final List<MachineModelLocation> list = new ArrayList<>();
        private final int maxColumns;

        private Consumer<MachineModelLocation> responder;
        private MachineModelLocation selected;
        private double scrollAmount;
        private boolean scrolling = false;

        public ModelSelectionList(int x, int y, int width, int height) {
            super(x, y, width, height, Component.empty());
            this.maxColumns = width / 20;
        }

        public void setList(List<MachineModelLocation> list) {
            this.list.clear();
            this.list.addAll(list);
        }

        public void setResponder(Consumer<MachineModelLocation> responder) {
            this.responder = responder;
        }

        @Nullable
        public MachineModelLocation getElementUnderMouse(double mouseX, double mouseY) {
            if(mouseX < this.getX() || mouseX > this.getX() + this.maxColumns * 20 || mouseY < this.getY() || mouseY > this.getY() + this.getHeight())
                return null;
            int index = (int)((mouseY - this.getY() + this.scrollAmount) / 20) * this.maxColumns + (int)((mouseX - this.getX()) / 20);
            if(index < 0 || index >= this.list.size())
                return null;
            return this.list.get(index);
        }

        private void scroll(int scroll) {
            this.setScrollAmount(this.getScrollAmount() + (double)scroll);
        }

        public double getScrollAmount() {
            return this.scrollAmount;
        }

        public void setScrollAmount(double scroll) {
            this.scrollAmount = Mth.clamp(scroll, 0.0, this.getMaxScroll());
        }

        public int getMaxScroll() {
            return Math.max(0, this.getMaxPosition() - this.getHeight() - 4);
        }

        protected int getMaxPosition() {
            return this.list.size() / this.maxColumns * 20;
        }

        public int getScrollBottom() {
            return (int)this.getScrollAmount() - this.getHeight();
        }

        protected void updateScrollingState(double mouseX, double mouseY, int button) {
            this.scrolling = button == 0 && mouseX >= this.getScrollbarPosition() && mouseX < this.getScrollbarPosition() + 6;
        }

        protected int getScrollbarPosition() {
            return this.width / 2 + 124;
        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            graphics.enableScissor(this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + this.getHeight());
            graphics.pose().pushPose();
            graphics.pose().translate(this.getX(), this.getY(), 0);
            for(int i = 0; i < this.list.size(); i++) {
                int x = (i % this.maxColumns) * 20;
                int y = i / this.maxColumns * 20 - (int)this.getScrollAmount();
                graphics.pose().pushPose();
                graphics.pose().translate(x, y, 100);

                MachineModelLocation loc = this.list.get(i);

                if(this.selected == loc)
                    graphics.fill(0, 0, 20, 20, FastColor.ARGB32.color(255, 255, 0, 0));

                renderModel(graphics, 10, 10, loc, 16F);

                graphics.pose().popPose();
            }
            graphics.pose().popPose();

            if(this.getMaxScroll() > 0) {
                int i = this.getX() + this.getWidth() - 10;
                int j = i + 6;
                int n = (this.getHeight() * this.getHeight()) / this.getMaxPosition();
                n = Mth.clamp(n, 32, this.getHeight() - 8);
                int o = (int)this.getScrollAmount() * (this.getHeight() - n) / this.getMaxScroll() + this.getY();
                if (o < this.getY()) {
                    o = this.getY();
                }
                graphics.fill(i, this.getY(), j, this.getY() + this.getHeight(), -16777216);
                graphics.fill(i, o, j, o + n, -8355712);
                graphics.fill(i, o, j - 1, o + n - 1, -4144960);
            }

            graphics.disableScissor();

            MachineModelLocation hovered = this.getElementUnderMouse(mouseX, mouseY);
            if(hovered != null)
                graphics.renderTooltip(Minecraft.getInstance().font, Component.literal(hovered.toString()), mouseX, mouseY);
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            this.updateScrollingState(mouseX, mouseY, button);
            MachineModelLocation selected = this.getElementUnderMouse(mouseX, mouseY);
            if(selected != null) {
                this.selected = selected;
                this.responder.accept(selected);
                return true;
            }
            return super.mouseClicked(mouseX, mouseY, button);
        }

        @Override
        public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
            if(super.mouseDragged(mouseX, mouseY, button, dragX, dragY))
                return true;
            if (button != 0 || !this.scrolling)
                return false;
            if (mouseY < this.getY()) {
                this.setScrollAmount(0.0);
            } else if (mouseY > this.getY() + this.getHeight()) {
                this.setScrollAmount(this.getMaxScroll());
            } else {
                double d = Math.max(1, this.getMaxScroll());
                int i = this.getHeight();
                int j = Mth.clamp((int)((float)(i * i) / (float)this.getMaxPosition()), 32, i - 8);
                double e = Math.max(1.0, d / (double)(i - j));
                this.setScrollAmount(this.getScrollAmount() + dragY * e);
            }
            return true;
        }

        @Override
        public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
            this.setScrollAmount(this.getScrollAmount() - scrollY * 10);
            return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
        }
    }
}
