package su.plo.voice.discs.utils.extend

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import net.kyori.adventure.text.serializer.plain.PlainComponentSerializer
import su.plo.slib.api.chat.component.McTranslatableText
import su.plo.voice.api.server.PlasmoVoiceServer
import su.plo.voice.api.server.player.VoicePlayer

fun Component.toPlainText(): String =
    PlainComponentSerializer.plain().serialize(this)

fun McTranslatableText.render(voicePlayer: VoicePlayer, voiceServer: PlasmoVoiceServer): Component {
    val json = voiceServer.minecraftServer
        .textConverter
        .convertToJson(voicePlayer.instance, this)

    return GsonComponentSerializer.gson().deserialize(json)
}