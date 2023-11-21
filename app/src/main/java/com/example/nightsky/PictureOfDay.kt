package com.example.nightsky

import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

data class PictureOfDay(
    var copyright: String?,
    var date: String?,
    var explanation: String?,
    var hdurl: String?,
    var mediaType: String?,
    var serviceVersion: String?,
    var title: String?,
    var url: String?
)