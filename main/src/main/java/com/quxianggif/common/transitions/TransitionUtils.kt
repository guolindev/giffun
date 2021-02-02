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

import android.annotation.TargetApi
import android.os.Build
import androidx.annotation.IdRes
import android.transition.Transition
import android.transition.TransitionSet
import android.view.View
import android.view.ViewGroup

import java.util.ArrayList

/**
 * Utility methods for working with transitions
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
object TransitionUtils {

    fun findTransition(
            set: TransitionSet, clazz: Class<out Transition>): Transition? {
        for (i in 0 until set.transitionCount) {
            val transition = set.getTransitionAt(i)
            if (transition.javaClass == clazz) {
                return transition
            }
            if (transition is TransitionSet) {
                val child = findTransition(transition, clazz)
                if (child != null) return child
            }
        }
        return null
    }

    fun findTransition(
            set: TransitionSet,
            clazz: Class<out Transition>,
            @IdRes targetId: Int): Transition? {
        for (i in 0 until set.transitionCount) {
            val transition = set.getTransitionAt(i)
            if (transition.javaClass == clazz) {
                if (transition.targetIds.contains(targetId)) {
                    return transition
                }
            }
            if (transition is TransitionSet) {
                val child = findTransition(transition, clazz, targetId)
                if (child != null) return child
            }
        }
        return null
    }

    fun setAncestralClipping(view: View, clipChildren: Boolean): List<Boolean> {
        return setAncestralClipping(view, clipChildren, ArrayList())
    }

    private fun setAncestralClipping(
            view: View, clipChildren: Boolean, was: MutableList<Boolean>): List<Boolean> {
        if (view is ViewGroup) {
            was.add(view.clipChildren)
            view.clipChildren = clipChildren
        }
        val parent = view.parent
        if (parent != null && parent is ViewGroup) {
            setAncestralClipping(parent, clipChildren, was)
        }
        return was
    }

    fun restoreAncestralClipping(view: View, was: MutableList<Boolean>) {
        if (view is ViewGroup) {
            view.clipChildren = was.removeAt(0)
        }
        val parent = view.parent
        if (parent != null && parent is ViewGroup) {
            restoreAncestralClipping(parent, was)
        }
    }

    open class TransitionListenerAdapter : Transition.TransitionListener {

        override fun onTransitionStart(transition: Transition) {}

        override fun onTransitionEnd(transition: Transition) {}

        override fun onTransitionCancel(transition: Transition) {}

        override fun onTransitionPause(transition: Transition) {}

        override fun onTransitionResume(transition: Transition) {}
    }
}
