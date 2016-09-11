package ac.uk.bangor.novelty.ensemble;

import ac.uk.bangor.novelty.Detector;

/**
 * @author Will Faithfull
 */
public interface VotingScheme {

    void registerVoter(Detector voter);

    void registerVoterWithWeight(Detector voter, double weight);

    boolean getResult();

    int getVotesFor();
    int getVotesAgainst();

}
