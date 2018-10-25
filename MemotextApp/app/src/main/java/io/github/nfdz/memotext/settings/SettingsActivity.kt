package io.github.nfdz.memotext.settings

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.preference.Preference
import android.preference.PreferenceActivity
import android.preference.PreferenceFragment
import android.preference.PreferenceManager
import android.view.MenuItem
import android.view.View
import io.github.nfdz.memotext.R
import io.github.nfdz.memotext.common.showSnackbar
import timber.log.Timber

fun Context.startSettingsActivity(flags: Int = 0) {
    val starter = Intent(this, SettingsActivity::class.java)
    starter.flags = flags
    startActivity(starter)
}

class SettingsActivity : AppCompatPreferenceActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupActionBar()
    }

    private fun setupActionBar() {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onIsMultiPane(): Boolean {
        return isXLargeTablet(this)
    }

    private fun isXLargeTablet(context: Context): Boolean {
        return context.resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK >= Configuration.SCREENLAYOUT_SIZE_XLARGE
    }

    override fun onBuildHeaders(target: List<PreferenceActivity.Header>) {
        loadHeadersFromResource(R.xml.pref_headers, target)
    }

    /**
     * This method stops fragment injection in malicious applications.
     * Make sure to deny any unknown fragments here.
     */
    override fun isValidFragment(fragmentName: String): Boolean {
        return PreferenceFragment::class.java.name == fragmentName
                || LevelPreferenceFragment::class.java.name == fragmentName
                || AboutPreferenceFragment::class.java.name == fragmentName
    }

    class LevelPreferenceFragment : PreferenceFragment() {

        private val bindPreferenceSummaryToValueListener = Preference.OnPreferenceChangeListener { preference, value ->
            preference.summary = value.toString() + "%"
            true
        }

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            addPreferencesFromResource(R.xml.pref_level)
            setHasOptionsMenu(true)
            setupPreferences()
        }

        private fun setupPreferences() {
            val bronzeWordsKey = getString(R.string.pref_lvl_bronze_words_key)
            val bronzeLettersKey = getString(R.string.pref_lvl_bronze_letters_key)
            val silverWordsKey = getString(R.string.pref_lvl_silver_words_key)
            val silverLettersKey = getString(R.string.pref_lvl_silver_letters_key)
            val goldWordsKey = getString(R.string.pref_lvl_gold_words_key)
            val goldLettersKey = getString(R.string.pref_lvl_gold_letters_key)
            val bronzeWordsDefault = getString(R.string.pref_lvl_bronze_words_default)
            val bronzeLettersDefault = getString(R.string.pref_lvl_bronze_letters_default)
            val silverWordsDefault = getString(R.string.pref_lvl_silver_words_default)
            val silverLettersDefault = getString(R.string.pref_lvl_silver_letters_default)
            val goldWordsDefault = getString(R.string.pref_lvl_gold_words_default)
            val goldLettersDefault = getString(R.string.pref_lvl_gold_letters_default)
            bindPreferences(bronzeWordsKey, bronzeWordsDefault,
                bronzeLettersKey, bronzeLettersDefault,
                silverWordsKey, silverWordsDefault,
                silverLettersKey, silverLettersDefault,
                goldWordsKey, goldWordsDefault,
                goldLettersKey, goldLettersDefault)
            findPreference(getString(R.string.pref_lvl_restore_default_key)).setOnPreferenceClickListener {
                PreferenceManager.getDefaultSharedPreferences(it.context).edit()
                    .putString(bronzeWordsKey, bronzeWordsDefault)
                    .putString(bronzeLettersKey, bronzeLettersDefault)
                    .putString(silverWordsKey, silverWordsDefault)
                    .putString(silverLettersKey, silverLettersDefault)
                    .putString(goldWordsKey, goldWordsDefault)
                    .putString(goldLettersKey, goldLettersDefault)
                    .commit()
                bindPreferences(bronzeWordsKey, bronzeWordsDefault,
                    bronzeLettersKey, bronzeLettersDefault,
                    silverWordsKey, silverWordsDefault,
                    silverLettersKey, silverLettersDefault,
                    goldWordsKey, goldWordsDefault,
                    goldLettersKey, goldLettersDefault)
                true
            }
        }

        private fun bindPreferences(bronzeWordsKey: String, bronzeWordsDefault: String,
                                    bronzeLettersKey: String, bronzeLettersDefault: String,
                                    silverWordsKey: String, silverWordsDefault: String,
                                    silverLettersKey: String, silverLettersDefault: String,
                                    goldWordsKey: String, goldWordsDefault: String,
                                    goldLettersKey: String, goldLettersDefault: String) {
            bindPreferenceSummaryToValue(findPreference(bronzeWordsKey), bronzeWordsDefault)
            bindPreferenceSummaryToValue(findPreference(bronzeLettersKey), bronzeLettersDefault)
            bindPreferenceSummaryToValue(findPreference(silverWordsKey), silverWordsDefault)
            bindPreferenceSummaryToValue(findPreference(silverLettersKey), silverLettersDefault)
            bindPreferenceSummaryToValue(findPreference(goldWordsKey), goldWordsDefault)
            bindPreferenceSummaryToValue(findPreference(goldLettersKey), goldLettersDefault)
        }

        private fun bindPreferenceSummaryToValue(preference: Preference, defaultValue: String) {
            preference.onPreferenceChangeListener = bindPreferenceSummaryToValueListener
            bindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager.getDefaultSharedPreferences(preference.context).getString(preference.key, defaultValue)
            )
        }

        override fun onOptionsItemSelected(item: MenuItem): Boolean {
            return if(handleOptionsItemSelected(item)) { true } else { super.onOptionsItemSelected(item) }
        }
    }

    class AboutPreferenceFragment : PreferenceFragment() {

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            addPreferencesFromResource(R.xml.pref_about)
            setHasOptionsMenu(true)
            setupFeedbackPreference()
            setupRepoPreference()
        }

        private fun setupFeedbackPreference() {
            findPreference(getString(R.string.pref_about_feedback_key)).setOnPreferenceClickListener {
                try {
                    val email = getString(R.string.email_feedback_address)
                    val starter = Intent(Intent.ACTION_SEND)
                    starter.putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
                    starter.data = Uri.parse(email)
                    starter.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.email_feedback_subject));
                    starter.type = "plain/text"
                    startActivity(starter)
                } catch (e: Exception) {
                    Timber.e(e)
                    activity.findViewById<View>(android.R.id.content).showSnackbar(getString(R.string.email_feedback_error))
                }
                true
            }
        }

        private fun setupRepoPreference() {
            findPreference(getString(R.string.pref_about_repo_key)).setOnPreferenceClickListener {
                try {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.url_repo_website))))
                } catch (e: Exception) {
                    Timber.e(e)
                    activity.findViewById<View>(android.R.id.content).showSnackbar(getString(R.string.url_repo_error))
                }
                true
            }
        }

        override fun onOptionsItemSelected(item: MenuItem): Boolean {
            return if(handleOptionsItemSelected(item)) { true } else { super.onOptionsItemSelected(item) }
        }
    }

}

fun PreferenceFragment.handleOptionsItemSelected(item: MenuItem): Boolean {
    return if (item.itemId == android.R.id.home) {
        activity.startSettingsActivity(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        true
    } else {
        false
    }
}