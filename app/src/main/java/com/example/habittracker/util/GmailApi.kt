package com.example.habittracker.util

import android.content.Context
import android.util.Log
import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.util.store.FileDataStoreFactory
import com.google.api.services.gmail.Gmail
import com.google.api.services.gmail.GmailScopes
import java.io.File
import java.io.FileNotFoundException
import java.io.InputStreamReader


/* TODO: Use coroutines to make this asynchronous (https://developer.android.com/topic/libraries/architecture/coroutines)
    or alternatively, use this inside a service */

const val APPLICATION_NAME = "Gmail API Java Quickstart"
val SCOPES = listOf(GmailScopes.GMAIL_LABELS)
const val TOKENS_DIRECTORY_PATH = "tokens"
const val CREDENTIALS_FILE_PATH = "/credentials.json"
val JSON_FACTORY: JsonFactory = GsonFactory.getDefaultInstance()

class GmailApi {
    fun getCredentials(HTTP_TRANSPORT: NetHttpTransport, context: Context): Credential {
        // Load client secrets.
        val inputStream = GmailApi::class.java.getResourceAsStream(CREDENTIALS_FILE_PATH)
            ?: throw FileNotFoundException("Resource not found: $CREDENTIALS_FILE_PATH")
        val clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, InputStreamReader(inputStream))

        // create an internal folder for tokens
        val filesDir = context.filesDir
        val tokensDir = File(filesDir, TOKENS_DIRECTORY_PATH)
        if (!tokensDir.exists()) {
            tokensDir.mkdirs()
        }
        val tokenStoreFactory = FileDataStoreFactory(tokensDir)

        // Build flow and trigger user authorization request.
        val flow =
            GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(tokenStoreFactory)
                //.setAccessType("offline")
                .build()
        val receiver = LocalServerReceiver.Builder().setPort(8888).build()

        // returns an authorized Credential object.
        val app = AuthorizationCodeInstalledApp(flow, receiver)

        val credential = app.authorize("user")
        return credential
    }

    fun getLabels(context: Context) {
        // Build a new authorized API client service.
        val HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport()
        val service =
            Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT, context))
                .setApplicationName(APPLICATION_NAME)
                .build()

        // Print the labels in the user's account.
        val user = "me"
        val listResponse = service.users().labels().list(user).execute()
        val labels = listResponse.labels
        if (labels.isEmpty()) {
            Log.i("GMAIL", "No labels found.")
        } else {
            Log.i("GMAIL", "Labels:")
            for (label in labels) {
                Log.i("GMAIL", label.name)
            }
        }
    }
}