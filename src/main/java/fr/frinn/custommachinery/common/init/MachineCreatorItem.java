package fr.frinn.custommachinery.common.init;

import fr.frinn.custommachinery.client.ClientHandler;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@ParametersAreNonnullByDefault
public class MachineCreatorItem extends Item {

    public MachineCreatorItem(Properties properties) {
        super(properties);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand) {
        if(world.isRemote())
            ClientHandler.openMachineLoadingScreen();
        return super.onItemRightClick(world, player, hand);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltips, ITooltipFlag flagIn) {
        tooltips.add(new TranslationTextComponent("custommachinery.machine_creator.warning").mergeStyle(TextFormatting.DARK_RED));
    }
}
