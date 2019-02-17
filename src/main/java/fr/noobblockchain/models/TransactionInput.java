package fr.noobblockchain.models;

public class TransactionInput {
	public String transactionOutputId;
	public TransactionOutput UTXO;

	public TransactionInput(String transactionOutputId) {
		this.transactionOutputId = transactionOutputId;
	}
}
