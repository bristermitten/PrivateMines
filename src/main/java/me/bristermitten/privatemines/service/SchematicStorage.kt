package me.bristermitten.privatemines.service

import me.bristermitten.privatemines.PrivateMines
import me.bristermitten.privatemines.Util.deserializeStack
import me.bristermitten.privatemines.data.MineSchematic
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

object SchematicStorage {
	private val schematics: MutableMap<String, MineSchematic> = HashMap()
	private val schematicsDir = PrivateMines.getPlugin().dataFolder.resolve("schematics")

	var default: MineSchematic? = null
		private set

	fun loadAll(config: YamlConfiguration) {
		schematics.clear()

		val section = config.getConfigurationSection("Schematics")
		for (name in section.getKeys(false)) {
			val schematicSection = section.getConfigurationSection(name)

			val description = schematicSection.getStringList("Description")

			val file = File(schematicsDir, schematicSection.getString("File"))

			if (!file.exists()) {
				PrivateMines.getPlugin().logger.warning("Schematic $file does not exist, not registered")
				continue
			}

			val item = deserializeStack(schematicSection.getConfigurationSection("Icon").getValues(true))

			val schematic = MineSchematic(name, description, file, item)
			schematics[name] = schematic

			if (schematicSection.getBoolean("Default")) {
				default = schematic
			}
		}
	}

	operator fun get(name: String): MineSchematic? {
		return schematics[name]
	}

	fun getAll(): Array<MineSchematic> = schematics.values.toTypedArray()
}

