package su.plo.voice.discs.packet

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.events.ListenerPriority
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketEvent
import su.plo.voice.discs.DiscsPlugin
import su.plo.voice.discs.utils.extend.asJukebox

class CancelJukeboxPlayEvent(
    private val testPlugin: DiscsPlugin,
    priority: ListenerPriority,
): PacketAdapter(
    testPlugin,
    priority,
    PacketType.Play.Server.WORLD_EVENT,
) {
    override fun onPacketSending(event: PacketEvent) {

        val worldEventId = event.packet.integers.read(0)

        // https://wiki.vg/Protocol#World_Event
        // 1010: Play record
        if (worldEventId != 1010) return

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