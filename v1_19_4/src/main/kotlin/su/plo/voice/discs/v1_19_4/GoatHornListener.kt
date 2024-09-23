package su.plo.voice.discs.v1_19_4

import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.koin.core.component.inject
import su.plo.voice.discs.AddonConfig
import su.plo.voice.discs.AddonKeys
import su.plo.voice.discs.GoatHornManager
import su.plo.voice.discs.utils.PluginKoinComponent
import su.plo.voice.discs.utils.extend.hasIdentifier

class GoatHornListener : Listener, PluginKoinComponent {

    private val keys: AddonKeys by inject()
    private val config: AddonConfig by inject()
    private val hornManager: GoatHornManager by inject()

    @EventHandler(priority = EventPriority.MONITOR)
    fun onHornInteract(event: PlayerInteractEvent) = with(keys) {
        if (!config.goatHorn.enabled) return

        val item = event.item ?: return
        val player = event.player

        if (item.type != Material.GOAT_HORN) return
        if (player.hasCooldown(Material.GOAT_HORN)) return

        if (!item.hasIdentifier()) return
        if (!player.hasPermission("pv.addon.discs.play")) return

        player.setCooldown(Material.GOAT_HORN, 140)

        hornManager.playTrack(player, item)
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        hornManager.cancelTrack(event.player)
    }
}