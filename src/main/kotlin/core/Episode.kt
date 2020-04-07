package latiif.hotcorn.app.core

data class Episode(
    val firstAired: Long,
    val overview: String,
    val title: String,
    val episode: Int,
    val season: Int,
    val tvdbId: Long,
    val showTitle: String,
    val firstAiredUTC: String,
    val torrents: Map<String,String>
)