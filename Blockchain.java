package blockchain;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

public class Blockchain implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<Block> blockchain;
    private List<Message> messages ;


    public Blockchain(String filename) {
        messages = new ArrayList<>();

        File file = new File(filename);
        if (file.exists() && file.isFile()) {
            try {
                this.blockchain = (ArrayList<Block>) SerializationUtils.deserialize(filename);
            } catch (IOException | ClassNotFoundException exception) {
                exception.printStackTrace();
            }
            Block.setIdCounter(this.blockchain.get(this.blockchain.size() - 1).getId() + 1);
            if (!this.isValid(blockchain)) {
                throw new RuntimeException("Invalid Blockchain");
            }
        } else {
            this.blockchain = new ArrayList<>();
        }
    }

    public void generateBlocks(int numberOfBlocks, String filename, ExecutorService executor) {

        int blockChainSize = this.blockchain.size();

        for (int i = blockChainSize; i < blockChainSize + numberOfBlocks; i++) {
            Queue<Miner> minerList = new ConcurrentLinkedQueue<>();
            minerList.add(new Miner(this.blockchain.size() == 0 ? "0" :
                    this.blockchain.get(i - 1).getHash(), messages));
            try {
                Block block = executor.invokeAny(minerList);
                this.blockchain.add(block);
                SerializationUtils.serialize(this.blockchain, filename);
            } catch (InterruptedException | ExecutionException | IOException e) {
                e.printStackTrace();
            }
            if (i > 0) {
                messages.clear();
            }
        }

    }

    public boolean isValid(List<Block> blocks) {
        if (blocks.size() <= 1) {
            return blocks.isEmpty() || "0".equals(blocks.get(0).getPreviousHash());
        }
        for (int i = 1; i < blocks.size(); i++) {
            boolean valid = blocks.get(i).getPreviousHash().equals(blocks.get(i - 1).getHash());
            if (!valid) {
                return false;
            }
        }
        return true;

    }

    public int getSize() {
        return this.blockchain.size();
    }

    public Block getBlockAt(int blockId) throws IndexOutOfBoundsException {
        try {
            return this.blockchain.get(blockId);
        } catch (RuntimeException e) {
            throw new ArrayIndexOutOfBoundsException(e.getMessage());
        }
    }

    public void addMessage(Message message) {
        messages.add(message);

    }

    @Override
    public String toString() {
        StringBuilder blockchainString = new StringBuilder();
        for (Block block : blockchain) {
            blockchainString.append(block.toString()).append("\n\n");
        }
        return blockchainString.toString();
    }


}