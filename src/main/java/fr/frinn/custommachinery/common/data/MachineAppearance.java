package fr.frinn.custommachinery.common.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.frinn.custommachinery.client.ClientHandler;
import fr.frinn.custommachinery.common.util.Codecs;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

import java.util.Locale;

public class MachineAppearance {

    public static final ResourceLocation DEFAULT_MODEL = new ResourceLocation("minecraft", "block/missing");
    public static final Block DEFAULT_BLOCK = Blocks.AIR;
    public static final ModelResourceLocation DEFAULT_BLOCKSTATE = new ModelResourceLocation("minecraft:air", "");
    public static final ResourceLocation DEFAULT_ITEM = new ResourceLocation("");
    public static final SoundEvent DEFAULT_SOUND = new SoundEvent(new ResourceLocation(""));
    public static final LightMode DEFAULT_LIGHT_MODE = LightMode.NEVER;
    public static final int DEFAULT_LIGHT_LEVEL = 0;

    @SuppressWarnings("deprecation")
    public static final Codec<MachineAppearance> CODEC = RecordCodecBuilder.create(machineAppearanceCodec ->
            machineAppearanceCodec.group(
                    Codecs.APPEARANCE_TYPE_CODEC.optionalFieldOf("type", AppearanceType.DEFAULT).forGetter(machineAppearance -> machineAppearance.type),
                    ResourceLocation.CODEC.optionalFieldOf("model", DEFAULT_MODEL).forGetter(machineAppearance -> machineAppearance.model),
                    Registry.BLOCK.optionalFieldOf("block", DEFAULT_BLOCK).forGetter(machineAppearance -> machineAppearance.block),
                    Codecs.MODEL_RESOURCE_LOCATION_CODEC.optionalFieldOf("blockstate", DEFAULT_BLOCKSTATE).forGetter(machineAppearance -> machineAppearance.blockstate),
                    ResourceLocation.CODEC.optionalFieldOf("item", DEFAULT_ITEM).forGetter(machineAppearance -> machineAppearance.itemTexture),
                    SoundEvent.CODEC.optionalFieldOf("sound", DEFAULT_SOUND).forGetter(machineAppearance -> machineAppearance.sound),
                    Codecs.LIGHT_MODE_CODEC.optionalFieldOf("lightmode", DEFAULT_LIGHT_MODE).forGetter(machineAppearance -> machineAppearance.lightMode),
                    Codec.INT.optionalFieldOf("lightlevel", DEFAULT_LIGHT_LEVEL).forGetter(machineAppearance -> machineAppearance.lightLevel)
            ).apply(machineAppearanceCodec, MachineAppearance::new)
    );


    public static final MachineAppearance DUMMY = new MachineAppearance(AppearanceType.DEFAULT, DEFAULT_MODEL, DEFAULT_BLOCK, DEFAULT_BLOCKSTATE, DEFAULT_ITEM, DEFAULT_SOUND, DEFAULT_LIGHT_MODE, DEFAULT_LIGHT_LEVEL);

    private AppearanceType type;
    private ResourceLocation model;
    private Block block;
    private ModelResourceLocation blockstate;
    private ResourceLocation itemTexture;
    private SoundEvent sound;
    private LightMode lightMode;
    private int lightLevel;

    public MachineAppearance(AppearanceType type, ResourceLocation model, Block block, ModelResourceLocation state, ResourceLocation itemTexture, SoundEvent sound, LightMode lightMode, int lightLevel) {
        this.type = type;
        this.model = model;
        this.block = block;
        this.blockstate = state;
        this.itemTexture = itemTexture;
        this.sound = sound;
        this.lightMode = lightMode;
        this.lightLevel = MathHelper.clamp(lightLevel, 0, 15);
        if(this.type == AppearanceType.DEFAULT) {
            if (this.blockstate != null && this.blockstate != DEFAULT_BLOCKSTATE)
                this.type = AppearanceType.BLOCKSTATE;
            else if (this.block != null && this.block != DEFAULT_BLOCK)
                this.type = AppearanceType.BLOCK;
            else if (this.model != null && this.model != DEFAULT_MODEL)
                this.type = AppearanceType.MODEL;
        }
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientHandler.setParticleTexture(this));
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

    public SoundEvent getSound() {
        return this.sound;
    }

    public LightMode getLightMode() {
        return this.lightMode;
    }

    public int getLightLevel() {
        return this.lightLevel;
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

        @Override
        public String toString() {
            return super.toString().toLowerCase(Locale.ENGLISH);
        }
    }

    public enum LightMode {
        ALWAYS,
        RUNNING,
        IDLE,
        ERRORED,
        NEVER;

        public static LightMode value(String value) {
            return valueOf(value.toUpperCase(Locale.ENGLISH));
        }

        @Override
        public String toString() {
            return super.toString().toLowerCase(Locale.ENGLISH);
        }
    }
}
