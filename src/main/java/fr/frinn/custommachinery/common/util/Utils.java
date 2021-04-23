package fr.frinn.custommachinery.common.util;

import com.mojang.serialization.DataResult;
import fr.frinn.custommachinery.common.crafting.CustomMachineRecipe;
import fr.frinn.custommachinery.common.data.MachineAppearance;
import fr.frinn.custommachinery.common.data.component.Mode;
import fr.frinn.custommachinery.common.data.gui.IGuiElement;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocationException;

import java.util.Comparator;
import java.util.Objects;

public class Utils {

    public static final Comparator<IGuiElement> GUI_ELEMENTS_COMPARATOR = Comparator.comparingInt(IGuiElement::getPriority);

    public static final Comparator<CustomMachineRecipe> CUSTOM_MACHINE_RECIPE_COMPARATOR = Comparator.comparingInt(CustomMachineRecipe::getPriority);

    public static DataResult<ModelResourceLocation> decodeModelResourceLocation(String encoded) {
        try {
            return DataResult.success(new ModelResourceLocation(encoded));
        } catch (ResourceLocationException resourcelocationexception) {
            return DataResult.error("Not a valid model resource location: " + encoded + " " + resourcelocationexception.getMessage());
        }
    }

    public static DataResult<MachineAppearance.AppearanceType> decodeAppearanceType(String encoded) {
        try {
            return DataResult.success(MachineAppearance.AppearanceType.value(encoded));
        } catch (IllegalArgumentException e) {
            return DataResult.error("Not a valid Appearance Type: " + encoded + " " + e.getMessage());
        }
    }

    public static DataResult<Mode> decodeMode(String encoded) {
        try {
            return DataResult.success(Mode.value(encoded));
        } catch (IllegalArgumentException e) {
            return DataResult.error("Not a valid Appearance Type: " + encoded + " " + e.getMessage());
        }
    }

    public static boolean canPlayerManageMachines(PlayerEntity player) {
        return player.hasPermissionLevel(Objects.requireNonNull(player.getServer()).getOpPermissionLevel());
    }
}
