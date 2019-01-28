package com.bumptech.glide.request.animation;

import android.view.View;
import android.view.animation.Animation;

/**
 * A {@link GlideAnimation GlideAnimation} that can apply a
 * {@link Animation Animation} to a {@link View View} using
 * {@link View#startAnimation(Animation) View.startAnimation}.
 *
 * @param <R> The type of the resource displayed in the view that is animated
 */
public class ViewAnimation<R> implements GlideAnimation<R> {

    private final AnimationFactory animationFactory;

    /**
     * Constructs a new ViewAnimation that will start the given {@link Animation}.
     */
    ViewAnimation(AnimationFactory animationFactory) {
        this.animationFactory = animationFactory;
    }

    /**
     * Always clears the current animation on the view using {@link View#clearAnimation()}, then
     * starts the {@link Animation} given in the constructor using
     * {@link View#startAnimation(Animation)} and then returns {@code false} because
     * the animation does not actually set the current resource on the view.
     *
     * @param current {@inheritDoc}
     * @param adapter {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public boolean animate(R current, ViewAdapter adapter) {
        View view = adapter.getView();
        if (view != null) {
            view.clearAnimation();
            Animation animation = animationFactory.build();
            view.startAnimation(animation);
        }

        return false;
    }

    interface AnimationFactory {
        Animation build();
    }
}
