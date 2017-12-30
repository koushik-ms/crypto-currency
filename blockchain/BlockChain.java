import java.util.ArrayList ;
import java.util.Arrays ;
import java.util.HashMap ;

// Block Chain should maintain only limited block nodes to satisfy the functions
// You should not have all the blocks added to the block chain in memory 
// as it would cause a memory overflow.

public class BlockChain {
    public static final int CUT_OFF_AGE = 10;
    public static final int COINBASE_INDEX = 0 ;

    // We will use a linked list node class wrapped on Block class to
    // build and manage the blockchain.
    private class BlockChainNode {
        // Data is mainly the block and some metadata like height and the 
        // UTXOPool associated with this node.
        public Block block ;
        public int height ; 
        public UTXOPool utxoPool ;

        // There could be only on previous node but multiple next nodes.
        public BlockChainNode prev ;
        public ArrayList<BlockChainNode> next ;

        public BlockChainNode(Block block, BlockChainNode prev, UTXOPool utxoPool) {
            this.block = block ;
            if(prev == null) {
                // Genesis block
                this.height = 1 ;
            } else {
                this.height = prev.height + 1 ;
                prev.next.add(this) ; // double-counting ?
            }
            this.utxoPool = utxoPool ;
            this.prev = prev ;
            this.next = new ArrayList<BlockChainNode>() ;
        }
    }

    private ArrayList<BlockChainNode> head ;
    private int height ;
    private TransactionPool txPool ;

    private HashMap<ByteArrayWrapper, BlockChainNode> blockDict ;
    private BlockChainNode tipTop ; // Tip or Top of the chain (could be multiple?)

    /**
     * create an empty block chain with just a genesis block. Assume {@code genesisBlock} is a valid
     * block
     */
    public BlockChain(Block genesisBlock) {
        BlockChainNode bg = new BlockChainNode(genesisBlock, null, new UTXOPool()) ;
        Transaction coinbaseTx = genesisBlock.getCoinbase() ;
        bg.utxoPool.addUTXO(
            new UTXO(coinbaseTx.getHash(), COINBASE_INDEX),
            coinbaseTx.getOutput(COINBASE_INDEX) // Spl Tx
            ) ;
        // head = bg ;
        this.head = new ArrayList<BlockChainNode>() ;
        this.head.add(bg) ;
        this.height = 1 ;
        this.tipTop = bg ;
        this.txPool = new TransactionPool() ;
        this.blockDict = new HashMap<ByteArrayWrapper, BlockChainNode>() ;
        this.blockDict.put(new ByteArrayWrapper(genesisBlock.getHash()), bg) ;
    }

    /** Get the maximum height block */
    public Block getMaxHeightBlock() {
        return this.tipTop.block ;
    }

    /** Get the UTXOPool for mining a new block on top of max height block */
    public UTXOPool getMaxHeightUTXOPool() {
        return new UTXOPool(this.tipTop.utxoPool) ;
    }

    /** Get the transaction pool to mine a new block */
    public TransactionPool getTransactionPool() {
        return this.txPool ;
    }

    /**
     * Add {@code block} to the block chain if it is valid. For validity, all transactions should be
     * valid and block should be at {@code height > (maxHeight - CUT_OFF_AGE)}.
     * 
     * <p>
     * For example, you can try creating a new block over the genesis block (block height 2) if the
     * block chain height is {@code <=
     * CUT_OFF_AGE + 1}. As soon as {@code height > CUT_OFF_AGE + 1}, you cannot create a new block
     * at height 2.
     * 
     * @return true if block is successfully added
     */
    public boolean addBlock(Block block) {
        byte[] pbh = block.getPrevBlockHash() ;
        // reject genesis blocks
        if( (pbh != null) && blockDict.containsKey(new ByteArrayWrapper(pbh)) ) {
            BlockChainNode prev = blockDict.get(new ByteArrayWrapper(pbh)) ;
            BlockChainNode bcn = new BlockChainNode(block, 
                prev,
                new UTXOPool(prev.utxoPool) 
            ) ;
            // Update bcn's UTXO Pool to remove Tx's in block
            TxHandler h = new TxHandler(bcn.utxoPool) ;
            Transaction[] pTx = block.getTransactions().toArray(new Transaction[0]) ;
            Transaction[] sTx = h.handleTxs(pTx) ;
            if (pTx.length == sTx.length) {
                // Assume all transactions are picked
                bcn.utxoPool = h.getUTXOPool() ;
                bcn.utxoPool.addUTXO(
                    new UTXO(block.getCoinbase().getHash(), COINBASE_INDEX),
                    block.getCoinbase().getOutput(COINBASE_INDEX)
                ) ;
                // Update Transaction Pool
                for(Transaction tx: block.getTransactions()) {
                    txPool.removeTransaction(tx.getHash()) ;
                }
                // Update state/ metadata
                blockDict.put(new ByteArrayWrapper(block.getHash()), bcn) ;
                if(bcn.height > this.height) {
                    // we found a taller branch!
                    this.height = bcn.height ;
                    this.tipTop = bcn ;
                    // This also means some old head(s) have to go... 
                    if((height - this.head.get(0).height) > CUT_OFF_AGE) {
                        ArrayList<BlockChainNode> nl = new ArrayList<BlockChainNode>() ;
                        for(BlockChainNode aHead: head) {
                            for(BlockChainNode n: aHead.next) {
                                nl.add(n) ;
                            }
                            blockDict.remove(new ByteArrayWrapper(aHead.block.getHash())) ;
                        }
                        this.head = nl ;
                    }
                }
                return true ;
            } else {
                return false ;
            }
        }
        return false ;
    }

    /** Add a transaction to the transaction pool */
    public void addTransaction(Transaction tx) {
        this.txPool.addTransaction(tx) ;
    }
}