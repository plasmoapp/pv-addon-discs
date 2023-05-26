package su.plo.voice.discs.crafting

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.*
import org.bukkit.persistence.PersistentDataType
import su.plo.voice.discs.DiscsPlugin


object BurnableDiscCraft {

    private val records = arrayOf(
        Material.MUSIC_DISC_13,
        Material.MUSIC_DISC_5,
        Material.MUSIC_DISC_11,
        Material.MUSIC_DISC_13,
        Material.MUSIC_DISC_BLOCKS,
        Material.MUSIC_DISC_CAT,
        Material.MUSIC_DISC_CHIRP,
        Material.MUSIC_DISC_FAR,
        Material.MUSIC_DISC_MALL,
        Material.MUSIC_DISC_MELLOHI,
        Material.MUSIC_DISC_OTHERSIDE,
        Material.MUSIC_DISC_PIGSTEP,
        Material.MUSIC_DISC_STAL,
        Material.MUSIC_DISC_STRAD,
        Material.MUSIC_DISC_WAIT,
        Material.MUSIC_DISC_WARD,
    )

    fun registerRecipies(plugin: DiscsPlugin) {
        val recipeKey = NamespacedKey(plugin, "burnableCraft")
        val burnableKey = plugin.burnableKey
        for (record in records) {
            Bukkit.addRecipe(
                createRecipe(record, recipeKey, burnableKey)
            )
        }
    }

    private fun createRecipe(record: Material, recipeKey: NamespacedKey, burnableKey: NamespacedKey): ShapelessRecipe =
        ShapelessRecipe(recipeKey, createCustomRecord(record, burnableKey)).also {
            it.addIngredient(ItemStack(record))
            it.addIngredient(6, Material.DIAMOND)
        }

    private fun createCustomRecord(record: Material, burnableKey: NamespacedKey): ItemStack {
        val itemStack = ItemStack(record)
        itemStack.editMeta {
            it.addEnchant(Enchantment.MENDING, 1, true)
            it.addItemFlags(*ItemFlag.values())
            it.persistentDataContainer.set(burnableKey, PersistentDataType.BYTE, 1)
        }
        return itemStack
    }
}