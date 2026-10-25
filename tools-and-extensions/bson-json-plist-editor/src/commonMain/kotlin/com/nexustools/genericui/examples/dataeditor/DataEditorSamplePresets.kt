package com.nexustools.genericui.examples.dataeditor

/**
 * Built-in sample documents so users on any platform (including Web, Android, iOS, and Desktop)
 * can immediately test multi-tab JSON, BinaryJson (BSON), and BinaryPlist (`bplist00`) editing.
 */
object DataEditorSamplePresets {

    val USER_PROFILE_JSON: String = """
        {
          "userId": 1042,
          "username": "aria_vance",
          "displayName": "Aria Vance",
          "verified": true,
          "accountBalance": 284.75,
          "roles": [
            "developer",
            "maintainer",
            "reviewer"
          ],
          "preferences": {
            "theme": "dark-slate",
            "notificationsEnabled": true,
            "maxRecentTabs": 12
          },
          "lastLoginNote": null
        }
    """.trimIndent()

    val APP_MANIFEST_JSON: String = """
        {
          "appId": "com.nexustools.genericui.examples.dataeditor",
          "appName": "BSON / JSON / Binary Plist Editor",
          "version": "1.0.0",
          "supportedFormats": [
            ".json",
            ".bson",
            ".plist"
          ],
          "backends": {
            "windows": true,
            "macos": true,
            "gtk": true,
            "qt": true,
            "swing": true,
            "android": true,
            "ios": true,
            "web": true
          },
          "pwa": {
            "allowInstallAsWebApp": true,
            "display": "standalone",
            "themeColor": "#1E293B"
          }
        }
    """.trimIndent()

    val GAME_SAVE_JSON: String = """
        {
          "saveSlot": 1,
          "chapterTitle": "Citadel of Glass",
          "playTimeSeconds": 18420,
          "hardcoreMode": false,
          "hero": {
            "name": "Kaelen",
            "level": 18,
            "health": 420,
            "mana": 195,
            "critChance": 0.275
          },
          "inventory": [
            {
              "itemId": "blade_astral",
              "name": "Astral Longsword",
              "quantity": 1,
              "equipped": true
            },
            {
              "itemId": "elixir_mana",
              "name": "Greater Mana Elixir",
              "quantity": 5,
              "equipped": false
            }
          ]
        }
    """.trimIndent()

    fun createSampleTabState(
        tabId: String,
        presetName: String,
        format: DataFormat = DataFormat.JSON
    ): DataDocumentTabState {
        val (baseFileStem, jsonSource) = when (presetName.lowercase()) {
            "manifest", "app_manifest", "appmanifest" -> "app-manifest" to APP_MANIFEST_JSON
            "game", "game_save", "gamesave" -> "game-save" to GAME_SAVE_JSON
            else -> "user-profile" to USER_PROFILE_JSON
        }
        val state = DataDocumentTabState(
            tabId = tabId,
            title = "$baseFileStem${format.defaultExtension}",
            format = format
        )
        state.loadFromJsonString(jsonSource, markDirty = false)
        return state
    }
}
