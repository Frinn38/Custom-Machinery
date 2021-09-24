package fr.frinn.custommachinery.common.data.component;

import fr.frinn.custommachinery.api.component.ComponentIOMode;
import fr.frinn.custommachinery.api.component.IMachineComponentManager;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.impl.component.AbstractMachineComponent;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

public class PositionMachineComponent extends AbstractMachineComponent {

    public PositionMachineComponent(IMachineComponentManager manager) {
        super(manager, ComponentIOMode.NONE);
    }

    @Override
    public MachineComponentType<PositionMachineComponent> getType() {
        return Registration.POSITION_MACHINE_COMPONENT.get();
    }

    public BlockPos getPosition() {
        return this.getManager().getTile().getPos();
    }

    public Biome getBiome() {
        if(getManager().getTile().getWorld() == null)
            return ServerLifecycleHooks.getCurrentServer().getDynamicRegistries().getRegistry(Registry.BIOME_KEY).getOrDefault(new ResourceLocation("the_void"));
        return this.getManager().getTile().getWorld().getBiome(getPosition());
    }

    public RegistryKey<World> getDimension() {
        if(getManager().getTile().getWorld() == null)
            return World.OVERWORLD;
        return getManager().getTile().getWorld().getDimensionKey();
    }
}
