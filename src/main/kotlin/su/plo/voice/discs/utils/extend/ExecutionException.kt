package su.plo.voice.discs.utils.extend

import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import java.util.concurrent.ExecutionException

fun ExecutionException.friendlyMessage() = when (val cause = cause) {
    is FriendlyException -> cause.message
    else -> message
}