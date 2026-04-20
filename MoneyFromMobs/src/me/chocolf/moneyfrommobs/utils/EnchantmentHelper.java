package me.chocolf.moneyfrommobs.utils;

import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;

public final class EnchantmentHelper {

    public static final Enchantment UNBREAKING;
    public static final Enchantment LOOTING;
    static {
        boolean registries;
        try {
            Class.forName("org.bukkit.Registry");
            registries = true;
        } catch (ClassNotFoundException e) {
            registries = false;
        }

        if (registries) {
            UNBREAKING = Registry.ENCHANTMENT.get(NamespacedKey.minecraft("unbreaking"));
            LOOTING = Registry.ENCHANTMENT.get(NamespacedKey.minecraft("looting"));
        } else {
            UNBREAKING = findEnchantmentLegacy("unbreaking");
            LOOTING = findEnchantmentLegacy("looting");
        }
    }

    private EnchantmentHelper() {

    }

    private static Enchantment findEnchantmentLegacy(String... names) {
        for (String name : names) {
            Enchantment enchantment = Enchantment.getByKey(NamespacedKey.fromString(name));
            if (enchantment != null)
                return enchantment;
        }
        return null;
    }

}
