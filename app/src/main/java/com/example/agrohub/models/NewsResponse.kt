package com.example.agrohub.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class NewsResponse(
    @SerializedName("search_metadata") val searchMetadata: NewsSearchMetadata? = null,
    @SerializedName("search_parameters") val searchParameters: NewsSearchParameters? = null,
    @SerializedName("news_results") val newsResults: List<NewsResult>? = null,
    @SerializedName("menu_links") val menuLinks: List<MenuLink>? = null,
    @SerializedName("related_topics") val relatedTopics: List<RelatedTopic>? = null
) : Parcelable

@Parcelize
data class MenuLink(
    val title: String? = null,
    @SerializedName("topic_token") val topicToken: String? = null,
    @SerializedName("serpapi_link") val serpapiLink: String? = null
) : Parcelable

@Parcelize
data class NewsResult(
    val position: Int? = null,
    val title: String? = null,
    val source: Source? = null,
    val link: String? = null,
    val thumbnail: String? = null,
    @SerializedName("thumbnail_small") val thumbnailSmall: String? = null,
    val date: String? = null,
    // keep as String unless you've configured Gson date parsing
    @SerializedName("iso_date") val isoDate: String? = null
) : Parcelable

@Parcelize
data class RelatedTopic(
    @SerializedName("topic_token") val topicToken: String? = null,
    @SerializedName("serpapi_link") val serpapiLink: String? = null,
    val title: String? = null,
    val thumbnail: String? = null
) : Parcelable

@Parcelize
data class NewsSearchMetadata(
    val id: String? = null,
    val status: String? = null,
    @SerializedName("json_endpoint") val jsonEndpoint: String? = null,
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("processed_at") val processedAt: String? = null,
    @SerializedName("google_news_url") val googleNewsUrl: String? = null,
    @SerializedName("raw_html_file") val rawHtmlFile: String? = null,
    @SerializedName("total_time_taken") val totalTimeTaken: Double? = null
) : Parcelable

@Parcelize
data class NewsSearchParameters(
    val engine: String? = null,
    val gl: String? = null,
    val q: String? = null
) : Parcelable

@Parcelize
data class Source(
    val name: String? = null,
    val icon: String? = null,
    val authors: List<String>? = null
) : Parcelable
