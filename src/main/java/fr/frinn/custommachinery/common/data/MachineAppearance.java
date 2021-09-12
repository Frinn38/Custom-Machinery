package fr.frinn.custommachinery.common.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.api.machine.IMachineAppearance;
import fr.frinn.custommachinery.api.utils.CodecLogger;
import fr.frinn.custommachinery.common.util.Codecs;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.MathHelper;

public class MachineAppearance implements IMachineAppearance {

    public static final ResourceLocation DEFAULT_MODEL = new ResourceLocation(CustomMachinery.MODID, "block/custom_machine_block");
    public static final SoundEvent DEFAULT_SOUND = new SoundEvent(new ResourceLocation(""));
    public static final int DEFAULT_LIGHT_LEVEL = 0;
    public static final int DEFAULT_COLOR = 0xFFFFFF;

    public static final Codec<MachineAppearance> CODEC = RecordCodecBuilder.create(machineAppearanceCodec ->
            machineAppearanceCodec.group(
                    CodecLogger.loggedOptional(Codecs.BLOCK_MODEL_CODEC,"block", DEFAULT_MODEL).forGetter(appearance -> appearance.blockModel),
                    CodecLogger.loggedOptional(Codecs.ITEM_MODEL_CODEC,"item", DEFAULT_MODEL).forGetter(appearance -> appearance.itemModel),
                    CodecLogger.loggedOptional(SoundEvent.CODEC,"sound", DEFAULT_SOUND).forGetter(appearance -> appearance.sound),
                    CodecLogger.loggedOptional(Codec.INT,"lightlevel", DEFAULT_LIGHT_LEVEL).forGetter(appearance -> appearance.lightLevel),
                    CodecLogger.loggedOptional(Codec.INT,"color", DEFAULT_COLOR).forGetter(appearance -> appearance.color)
            ).apply(machineAppearanceCodec, MachineAppearance::new)
    );


    public static final MachineAppearance DEFAULT = new MachineAppearance(DEFAULT_MODEL, DEFAULT_MODEL, DEFAULT_SOUND, DEFAULT_LIGHT_LEVEL, DEFAULT_COLOR);

    private ResourceLocation blockModel;
    private ResourceLocation itemModel;
    private SoundEvent sound;
    private int lightLevel;
    private int color;

    public MachineAppearance(ResourceLocation blockModel, ResourceLocation itemModel, SoundEvent sound, int lightLevel, int color) {
        this.blockModel = blockModel;
        if(blockModel != DEFAULT_MODEL && itemModel == DEFAULT_MODEL)
            this.itemModel = new ResourceLocation(blockModel.getNamespace(), blockModel.getPath());
        else
            this.itemModel = itemModel;
        this.sound = sound;
        this.lightLevel = MathHelper.clamp(lightLevel, 0, 15);
        this.color = color;
    }

    @Override
    public ResourceLocation getBlockModel() {
        return this.blockModel;
    }

    @Override
    public ResourceLocation getItemModel() {
        return this.itemModel;
    }

    @Override
    public SoundEvent getSound() {
        return this.sound;
    }

    @Override
    public int getLightLevel() {
        return this.lightLevel;
    }

    @Override
    public int getColor() {
        return this.color;
    }

    public MachineAppearance copy() {
        return new MachineAppearance(this.blockModel, this.itemModel, this.sound, this.lightLevel, this.color);
    }
}
