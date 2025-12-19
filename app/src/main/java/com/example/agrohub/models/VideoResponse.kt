package com.example.agrohub.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class VideoResponse(
    @SerializedName("search_metadata") val searchMetadata: VideoSearchMetadata? = null,
    @SerializedName("search_parameters") val searchParameters: VideoSearchParameters? = null,
    @SerializedName("search_information") val searchInformation: SearchInformation? = null,
    @SerializedName("shorts_results") val shortsResults: List<ShortsResult>? = null,
    @SerializedName("video_results") val videoResults: List<VideoResult>? = null,
    @SerializedName("playlist_results") val playlistResults: List<PlaylistResult>? = null,
    @SerializedName("channels_new_to_you") val channelsNewToYou: List<ChannelsNewToYou>? = null,
    @SerializedName("people_also_search_for") val peopleAlsoSearchFor: PeopleAlsoSearchFor? = null,
    @SerializedName("from_related_searches") val fromRelatedSearches: List<FromRelatedSearch>? = null,
    val pagination: Pagination? = null,
    @SerializedName("serpapi_pagination") val serpapiPagination: SerpapiPagination? = null
) : Parcelable

@Parcelize
data class Channel(
    val name: String? = null,
    val link: String? = null,
    val verified: Boolean? = null,
    val thumbnail: String? = null
) : Parcelable

@Parcelize
data class ChannelsNewToYou(
    @SerializedName("position_on_page") val positionOnPage: String? = null,
    val title: String? = null,
    val link: String? = null,
    @SerializedName("serpapi_link") val serpapiLink: String? = null,
    val channel: Channel? = null,
    @SerializedName("published_date") val publishedDate: String? = null,
    val views: Int? = null,
    val length: String? = null,
    val description: String? = null,
    val thumbnail: Thumbnail? = null,
    val extensions: List<String>? = null
) : Parcelable

@Parcelize
data class FromRelatedSearch(
    @SerializedName("position_on_page") val positionOnPage: String? = null,
    val title: String? = null,
    val link: String? = null,
    @SerializedName("serpapi_link") val serpapiLink: String? = null,
    val channel: Channel? = null,
    @SerializedName("published_date") val publishedDate: String? = null,
    val views: Int? = null,
    val length: String? = null,
    val description: String? = null,
    val thumbnail: Thumbnail? = null,
    val extensions: List<String>? = null
) : Parcelable

@Parcelize
data class Pagination(
    val current: String? = null,
    val next: String? = null,
    @SerializedName("next_page_token") val nextPageToken: String? = null
) : Parcelable

@Parcelize
data class PeopleAlsoSearchFor(
    @SerializedName("position_on_page") val positionOnPage: Int? = null,
    val searches: List<Search>? = null
) : Parcelable

@Parcelize
data class PlaylistResult(
    @SerializedName("position_on_page") val positionOnPage: Int? = null,
    val title: String? = null,
    val link: String? = null,
    val channel: Channel? = null,
    val videos: List<Video>? = null,
    @SerializedName("video_count") val videoCount: Int? = null,
    val thumbnail: String? = null
) : Parcelable

@Parcelize
data class Search(
    val query: String? = null,
    val link: String? = null,
    val thumbnail: String? = null
) : Parcelable

@Parcelize
data class SearchInformation(
    @SerializedName("total_results") val totalResults: Int? = null,
    @SerializedName("video_results_state") val videoResultsState: String? = null
) : Parcelable

@Parcelize
data class VideoSearchMetadata(
    val id: String? = null,
    val status: String? = null,
    @SerializedName("json_endpoint") val jsonEndpoint: String? = null,
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("processed_at") val processedAt: String? = null,
    @SerializedName("youtube_url") val youtubeUrl: String? = null,
    @SerializedName("raw_html_file") val rawHtmlFile: String? = null,
    @SerializedName("total_time_taken") val totalTimeTaken: Double? = null
) : Parcelable

@Parcelize
data class VideoSearchParameters(
    val engine: String? = null,
    @SerializedName("search_query") val searchQuery: String? = null
) : Parcelable

@Parcelize
data class SerpapiPagination(
    val current: String? = null,
    val next: String? = null,
    @SerializedName("next_page_token") val nextPageToken: String? = null
) : Parcelable

@Parcelize
data class ShortsResult(
    @SerializedName("position_on_page") val positionOnPage: Int? = null,
    val shorts: List<ShortItem>? = null
) : Parcelable

@Parcelize
data class ShortItem(
    val title: String? = null,
    val link: String? = null,
    val thumbnail: String? = null,
    @SerializedName("views_original") val viewsOriginal: String? = null,
    val views: Int? = null,
    @SerializedName("video_id") val videoId: String? = null
) : Parcelable

@Parcelize
data class Thumbnail(
    @SerializedName("static") val mystatic: String? = null,
    val rich: String? = null
) : Parcelable

@Parcelize
data class Video(
    val title: String? = null,
    val length: String? = null,
    val link: String? = null
) : Parcelable

@Parcelize
data class VideoResult(
    @SerializedName("position_on_page") val positionOnPage: Int? = null,
    val title: String? = null,
    val link: String? = null,
    @SerializedName("serpapi_link") val serpapiLink: String? = null,
    val channel: Channel? = null,
    @SerializedName("published_date") val publishedDate: String? = null,
    val views: Int? = null,
    val length: String? = null,
    val description: String? = null,
    val thumbnail: Thumbnail? = null,
    val extensions: List<String>? = null
) : Parcelable
