package me.bristermitten.privatemines

import org.apache.commons.lang.WordUtils
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import java.util.*
import com.sk89q.worldedit.Vector as WEVector
import org.bukkit.util.Vector as BukkitVector

object Util {

    @JvmStatic
    fun WEVector.toBukkitVector(): org.bukkit.util.Vector {
        return BukkitVector(x, y, z)
    }

    @JvmStatic
    fun BukkitVector.toWEVector(): WEVector {
        return WEVector(x, y, z)
    }

    @JvmStatic
    fun WEVector.toLocation(world: World): Location {
        return Location(world, x, y, z)
    }

    @JvmStatic
    fun deserializeStack(map: Map<String, Any>, vararg placeholders: Any): ItemStack {
        val replacements = arrayToMap(*placeholders)

        val s = ItemStack(Material.AIR)
        s.amount = (map["Amount"] ?: 1) as Int
        var type = (map["Type"] ?: Material.STONE.name) as String
        type = replacements[type] ?: type
        s.type = Material.matchMaterial(type)
        s.durability = ((map["Data"] ?: 0) as Number).toShort()
        val itemMeta = s.itemMeta

        itemMeta.displayName = (map["Name"] as String?).color()

        @Suppress("UNCHECKED_CAST")
        itemMeta.lore = (map["Lore"] as List<String>?)?.color()

        s.itemMeta = itemMeta
        return s
    }

    @JvmStatic
    fun String?.color(): String? {
        return if (this == null) null else ChatColor.translateAlternateColorCodes('&', this)
    }

    @JvmStatic
    fun List<String?>.color(): List<String?> {
        return map { it.color() }
    }

    @JvmStatic
    fun arrayToMap(vararg replacements: Any): Map<String, String> {
        if (replacements.isEmpty()) return emptyMap()

        if (replacements.size % 2 != 0) {
            throw IllegalArgumentException("Replacements size must be a multiple of 2")
        }

        val placeholderMap = LinkedHashMap<String, String>(replacements.size / 2)
        var i = 0
        while (i < replacements.size - 1) {
            placeholderMap[replacements[i].toString()] = replacements[i + 1].toString()
            i += 2
        }
        return placeholderMap
    }

    @JvmStatic
    fun deserializeWorldEditVector(map: Map<String?, Any?>): WEVector {
        return BukkitVector.deserialize(map).toWEVector()
    }

    @JvmStatic
    fun replaceMeta(meta: ItemMeta, vararg replacements: Any) {
        val replace = arrayToMap(*replacements)
        if (meta.hasDisplayName()) {
            replace.forEach { (k: String?, v: String?) -> meta.displayName = meta.displayName.replace(k, v) }
        }
        if (meta.hasLore()) {
            meta.lore = meta.lore.map { line: String ->
                var mutableLine = line

                replace.forEach { (key, value) ->
                    mutableLine = mutableLine.replace(key, value)
                }
                mutableLine
            }.toList()
        }
    }

    @JvmStatic
    fun String.prettify(): String {
        return WordUtils.capitalize(toLowerCase().replace("_", " "))
    }

    @JvmStatic
    fun getYaw(face: BlockFace?): Float {
        return when (face) {
            BlockFace.WEST -> 90f
            BlockFace.NORTH -> 180f
            BlockFace.EAST -> -90f
            BlockFace.SOUTH -> -180f
            else -> 0f
        }
    }
}
