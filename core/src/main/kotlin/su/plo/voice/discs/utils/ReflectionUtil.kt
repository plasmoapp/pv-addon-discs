package su.plo.voice.discs.utils

import org.bukkit.Bukkit
import org.bukkit.inventory.ItemStack

object ReflectionUtil {

    private val craftBukkitPackage: String = Bukkit.getServer().javaClass.getPackage().name

    fun craftBukkitClassName(className: String): String =
        "$craftBukkitPackage.$className"

    fun craftBukkitClass(className: String): Class<*> =
        Class.forName(craftBukkitClassName(className))

    fun getMinecraftItemStack(itemStack: ItemStack): Any {
        val craftItemClass = craftBukkitClass("inventory.CraftItemStack")
        val mcItemField = craftItemClass.getDeclaredField("handle")

        return mcItemField.get(itemStack)
    }

//    fun remappedClass(className: String): Class<*> =
//        Class.forName(remapClassName(className))
//
//    fun remappedMethod(clazz: Class<*>, methodName: String, vararg parameterTypes: Class<*>): Method =
//        clazz.getDeclaredMethod(
//            remapMethodName(clazz, methodName, *parameterTypes),
//            *parameterTypes
//        )
//
//    fun remappedField(clazz: Class<*>, fieldName: String): Field =
//        clazz.getDeclaredField(remapFieldName(clazz, fieldName))
}