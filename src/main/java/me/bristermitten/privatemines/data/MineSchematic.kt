package me.bristermitten.privatemines.data

import com.boydti.fawe.`object`.schematic.Schematic
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat
import org.bukkit.inventory.ItemStack
import java.io.File
import java.io.FileInputStream

data class MineSchematic(
		val name: String,
		val description: List<String>,
		val file: File,
		val icon: ItemStack
) {

	/*
	   Load the private mine schematic.
	 */

	val schematic: Schematic
		get() {
			if (!file.exists()) {
				throw IllegalStateException("File ${file.absolutePath} does not exist")
			}
			val format = ClipboardFormat.SCHEMATIC
			return format.load(FileInputStream(file))
		}
}
