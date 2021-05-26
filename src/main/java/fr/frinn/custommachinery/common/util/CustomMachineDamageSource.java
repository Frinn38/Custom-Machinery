package fr.frinn.custommachinery.common.util;

import fr.frinn.custommachinery.CustomMachinery;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import javax.annotation.ParametersAreNonnullByDefault;

public class CustomMachineDamageSource extends DamageSource {

    private String machineName;

    public CustomMachineDamageSource(String machineName) {
        super(CustomMachinery.MODID + "_damagesource");
        this.machineName = machineName;
        this.setDamageBypassesArmor();
        this.setDamageIsAbsolute();
    }

    @ParametersAreNonnullByDefault
    @Override
    public ITextComponent getDeathMessage(LivingEntity dead) {
        return new TranslationTextComponent("custommachinery.damagesource.kill", dead.getDisplayName(), this.machineName);
    }
}
