package ac.uk.bangor.novelty.ensemble;

import ac.uk.bangor.novelty.Detector;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Will Faithfull
 */
@Slf4j
public class QuorumScheme implements VotingScheme {

    Set<Detector> voters = new HashSet<>();

    @Getter
    int votesFor;

    @Getter
    int votesAgainst;
    private double quorum;

    public QuorumScheme(double quorum) {
        if(quorum < 0 || quorum > 1.0) {
            throw new IllegalArgumentException("Quorum value must be between 0.0 and 1.0.");
        }
        this.quorum = quorum;
    }

    @Override
    public void registerVoter(Detector voter) {
        voters.add(voter);
    }

    @Override
    public void registerVoterWithWeight(Detector voter, double weight) {
        registerVoter(voter); // No weights needed in a simple quorum scheme. Delegate to simply register voter.
    }

    @Override
    public boolean getResult() {
        votesFor = 0;
        for(Detector voter : voters) {
            if(voter.isChangeDetected())
                votesFor++;
        }

        votesAgainst = voters.size() - votesFor;
        double percentage = votesFor / (double)voters.size();

        return percentage >= quorum;
    }
}
