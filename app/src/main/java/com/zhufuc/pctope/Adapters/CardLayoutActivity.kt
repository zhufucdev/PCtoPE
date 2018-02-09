package com.zhufuc.pctope.Adapters

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.MenuItem
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import com.zhufuc.pctope.Activities.BaseActivity
import com.zhufuc.pctope.R
import com.zhufuc.pctope.View.TitleCard

@SuppressLint("Registered")
/**
 * Created by zhufu on 1/25/18.
 */
open class CardLayoutActivity : BaseActivity() {

    lateinit var neededCard : TitleCard
    lateinit var extraCard: TitleCard
    lateinit var contentRecyclerView: RecyclerView

    var adapterForNeededCard : RecyclerView.Adapter<*>? = null
    set(value) {
        neededCard.recyclerView.adapter = value
        neededCard.recyclerView.layoutManager = LinearLayoutManager(this)
    }
    var adapterForExtraCard : RecyclerView.Adapter<*>? = null
    set(value) {
        extraCard.recyclerView.adapter = value
        extraCard.recyclerView.layoutManager = LinearLayoutManager(this)
    }

    lateinit var loadingMsg : LinearLayout
    fun showLoading(){
        loadingMsg.visibility = View.VISIBLE
        loadingMsg.startAnimation(AnimationUtils.loadAnimation(this,R.anim.cards_show))
    }
    fun hideLoading(){
        val animation = AnimationUtils.loadAnimation(this,R.anim.cards_hide)
        loadingMsg.startAnimation(animation)
        animation.setAnimationListener(object : Animation.AnimationListener{
            override fun onAnimationRepeat(animation: Animation?) {
            }

            override fun onAnimationEnd(animation: Animation?) {
                loadingMsg.visibility = View.INVISIBLE
            }

            override fun onAnimationStart(animation: Animation?) {
            }

        })
    }

    public var mode: Int
    set(value) {
        when(value){
            MODE_LOADING -> {
                neededCard.visibility = View.GONE
                extraCard.visibility = View.GONE
                contentRecyclerView.visibility = View.GONE
                showLoading()
            }
            MODE_TWO_CARDS -> {
                neededCard.visibility = View.VISIBLE
                extraCard.visibility = View.VISIBLE
                contentRecyclerView.visibility = View.GONE
                hideLoading()
            }
            MODE_CONTENT -> {
                neededCard.visibility = View.GONE
                extraCard.visibility = View.GONE
                contentRecyclerView.visibility = View.VISIBLE
                hideLoading()
            }
        }
    }
    get() = if (neededCard.visibility == View.VISIBLE && extraCard.visibility == View.VISIBLE) MODE_TWO_CARDS else MODE_CONTENT

    override fun onCreate(bundle: Bundle?) {
        super.onCreate(bundle)
        setContentView(R.layout.activity_jsonedit)
        neededCard = findViewById(R.id.card_needed)
        extraCard = findViewById(R.id.card_extra)
        contentRecyclerView = findViewById(R.id.recycler_view)
        loadingMsg = findViewById(R.id.loading_msg)

        mode = MODE_TWO_CARDS

        initToolbar()
    }

    fun initToolbar(){
        val actionBar = supportActionBar ?:return
        actionBar.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId){
            android.R.id.home -> onBackPressed()
        }
        return true
    }

    companion object {
        const val MODE_TWO_CARDS = 0
        const val MODE_CONTENT = 1
        const val MODE_LOADING = 2
    }
}