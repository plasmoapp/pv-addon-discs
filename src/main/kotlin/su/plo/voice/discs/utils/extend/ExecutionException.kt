package su.plo.voice.discs.utils.extend

import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import java.util.concurrent.ExecutionException

val ExecutionException.friendlyMessage: String?
    get() = when (val cause = cause) {
        is FriendlyException -> cause.message
        else -> message
    }