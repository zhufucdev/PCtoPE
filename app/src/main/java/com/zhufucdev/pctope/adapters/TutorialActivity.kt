package com.zhufucdev.pctope.adapters

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
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
    var layouts = ArrayList<TutorialLayout>()
    var showingPosition = 0
    var isInAnimations = false
    lateinit var animationLeftToCenter : Animation
    lateinit var animationCenterToLeft : Animation
    lateinit var animationRightToCenter : Animation
    lateinit var animationCenterToRight : Animation

    open class TutorialLayout(var view: View) {
        var parmas = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT, Gravity.START)
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        for (i in res.indices){
            layouts.add(TutorialLayout(LayoutInflater.from(this).inflate(res[i],null)))
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
        if (layouts.size>index && index>=0){
            if (index>showingPosition){
                //Next
                layouts[showingPosition].view.startAnimation(animationCenterToLeft)
                addContentView(layouts[index].view,layouts[index].parmas)
                layouts[index].view.startAnimation(animationRightToCenter)

                animationRightToCenter.setAnimationListener(object : Animation.AnimationListener{
                    override fun onAnimationRepeat(animation: Animation?) {}

                    override fun onAnimationEnd(animation: Animation?) {
                        (layouts[showingPosition].view.parent as ViewGroup).removeView(layouts[showingPosition].view)
                        animationRightToCenter.setAnimationListener(null)

                        showingPosition++
                        isInAnimations = false
                        onPageSwitched()
                    }

                    override fun onAnimationStart(animation: Animation?) {
                        isInAnimations = true
                    }

                })


            }
            else if (index<showingPosition){
                //Back
                layouts[showingPosition].view.startAnimation(animationCenterToRight)
                addContentView(layouts[index].view,layouts[index].parmas)
                layouts[index].view.startAnimation(animationLeftToCenter)

                animationCenterToRight.setAnimationListener(object : Animation.AnimationListener{
                    override fun onAnimationRepeat(animation: Animation?) {}

                    override fun onAnimationEnd(animation: Animation?) {
                        (layouts[showingPosition].view.parent as ViewGroup).removeView(layouts[showingPosition].view)
                        animationCenterToRight.setAnimationListener(null)

                        showingPosition--
                        isInAnimations = false
                        onPageSwitched()
                    }

                    override fun onAnimationStart(animation: Animation?) {
                        isInAnimations = true
                    }

                })

            }
            else if (index==showingPosition){
                setContentView(layouts[index].view)
            }
        }

    }

    fun next(){
        show(showingPosition+1)
    }

    fun back(){
        show(showingPosition-1)
    }

    override fun <T : View?> findViewById(id: Int): T  = layouts[showingPosition].view.findViewById<T>(id)



    abstract fun onPageSwitched()
}