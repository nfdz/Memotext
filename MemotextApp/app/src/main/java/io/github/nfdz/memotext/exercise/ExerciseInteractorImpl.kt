package io.github.nfdz.memotext.exercise

import android.content.Context
import android.support.v4.text.HtmlCompat
import io.github.nfdz.memotext.R
import io.github.nfdz.memotext.common.*
import timber.log.Timber

class ExerciseInteractorImpl(val context: Context) : ExerciseInteractor {

    override fun prepareExercise(content: String, level: Level, success: (exercise: Exercise) -> Unit, error: () -> Unit) {

        val wordsToHide = when(level) {
            Level.BRONZE -> { context.getStringFromPreferences(R.string.pref_lvl_bronze_words_key, R.string.pref_lvl_bronze_words_default) }
            Level.SILVER -> { context.getStringFromPreferences(R.string.pref_lvl_silver_words_key, R.string.pref_lvl_silver_words_default) }
            Level.GOLD -> { context.getStringFromPreferences(R.string.pref_lvl_gold_words_key, R.string.pref_lvl_gold_words_default) }
        }.toInt()

        doAsync {
            try {
                val result = ExerciseAlgorithmImpl().execute(content, wordsToHide)
                doMainThread { success(result) }
            } catch (e: Exception) {
                Timber.e(e, "Cannot generate exercise")
                doMainThread { error() }
            }
        }

    }

    override fun checkAnswers(title: String, content: String, level: Level, exercise: Exercise, exerciseAnswers: ExerciseAnswers, callback: (exerciseResult: ExerciseResult) -> Unit) {
        // TODO save
        doAsync {
            var slotsCount = 0
            var validAnswers = 0
            val solutionBld = StringBuilder()
            exercise.elements.forEachIndexed { index, element ->
                when(element) {
                    is TextElement -> { solutionBld.append(element.text) }
                    is SpaceElement -> { solutionBld.append(" ") }
                    is SlotElement -> {
                        slotsCount++
                        val answer = exerciseAnswers.answers[index] ?: ""
                        val validAnswer = element.text == answer
                        if (validAnswer) {
                            validAnswers++
                            solutionBld.append("<b><font color=\"#009900\">${element.text}</font></b>")
                        } else {
                            if (!answer.isEmpty()) {
                                solutionBld.append("<b><strike><font color=\"#CC0000\">$answer</font></strike></b>")
                                solutionBld.append(" ")
                            }
                            solutionBld.append("<b><u>${element.text}</u></b>")
                        }
                    }
                }
            }

            val percentage: Int = Math.ceil(100*validAnswers.toDouble()/slotsCount).toInt().bound(0, 100)
            val textSolution: CharSequence = HtmlCompat.fromHtml(solutionBld.toString(), HtmlCompat.FROM_HTML_MODE_LEGACY)
            doMainThread {
                callback(ExerciseResult(title, content, level, percentage, textSolution))
            }
        }
    }

}