package ac.uk.bangor.novelty.ensemble;

import ac.uk.bangor.novelty.Detector;

/**
 * Base abstraction for a voting scheme, so that the ensemble decision mechanism can be swapped in and out.
 * @author Will Faithfull
 */
public interface VotingScheme {

    /**
     * Register a voter with this voting scheme.
     *
     * Delegates to {@link #setWeight(Detector, double)} with a weight of 1.0.
     * @param voter
     *              Unique detector with a vote.
     */
    void registerVoter(Detector voter);

    /**
     * Register a voter with this voting scheme with the specified voting weight.
     * @param voter
     *              Unique detector with a vote.
     * @param weight
     *              Double precision value representing the influence of this voter. Default 1.0.
     */
    void setWeight(Detector voter, double weight);

    /**
     * Ask the voters for their decisions, apply the scheme, and work out the result.
     * @return
     *          The combiend decision of the voters, according to the scheme.
     */
    boolean getResult();

    /**
     * Called after {@link #getResult()}, provides the number of votes cast in favour.
     * @return
     *          The weighted number of votes cast in favour.
     */
    double getVotesFor();

    /**
     * Called after {@link #getResult()}, provides the number of votes cast against.
     * @return
     *          The weighted number of votes cast against.
     */
    double getVotesAgainst();

}
