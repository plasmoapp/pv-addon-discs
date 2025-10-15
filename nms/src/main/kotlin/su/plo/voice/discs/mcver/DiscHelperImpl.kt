//? if >=1.21.3
/*@file:Suppress("UnstableApiUsage")*/
package su.plo.voice.discs.mcver
//? if >=1.21.3
/*import io.papermc.paper.datacomponent.DataComponentTypes*/
import org.bukkit.inventory.ItemStack
import su.plo.voice.discs.item.DiscHelper

//? if >=1.21.5
/*import io.papermc.paper.datacomponent.item.TooltipDisplay*/

class DiscHelperImpl : DiscHelper {
    //? if >=1.21.3
    /*@Suppress("Expier")*/
    override fun showSongTooltip(item: ItemStack, show: Boolean) {
        //? if >=1.21.5 {
        /*val tooltipDisplay = item.getData(DataComponentTypes.TOOLTIP_DISPLAY)
        val hiddenComponents = (tooltipDisplay?.hiddenComponents() ?: emptySet())
            .filter { it != DataComponentTypes.JUKEBOX_PLAYABLE }

        val newTooltipDisplay =
            if (show) {
                TooltipDisplay.tooltipDisplay()
                    .hideTooltip(tooltipDisplay?.hideTooltip() ?: false)
                    .addHiddenComponents(*hiddenComponents.toTypedArray())
                    .build()
            } else {
                TooltipDisplay.tooltipDisplay()
                    .hideTooltip(tooltipDisplay?.hideTooltip() ?: false)
                    .addHiddenComponents(*hiddenComponents.toTypedArray())
                    .addHiddenComponents(DataComponentTypes.JUKEBOX_PLAYABLE)
                    .build()
            }
        item.setData(DataComponentTypes.TOOLTIP_DISPLAY, newTooltipDisplay)
        *///?} else if >=1.21.3 {
        /*val jukeboxPlayable = item.getData(DataComponentTypes.JUKEBOX_PLAYABLE) ?: return
        val newJukeboxPlayable = jukeboxPlayable.showInTooltip(show)
        item.setData(DataComponentTypes.JUKEBOX_PLAYABLE, newJukeboxPlayable)
        *///?}
    }

    override fun isRecord(item: ItemStack) : Boolean {
        return item.type.isRecord
                //? if >=1.21.3
                /*|| item.hasData(DataComponentTypes.JUKEBOX_PLAYABLE)*/
    }
}

