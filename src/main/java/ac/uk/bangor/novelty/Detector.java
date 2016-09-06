package ac.uk.bangor.novelty;

/**
 * The basic abstraction for an online novelty detector. The expectation is that a detector will handle it's own
 * windowing scheme, and will be updated with examples from the stream. If the detector has not yet seen enough examples
 * to draw conclusions, it should return false from {@link #isChangeDetected()}.
 * @param <T>
 *              The stream example type.
 */
public interface Detector<T> {

    /**
     * Update this detector with the next example from the stream.
     * @param example
     *          The next example from the stream.
     */
    void update(T example);

    /**
     * Retrieve the decision of the detector about the last example it was provided.
     * @return
     *          True if change was detected on the last example, otherwise false.
     */
    boolean isChangeDetected();

}
