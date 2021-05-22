package eu.kanade.tachiyomi.source

import android.graphics.drawable.Drawable
import eu.kanade.tachiyomi.extension.AnimeExtensionManager
import eu.kanade.tachiyomi.source.model.SAnime
import eu.kanade.tachiyomi.source.model.SEpisode
import eu.kanade.tachiyomi.source.model.toAnimeInfo
import eu.kanade.tachiyomi.source.model.toEpisodeInfo
import eu.kanade.tachiyomi.source.model.toSAnime
import eu.kanade.tachiyomi.source.model.toSEpisode
import eu.kanade.tachiyomi.util.lang.awaitSingle
import rx.Observable
import tachiyomi.source.model.AnimeInfo
import tachiyomi.source.model.EpisodeInfo
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

/**
 * A basic interface for creating a source. It could be an online source, a local source, etc...
 */
interface AnimeSource : tachiyomi.source.AnimeSource {

    /**
     * Id for the source. Must be unique.
     */
    override val id: Long

    /**
     * Name of the source.
     */
    override val name: String

    override val lang: String
        get() = ""

    /**
     * Returns an observable with the updated details for a anime.
     *
     * @param anime the anime to update.
     */
    @Deprecated("Use getAnimeDetails instead")
    fun fetchAnimeDetails(anime: SAnime): Observable<SAnime>

    /**
     * Returns an observable with all the available episodes for an anime.
     *
     * @param anime the anime to update.
     */
    @Deprecated("Use getEpisodeList instead")
    fun fetchEpisodeList(anime: SAnime): Observable<List<SEpisode>>

    /**
     * Returns an observable with a link for the episode of an anime.
     *
     * @param episode the episode to get the link for.
     */
    @Deprecated("Use getEpisodeList instead")
    fun fetchEpisodeLink(episode: SEpisode): Observable<String>

    /**
     * [1.x API] Get the updated details for a anime.
     */
    @Suppress("DEPRECATION")
    override suspend fun getAnimeDetails(anime: AnimeInfo): AnimeInfo {
        val sAnime = anime.toSAnime()
        val networkAnime = fetchAnimeDetails(sAnime).awaitSingle()
        sAnime.copyFrom(networkAnime)
        return sAnime.toAnimeInfo()
    }

    /**
     * [1.x API] Get all the available episodes for a anime.
     */
    @Suppress("DEPRECATION")
    override suspend fun getEpisodeList(anime: AnimeInfo): List<EpisodeInfo> {
        return fetchEpisodeList(anime.toSAnime()).awaitSingle()
            .map { it.toEpisodeInfo() }
    }

    /**
     * [1.x API] Get a link for the episode of an anime.
     */
    @Suppress("DEPRECATION")
    override suspend fun getEpisodeLink(episode: EpisodeInfo): String {
        return fetchEpisodeLink(episode.toSEpisode()).awaitSingle()
    }
}

fun AnimeSource.icon(): Drawable? = Injekt.get<AnimeExtensionManager>().getAppIconForSource(this)

fun AnimeSource.getPreferenceKey(): String = "source_$id"