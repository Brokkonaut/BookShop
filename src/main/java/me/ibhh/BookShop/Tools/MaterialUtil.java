package me.ibhh.BookShop.Tools;

import java.util.EnumSet;
import org.bukkit.Material;

public class MaterialUtil {
    private static final EnumSet<Material> SIGNS = EnumSet.of(Material.ACACIA_SIGN, Material.BIRCH_SIGN, Material.DARK_OAK_SIGN, Material.JUNGLE_SIGN, Material.OAK_SIGN, Material.SPRUCE_SIGN);
    private static final EnumSet<Material> WALL_SIGNS = EnumSet.of(Material.ACACIA_WALL_SIGN, Material.BIRCH_WALL_SIGN, Material.DARK_OAK_WALL_SIGN, Material.JUNGLE_WALL_SIGN, Material.OAK_WALL_SIGN, Material.SPRUCE_WALL_SIGN);
    private static final EnumSet<Material> ALL_SIGNS;
    static {
        ALL_SIGNS = EnumSet.copyOf(SIGNS);
        ALL_SIGNS.addAll(WALL_SIGNS);
    }

    public static boolean isSign(Material m) {
        return ALL_SIGNS.contains(m);
    }
}
