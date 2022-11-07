package fr.frinn.custommachinery.common.util;

import fr.frinn.custommachinery.CustomMachinery;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

public class CustomMachineDamageSource extends DamageSource {

    private String machineName;

    public CustomMachineDamageSource(String machineName) {
        super(CustomMachinery.MODID + "_damagesource");
        this.machineName = machineName;
        this.bypassArmor();
        this.bypassMagic();
    }

    @Override
    public Component getLocalizedDeathMessage(LivingEntity dead) {
        return new TranslatableComponent("custommachinery.damagesource.kill", dead.getDisplayName(), this.machineName);
    }
}
