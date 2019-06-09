package com.lpw.kotlinquiz.Common

import com.lpw.kotlinquiz.Fragments.QuestionFragment
import com.lpw.kotlinquiz.Model.Category
import com.lpw.kotlinquiz.Model.CurrentQuestion
import com.lpw.kotlinquiz.Model.Question

object Common {
    val TOTAL_TIME = 20 * 60 * 1000

    var answerSheetList:MutableList<CurrentQuestion> = ArrayList()
    var questionList:MutableList<Question> = ArrayList()
    var selectedCategory:Category?=null

    var fragmentList:MutableList<QuestionFragment> = ArrayList()

    var selected_values:MutableList<String> = ArrayList()

    var timer = 0
    var right_answer_count = 0
    var wrong_answer_count = 0
    var no_answer_count = 0
    var data_question = StringBuilder()

    enum class ANSWER_TYPE{
        NO_ANSWER,
        RIGHT_ANSWER,
        WRONG_ANSWER
    }
}