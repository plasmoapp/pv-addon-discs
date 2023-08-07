package su.plo.voice.discs.utils.extend

import org.bukkit.block.Jukebox

fun Jukebox.stopPlayingWithUpdate() {
    val oldRecord = record
    setRecord(null)
    update()

    setRecord(oldRecord)
    stopPlaying()
    update()
}
