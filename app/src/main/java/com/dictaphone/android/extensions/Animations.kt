package com.dictaphone.android.extensions

import android.animation.Animator
import android.app.Activity
import android.view.View
import android.view.ViewAnimationUtils
import android.view.ViewTreeObserver

/**
 * This Extenion provides useful animation functionality to UI
 */
fun Activity.revealAnimateWhenReady() {
    val root = findViewById<View>(android.R.id.content)
    root.visibility = View.INVISIBLE
    val viewTreeObserver = root.viewTreeObserver
    if (viewTreeObserver.isAlive) {
        viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                revealAnimation()
                root.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })
    }
}

fun Activity.revealAnimation() {
    val root = findViewById<View>(android.R.id.content)

    val cx: Int = root.right - dp(44)
    val cy: Int = root.bottom - dp(44)
    val finalRadius = root.width.coerceAtLeast(root.height)
    val circularReveal = ViewAnimationUtils.createCircularReveal(
        root,
        cx,
        cy,
        0f,
        finalRadius.toFloat()
    )
    circularReveal.duration = 200
    root.visibility = View.VISIBLE
    circularReveal.start()
}

fun Activity.reverseRevealAnimation() {
    val root = findViewById<View>(android.R.id.content)

    val cx: Int = root.width - dp(44)
    val cy: Int = root.bottom - dp(44)
    val finalRadius: Int = root.width.coerceAtLeast(root.height)
    val circularReveal = ViewAnimationUtils.createCircularReveal(root, cx, cy, finalRadius.toFloat(), 0f)
    circularReveal.addListener(object : Animator.AnimatorListener {
        override fun onAnimationStart(animator: Animator) {}
        override fun onAnimationEnd(animator: Animator) {
            root.visibility = View.INVISIBLE
            finish()
        }

        override fun onAnimationCancel(animator: Animator) {}
        override fun onAnimationRepeat(animator: Animator) {}
    })
    circularReveal.duration = 200
    circularReveal.start()
}