package ac.uk.bangor.novelty.ensemble;

import ac.uk.bangor.novelty.Detector;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a basic quorum scheme which can be configured to provide simple majority or weighted majority voting, to
 * various quorums.
 *
 * @author Will Faithfull
 */
@Slf4j
public class QuorumScheme implements VotingScheme {

    Map<Detector,Double> voters = new HashMap<>();

    @Getter
    double votesFor;

    @Getter
    double votesAgainst;
    private double quorum;

    /**
     * Construct a voting scheme with the specified quorum
     * @param quorum
     *              A value between 0.0 and 1.0 representing the size of the quorum that must be achieved to signal an
     *              outcome.
     */
    public QuorumScheme(double quorum) {
        if(quorum < 0 || quorum > 1.0) {
            throw new IllegalArgumentException("Quorum value must be between 0.0 and 1.0.");
        }
        this.quorum = quorum;
    }

    @Override
    public void registerVoter(Detector voter) {
        registerVoterWithWeight(voter, 1.0);
    }

    @Override
    public void registerVoterWithWeight(Detector voter, double weight) {
        voters.put(voter, weight);
    }

    @Override
    public boolean getResult() {
        votesFor = 0;
        double weightedTotal = 0;
        for(Map.Entry<Detector, Double> entry : voters.entrySet()) {
            Detector voter = entry.getKey();
            Double weight = entry.getValue();
            if(voter.isChangeDetected())
                votesFor += weight;

            weightedTotal += weight;
        }

        votesAgainst = weightedTotal - votesFor;
        double percentage = votesFor / weightedTotal;

        return percentage >= quorum;
    }
}
