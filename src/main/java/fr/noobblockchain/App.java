package fr.noobblockchain;

import java.security.Security;
import java.util.ArrayList;
import java.util.HashMap;

import fr.noobblockchain.models.Block;
import fr.noobblockchain.models.Transaction;
import fr.noobblockchain.models.TransactionInput;
import fr.noobblockchain.models.TransactionOutput;
import fr.noobblockchain.models.Wallet;

public class App {

	public static HashMap<String, TransactionOutput> UTXOs = new HashMap<String, TransactionOutput>();
	public static ArrayList<Block> blockChain = new ArrayList<Block>();
	public static int difficulty = 5; /*
										 * Changer la difficulté pour modifier le temps de minage nécessaire
										 * (exponentiel, éviter de monter au dessus de 6)
										 */
	public static float minimumTransaction = 0.1f;
	public static Wallet walletA;
	public static Wallet walletB;
	public static Transaction genesisTransaction;

	public static void main(String[] args) {
		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

		walletA = new Wallet();
		walletB = new Wallet();
		Wallet coinbase = new Wallet();

		genesisTransaction = new Transaction(coinbase.publicKey, walletA.publicKey, 100f, null);
		genesisTransaction.generateSignature(coinbase.privateKey);
		genesisTransaction.transactionId = "0";
		genesisTransaction.outputs.add(new TransactionOutput(genesisTransaction.reciepient, genesisTransaction.value,
				genesisTransaction.transactionId));
		UTXOs.put(genesisTransaction.outputs.get(0).id, genesisTransaction.outputs.get(0));

		System.out.println("Creating and mining Genesis block ... ");
		Block genesis = new Block("0");
		genesis.addTransaction(genesisTransaction);
		addBlock(genesis);

		Block block1 = new Block(genesis.hash);
		System.out.println("WalletA's balance is : " + walletA.getBalance());
		System.out.println("WalletA is attempting to send funds (40) to WalletB ...");
		block1.addTransaction(walletA.sendFunds(walletB.publicKey, 40f));
		addBlock(block1);
		System.out.println("WalletA's blanace is : " + walletA.getBalance());
		System.out.println("WalletB's blanace is : " + walletB.getBalance());

		Block block2 = new Block(block1.hash);
		System.out.println("WalletA is attempting to send more funds (1000) than it has ...");
		block1.addTransaction(walletA.sendFunds(walletB.publicKey, 1000f));
		addBlock(block2);
		System.out.println("WalletA's blanace is : " + walletA.getBalance());
		System.out.println("WalletB's blanace is : " + walletB.getBalance());

		Block block3 = new Block(block2.hash);
		System.out.println("WalletB is Attempting to send funds (20) to WalletA...");
		block3.addTransaction(walletB.sendFunds(walletA.publicKey, 20));
		System.out.println("WalletA's balance is: " + walletA.getBalance());
		System.out.println("WalletB's balance is: " + walletB.getBalance());

		isChainValid();

//		System.out.println("Private and public keys : ");
//		System.out.println(StringUtil.getStringFromKey(walletA.privateKey));
//		System.out.println(StringUtil.getStringFromKey(walletA.publicKey));
//
//		Transaction transaction = new Transaction(walletA.publicKey, walletB.publicKey, 5, null);
//		transaction.generateSignature(walletA.privateKey);
//
//		System.out.print("Is signature verified : ");
//		System.out.println(transaction.verifySignature());

//		blockChain.add(new Block("Hi, i m the first block", "0"));
//		System.out.println("Trying to Mine block 1 ...");
//		blockChain.get(0).mineBlock(difficulty);
//
//		blockChain.add(new Block("Yo, i m the second block", blockChain.get(blockChain.size() - 1).hash));
//		System.out.println("Trying to Mine block 2 ...");
//		blockChain.get(blockChain.size() - 1).mineBlock(difficulty);
//
//		blockChain.add(new Block("Hey, i m the third block", blockChain.get(blockChain.size() - 1).hash));
//		System.out.println("Trying to Mine block 3 ...");
//		blockChain.get(blockChain.size() - 1).mineBlock(difficulty);
//
//		System.out.println("\nBlockchain is Valid : " + isChainValid());
//
//		String blockChainJson = new GsonBuilder().setPrettyPrinting().create().toJson(blockChain);
//		System.out.println("\nThe blockchain: ");
//		System.out.println(blockChainJson);
	}

	public static Boolean isChainValid() {
		Block currentBlock;
		Block previousBlock;
		String hashTarget = new String(new char[difficulty]).replace('\0', '0');
		HashMap<String, TransactionOutput> tempUTXOs = new HashMap<String, TransactionOutput>();
		tempUTXOs.put(genesisTransaction.outputs.get(0).id, genesisTransaction.outputs.get(0));

		for (int i = 1; i < blockChain.size(); i++) {
			currentBlock = blockChain.get(i);
			previousBlock = blockChain.get(i - 1);

			if (!currentBlock.hash.equals(currentBlock.calculateHash())) {
				System.out.println("Current Hashes not equal");
				return false;
			}

			if (!previousBlock.hash.equals(currentBlock.previousHash)) {
				System.out.println("Previous Hashes not equal");
				return false;
			}

			if (!currentBlock.hash.substring(0, difficulty).equals(hashTarget)) {
				System.out.println("This block hasn't been mined");
				return false;
			}

			TransactionOutput tempOutput;
			for (int j = 0; j < currentBlock.transactions.size(); j++) {
				Transaction currentTransaction = currentBlock.transactions.get(j);

				if (!currentTransaction.verifySignature()) {
					System.out.println("Signature on Transaction(" + j + ") is invalid");
					return false;
				}

				if (currentTransaction.getInputsValue() != currentTransaction.getOutputsValue()) {
					System.out.println("Inputs are note equal to outputs on Transaction(" + j + ")");
					return false;
				}

				for (TransactionInput input : currentTransaction.inputs) {
					tempOutput = tempUTXOs.get(input.transactionOutputId);

					if (tempOutput == null) {
						System.out.println("Referenced input on Transaction(" + j + ") is missing.");
						return false;
					}

					if (input.UTXO.value != tempOutput.value) {
						System.out.println("Referenced input on Transaction(" + j + ") is invalid.");
						return false;
					}

					tempUTXOs.remove(input.transactionOutputId);
				}

				for (TransactionOutput o : currentTransaction.outputs) {
					tempUTXOs.put(o.id, o);
				}

				if (currentTransaction.outputs.get(0).reciepient != currentTransaction.reciepient) {
					System.out.println("Transaction (" + j + "( output reciepient is not who it should be.");
					return false;
				}

				if (currentTransaction.outputs.get(1).reciepient != currentTransaction.sender) {
					System.out.println("Transaction (" + j + "( output 'change' is not sender.");
					return false;
				}
			}
		}
		System.out.println("Blockchain is valid.");
		return true;
	}

	public static void addBlock(Block newBlock) {
		newBlock.mineBlock(difficulty);
		blockChain.add(newBlock);
	}
}
