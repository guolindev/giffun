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
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.annotation.TargetApi
import android.content.Context
import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.os.Build
import android.transition.Transition
import android.transition.TransitionValues
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.ImageView
import com.quxianggif.R

/**
 * A transition which sets a specified [Animatable] `drawable` on a target
 * [ImageView] and [starts][Animatable.start] it when the transition begins.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
class StartAnimatable : Transition {

    private val animatable: Animatable?

    constructor(animatable: Animatable) : super() {
        if (animatable !is Drawable) {
            throw IllegalArgumentException("Non-Drawable resource provided.")
        }
        this.animatable = animatable
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        val a = context.obtainStyledAttributes(attrs, R.styleable.StartAnimatable)
        val drawable = a.getDrawable(R.styleable.StartAnimatable_android_src)
        a.recycle()
        if (drawable is Animatable) {
            animatable = drawable
        } else {
            throw IllegalArgumentException("Non-Animatable resource provided.")
        }
    }

    override fun captureStartValues(transitionValues: TransitionValues) {
        // no-op
    }

    override fun captureEndValues(transitionValues: TransitionValues) {
        // no-op
    }

    override fun createAnimator(sceneRoot: ViewGroup,
                                startValues: TransitionValues,
                                endValues: TransitionValues?): Animator? {
        if (animatable == null || endValues == null
                || endValues.view !is ImageView)
            return null

        val iv = endValues.view as ImageView
        iv.setImageDrawable(animatable as Drawable?)

        // need to return a non-null Animator even though we just want to listen for the start
        val transition = ValueAnimator.ofInt(0, 1)
        transition.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator) {
                animatable.start()
            }
        })
        return transition
    }
}
