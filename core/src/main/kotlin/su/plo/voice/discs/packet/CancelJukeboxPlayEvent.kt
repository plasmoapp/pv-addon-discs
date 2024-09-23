package su.plo.voice.discs.packet

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.events.ListenerPriority
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketEvent
import org.bukkit.plugin.java.JavaPlugin
import su.plo.voice.discs.AddonKeys
import su.plo.voice.discs.utils.extend.asJukebox
import su.plo.voice.discs.utils.extend.isCustomDisc

class CancelJukeboxPlayEvent(
    plugin: JavaPlugin,
    private val keys: AddonKeys,
    priority: ListenerPriority,
): PacketAdapter(
    plugin,
    priority,
    PacketType.Play.Server.WORLD_EVENT,
) {
    override fun onPacketSending(event: PacketEvent) = with(keys) {

        val worldEventId = event.packet.integers.read(0)

        // https://wiki.vg/Protocol#World_Event
        // 1010: Play record
        if (worldEventId != 1010) return

        val isCustomDisc = event.packet.blockPositionModifier
            .read(0)
            .toLocation(event.player.world)
            .block
            .asJukebox()
            ?.record
            ?.isCustomDisc() ?: false

        if (isCustomDisc) event.isCancelled = true
    }
}