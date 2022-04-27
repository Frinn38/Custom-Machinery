package fr.frinn.custommachinery.common.integration.crafttweaker.function;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import fr.frinn.custommachinery.api.crafting.ICraftingContext;
import fr.frinn.custommachinery.common.init.CustomMachineTile;
import net.minecraft.tileentity.TileEntity;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name("mods.custommachinery.Context")
public class Context {

    private final ICraftingContext internal;
    private final MachineCT machine;

    public Context(ICraftingContext internal) {
        this.internal = internal;
        this.machine = new MachineCT((CustomMachineTile)internal.getMachineTile());
    }

    @ZenCodeType.Getter("remainingTime")
    @ZenCodeType.Method
    public double getRemainingTime() {
        return internal.getRemainingTime();
    }

    @ZenCodeType.Getter("tile")
    @ZenCodeType.Method
    public TileEntity getTile() {
        return internal.getMachineTile();
    }

    @ZenCodeType.Getter("machine")
    @ZenCodeType.Method
    public MachineCT getMachine() {
        return this.machine;
    }
}
