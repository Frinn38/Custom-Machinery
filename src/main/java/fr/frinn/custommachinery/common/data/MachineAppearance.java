package fr.frinn.custommachinery.common.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.frinn.custommachinery.common.util.ClientUtils;
import fr.frinn.custommachinery.common.util.Utils;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

import java.util.Locale;

public class MachineAppearance {

    private static final Codec<ModelResourceLocation> MODEL_RESOURCE_LOCATION_CODEC = Codec.STRING.comapFlatMap(Utils::decodeModelResourceLocation, ModelResourceLocation::toString).stable();
    private static final Codec<AppearanceType> APPEARANCE_TYPE_CODEC = Codec.STRING.comapFlatMap(Utils::decodeAppearanceType, AppearanceType::toString).stable();

    public static final ResourceLocation DEFAULT_MODEL = new ResourceLocation("minecraft", "block/furnace_on");
    public static final Block DEFAULT_BLOCK = Blocks.FURNACE;
    public static final ModelResourceLocation DEFAULT_BLOCKSTATE = new ModelResourceLocation("minecraft:furnace", "facing=north,lit=true");
    public static final ResourceLocation DEFAULT_ITEM = new ResourceLocation("");

    @SuppressWarnings("deprecation")
    public static final Codec<MachineAppearance> CODEC = RecordCodecBuilder.create(machineAppearanceCodec ->
            machineAppearanceCodec.group(
                    APPEARANCE_TYPE_CODEC.optionalFieldOf("type", AppearanceType.DEFAULT).forGetter(machineAppearance -> machineAppearance.type),
                    ResourceLocation.CODEC.optionalFieldOf("model", DEFAULT_MODEL).forGetter(machineAppearance -> machineAppearance.model),
                    Registry.BLOCK.optionalFieldOf("block", DEFAULT_BLOCK).forGetter(machineAppearance -> machineAppearance.block),
                    MODEL_RESOURCE_LOCATION_CODEC.optionalFieldOf("blockstate", DEFAULT_BLOCKSTATE).forGetter(machineAppearance -> machineAppearance.blockstate),
                    ResourceLocation.CODEC.optionalFieldOf("item", DEFAULT_ITEM).forGetter(machineAppearance -> machineAppearance.itemTexture)
            ).apply(machineAppearanceCodec, MachineAppearance::new)
    );


    public static final MachineAppearance DUMMY = new MachineAppearance(AppearanceType.DEFAULT, DEFAULT_MODEL, DEFAULT_BLOCK, DEFAULT_BLOCKSTATE, DEFAULT_ITEM);

    private AppearanceType type;
    private ResourceLocation model;
    private Block block;
    private ModelResourceLocation blockstate;
    private ResourceLocation itemTexture;

    public MachineAppearance(AppearanceType type, ResourceLocation model, Block block, ModelResourceLocation state, ResourceLocation itemTexture) {
        this.type = type;
        this.model = model;
        this.block = block;
        this.blockstate = state;
        this.itemTexture = itemTexture;
        if(this.type == AppearanceType.DEFAULT) {
            if (this.blockstate != null && this.blockstate != DEFAULT_BLOCKSTATE)
                this.type = AppearanceType.BLOCKSTATE;
            else if (this.block != null && this.block != DEFAULT_BLOCK)
                this.type = AppearanceType.BLOCK;
            else if (this.model != null && this.model != DEFAULT_MODEL)
                this.type = AppearanceType.MODEL;
        }
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientUtils.setParticleTexture(this));
    }

    public ResourceLocation getModel() {
        return this.model;
    }

    public Block getBlock() {
        return this.block;
    }

    public ModelResourceLocation getBlockstate() {
        return this.blockstate;
    }

    public ResourceLocation getItemTexture() {
        return this.itemTexture;
    }

    public AppearanceType getType() {
        return this.type;
    }

    public enum AppearanceType {
        MODEL,
        BLOCK,
        BLOCKSTATE,
        DEFAULT;

        public static AppearanceType value(String value) {
            return valueOf(value.toUpperCase(Locale.ENGLISH));
        }
    }
}
