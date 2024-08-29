package fr.frinn.custommachinery.impl.integration.jei;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record Energy(int amount, double chance, boolean isPerTick) {
    public static final Codec<Energy> CODEC = RecordCodecBuilder.create(energyInstance ->
            energyInstance.group(
                    Codec.INT.fieldOf("amount").forGetter(Energy::amount),
                    Codec.DOUBLE.fieldOf("chance").forGetter(Energy::chance),
                    Codec.BOOL.fieldOf("perTick").forGetter(Energy::isPerTick)
            ).apply(energyInstance, Energy::new)
    );
}
