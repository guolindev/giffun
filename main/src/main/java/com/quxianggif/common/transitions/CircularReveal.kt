/*
 * Copyright 2016 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.quxianggif.common.transitions

import android.animation.Animator
import android.annotation.TargetApi
import android.content.Context
import android.graphics.Point
import android.os.Build
import androidx.annotation.IdRes
import android.transition.TransitionValues
import android.transition.Visibility
import android.util.AttributeSet
import android.view.View
import android.view.ViewAnimationUtils
import android.view.ViewGroup
import com.quxianggif.R
import com.quxianggif.util.AnimUtils

/**
 * A transition which shows/hides a view with a circular clipping mask. Callers should provide the
 * center point of the reveal either [directly][.setCenter] or by
 * [specifying][.centerOn] another view to center on; otherwise the target `view`'s
 * pivot point will be used.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
class CircularReveal : Visibility {

    private var center: Point? = null
    private var startRadius: Float = 0.toFloat()
    private var endRadius: Float = 0.toFloat()
    @IdRes
    private var centerOnId = View.NO_ID
    private var centerOn: View? = null

    constructor() : super() {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        val a = context.obtainStyledAttributes(attrs, R.styleable.CircularReveal)
        startRadius = a.getDimension(R.styleable.CircularReveal_startRadius, 0f)
        endRadius = a.getDimension(R.styleable.CircularReveal_endRadius, 0f)
        centerOnId = a.getResourceId(R.styleable.CircularReveal_centerOn, View.NO_ID)
        a.recycle()
    }

    /**
     * The center point of the reveal or conceal, relative to the target `view`.
     */
    fun setCenter(center: Point) {
        this.center = center
    }

    /**
     * Center the reveal or conceal on this view.
     */
    fun centerOn(source: View) {
        centerOn = source
    }

    /**
     * Sets the radius that **reveals** start from.
     */
    fun setStartRadius(startRadius: Float) {
        this.startRadius = startRadius
    }

    /**
     * Sets the radius that **conceals** end at.
     */
    fun setEndRadius(endRadius: Float) {
        this.endRadius = endRadius
    }

    override fun onAppear(sceneRoot: ViewGroup, view: View?,
                          startValues: TransitionValues,
                          endValues: TransitionValues): Animator? {
        if (view == null || view.height == 0 || view.width == 0) return null
        ensureCenterPoint(sceneRoot, view)
        return AnimUtils.NoPauseAnimator(ViewAnimationUtils.createCircularReveal(
                view,
                center!!.x,
                center!!.y,
                startRadius,
                getFullyRevealedRadius(view)))
    }

    override fun onDisappear(sceneRoot: ViewGroup, view: View?,
                             startValues: TransitionValues,
                             endValues: TransitionValues): Animator? {
        if (view == null || view.height == 0 || view.width == 0) return null
        ensureCenterPoint(sceneRoot, view)
        return AnimUtils.NoPauseAnimator(ViewAnimationUtils.createCircularReveal(
                view,
                center!!.x,
                center!!.y,
                getFullyRevealedRadius(view),
                endRadius))
    }

    private fun ensureCenterPoint(sceneRoot: ViewGroup, view: View) {
        if (center != null) return
        if (centerOn != null || centerOnId != View.NO_ID) {
            val source: View? = if (centerOn != null) {
                centerOn
            } else {
                sceneRoot.findViewById(centerOnId)
            }
            if (source != null) {
                // use window location to allow views in diff hierarchies
                val loc = IntArray(2)
                source.getLocationInWindow(loc)
                val srcX = loc[0] + source.width / 2
                val srcY = loc[1] + source.height / 2
                view.getLocationInWindow(loc)
                center = Point(srcX - loc[0], srcY - loc[1])
            }
        }
        // else use the pivot point
        if (center == null) {
            center = Point(Math.round(view.pivotX), Math.round(view.pivotY))
        }
    }

    private fun getFullyRevealedRadius(view: View): Float {
        return Math.hypot(
                Math.max(center!!.x, view.width - center!!.x).toDouble(),
                Math.max(center!!.y, view.height - center!!.y).toDouble()).toFloat()
    }
}
