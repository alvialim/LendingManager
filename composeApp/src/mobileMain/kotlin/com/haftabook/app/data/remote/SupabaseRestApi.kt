package com.haftabook.app.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

/**
 * PostgREST over HTTPS. Uses anon key (RLS must allow operations for your policies).
 */
class SupabaseRestApi(
    private val client: HttpClient,
    private val config: SupabaseConfig,
    private val json: Json = Json { ignoreUnknownKeys = true; encodeDefaults = true }
) {
    suspend fun upsertCustomer(row: CustomerRemote): Result<Unit> = runCatching {
        val response = client.post("${config.restBase}/customers") {
            header("apikey", config.anonKey)
            header(HttpHeaders.Authorization, "Bearer ${config.anonKey}")
            header("Prefer", "return=minimal,resolution=merge-duplicates")
            contentType(ContentType.Application.Json)
            setBody(json.encodeToString(CustomerRemote.serializer(), row))
        }
        if (response.status.value !in 200..299) {
            error("upsert customer ${response.status} ${response.bodyAsText()}")
        }
    }

    suspend fun deleteCustomer(remoteId: String): Result<Unit> = runCatching {
        val response = client.delete("${config.restBase}/customers") {
            header("apikey", config.anonKey)
            header(HttpHeaders.Authorization, "Bearer ${config.anonKey}")
            parameter("id", "eq.$remoteId")
        }
        if (response.status.value !in 200..299) {
            error("delete customer ${response.status} ${response.bodyAsText()}")
        }
    }

    suspend fun fetchCustomersSince(updatedAfter: Long): List<CustomerRemote> {
        val text: String = client.get("${config.restBase}/customers") {
            header("apikey", config.anonKey)
            header(HttpHeaders.Authorization, "Bearer ${config.anonKey}")
            header(HttpHeaders.Accept, "application/json")
            parameter("updated_at", "gt.$updatedAfter")
            parameter("select", "*")
            parameter("order", "updated_at.asc")
        }.let { resp ->
            if (resp.status != HttpStatusCode.OK) error("fetch customers ${resp.status} ${resp.bodyAsText()}")
            resp.bodyAsText()
        }
        return json.decodeFromString(ListSerializer(CustomerRemote.serializer()), text)
    }

    suspend fun upsertLoan(row: LoanRemote): Result<Unit> = runCatching {
        val response = client.post("${config.restBase}/loans") {
            header("apikey", config.anonKey)
            header(HttpHeaders.Authorization, "Bearer ${config.anonKey}")
            header("Prefer", "return=minimal,resolution=merge-duplicates")
            contentType(ContentType.Application.Json)
            setBody(json.encodeToString(LoanRemote.serializer(), row))
        }
        if (response.status.value !in 200..299) {
            error("upsert loan ${response.status} ${response.bodyAsText()}")
        }
    }

    suspend fun deleteLoan(remoteId: String): Result<Unit> = runCatching {
        val response = client.delete("${config.restBase}/loans") {
            header("apikey", config.anonKey)
            header(HttpHeaders.Authorization, "Bearer ${config.anonKey}")
            parameter("id", "eq.$remoteId")
        }
        if (response.status.value !in 200..299) {
            error("delete loan ${response.status} ${response.bodyAsText()}")
        }
    }

    suspend fun fetchLoansSince(updatedAfter: Long): List<LoanRemote> {
        val text: String = client.get("${config.restBase}/loans") {
            header("apikey", config.anonKey)
            header(HttpHeaders.Authorization, "Bearer ${config.anonKey}")
            header(HttpHeaders.Accept, "application/json")
            parameter("updated_at", "gt.$updatedAfter")
            parameter("select", "*")
            parameter("order", "updated_at.asc")
        }.let { resp ->
            if (resp.status != HttpStatusCode.OK) error("fetch loans ${resp.status} ${resp.bodyAsText()}")
            resp.bodyAsText()
        }
        return json.decodeFromString(ListSerializer(LoanRemote.serializer()), text)
    }

    suspend fun upsertEmi(row: EmiRemote): Result<Unit> = runCatching {
        val response = client.post("${config.restBase}/emis") {
            header("apikey", config.anonKey)
            header(HttpHeaders.Authorization, "Bearer ${config.anonKey}")
            header("Prefer", "return=minimal,resolution=merge-duplicates")
            contentType(ContentType.Application.Json)
            setBody(json.encodeToString(EmiRemote.serializer(), row))
        }
        if (response.status.value !in 200..299) {
            error("upsert emi ${response.status} ${response.bodyAsText()}")
        }
    }

    suspend fun deleteEmi(remoteId: String): Result<Unit> = runCatching {
        val response = client.delete("${config.restBase}/emis") {
            header("apikey", config.anonKey)
            header(HttpHeaders.Authorization, "Bearer ${config.anonKey}")
            parameter("id", "eq.$remoteId")
        }
        if (response.status.value !in 200..299) {
            error("delete emi ${response.status} ${response.bodyAsText()}")
        }
    }

    suspend fun fetchEmisSince(updatedAfter: Long): List<EmiRemote> {
        val text: String = client.get("${config.restBase}/emis") {
            header("apikey", config.anonKey)
            header(HttpHeaders.Authorization, "Bearer ${config.anonKey}")
            header(HttpHeaders.Accept, "application/json")
            parameter("updated_at", "gt.$updatedAfter")
            parameter("select", "*")
            parameter("order", "updated_at.asc")
        }.let { resp ->
            if (resp.status != HttpStatusCode.OK) error("fetch emis ${resp.status} ${resp.bodyAsText()}")
            resp.bodyAsText()
        }
        return json.decodeFromString(ListSerializer(EmiRemote.serializer()), text)
    }
}
