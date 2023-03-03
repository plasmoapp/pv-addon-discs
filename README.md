# pv-addon-disks
Paper only [Plasmo Voice](https://github.com/plasmoapp/plasmo-voice) addon. Play audio from Youtube and other sources in Minecraft using music disks.

The addon can stream audio from various sources instead of saving audio files on the server. It even supports Youtube and Twitch live streams. Thanks to the brilliant [LavaPlayer](https://github.com/sedmelluq/lavaplayer) library.

**Depends on [ProtocolLib](https://www.spigotmc.org/resources/protocollib.1997/)**

Inspired by [SVC](https://github.com/henkelmax/simple-voice-chat)'s [AudioPlayer](https://github.com/henkelmax/audio-player) & [CustomDisks](https://github.com/Navoei/CustomDiscs).

## Commands

`/disc burn <url> [name]` – Create a disc with custom audio from the URL. You need to hold a music disk in your hand.

`/disc erase` – Return a custom disc to a normal one.

## Permissions
Commands are only available to OP by default:

`pv.addon.disks.burn` – Burn command

`pv.addon.disks.erase` – Erase command

Available to everyone by default:

`pv.addon.disks.play` – Use custom music disks

## Supported sources

Addon can load audio from:

- YouTube videos and live streams
- SoundCloud
- Bandcamp
- Vimeo
- Twitch live streams
- Direct HTTP URLs

## Supported file formats

If you want to stream audio from direct URLs

- MP3
- FLAC
- WAV
- Matroska/WebM (AAC, Opus or Vorbis codecs)
- MP4/M4A (AAC codec)
- OGG streams (Opus, Vorbis and FLAC codecs)
- AAC streams
- Stream playlists (M3U and PLS)

## Planned features

- YouTube search
- Set distance on a jukebox
- Make it work with hoppers
- Custom goat horn audio
- Copy disc like you can copy maps
- Optional: Add glint to custom music disks to make them stand out
