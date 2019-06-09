package com.lpw.kotlinquiz.UI

import android.drm.DrmStore
import android.os.Bundle
import android.os.CountDownTimer
import android.support.constraint.ConstraintLayout
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v4.view.ViewPager
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog
import com.lpw.kotlinquiz.Adapter.GridAnswerAdapter
import com.lpw.kotlinquiz.Adapter.MyFragmentAdapter
import com.lpw.kotlinquiz.Common.Common
import com.lpw.kotlinquiz.DBHelper.DBHelper
import com.lpw.kotlinquiz.Fragments.QuestionFragment
import com.lpw.kotlinquiz.Model.CurrentQuestion
import com.lpw.kotlinquiz.R
import kotlinx.android.synthetic.main.content_main_question.*
import java.util.concurrent.TimeUnit

class MainQuestionActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    lateinit var countDownTimer: CountDownTimer
    var time_play = Common.TOTAL_TIME
    lateinit var adapter : GridAnswerAdapter

    lateinit var txt_wrong_answer:TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_question)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navView.setNavigationItemSelectedListener(this)

        genQuestion()

        if(Common.questionList.size > 0){
            txt_timer.visibility = View.VISIBLE
            txt_right_answer.visibility = View.VISIBLE

            countTimer()
            genItems()
            grid_answer.setHasFixedSize(true)
            if(Common.questionList.size > 0)
                grid_answer.layoutManager = GridLayoutManager(this,
                    if(Common.questionList.size>5)Common.questionList.size/2
                else Common.questionList.size)

            adapter = GridAnswerAdapter(this,Common.answerSheetList)
            grid_answer.adapter = adapter

            //generate fragment list
            genFragmentList()

            val fragmentAdapter = MyFragmentAdapter(supportFragmentManager, this, Common.fragmentList)
            view_pager.offscreenPageLimit = Common.questionList.size
            view_pager.adapter = fragmentAdapter

            sliding_tabs.setupWithViewPager(view_pager)

            //event
            view_pager.addOnPageChangeListener(object:ViewPager.OnPageChangeListener{

                val SCROLLING_RIGHT = 0
                val SCROLLING_LEFT = 1
                val SCROLLING_UBDETERMINED = 2

                var currentScrollDirection = SCROLLING_UBDETERMINED

                private val isScrollingDirectionUndetermined:Boolean
                get() = currentScrollDirection == SCROLLING_UBDETERMINED

                private val isScrollingDirectionRight:Boolean
                get() = currentScrollDirection == SCROLLING_RIGHT

                private val isScrollingDirectionLeft:Boolean
                    get() = currentScrollDirection == SCROLLING_LEFT

                private fun setScrollingDirection(positionOffset:Float){
                    if(1-positionOffset >=.5f)
                        this.currentScrollDirection = SCROLLING_RIGHT
                    else if(1-positionOffset <=.5f)
                    this.currentScrollDirection = SCROLLING_LEFT
                }

                override fun onPageScrollStateChanged(p0: Int) {
                    if(p0 == ViewPager.SCROLL_STATE_IDLE)
                        this.currentScrollDirection = SCROLLING_UBDETERMINED
                }

                override fun onPageScrolled(p0: Int, p1: Float, p2: Int) {
                    if(isScrollingDirectionUndetermined)
                        setScrollingDirection(p1)
                }

                override fun onPageSelected(pos: Int) {
                    val questionFragment: QuestionFragment
                    var position = 0
                    if(pos > 0){
                        if (isScrollingDirectionRight)
                        {
                            questionFragment = Common.fragmentList[pos-1]
                            position = pos - 1
                        }else if(isScrollingDirectionLeft){
                            questionFragment = Common.fragmentList[pos + 1]
                            position = pos + 1
                        }else{
                            questionFragment = Common.fragmentList[pos]
                        }
                    }else{
                        questionFragment = Common.fragmentList[0]
                        position = 0
                    }

                    if(Common.answerSheetList[position].type == Common.ANSWER_TYPE.NO_ANSWER)
                    {
                        val question_state: CurrentQuestion = questionFragment.selectedAnswer()
                        Common.answerSheetList[position] = question_state
                        adapter.notifyDataSetChanged()

                        countCorrectAnswer()

                        txt_right_answer.text = ("${Common.right_answer_count} / ${Common.questionList.size}")
                        txt_wrong_answer.text = ("${Common.wrong_answer_count}")

                        if(question_state.type != Common.ANSWER_TYPE.NO_ANSWER)
                        {
                            questionFragment.showCorrectAnswer()
                            questionFragment.disableAnswer()
                        }
                    }

                }

            })
        }

    }

    private fun countCorrectAnswer() {
        Common.right_answer_count = 0
        Common.wrong_answer_count = 0

        for (item in Common.answerSheetList)
            if (item.type == Common.ANSWER_TYPE.RIGHT_ANSWER)
                Common.right_answer_count++
        else if(item.type == Common.ANSWER_TYPE.WRONG_ANSWER)
                Common.wrong_answer_count++
    }

    private fun genFragmentList() {
        for (i in Common.questionList.indices){
            val bundle = Bundle()
            bundle.putInt("index",i)
            val fragment = QuestionFragment()
            fragment.arguments = bundle

            Common.fragmentList.add(fragment)
        }
    }

    private fun genItems() {
        for(i in Common.questionList.indices){
            Common.answerSheetList.add(CurrentQuestion(i, Common.ANSWER_TYPE.NO_ANSWER))
        }
    }

    private fun countTimer() {
        countDownTimer = object:CountDownTimer(Common.TOTAL_TIME.toLong(),1000){
            override fun onFinish() {
                finishGame()
            }

            override fun onTick(interval: Long) {
                txt_timer.text = (java.lang.String.format("%02d:%02d",
                    TimeUnit.MILLISECONDS.toMinutes(interval),
                TimeUnit.MILLISECONDS.toSeconds(interval) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(interval))))
                time_play-=1000
            }

        }
    }

    private fun finishGame() {
        val position = view_pager.currentItem
        val questionFragment = Common.fragmentList[position]

        val question_state: CurrentQuestion = questionFragment.selectedAnswer()

        Common.answerSheetList[position] = question_state
        adapter.notifyDataSetChanged()

        countCorrectAnswer()

        txt_right_answer.text = ("${Common.right_answer_count} / ${Common.questionList.size}")
        txt_wrong_answer.text = ("${Common.wrong_answer_count}")

        if(question_state.type != Common.ANSWER_TYPE.NO_ANSWER)
        {
            questionFragment.showCorrectAnswer()
            questionFragment.disableAnswer()
        }
    }

    private fun genQuestion() {
        Common.questionList = DBHelper.getInstance(this).getQuestionByCategories(Common.selectedCategory!!.id)

        if (Common.questionList.size==0){
            MaterialStyledDialog.Builder(this)
                .setTitle("Ooops!")
                .setIcon(R.drawable.ic_sentiment_dissatisfied_black_24dp)
                .setDescription("We don't have any question in this ${Common.selectedCategory!!.name} category")
                .setPositiveText("Ok")
                .onPositive{ dialog, _ ->
                    dialog.dismiss()
                    finish()
                }.show()
        }
    }

    override fun onBackPressed() {
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        val item:MenuItem = menu!!.findItem(R.id.menu_wrong_answer)
        val layout = item.actionView as ConstraintLayout
        txt_wrong_answer = layout.findViewById(R.id.txt_wrong_answer) as TextView
        txt_wrong_answer.text = 0.toString()
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.question, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return super.onOptionsItemSelected(item)
    }


    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.nav_home -> {
                // Handle the camera action
            }
            R.id.nav_gallery -> {

            }
            R.id.nav_slideshow -> {

            }
            R.id.nav_tools -> {

            }
            R.id.nav_share -> {

            }
            R.id.nav_send -> {

            }
        }
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }
}
