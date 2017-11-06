import java.util.ArrayList ;

public class TxHandler {

    private UTXOPool utxoPool;

    /**
     * Creates a public ledger whose current UTXOPool (collection of unspent transaction outputs) is
     * {@code utxoPool}. This should make a copy of utxoPool by using the UTXOPool(UTXOPool uPool)
     * constructor.
     */
    public TxHandler(UTXOPool utxoPool) {
        this.utxoPool = new UTXOPool(utxoPool);
    }

    /**
     * @return true if:
     * (1) all outputs claimed by {@code tx} are in the current UTXO pool, 
     * (2) the signatures on each input of {@code tx} are valid,
     * (3) no UTXO is claimed multiple times by {@code tx},
     * (4) all of {@code tx}s output values are non-negative, and
     * (5) the sum of {@code tx}s input values is greater than or equal to the sum of its output
     *     values; and false otherwise.
     */
    public boolean isValidTx(Transaction tx) {
        double totalOutputValue = 0 ;
        double totalInputValue = 0 ;
        ArrayList<UTXO> unspentInputs = new ArrayList<UTXO>() ;

        // (4) all of {@code tx}s output values are non-negative, and
        for (Transaction.Output op : tx.getOutputs()) {
            if(op.value < 0) {
                System.out.println("Negative value for output") ;
                return false ;
            }
            totalOutputValue += op.value ;
        }

        //(5) the sum of {@code tx}s input values is greater than or equal to the sum of its output
        // values; and false otherwise.
        for (int inputId = 0; inputId < tx.numInputs(); inputId++) {
            Transaction.Input ip = tx.getInput(inputId) ;
            // Create a UTXO representing this input and...
            UTXO u = new UTXO(ip.prevTxHash, ip.outputIndex) ;
            // check if this UTXO is already claimed in this transaction.
            if ((unspentInputs.size() > 0) && UTXOInList(unspentInputs, u)) {
                // (3) no UTXO is claimed multiple times by {@code tx},
                System.out.println("Double spend at " + inputId) ;
                return false ;
            }
            // if not, then add to list to check for future double-spends if any.
            unspentInputs.add(u) ;

            // If the previous trasaction represented by this input is unspent...
            if (this.utxoPool.contains(u)) {
                Transaction.Output mappedOutput = this.utxoPool.getTxOutput(u) ;
                // ... add it to totalInputValue to check (5)
                totalInputValue += mappedOutput.value ;
                if(!(Crypto.verifySignature(
                        mappedOutput.address, tx.getRawDataToSign(inputId), ip.signature
                ))) {
                    // (2) the signatures on each input of {@code tx} are valid,
                    System.out.println("Singature invalid for " + inputId) ;
                    return false ;
                }
            } else {
                // (1) all outputs claimed by {@code tx} are in the current UTXO pool,
                System.out.println("Not found in pool " + inputId) ;
                return false ;
            }
        }

        if(totalInputValue < totalOutputValue) {
            // (5) the sum of {@code tx}s input values is greater than or equal to its ...
            System.out.println("Total Inputs " + totalInputValue + " less than total outputs " + totalOutputValue) ;
            return false ;
        }
        System.out.println("Transaction Valid!") ;
        return true ;
    }

    public boolean UTXOInList(ArrayList<UTXO> items, UTXO u) {
        for(UTXO i : items) {
            if(i.equals(u)) { return false ; }
        }
        return true ;
    }

    /**
     * Handles each epoch by receiving an unordered array of proposed transactions, checking each
     * transaction for correctness, returning a mutually valid array of accepted transactions, and
     * updating the current UTXO pool as appropriate.
     */
    public Transaction[] handleTxs(Transaction[] possibleTxs) {
        // IMPLEMENT THIS
        return new Transaction[2] ;
    }

}
