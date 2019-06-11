package com.lpw.kotlinquiz.UI

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import com.lpw.kotlinquiz.Adapter.ResultGridAdapter
import com.lpw.kotlinquiz.Common.Common
import com.lpw.kotlinquiz.Common.SpaceItemDecoration
import com.lpw.kotlinquiz.R
import kotlinx.android.synthetic.main.activity_result.*
import java.util.concurrent.TimeUnit

class ResultActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        toolbar.title = "Result"
        setSupportActionBar(toolbar)

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)

        txt_time.text = java.lang.String.format("%02d:%02d",
            TimeUnit.MILLISECONDS.toMinutes(Common.timer.toLong()),
            TimeUnit.MILLISECONDS.toSeconds(Common.timer.toLong()) - TimeUnit.MINUTES.toSeconds(
                TimeUnit.MILLISECONDS.toMinutes(Common.timer.toLong())
            ))

        txt_right_answer.text = "${Common.right_answer_count}/${Common.questionList.size}"

        btn_filter_total.text = "${Common.questionList.size}"
        btn_filter_right.text = "${Common.right_answer_count}"
        btn_filter_wrong.text = "${Common.wrong_answer_count}"
        btn_no_answer.text = "${Common.no_answer_count}"

        val percent = Common.right_answer_count * 100 / Common.questionList.size
        if (percent > 80)
            txt_result.text = "EXCELLENT"
        else if (percent > 70)
            txt_result.text = "GOOD"
        else if (percent > 60)
            txt_result.text = "FAIR"
        else if (percent > 50)
            txt_result.text = "POOR"
        else if (percent > 40)
            txt_result.text = "BAD"
        else
            txt_result.text = "FAILING"

        btn_filter_total.setOnClickListener {
            val adapter = ResultGridAdapter(this, Common.answerSheetList)
            recycler_result.adapter = adapter
        }

        btn_no_answer.setOnClickListener {
            Common.answerSheetListFiltered.clear()

            for (currentQuestion in Common.answerSheetList)
                if(currentQuestion.type == Common.ANSWER_TYPE.NO_ANSWER)
                    Common.answerSheetListFiltered.add(currentQuestion)

            val adapter = ResultGridAdapter(this, Common.answerSheetListFiltered)
            recycler_result.adapter = adapter
        }

        val adapter = ResultGridAdapter(this, Common.answerSheetList)
        recycler_result.setHasFixedSize(true)
        recycler_result.layoutManager = GridLayoutManager(this, 4)
        recycler_result.addItemDecoration(SpaceItemDecoration(4))
        recycler_result.adapter = adapter
    }
}
