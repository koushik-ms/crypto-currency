import java.util.ArrayList;
import java.util.Set;
import java.util.HashMap ;

/* CompliantNode refers to a node that follows the rules (not malicious)*/
public class CompliantNode implements Node {
    double p_graph ;
    double p_malicious ;
    double p_txDistribution ;
    int numRounds ;
    int numNodes ; // # nodes in graph
    boolean[] followees ;
    Set<Transaction> pendingTransactions ;

    public CompliantNode(double p_graph, double p_malicious, double p_txDistribution, int numRounds) {
        this.p_graph = p_graph ;
        this.p_malicious = p_malicious ;
        this.p_txDistribution = p_txDistribution ;
        this.numRounds = numRounds ;
        this.numNodes = 0 ;
        this.followees = null ;
        this.pendingTransactions = null ;
    }

    public void setFollowees(boolean[] followees) {
        this.numNodes = followees.length ;
        this.followees = followees ;
    }

    public void setPendingTransaction(Set<Transaction> pendingTransactions) {
        this.pendingTransactions = pendingTransactions ;
    }

    public Set<Transaction> sendToFollowers() {
        return this.pendingTransactions ;
    }

    public void receiveFromFollowees(Set<Candidate> candidates) {
        HashMap<Transaction, Integer> frequencyMap = new HashMap<>() ;
        int threshold = 1 ;

        for(Candidate c: candidates) {
            // If c doesn't exist in pendingTransactions, add to pending transaction
            // This is the most simple implementation.
            // if( !this.pendingTransactions.contains(c.tx) ) {
            //     this.pendingTransactions.add(c.tx) ;
            // }
            // A more reasonable approach would be:
            // Create HashMap<tx: Transaction, freq: Integer> and iterate through the candidate list
            // updating the frequency for any new tx not in pendingTransactions. At the end threshold
            // frequency based on p_graph, p_malicious and numNodes. Maybe we can use the current 
            // round number vs numRounds to progressively vary the threshold (increase/ decrease?)
            if( !this.pendingTransactions.contains(c.tx) ) {
                if( !frequencyMap.containsKey(c.tx) ) 
                    frequencyMap.put(c.tx, 1) ;
                else 
                    frequencyMap.put(c.tx, 1 + frequencyMap.get(c.tx)) ;
            }
        }

        threshold = (int) (this.numNodes * p_graph * p_malicious * p_txDistribution * 0.5 + 0.5d) ;
        for( Transaction tx: frequencyMap.keySet() ) {
            if( threshold <= frequencyMap.get(tx) ) 
                this.pendingTransactions.add(tx) ;
        }
    }
}
