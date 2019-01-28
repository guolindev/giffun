package com.bumptech.glide.request.animation;

/**
 * A simple {@link GlideAnimation} that performs no actions.
 *
 * @param <R> animated resource type
 */
public class NoAnimation<R> implements GlideAnimation<R> {
    private static final NoAnimation<?> NO_ANIMATION = new NoAnimation<Object>();
    @SuppressWarnings("rawtypes")
    private static final GlideAnimationFactory<?> NO_ANIMATION_FACTORY = new NoAnimationFactory();

    /**
     * A factory that always returns the same {@link NoAnimation}.
     */
    public static class NoAnimationFactory<R> implements GlideAnimationFactory<R> {
        @SuppressWarnings("unchecked")
        @Override
        public GlideAnimation<R> build(boolean isFromMemoryCache, boolean isFirstResource) {
            return (GlideAnimation<R>) NO_ANIMATION;
        }
    }

    /**
     * Returns an instance of a factory that produces {@link NoAnimation}s.
     */
    @SuppressWarnings("unchecked")
    public static <R> GlideAnimationFactory<R> getFactory() {
        return (GlideAnimationFactory<R>) NO_ANIMATION_FACTORY;
    }

    /**
     * Returns an instance of {@link NoAnimation}.
     */
    @SuppressWarnings("unchecked")
    public static <R> GlideAnimation<R> get() {
        return (GlideAnimation<R>) NO_ANIMATION;
    }

    /**
     * Performs no animation and always returns {@code false}.
     */
    @Override
    public boolean animate(Object current, ViewAdapter adapter) {
        return false;
    }
}
