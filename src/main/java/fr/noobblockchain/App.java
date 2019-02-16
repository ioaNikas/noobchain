package fr.noobblockchain;

import java.util.ArrayList;

import com.google.gson.GsonBuilder;

import fr.noobblockchain.models.Block;

public class App {

	public static ArrayList<Block> blockChain = new ArrayList<Block>();
	public static int difficulty = 5; /*
										 * Changer la difficulté pour modifier le temps de minage nécessaire
										 * (exponentiel, éviter de monter au dessus de 6)
										 */

	public static void main(String[] args) {

		blockChain.add(new Block("Hi, i m the first block", "0"));
		System.out.println("Trying to Mine block 1 ...");
		blockChain.get(0).mineBlock(difficulty);

		blockChain.add(new Block("Yo, i m the second block", blockChain.get(blockChain.size() - 1).hash));
		System.out.println("Trying to Mine block 2 ...");
		blockChain.get(blockChain.size() - 1).mineBlock(difficulty);

		blockChain.add(new Block("Hey, i m the third block", blockChain.get(blockChain.size() - 1).hash));
		System.out.println("Trying to Mine block 3 ...");
		blockChain.get(blockChain.size() - 1).mineBlock(difficulty);

		System.out.println("\nBlockchain is Valid : " + isChainValid());

		String blockChainJson = new GsonBuilder().setPrettyPrinting().create().toJson(blockChain);
		System.out.println("\nThe blockchain: ");
		System.out.println(blockChainJson);
	}

	public static Boolean isChainValid() {
		Block currentBlock;
		Block previousBlock;
		String hashTarget = new String(new char[difficulty]).replace('\0', '0');

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
			}
		}
		return true;
	}
}
