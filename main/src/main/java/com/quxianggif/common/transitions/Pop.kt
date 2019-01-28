/*
 * Copyright 2015 Google Inc.
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
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.transition.TransitionValues
import android.transition.Visibility
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewGroup

/**
 * A transition that animates the alpha, scale X & Y of a view simultaneously.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
class Pop(context: Context, attrs: AttributeSet) : Visibility(context, attrs) {

    override fun onAppear(sceneRoot: ViewGroup, view: View, startValues: TransitionValues,
                          endValues: TransitionValues): Animator {
        Log.d(TAG, "onAppear: sceneRoot $sceneRoot")
        view.alpha = 0f
        view.scaleX = 0f
        view.scaleY = 0f
        return ObjectAnimator.ofPropertyValuesHolder(
                view,
                PropertyValuesHolder.ofFloat(View.ALPHA, 1f),
                PropertyValuesHolder.ofFloat(View.SCALE_X, 1f),
                PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f))
    }

    override fun onDisappear(sceneRoot: ViewGroup, view: View, startValues: TransitionValues,
                             endValues: TransitionValues): Animator {
        Log.d(TAG, "onDisappear: sceneRoot $sceneRoot")
        return ObjectAnimator.ofPropertyValuesHolder(
                view,
                PropertyValuesHolder.ofFloat(View.ALPHA, 0f),
                PropertyValuesHolder.ofFloat(View.SCALE_X, 0f),
                PropertyValuesHolder.ofFloat(View.SCALE_Y, 0f))
    }

    override fun captureStartValues(transitionValues: TransitionValues) {
        Log.d(TAG, "captureStartValues: " + transitionValues.view)
        super.captureStartValues(transitionValues)
    }

    override fun captureEndValues(transitionValues: TransitionValues) {
        Log.d(TAG, "captureEndValues: " + transitionValues.view)
        super.captureEndValues(transitionValues)
    }

    override fun createAnimator(sceneRoot: ViewGroup, startValues: TransitionValues, endValues: TransitionValues): Animator {
        //        startValues.values.clear();
        //        startValues.values.putAll(endValues.values);
        Log.d(TAG, "createAnimator: startValues " + startValues.values)
        Log.d(TAG, "createAnimator: endValues " + endValues.values)
        val animator = super.createAnimator(sceneRoot, startValues, endValues)
        Log.d(TAG, "createAnimator: animator is $animator")
        return animator
    }

    companion object {

        private val TAG = "Pop"
    }


}