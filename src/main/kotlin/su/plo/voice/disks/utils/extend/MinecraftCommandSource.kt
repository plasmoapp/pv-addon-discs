//package su.plo.voice.disks.utils.extend
//
//import su.plo.lib.api.chat.MinecraftTextComponent
//import su.plo.lib.api.server.command.MinecraftCommandSource
//import su.plo.lib.api.server.player.MinecraftServerPlayer
//import su.plo.voice.api.server.PlasmoCommonVoiceServer
//
//fun MinecraftCommandSource.sendTranslatable(key: String, vararg args: Any?) {
//    sendMessage(MinecraftTextComponent.translatable(key, *args))
//}
//
//fun MinecraftCommandSource.getVoicePlayer(server: PlasmoCommonVoiceServer) = if (this is MinecraftServerPlayer) {
//    server.playerManager.getPlayerById(this.uuid).orElse(null)
//} else null