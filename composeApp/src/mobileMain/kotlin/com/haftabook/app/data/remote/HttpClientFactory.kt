package com.haftabook.app.data.remote

import io.ktor.client.HttpClient

expect fun createSupabaseHttpClient(): HttpClient
