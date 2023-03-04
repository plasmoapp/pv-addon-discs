package su.plo.voice.discs.utils.extend

import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.Jukebox

fun Block.isJukebox() = this.type == Material.JUKEBOX

fun Block.asJukebox() = this.takeIf { it.isJukebox() }?.let { it.state as? Jukebox }