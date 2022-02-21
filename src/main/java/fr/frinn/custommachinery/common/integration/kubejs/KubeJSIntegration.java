package fr.frinn.custommachinery.common.integration.kubejs;

import dev.latvian.kubejs.script.ScriptType;
import dev.latvian.kubejs.text.Text;
import fr.frinn.custommachinery.api.crafting.CraftingResult;
import fr.frinn.custommachinery.api.crafting.ICraftingContext;
import fr.frinn.custommachinery.api.machine.MachineTile;
import fr.frinn.custommachinery.common.data.upgrade.MachineUpgrade;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class KubeJSIntegration {

    public static List<MachineUpgrade> collectMachineUpgrades() {
        ScriptType.SERVER.console.info("Collecting Custom Machine upgrades from JS scripts.");

        CustomMachineJSUpgradeBuilder.UpgradeEvent event = new CustomMachineJSUpgradeBuilder.UpgradeEvent();
        event.post(ScriptType.SERVER, "cm_upgrades");

        List<MachineUpgrade> upgrades = new ArrayList<>();

        try {
            upgrades = event.getBuilders().stream().map(CustomMachineJSUpgradeBuilder::build).collect(Collectors.toList());
        } catch (Exception e) {
            ScriptType.SERVER.console.warn("Couldn't build machine upgrade", e);
        }

        ScriptType.SERVER.console.infof("Successfully added %s Custom Machine upgrades", event.getBuilders().size());
        return upgrades;
    }

    public interface RecipeFunction extends Function<KubeJSIntegration.Context, KubeJSIntegration.Result> {}

    public static class KJSFunction implements Function<ICraftingContext, CraftingResult> {

        private final Function<Context, Result> function;

        public KJSFunction(Function<Context, Result> function) {
            this.function = function;
        }

        @Override
        public CraftingResult apply(ICraftingContext context) {
            return this.function.apply(new Context(context)).internal;
        }
    }

    public static class Context {

        private final ICraftingContext internal;

        public Context(ICraftingContext internal) {
            this.internal = internal;
        }

        public double getRemainingTime() {
            return this.internal.getRemainingTime();
        }

        public MachineTile getMachine() {
            return this.internal.getMachineTile();
        }
    }

    public static class Result {

        private final CraftingResult internal;

        private Result(CraftingResult internal) {
            this.internal = internal;
        }

        public static Result success() {
            return new Result(CraftingResult.success());
        }

        public static Result error(Text error) {
            return new Result(CraftingResult.error(error.component()));
        }
    }
}
