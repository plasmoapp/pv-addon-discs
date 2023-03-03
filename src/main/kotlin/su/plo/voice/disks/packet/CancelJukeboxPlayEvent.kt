package su.plo.voice.disks.packet

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.events.ListenerPriority
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketEvent
import su.plo.voice.disks.TestPlugin
import su.plo.voice.disks.utils.extend.asJukebox

class CancelJukeboxPlayEvent(
    private val testPlugin: TestPlugin,
    priority: ListenerPriority,
): PacketAdapter(
    testPlugin,
    priority,
    PacketType.Play.Server.WORLD_EVENT,
) {
    override fun onPacketSending(event: PacketEvent) {

        if (event.packet.integers.read(0) != 1010) return

        val jukebox = event.packet.blockPositionModifier
            .read(0)
            .toLocation(event.player.world)
            .block
            .asJukebox()
            ?.takeIf { it.record.hasItemMeta() } ?: return

        val isCustomDisk = jukebox.record.itemMeta
            .persistentDataContainer
            .has(testPlugin.identifierKey)

        if (isCustomDisk) event.isCancelled = true
    }
}