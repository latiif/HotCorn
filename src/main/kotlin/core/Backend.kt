package latiif.hotcorn.app.core

import com.github.kittinunf.fuel.httpGet

object Backend {
    private val backends = arrayOf(
            "https://tv-v2.api-fetch.sh",
            "https://tv-v2.api-fetch.am",
            "https://tv-v2.api-fetch.website"
    )

    private val backend: String by lazy {
        backends.forEach {
            val (_, response, _) = it.httpGet().response()
            if (response.statusCode != -1) {
                return@lazy it
            }
        }
        ""
    }

    fun getSeriesInfoUrl(): String {
        return "$backend/show/"
    }

    fun getSeriesSearchUrl(): String {
        return "$backend/shows/1?sort=rating&order=-1&genre=all&keywords="
    }
}
