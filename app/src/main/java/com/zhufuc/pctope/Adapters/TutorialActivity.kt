package com.zhufuc.pctope.Adapters

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.os.PersistableBundle
import android.support.annotation.LayoutRes
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import android.widget.FrameLayout

/**
 * Created by zhufu on 17-10-21.
 */
abstract class TutorialActivity(LayoutRes : Array<Int>) : AppCompatActivity(){
    private val res = LayoutRes
    var Layouts = ArrayList<TutorialLayout>()
    var showingPostition = 0
    lateinit var animationLeftToCenter : Animation
    lateinit var animationCenterToLeft : Animation
    lateinit var animationRightToCenter : Animation
    lateinit var animationCenterToRight : Animation

    open class TutorialLayout(var view: View) {
        var parmas = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT, Gravity.START)
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        for (i in 0 until res.size){
            Layouts.add(TutorialLayout(LayoutInflater.from(this).inflate(res[i],null)))
            Log.i("Tutorial","Added layout ${res[i]}.")
        }

        animationLeftToCenter = TranslateAnimation(-resources.displayMetrics.widthPixels.toFloat(),0f,0f,0f)
        animationLeftToCenter.duration = 400

        animationCenterToLeft = TranslateAnimation(0f,-resources.displayMetrics.widthPixels.toFloat(),0f,0f)
        animationCenterToLeft.duration = 400

        animationRightToCenter = TranslateAnimation(resources.displayMetrics.widthPixels.toFloat(),0f,0f,0f)
        animationRightToCenter.duration = 400

        animationCenterToRight = TranslateAnimation(0f,resources.displayMetrics.widthPixels.toFloat(),0f,0f)
        animationCenterToRight.duration = 400

        super.onCreate(savedInstanceState)

        onPageSwitched()
    }

    fun show(index : Int){
        if (Layouts.size>index && index>=0){
            if (index>showingPostition){
                //Next
                Layouts[showingPostition].view.startAnimation(animationCenterToLeft)
                addContentView(Layouts[index].view,Layouts[index].parmas)
                Layouts[index].view.startAnimation(animationRightToCenter)

                animationRightToCenter.setAnimationListener(object : Animation.AnimationListener{
                    override fun onAnimationRepeat(animation: Animation?) {}

                    override fun onAnimationEnd(animation: Animation?) {
                        (Layouts[showingPostition].view.parent as ViewGroup).removeView(Layouts[showingPostition].view)
                        animationRightToCenter.setAnimationListener(null)

                        showingPostition++
                        onPageSwitched()
                    }

                    override fun onAnimationStart(animation: Animation?) {}

                })


            }
            else if (index<showingPostition){
                //Back
                Layouts[showingPostition].view.startAnimation(animationCenterToRight)
                addContentView(Layouts[index].view,Layouts[index].parmas)
                Layouts[index].view.startAnimation(animationLeftToCenter)

                animationCenterToRight.setAnimationListener(object : Animation.AnimationListener{
                    override fun onAnimationRepeat(animation: Animation?) {}

                    override fun onAnimationEnd(animation: Animation?) {
                        (Layouts[showingPostition].view.parent as ViewGroup).removeView(Layouts[showingPostition].view)
                        animationCenterToRight.setAnimationListener(null)

                        showingPostition--
                        onPageSwitched()
                    }

                    override fun onAnimationStart(animation: Animation?) {}

                })

            }
            else if (index==showingPostition){
                setContentView(Layouts[index].view)
            }
        }

    }

    fun next(){
        show(showingPostition+1)
    }

    fun back(){
        show(showingPostition-1)
    }

    override fun <T : View?> findViewById(id: Int): T  = Layouts[showingPostition].view.findViewById<T>(id)



    abstract fun onPageSwitched()
}