package com.chaitupenju.popularmovies2

import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat

class MovieCriteriaFragment : PreferenceFragmentCompat(), OnSharedPreferenceChangeListener {


    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.sort_criteria_preference)

        val sharedPreferences = preferenceScreen.sharedPreferences
        val prefScreen = preferenceScreen
        val count = prefScreen.preferenceCount

        (0..count).forEach {
            val p = prefScreen.getPreference(it)
            if (p is ListPreference) {
                val value = sharedPreferences!!.getString(p.getKey(), "")
                setPreferenceSummary(p, value!!)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        preferenceScreen.sharedPreferences
            ?.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()

        preferenceScreen.sharedPreferences
            ?.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        val preference = findPreference<Preference>(
            key!!
        )
        val value = sharedPreferences!!.getString(preference!!.key, "")
        setPreferenceSummary(preference, value!!)
    }

    private fun setPreferenceSummary(preference: Preference, newValue: String) {
        if (preference is ListPreference) {
            val prefIndex = preference.findIndexOfValue(newValue)
            if (prefIndex >= 0) {
                preference.summary = preference.entries[prefIndex]
            }
        }
    }
}