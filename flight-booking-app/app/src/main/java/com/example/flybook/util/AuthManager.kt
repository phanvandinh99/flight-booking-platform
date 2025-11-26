package com.example.flybook.util

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.flybook.data.api.ApiClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_prefs")

object AuthManager {
    private val TOKEN_KEY = stringPreferencesKey("auth_token")
    private val USER_EMAIL_KEY = stringPreferencesKey("user_email")
    
    fun getToken(context: Context): Flow<String?> {
        return context.dataStore.data.map { preferences ->
            preferences[TOKEN_KEY]
        }
    }
    
    suspend fun saveToken(context: Context, token: String) {
        context.dataStore.edit { preferences ->
            preferences[TOKEN_KEY] = token
        }
        ApiClient.setToken(token)
        Log.d("AuthManager", "Token saved and set in ApiClient (length: ${token.length})")
    }
    
    suspend fun clearToken(context: Context) {
        context.dataStore.edit { preferences ->
            preferences.remove(TOKEN_KEY)
            preferences.remove(USER_EMAIL_KEY)
        }
        ApiClient.setToken(null)
    }
    
    suspend fun saveUserEmail(context: Context, email: String) {
        context.dataStore.edit { preferences ->
            preferences[USER_EMAIL_KEY] = email
        }
    }
    
    fun getUserEmail(context: Context): Flow<String?> {
        return context.dataStore.data.map { preferences ->
            preferences[USER_EMAIL_KEY]
        }
    }
    
    suspend fun loadToken(context: Context) {
        val token = context.dataStore.data.first()[TOKEN_KEY]
        if (token != null) {
            ApiClient.setToken(token)
            Log.d("AuthManager", "Token loaded from DataStore and set in ApiClient (length: ${token.length})")
        } else {
            Log.w("AuthManager", "No token found in DataStore")
        }
    }
}

