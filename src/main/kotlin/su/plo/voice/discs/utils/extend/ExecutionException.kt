package su.plo.voice.discs.utils.extend

import su.plo.voice.lavaplayer.libs.com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import java.util.concurrent.ExecutionException

fun ExecutionException.friendlyMessage() = when (val cause = cause) {
    is FriendlyException -> cause.message
    else -> message
}