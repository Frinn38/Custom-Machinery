package fr.frinn.custommachinery.common.integration.crafttweaker.function;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import fr.frinn.custommachinery.api.crafting.CraftingResult;
import fr.frinn.custommachinery.api.crafting.ICraftingContext;
import fr.frinn.custommachinery.common.init.CustomMachineTile;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.openzen.zencode.java.ZenCodeType.Getter;
import org.openzen.zencode.java.ZenCodeType.Method;
import org.openzen.zencode.java.ZenCodeType.Name;
import org.openzen.zencode.java.ZenCodeType.Setter;

@ZenRegister
@Name("mods.custommachinery.Context")
public class Context {

    private final ICraftingContext internal;
    private final MachineCT machine;

    public Context(ICraftingContext internal) {
        this.internal = internal;
        this.machine = new MachineCT((CustomMachineTile)internal.getMachineTile());
    }

    @Getter("remainingTime")
    @Method
    public double getRemainingTime() {
        return this.internal.getRemainingTime();
    }

    @Getter("baseSpeed")
    @Method
    public double getBaseSpeed() {
        return this.internal.getBaseSpeed();
    }

    @Setter("baseSpeed")
    @Method
    public void setBaseSpeed(double baseSpeed) {
        this.internal.setBaseSpeed(baseSpeed);
    }

    @Getter("modifiedSpeed")
    @Method
    public double getModifiedSpeed() {
        return this.internal.getModifiedSpeed();
    }

    @Getter("tile")
    @Method
    public BlockEntity getTile() {
        return this.internal.getMachineTile();
    }

    @Getter("machine")
    @Method
    public MachineCT getMachine() {
        return this.machine;
    }

    @Method
    public CraftingResult success() {
        return Result.success();
    }

    @Method
    public CraftingResult error(String error) {
        return Result.error(error);
    }
}
