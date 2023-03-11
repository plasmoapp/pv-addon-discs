package su.plo.voice.discs.packet

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.events.ListenerPriority
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketEvent
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import su.plo.voice.discs.DiscsPlugin
import su.plo.voice.discs.utils.extend.asJukebox
import su.plo.voice.discs.utils.extend.isCustomDisc

class CancelJukeboxPlayEvent(
    private val discsPlugin: DiscsPlugin,
    priority: ListenerPriority,
): PacketAdapter(
    discsPlugin,
    priority,
    PacketType.Play.Server.WORLD_EVENT,
) {
    override fun onPacketSending(event: PacketEvent) {

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
            ?.isCustomDisc(discsPlugin) ?: false

        if (isCustomDisc) event.isCancelled = true
    }
}