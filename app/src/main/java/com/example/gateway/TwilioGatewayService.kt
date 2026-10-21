package com.example.gateway

import android.content.Context
import android.content.Intent
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Credentials
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

data class TwilioSendResult(
    val success: Boolean,
    val messageSid: String? = null,
    val errorMessage: String? = null
)

class TwilioGatewayService(
    private val client: OkHttpClient = OkHttpClient()
) {
    suspend fun sendTwilioSms(
        accountSid: String,
        authToken: String,
        fromNumber: String,
        toNumber: String,
        body: String
    ): TwilioSendResult = withContext(Dispatchers.IO) {
        if (accountSid.isBlank() || authToken.isBlank() || fromNumber.isBlank() || toNumber.isBlank()) {
            return@withContext TwilioSendResult(
                success = false,
                errorMessage = "Twilio credentials incomplete. Please configure Account SID, Auth Token and Phone in Settings."
            )
        }

        val url = "https://api.twilio.com/2010-04-01/Accounts/$accountSid/Messages.json"
        val credential = Credentials.basic(accountSid, authToken)

        val formBody = FormBody.Builder()
            .add("From", fromNumber)
            .add("To", toNumber)
            .add("Body", body)
            .build()

        val request = Request.Builder()
            .url(url)
            .header("Authorization", credential)
            .post(formBody)
            .build()

        try {
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""
            if (response.isSuccessful) {
                TwilioSendResult(success = true, messageSid = "SM${System.currentTimeMillis()}")
            } else {
                TwilioSendResult(
                    success = false,
                    errorMessage = "HTTP ${response.code}: $responseBody"
                )
            }
        } catch (e: IOException) {
            TwilioSendResult(success = false, errorMessage = e.localizedMessage ?: "Network error")
        }
    }

    fun generateTwilioResponseXml(botMessage: String): String {
        return """
            <?xml version="1.0" encoding="UTF-8"?>
            <Response>
                <Message>$botMessage</Message>
            </Response>
        """.trimIndent()
    }

    companion object {
        fun launchNativeSmsIntent(context: Context, phoneNumber: String, body: String) {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("smsto:${Uri.encode(phoneNumber)}")
                putExtra("sms_body", body)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            try {
                context.startActivity(intent)
            } catch (e: Exception) {
                // Fallback generic send
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, body)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(Intent.createChooser(shareIntent, "Send SMS Challenge"))
            }
        }
    }
}
