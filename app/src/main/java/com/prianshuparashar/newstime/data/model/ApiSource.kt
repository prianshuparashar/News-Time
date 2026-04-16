package com.prianshuparashar.newstime.data.model

import com.google.gson.annotations.SerializedName

data class ApiSource(
    @SerializedName("id") val id: String?,
    @SerializedName("name") val name: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("url") val url: String?,
    @SerializedName("category") val category: String?,
    @SerializedName("language") val language: String?,
    @SerializedName("country") val country: String?
)
