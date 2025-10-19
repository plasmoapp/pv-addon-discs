package su.plo.voice.discs.utils

import kotlin.coroutines.cancellation.CancellationException

sealed class DiscCancellationCause(message: String) : CancellationException(message)

class DiscChunkUnloadCause : DiscCancellationCause("Chunk unloaded")

class DiscReplaceCause : DiscCancellationCause("Replaced with a new one in a jukebox")

class DiscEjectCause : DiscCancellationCause("Disc was ejected from a jukebox")

class DiscPullCause : DiscCancellationCause("Disc was pulled from a jukebox using minecart with hopper")

sealed class HornCancellationCause(message: String) : CancellationException(message)

class HornReplaceCause : HornCancellationCause("Replaced with a new track")

class HornCancelCommandCause : HornCancellationCause("Cancelled by command")

class HornPlayerQuitCause : HornCancellationCause("Player quit")
