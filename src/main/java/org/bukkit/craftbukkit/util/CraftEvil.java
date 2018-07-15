package org.bukkit.craftbukkit.util;

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.inventory.ItemStack;

/**
 * @deprecated do not use for any reason
 */
@Deprecated
public class CraftEvil {

    private static final Int2ObjectMap<Material> byId = new Int2ObjectLinkedOpenHashMap<>();

    static {
        for (Material material : Material.values()) {
            Preconditions.checkState(!byId.containsKey(material.getId()), "Duplicate material ID for", material);
            byId.put(material.getId(), material);
        }
    }

    public static int getBlockTypeIdAt(World world, int x, int y, int z) {
        return getId(world.getBlockAt(x, y, z).getType());
    }

    public static int getBlockTypeIdAt(World world, Location location) {
        return getId(world.getBlockAt(location).getType());
    }

    public static int getTypeId(Block block) {
        return getId(block.getType());
    }

    public static boolean setTypeId(Block block, int type) {
        block.setType(getMaterial(type));
        return true;
    }

    public static boolean setTypeId(Block block, int type, boolean applyPhysics) {
        block.setType(getMaterial(type), applyPhysics);
        return true;
    }

    public static boolean setTypeIdAndData(Block block, int type, byte data, boolean applyPhysics) {
        block.setType(getMaterial(type), applyPhysics);
        block.setData(data);
        return true;
    }

    public static int getTypeId(BlockState state) {
        return getId(state.getType());
    }

    public static boolean setTypeId(BlockState state, int type) {
        state.setType(getMaterial(type));
        return true;
    }

    public static int getTypeId(ItemStack stack) {
        return getId(stack.getType());
    }

    public static void setTypeId(ItemStack stack, int type) {
        stack.setType(getMaterial(type));
    }

    public static Material getMaterial(int id) {
        return byId.get(id);
    }

    public static int getId(Material material) {
        return CraftLegacy.toLegacy(material).getId();
    }
}
