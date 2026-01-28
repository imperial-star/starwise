/*
 * This is the source code of Starwise for Android v. 10.x.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Gleb Obitocjkiy, 2026.
 */

package org.gua.imper

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

data class Roadmap(val name: String, val description: String, val filePath: String)

object RoadmapManager {

    private const val PREFS_NAME = "RoadmapPrefs"
    private const val KEY_ROADMAPS = "roadmaps"

    fun getRoadmaps(context: Context): List<Roadmap> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val jsonString = prefs.getString(KEY_ROADMAPS, null) ?: return emptyList()

        val roadmaps = mutableListOf<Roadmap>()
        val jsonArray = JSONArray(jsonString)
        for (i in 0 until jsonArray.length()) {
            val jsonObject = jsonArray.getJSONObject(i)
            roadmaps.add(
                Roadmap(
                    name = jsonObject.getString("name"),
                    description = jsonObject.getString("description"),
                    filePath = jsonObject.getString("filePath")
                )
            )
        }
        return roadmaps
    }

    fun addRoadmap(context: Context, roadmap: Roadmap) {
        val roadmaps = getRoadmaps(context).toMutableList()
        roadmaps.removeAll { it.name == roadmap.name }
        roadmaps.add(0, roadmap)
        saveRoadmaps(context, roadmaps)
    }

    fun deleteRoadmap(context: Context, roadmapToDelete: Roadmap) {
        try {
            File(roadmapToDelete.filePath).delete()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val roadmaps = getRoadmaps(context).toMutableList()
        roadmaps.removeAll { it.filePath == roadmapToDelete.filePath }
        saveRoadmaps(context, roadmaps)
    }

    private fun saveRoadmaps(context: Context, roadmaps: List<Roadmap>) {
        val jsonArray = JSONArray()
        roadmaps.forEach {
            val jsonObject = JSONObject()
            jsonObject.put("name", it.name)
            jsonObject.put("description", it.description)
            jsonObject.put("filePath", it.filePath)
            jsonArray.put(jsonObject)
        }

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_ROADMAPS, jsonArray.toString()).apply()
    }
}
