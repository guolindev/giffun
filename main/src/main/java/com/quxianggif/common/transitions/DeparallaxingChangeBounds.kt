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
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.TargetApi
import android.content.Context
import android.graphics.Rect
import android.os.Build
import android.transition.ChangeBounds
import android.transition.TransitionValues
import android.util.AttributeSet
import android.view.ViewGroup

import com.quxianggif.feeds.view.ParallaxImageView

/**
 * An extension to [ChangeBounds] designed to work with [ParallaxImageView]. This
 * will remove any parallax applied while also performing a `ChangeBounds` transition.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
class DeparallaxingChangeBounds(context: Context, attrs: AttributeSet) : ChangeBounds(context, attrs) {

    override fun captureEndValues(transitionValues: TransitionValues) {
        super.captureEndValues(transitionValues)
        if (transitionValues.view !is ParallaxImageView) return
        val piv = transitionValues.view as ParallaxImageView
        if (piv.offset == 0) return

        val bounds = transitionValues.values[PROPNAME_BOUNDS] as Rect
        bounds.offset(0, piv.offset)
        transitionValues.values[PROPNAME_BOUNDS] = bounds
    }

    override fun createAnimator(sceneRoot: ViewGroup,
                                startValues: TransitionValues?,
                                endValues: TransitionValues?): Animator {
        val changeBounds = super.createAnimator(sceneRoot, startValues, endValues)
        if (startValues == null || endValues == null
                || endValues.view !is ParallaxImageView)
            return changeBounds
        val psv = endValues.view as ParallaxImageView
        if (psv.offset == 0) return changeBounds

        val deparallax = ObjectAnimator.ofInt(psv, ParallaxImageView.OFFSET, 0)
        val transition = AnimatorSet()
        transition.playTogether(changeBounds, deparallax)
        return transition
    }

    companion object {

        private const val PROPNAME_BOUNDS = "android:changeBounds:bounds"
    }
}
