package blockchain;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.simple.JSONArray;
import org.json.JSONException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class BlockStructure {
    private static String FilePath = "C:\\Users\\M\\Documents\\NetBeansProjects\\blockAssignment\\src\\blockchain\\blockchain.json";
    private static SimpleDateFormat SimpleDate = new SimpleDateFormat("E MMM dd HH:mm:ss z yyyy");

    public static void main(String[] args) throws NoSuchAlgorithmException, NoSuchAlgorithmException, JSONException, java.text.ParseException, IOException, org.json.simple.parser.ParseException {
        Blockchain blockchain = new Blockchain();
        blockchain.loadingBlocks();
        blockchain.addBlock(new Block(1, "Hi", new Date()));
        blockchain.addBlock(new Block(2, "Blockchain", new Date()));
        blockchain.explore();
        blockchain.saveToJSON();
    }
    
        public static class Block {

        private long index;
        private String data;
        private Date timestamp;
        private String previousHash;
        private String currentHash;
        private long nonce;

        public Block() {
        }

        public Block(int index, String data, Date timestamp) throws NoSuchAlgorithmException {
            this.index = index;
            this.data = data;
            this.timestamp = timestamp;
            this.previousHash = "0000000000000000000000000000000000000000000000000000000000000000";
            this.currentHash = calculateHash();
        }

        private static byte[] getSHA(String input) throws NoSuchAlgorithmException {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return md.digest(input.getBytes(StandardCharsets.UTF_8));
        }

        private static String toHexString(byte[] hash) {
            BigInteger number = new BigInteger(1, hash);
            StringBuilder hexString = new StringBuilder(number.toString(16));
            while (hexString.length() < 32) {
                hexString.insert(0, '0');
            }
            return hexString.toString();
        }

        private String calculateHash() throws NoSuchAlgorithmException {
            return toHexString(getSHA(this.index + this.data + this.previousHash + this.timestamp + this.nonce));
        }

        public long getIndex() {
            return index;
        }

        public void setIndex(long index) {
            this.index = index;
        }

        public String getData() {
            return data;
        }

        public void setData(String data) {
            this.data = data;
        }

        public Date getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(Date timestamp) {
            this.timestamp = timestamp;
        }

        public String getPreviousHash() {
            return previousHash;
        }

        public void setPreviousHash(String previousHash) {
            this.previousHash = previousHash;
        }

        public String getCurrentHash() {
            return currentHash;
        }

        public void setCurrentHash(String currentHash) {
            this.currentHash = currentHash;
        }

        public long getNonce() {
            return nonce;
        }

        public void setNonce(long nonce) {
            this.nonce = nonce;
        }

        private void mineBlock(int diffculty) throws NoSuchAlgorithmException {
            String[] array = new String[diffculty + 1];
            for (int i = 0; i < array.length; i++) {
                array[i] = "";
            }
            this.currentHash = this.currentHash.replaceAll(this.currentHash.substring(0, diffculty), String.join("0", array));
            nonce = (int) Math.round(Math.random() * 10);
        }
    }
        
    public static class Blockchain {

        private LinkedList<Block> chain = new LinkedList();
        private int difficulty;

        public Blockchain() throws NoSuchAlgorithmException {
            chain.addFirst(GenesisBlock());
            this.difficulty = 2;
        }

        public Block GenesisBlock() throws NoSuchAlgorithmException {
            return new Block(0, "GenesisBlock", new Date());
        }

        public Block getBlock() {
            return chain.getLast();
        }

        public void addBlock(Block newBlock) throws NoSuchAlgorithmException {
            newBlock.setPreviousHash(this.getBlock().getCurrentHash());
            boolean isFound = false;
            for (Block block : chain) {
                if (block.getIndex() == newBlock.getIndex()) {
                    isFound = true;
                    break;
                }
            }
            if (isFound == false) {
                newBlock.mineBlock(this.difficulty);
                chain.addLast(newBlock);
            } 
        }

        private void loadingBlocks() throws JSONException, java.text.ParseException, IOException, org.json.simple.parser.ParseException {
            JSONParser jsonParser = new JSONParser();
            File file = new File(FilePath);
            if (Files.lines(Paths.get(FilePath)).count() != 0) {
                try (FileReader reader = new FileReader(FilePath)) {
                    Object object = jsonParser.parse(reader);
                    JSONArray blocksList = (JSONArray) object;
                    blocksList.forEach(block -> {
                        try {
                            decodeBlockObject((JSONObject) block);
                        } catch (JSONException ex) {
                            ex.printStackTrace();
                        } catch (ParseException ex) {
                            Logger.getLogger(BlockStructure.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    });
                } catch (FileNotFoundException excpetion) {
                    excpetion.printStackTrace();
                } catch (IOException excpetion) {
                    excpetion.printStackTrace();
                }
            } else {
                System.err.println("This JSON File NotFound");
            }
        }

        private void decodeBlockObject(JSONObject jsonBlock) throws JSONException, java.text.ParseException {
            JSONObject blockObject = (JSONObject) jsonBlock.get("block");
            Block tempBlock = new Block();
            tempBlock.setIndex((Long) blockObject.get("id"));
            tempBlock.setData((String) blockObject.get("data"));
            tempBlock.setCurrentHash((String) blockObject.get("current_hash"));
            tempBlock.setPreviousHash((String) blockObject.get("previous_hash"));
            tempBlock.setTimestamp(SimpleDate.parse((String) blockObject.get("timestamp")));
            tempBlock.setNonce((Long) blockObject.get("nonce"));
            boolean isFound = false;
            for (Block block : chain) {
                if ((long) block.getIndex() == tempBlock.getIndex()) {
                    isFound = true;
                    break;
                }
            }
            if (isFound == false) {
                chain.addLast(tempBlock);
            }
        }

        private void saveToJSON() throws JSONException {
            JSONArray jsonArray = new JSONArray();
            for (int i = 0; i < chain.size(); i++) {
                JSONObject object = new JSONObject();
                JSONObject objectItem = new JSONObject();
                objectItem.put("id", chain.get(i).getIndex());
                objectItem.put("data", chain.get(i).getData());
                objectItem.put("timestamp", chain.get(i).getTimestamp().toString());
                objectItem.put("previous_hash", chain.get(i).getPreviousHash());
                objectItem.put("current_hash", chain.get(i).getCurrentHash());
                objectItem.put("nonce", chain.get(i).getNonce());
                object.put("block", objectItem);
                jsonArray.add(object);
            }
            try (FileWriter file = new FileWriter(FilePath)) {
                file.write(jsonArray.toString());
                System.out.println("\nJSON Object: " + jsonArray);
            } catch (Exception exception) {
                System.out.println(exception);
            }
        }

        public void explore() {
            for (Block block : chain) {
                System.out.println("Index : " + block.index
                        + "\nNonce : " + block.nonce
                        + "\nData : " + block.data
                        + "\nTime Stamp : " + block.timestamp
                        + "\nPrevious Hash : " + block.previousHash
                        + "\nHash : " + block.currentHash);
                System.out.println("");
            }
        }

    }
}
