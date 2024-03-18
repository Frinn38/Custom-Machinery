package fr.frinn.custommachinery.common.util;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.api.machine.MachineTile;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

public class CustomMachineDamageSource extends DamageSource {

    private final String machineName;

    public CustomMachineDamageSource(MachineTile machine) {
        super(machine.getLevel().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(CustomMachinery.MODID, "machine_damage"))));
        this.machineName = machine.getMachine().getName().getString();
    }

    @Override
    public Component getLocalizedDeathMessage(LivingEntity dead) {
        return Component.translatable("custommachinery.damagesource.kill", dead.getDisplayName(), this.machineName);
    }
}
