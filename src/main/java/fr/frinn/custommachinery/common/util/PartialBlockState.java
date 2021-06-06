package fr.frinn.custommachinery.common.util;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.state.Property;
import net.minecraft.util.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class PartialBlockState {

    public static final PartialBlockState AIR = new PartialBlockState(Blocks.AIR);

    private Block block;
    private Map<Property, Comparable> properties;

    public PartialBlockState(Block block) {
        this.block = block;
        this.properties = new HashMap<>();
    }

    public <T extends Comparable<T>> PartialBlockState with(Property<T> property, T value) {
        this.properties.put(property, value);
        return this;
    }

    public Block getBlock() {
        return this.block;
    }

    public BlockState getBlockState() {
        BlockState state = this.block.getDefaultState();
        for(Map.Entry<Property, Comparable> entry : this.properties.entrySet()) {
            state = state.with(entry.getKey(), entry.getValue());
        }
        return state;
    }

    public boolean compareState(BlockState state) {
        if(state.getBlock() != this.block)
            return false;
        return this.properties.entrySet().stream().allMatch(entry -> state.get(entry.getKey()) != null && state.get(entry.getKey()).compareTo(entry.getValue()) == 0);
    }

    public ModelResourceLocation location() {
        ResourceLocation blockLocation = this.block.getRegistryName();
        StringBuilder variant = new StringBuilder();
        this.properties.entrySet().forEach(entry -> {
            variant.append(entry.getKey().toString());
            variant.append("=");
            variant.append(entry.getValue().toString());
            variant.append(",");
        });
        variant.delete(variant.length() - 1, variant.length());
        return new ModelResourceLocation(blockLocation, variant.toString());
    }
}
