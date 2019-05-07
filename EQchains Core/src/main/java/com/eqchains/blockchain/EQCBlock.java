/**
 * EQCoin core - EQCOIN Foundation's EQCoin core library
 * @copyright 2018-present EQCOIN Foundation All rights reserved...
 * Copyright of all works released by EQCOIN Foundation or jointly released by
 * EQCOIN Foundation with cooperative partners are owned by EQCOIN Foundation
 * and entitled to protection available from copyright law by country as well as
 * international conventions.
 * Attribution — You must give appropriate credit, provide a link to the license.
 * Non Commercial — You may not use the material for commercial purposes.
 * No Derivatives — If you remix, transform, or  upon the material, you may
 * not distribute the modified material.
 * For any use of above stated content of copyright beyond the scope of fair use
 * or without prior written permission, EQCOIN Foundation reserves all rights to
 * take any legal action and pursue any right or remedy available under applicable
 * law.
 * https://www.eqzip.com
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.eqchains.blockchain;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Vector;

import javax.naming.InitialContext;
import javax.print.attribute.Size2DSyntax;

import com.eqchains.blockchain.Account.Publickey;
import com.eqchains.blockchain.AccountsMerkleTree.Statistics;
import com.eqchains.blockchain.transaction.Address;
import com.eqchains.blockchain.transaction.CoinbaseTransaction;
import com.eqchains.blockchain.transaction.OperationTransaction;
import com.eqchains.blockchain.transaction.Transaction;
import com.eqchains.blockchain.transaction.TxIn;
import com.eqchains.blockchain.transaction.TxOut;
import com.eqchains.blockchain.transaction.Address.AddressShape;
import com.eqchains.blockchain.transaction.operation.UpdateAddressOperation;
import com.eqchains.keystore.Keystore;
import com.eqchains.persistence.avro.EQCBlockAvro;
import com.eqchains.persistence.h2.EQCBlockChainH2;
import com.eqchains.persistence.rocksdb.EQCBlockChainRocksDB;
import com.eqchains.serialization.EQCTypable;
import com.eqchains.serialization.EQCType;
import com.eqchains.service.MinerService;
import com.eqchains.util.ID;
import com.eqchains.util.Log;
import com.eqchains.util.Util;

/**
 * @author Xun Wang
 * @date Oct 1, 2018
 * @email 10509759@qq.com
 */
public class EQCBlock implements EQCTypable {
	private EQCHeader eqcHeader;
	private Root root;
	private Transactions transactions;
	// The following is Transactions' Segregated Witness members it's hash will be
	// recorded in the Root's accountsMerkelTreeRoot together with
	// Transaction&Publickey.
	private Signatures signatures;
	// private txReceipt;
	// The min size of the EQCHeader's is 142 bytes.
	private int size = 142;
	/*
	 * VERIFICATION_COUNT equal to the number of member variables of the class to be
	 * verified.
	 */
	private static byte VERIFICATION_COUNT = 4;

	public EQCBlock(EQCBlockAvro eqcBlockAvro) throws NoSuchFieldException, IOException {
		if (EQCHeader.isValid(eqcBlockAvro.getEQCHeader())) {
			eqcHeader = new EQCHeader(eqcBlockAvro.getEQCHeader());
		} else {
			throw new ClassCastException("EQCHeader's bytes are invalid.");
		}
		if (Transactions.isValid(eqcBlockAvro.getTransactions())) {
			transactions = new Transactions(eqcBlockAvro.getTransactions());
		} else {
			throw new ClassCastException("Transactions bytes are invalid.");
		}
		if (Signatures.isValid(eqcBlockAvro.getSignatures())) {
			signatures = new Signatures(eqcBlockAvro.getSignatures());
		} else {
			throw new ClassCastException("Signatures bytes are invalid.");
		}
	}

	public EQCBlock(EQCBlockAvro eqcBlockAvro, boolean isSegwit) throws NoSuchFieldException, IOException {
		if (EQCHeader.isValid(eqcBlockAvro.getEQCHeader())) {
			eqcHeader = new EQCHeader(eqcBlockAvro.getEQCHeader());
		} else {
			throw new ClassCastException("EQCHeader's bytes are invalid.");
		}
		if (Transactions.isValid(eqcBlockAvro.getTransactions())) {
			transactions = new Transactions(eqcBlockAvro.getTransactions());
		} else {
			throw new ClassCastException("Transactions bytes are invalid.");
		}
		if (!isSegwit) {
			if (Signatures.isValid(eqcBlockAvro.getSignatures())) {
				signatures = new Signatures(eqcBlockAvro.getSignatures());
			} else {
				throw new ClassCastException("Signatures bytes are invalid.");
			}
		}
	}

	public EQCBlock(byte[] bytes, boolean isSegwit) throws NoSuchFieldException, IOException {
		ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		byte[] data = null;
		// Parse EqcHeader
		if ((data = EQCType.parseBIN(is)) != null) {
			if (!EQCHeader.isValid(data)) {
				throw new ClassCastException("EQCHeader's bytes are invalid.");
			}
			eqcHeader = new EQCHeader(data);
		}

		// Parse Root
		if ((data = EQCType.parseBIN(is)) != null) {
			if (!Root.isValid(data)) {
				throw new ClassCastException("Root's bytes are invalid.");
			}
			root = new Root(data);
		}

		// Parse Transactions
		data = null;
		if ((data = EQCType.parseBIN(is)) != null) {
			if (!Transactions.isValid(data)) {
				throw new ClassCastException("Transactions bytes are invalid.");
			}
			transactions = new Transactions(data);
		}

		if (!isSegwit) {
			// Parse Signatures
			data = null;
			if ((data = is.readAllBytes()) != null) {
				if (!Signatures.isValid(data)) {
					throw new ClassCastException("Signatures bytes are invalid.");
				}
				signatures = new Signatures(data);
			}
		}
	}

	public static boolean isValid(byte[] bytes) throws NoSuchFieldException, IOException {
		ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		byte[] data = null;
		byte validCount = 0;

		// Parse EqcHeader
		if ((data = EQCType.parseBIN(is)) != null) {
			if (EQCHeader.isValid(data)) {
				++validCount;
			}
		}

		// Parse Root
		if ((data = EQCType.parseBIN(is)) != null) {
			if (Root.isValid(data)) {
				++validCount;
			}
		}

		// Parse Transactions
		data = null;
		if ((data = EQCType.parseBIN(is)) != null) {
			if (Transactions.isValid(data)) {
				++validCount;
			}
		}

		// Parse Signatures
		data = null;
		if ((data = is.readAllBytes()) != null) {
			if (Signatures.isValid(data)) {
				++validCount;
			}
		}

		return (validCount == VERIFICATION_COUNT) && EQCType.isInputStreamEnd(is);
	}

	public EQCBlock() {
		init();
	}

	private void init() {
		eqcHeader = new EQCHeader();
		root = new Root();
		transactions = new Transactions();
		signatures = new Signatures();
	}

	public EQCBlock(ID currentBlockHeight, byte[] previousBlockHeaderHash) {

		init();
		// Create EQC block header
		eqcHeader.setPreHash(previousBlockHeaderHash);
		eqcHeader.setHeight(currentBlockHeight);
		eqcHeader.setTarget(Util.cypherTarget(currentBlockHeight));
		eqcHeader.setTimestamp(new ID(System.currentTimeMillis()));
		eqcHeader.setNonce(ID.ZERO);

	}

	public boolean isTransactionExists(Transaction transaction) {
		return transactions.isTransactionExists(transaction);
	}

	/**
	 * @return the eqcHeader
	 */
	public EQCHeader getEqcHeader() {
		return eqcHeader;
	}

	/**
	 * @param eqcHeader the eqcHeader to set
	 */
	public void setEqcHeader(EQCHeader eqcHeader) {
		this.eqcHeader = eqcHeader;
	}

	/**
	 * @return the transactions
	 */
	public Transactions getTransactions() {
		return transactions;
	}

	/**
	 * @param transactions the transactions to set
	 */
	public void setTransactions(Transactions transactions) {
		this.transactions = transactions;
	}

	/**
	 * @param signatures the signatures to set
	 */
	public void setSignatures(Signatures signatures) {
		this.signatures = signatures;
	}

	/**
	 * @return the signatures
	 */
	public Signatures getSignatures() {
		return signatures;
	}

	public ID getHeight() {
		return eqcHeader.getHeight();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return

		"{\n" + toInnerJson() + "\n}";

	}

	public String toInnerJson() {
		return

		"\"EQCBlock\":{\n" + eqcHeader.toInnerJson() + ",\n" + root.toInnerJson() + ",\n" + transactions.toInnerJson()
				+ ",\n" + signatures.toInnerJson() + "\n" + "}";

	}

	public void accountingTransactions(ID height, AccountsMerkleTree accountsMerkleTree)
			throws NoSuchFieldException, IllegalStateException, IOException {
		Vector<Transaction> pendingTransactionList = new Vector<Transaction>();
		Transaction transaction = null;
		TxIn txIn = null;
		ID initID;

		if (!height.isNextID(accountsMerkleTree.getHeight())) {
			throw new IllegalStateException("Current block's height is wrong isn't previous block's next height!");
		}

		/**
		 * Heal Protocol If height equal to a specific height then update the ID No.1's
		 * Address to a new Address the more information you can reference to
		 * https://github.com/eqzip/eqcoin
		 */

		// Create CoinBase Transaction
		Address address = new Address();
		address.setReadableAddress(Keystore.getInstance().getUserAccounts().get(2).getReadableAddress());

		// Add CoinBase into Transactions
		pendingTransactionList.add(Util.generateCoinBaseTransaction(address, height, accountsMerkleTree));

		// Get Transaction list
		pendingTransactionList.addAll(EQCBlockChainH2.getInstance().getTransactionListInPool());
		Log.info("Current have " + pendingTransactionList.size() + " pending Transactions.");
		
		// Handle every pending Transaction
		for (int i = 0; i < pendingTransactionList.size(); ++i) {
			transaction = pendingTransactionList.get(i);

			// Only No.0 can be CoinBase Transaction
			if ((i > 0) && (transaction.isCoinBase() || transaction.getTxIn() == null)) {
				EQCBlockChainH2.getInstance().deleteTransactionInPool(transaction);
				Log.Error("Only No. 0 can be CoinBase Transaction but No." + i
						+ " is also CoinBase or TxIn is null this is invalid just discard it: "
						+ transaction.toString());
				continue;
			}
			// Check if the No.0 Transaction is CoinBase Transaction
			else if ((i == 0) && !transaction.isCoinBase()) {
				throw new IllegalArgumentException("No.0 Transaction must be CoinBase Transaction");
			}

			// If Transaction already in this.transactions just continue
			if (this.transactions.isTransactionExists(transaction)) {
				EQCBlockChainH2.getInstance().deleteTransactionInPool(transaction);
				Log.Error("Transaction already exists this is invalid just discard it: " + transaction.toString());
				continue;
			}
			
			// Check if isn't CoinbaseTransaction
			if (!transaction.isCoinBase()) {
				// Check if Transaction's TxIn's Address exists in Accounts must do this check
				// which doesn't equal to
				// check if accountsMerkleTree.getAccount return null
				if (!accountsMerkleTree.isAccountExists(transaction.getTxIn().getAddress(), false)) {
					EQCBlockChainH2.getInstance().deleteTransactionInPool(transaction);
					Log.Error("Transaction's TxIn Address doesn't exists in Accounts this is invalid just discard it: "
							+ transaction.toString());
					continue;
				}
				else {
					// Fill in TxIn's Address and Publickey's Serial Number
					transaction.getTxIn().getAddress().setID(accountsMerkleTree.getAddressID(transaction.getTxIn().getAddress()));
				}

				// Check if Transaction's Nonce is correct
				if (!transaction.isNonceCorrect(accountsMerkleTree)) {
					// Can't delte it need do more job to mark this add new flag -
					// Nonce doesn't continuous if more than 24 Hours just delete the relevant
					// transaction in pool
//					EQCBlockChainH2.getInstance().deleteTransactionInPool(transaction);
					Log.Error("Transaction's Nonce is invalid: ");
					continue;
				}
			}

			// Prepare Transaction
			transaction.prepareAccounting(accountsMerkleTree, transactions.getAccountListInitId(accountsMerkleTree));

			// If Transaction is OperationTransaction check if meet the preconditions
			if (transaction instanceof OperationTransaction) {
				if (!isMeetPreconditions(transaction, accountsMerkleTree)) {
					EQCBlockChainH2.getInstance().deleteTransactionInPool(transaction);
					Log.Error("Transaction is invalid: " + transaction);
					continue;
				}
			}

			// Check if Transaction is valid here doesn't check Coinbase which will be check later
			if (!transaction.isCoinBase() && !transaction.isValid(accountsMerkleTree, AddressShape.READABLE)) {
				EQCBlockChainH2.getInstance().deleteTransactionInPool(transaction);
				Log.Error("Transaction is invalid: " + transaction);
				continue;
			}

			// Add Transaction into Transactions
			if ((transactions.getSize() + transaction.getBillingSize()) <= Util.ONE_MB) {
				this.transactions.addTransaction(transaction);
				// If current Transaction isn't CoinBase
				if (!transaction.isCoinBase()) {
					// Update Transaction
					transaction.update(accountsMerkleTree);

					// Add signature
					signatures.addSignature(transaction.getSignature());

					// Update the CoinBase's TxFee
					transactions.getCoinbaseTransaction().updateTxFee(transaction.getTxFee());
				}
			} else {
				break;
			}
		}

		// Check if CoinBase isValid and update CoinBase's Account
		if(!transactions.getCoinbaseTransaction().isCoinbaseValid(accountsMerkleTree, transactions.getTxFee())) {
			throw new IllegalStateException("CoinbaseTransaction is invalid: " + transactions.getCoinbaseTransaction());
		}
		else {
			transactions.getCoinbaseTransaction().update(accountsMerkleTree);
		}

		// Build AccountsMerkleTree and generate Root
		accountsMerkleTree.buildAccountsMerkleTree();
		accountsMerkleTree.generateRoot();

		// Initial Root
		Statistics statistics = accountsMerkleTree.getStatistics();
		// Check total supply
		Log.info("" + statistics.totalSupply);
		Log.info("" + Util.cypherTotalSupply(eqcHeader.getHeight()));
		if (statistics.totalSupply != Util.cypherTotalSupply(eqcHeader.getHeight())) {
			Log.Error("Total supply is invalid!");
			throw new IllegalStateException("Total supply is invalid!");
		} else {
			root.setTotalSupply(Util.cypherTotalSupply(eqcHeader.getHeight()));
		}
		EQCBlock previousBlock = EQCBlockChainRocksDB.getInstance().getEQCBlock(eqcHeader.getHeight().getPreviousID(), true);
		// Check total Account numbers
		if(!previousBlock.getRoot().getTotalAccountNumbers().add(BigInteger.valueOf(transactions.getNewAccountList().size())).equals(accountsMerkleTree.getTotalAccountNumbers())){
			Log.Error("Total Account numbers is invalid!");
			throw new IllegalStateException("Total Account numbers is invalid!");
		}
		else {
			root.setTotalAccountNumbers(accountsMerkleTree.getTotalAccountNumbers());
		}
		// Check total Transaction numbers
		if (!previousBlock.getRoot().getTotalTransactionNumbers()
				.add(BigInteger.valueOf(transactions.getNewTransactionList().size())).equals(statistics.totalTransactionNumbers)) {
			Log.Error("Total Transaction numbers is invalid!");
			throw new IllegalStateException("Total Transaction numbers is invalid!");
		} else {
			root.setTotalTransactionNumbers(statistics.totalTransactionNumbers);
		}
		root.setAccountsMerkelTreeRoot(accountsMerkleTree.getRoot());
		root.setTransactionsMerkelTreeRoot(getTransactionsMerkelTreeRoot());
		// Set EQCHeader's Root's hash
		eqcHeader.setRootHash(root.getHash());
		
	}

	public boolean isMeetPreconditions(Object... objects) {
		OperationTransaction operationTransaction = (OperationTransaction) objects[0];
		AccountsMerkleTree accountsMerkleTree = (AccountsMerkleTree) objects[1];
		if (operationTransaction.getOperation() instanceof UpdateAddressOperation) {
			if (!operationTransaction.getOperation().isMeetPreconditions(accountsMerkleTree)) {
				return false;
			}
		} else {
			return false;
		}
		return true;
	}

	public boolean verify(AccountsMerkleTree accountsMerkleTree) throws NoSuchFieldException, IOException {

		/**
		 * Heal Protocol If height equal to a specific height then update the ID No.1's
		 * Address to a new Address the more information you can reference to
		 * https://github.com/eqzip/eqcoin
		 */

		// Check if Transactions' size < 1 MB
		if (transactions.getSize() > Util.ONE_MB) {
			Log.Error("EQCBlock size is invalid");
			return false;
		}

		// Check if Target is valid
		if (!eqcHeader.isDifficultyValid(accountsMerkleTree)) {
			Log.Error("EQCHeader difficulty is invalid");
			return false;
		}

		// Check if AccountList is valid
		if (!transactions.isAccountListValid(accountsMerkleTree)) {
			Log.Error("EQCHeader AccountList is invalid");
			return false;
		}

		// Check if PublickeyList is valid
		if (!transactions.isPublickeyListValid(accountsMerkleTree)) {
			Log.Error("EQCHeader PublickeyList is invalid");
			return false;
		}

		// Check if Signatures' size and Transaction size is valid
		if (transactions.getNewTransactionList().size() != (signatures.getSignatureList().size() + 1)) {
			return false;
		}

		// Check if every Transaction is valid
		long totalTxFee = 0;
		Vector<Transaction> transactionList = transactions.getNewTransactionList();
		for (int i = 0; i < transactionList.size(); ++i) {
			Transaction transaction = transactionList.get(i);
			if ((i == 0) && (!transaction.isCoinBase())) {
				return false;
			} else {
				CoinbaseTransaction coinbaseTransaction = (CoinbaseTransaction) transaction;
				if (!coinbaseTransaction.isCoinbaseValid(accountsMerkleTree, transactions.getTxFee())) {
					return false;
				}
			}

			// Check only No.0 is Coinbase
			if ((i > 0) && (transaction.isCoinBase())) {
				return false;
			}

			transaction.prepareVerify(accountsMerkleTree, signatures.getSignatureList().get(i + 1));

			// If is OperationTransaction check it's isMeetPreconditions
			if (transaction instanceof OperationTransaction) {
				if (!isMeetPreconditions(transaction, accountsMerkleTree)) {
					Log.Error("OperationTransaction doesn't meet preconditions: " + transaction);
					return false;
				}
			}

			// New Way doesn't need this now change to new method
//			// Check checkpoint
//			if (eqcHeader.getHeight().mod(Util.EUROPA).equals(BigInteger.ZERO)) {
//				if ((i == 1) && !transaction.getTxIn().getAddress().getID().equals(ID.TWO)) {
//					return false;
//				} else if (!(transaction instanceof OperationTransaction)) {
//					return false;
//				} else {
//					OperationTransaction operationTransaction = (OperationTransaction) transaction;
//					if (!(operationTransaction.getOperation() instanceof UpdateAddressOperation)) {
//						return false;
//					} else {
//						if (!operationTransaction.isValid(accountsMerkleTree, null)) {
//							return false;
//						} else {
//							operationTransaction.execute(accountsMerkleTree,
//									operationTransaction.getTxIn().getAddress().getID());
//							continue;
//						}
//					}
//				}
//			}

			if (!transaction.isValid(accountsMerkleTree, null)) {
				Log.info("Transaction is invalid: " + transaction);
				return false;
			} else {
				// If is OperationTransaction just execute it
				if (transaction instanceof OperationTransaction) {
					((OperationTransaction) transaction).execute(accountsMerkleTree,
							transaction.getTxIn().getAddress().getID());
				}
				// Update AccountsMerkleTree relevant Account's status
				transaction.update(accountsMerkleTree);
			}
		}

		// Build AccountsMerkleTree and generate Root and Statistics
		accountsMerkleTree.buildAccountsMerkleTree();
		accountsMerkleTree.generateRoot();
		Statistics statistics = accountsMerkleTree.getStatistics();

		// Verify Root
		// Check total supply
		if (statistics.totalSupply != Util.cypherTotalSupply(eqcHeader.getHeight())) {
			Log.Error("Total supply is invalid!");
			return false;
		}
		if(statistics.totalSupply != root.getTotalSupply()){
			Log.Error("Total supply is invalid!");
			return false;
		}
		
		// Check total Transaction numbers
		EQCBlock previousBlock = EQCBlockChainRocksDB.getInstance().getEQCBlock(eqcHeader.getHeight().getPreviousID(), true);
		if (!previousBlock.getRoot().getTotalAccountNumbers()
				.add(BigInteger.valueOf(transactions.getNewAccountList().size()))
				.equals(statistics.totalTransactionNumbers)) {
			Log.Error("Total Transaction numbers is invalid!");
			return false;
		}
		if(!statistics.totalTransactionNumbers.equals(root.getTotalAccountNumbers())) {
			Log.Error("Total Transaction numbers is invalid!");
			return false;
		}
		// Check AccountsMerkelTreeRoot
		if(!Arrays.equals(root.getAccountsMerkelTreeRoot(), accountsMerkleTree.getRoot())) {
			Log.Error("AccountsMerkelTreeRoot is invalid!");
			return false;
		}
		// Check TransactionsMerkelTreeRoot
		if(!Arrays.equals(root.getTransactionsMerkelTreeRoot(), getTransactionsMerkelTreeRoot())) {
			Log.Error("AccountsMerkelTreeRoot is invalid!");
			return false;
		}
		// Verify EQCHeader
		if(!eqcHeader.isValid(accountsMerkleTree, root.getHash())) {
			Log.Error("EQCHeader is invalid!");
			return false;
		}
		
		// Merge shouldn't be done at here
//		// Merge AccountsMerkleTree relevant Account's status
//		if(!accountsMerkleTree.merge()) {
//			Log.Error("Merge AccountsMerkleTree relevant Account's status error occur");
//			return false;
//		}

		return true;
	}

	@Deprecated
	public static boolean verify(EQCBlock eqcBlock, AccountsMerkleTree accountsMerkleTree) {
		// Check if EQCHeader is valid
		BigInteger target = Util.targetBytesToBigInteger(Util.cypherTarget(eqcBlock.getHeight()));
		if (new BigInteger(1, Util.EQCCHA_MULTIPLE_FIBONACCI_MERKEL(eqcBlock.getEqcHeader().getBytes(), Util.HUNDRED_THOUSAND, false))
				.compareTo(target) > 0) {
			Log.Error("EQCHeader is invalid");
			return false;
		}

		// Check if Transactions size is less than 1 MB
		if (eqcBlock.getTransactions().getSize() > Util.ONE_MB) {
			Log.Error("Transactions size  is invalid, size: " + eqcBlock.getTransactions().getSize());
			return false;
		}

		// Check if AddressList is correct the first Address' Serial Number is equal to
		// previous block's last Address' Serial Number + 1
		// Every Address should be unique in current AddressList and doesn't exists in
		// the history AddressList in H2
		// Every Address's Serial Number should equal to previous Address'
		// getNextSerialNumber

		// Check if PublicKeyList is correct the first PublicKey's Serial Number is
		// equal to previous block's last PublicKey's getNextSerialNumber
		// Every PublicKey should be unique in current PublicKeyList and doesn't exists
		// in the history PublicKeyList in H2
		// Every PublicKey's Serial Number should equal to previous PublicKey's
		// getNextSerialNumber

		// Get Transaction list and Signature list
		Vector<Transaction> transactinList = eqcBlock.getTransactions().getNewTransactionList();
		Vector<byte[]> signatureList = eqcBlock.getSignatures().getSignatureList();

		// In addition to the CoinBase transaction, the following checks are made for
		// all other transactions.
		// Check if every Transaction's PublicKey is exists
		if (!eqcBlock.isEveryPublicKeyExists()) {
			Log.Error("Every Transaction's PublicKey should exists");
			return false;
		}
		// Check if every Transaction's Address is exists
		if (!eqcBlock.isEveryAddressExists()) {
			Log.Error("Every Transaction's Address should exists");
			return false;
		}

		// Fill in every Transaction's PublicKey, Signature, relevant Address for verify
		eqcBlock.buildTransactionsForVerify();

		// Check if only have CoinBase Transaction
		if (signatureList == null) {
			if (transactinList.size() != 1) {
				Log.Error(
						"Only have CoinBase Transaction but the number of Transaction isn't equal to 1, current size: "
								+ transactinList.size());
				return false;
			}
		} else {
			// Check if every Transaction has it's Signature
			if ((transactinList.size() - 1) != signatureList.size()) {
				Log.Error("Transaction's number: " + (transactinList.size() - 1)
						+ " doesn't equal to Signature's number: " + signatureList.size());
				return false;
			}
		}

		// Check if CoinBase is correct - CoinBase's Address&Value is valid
		if (!transactinList.get(0).isCoinBase()) {
			Log.Error("The No.0 Transaction isn't CoinBase");
			return false;
		}
		// Check if CoinBase's TxOut Address is valid
		if (!transactinList.get(0).isTxOutAddressValid()) {
			Log.Error("The CoinBase's TxOut's Address is invalid: "
					+ transactinList.get(0).getTxOutList().get(0).toString());
			return false;
		}
		// Check if CoinBase's value is valid
		long totalTxFee = 0;
		for (int i = 1; i < transactinList.size(); ++i) {
			totalTxFee += transactinList.get(i).getTxFee();
		}
		long coinBaseValue = 0;
		if (eqcBlock.getHeight().compareTo(Util.MAX_COINBASE_HEIGHT) < 0) {
			coinBaseValue = Util.COINBASE_REWARD + totalTxFee;
			if (transactinList.get(0).getTxOutValues() != coinBaseValue) {
				Log.Error("CoinBase's value: " + transactinList.get(0).getTxOutValues()
						+ " doesn't equal to COINBASE_REWARD + totalTxFee: " + (Util.COINBASE_REWARD + totalTxFee));
				return false;
			}
		} else {
			coinBaseValue = totalTxFee;
			if (transactinList.get(0).getTxOutValues() != coinBaseValue) {
				Log.Error("CoinBase's value: " + transactinList.get(0).getTxOutValues()
						+ " doesn't equal to totalTxFee: " + totalTxFee);
				return false;
			}
		}

		// Check if only have one CoinBase
		for (int i = 1; i < transactinList.size(); ++i) {
			if (transactinList.get(i).isCoinBase()) {
				Log.Error("Every EQCBlock should has only one CoinBase but No. " + i + " is also CoinBase.");
				return false;
			}
		}

		// Check if Signature is unique in current Signatures and doesn't exists in the
		// history Signature table in H2
		for (int i = 0; i < signatureList.size(); ++i) {
			for (int j = i + 1; j < signatureList.size(); ++j) {
				if (Arrays.equals(signatureList.get(i), signatureList.get(j))) {
					Log.Error("Signature doesn't unique in current  Signature list");
					return false;
				}
			}
		}

		for (byte[] signature : signatureList) {
			if (EQCBlockChainH2.getInstance().isSignatureExists(signature)) {
				Log.Error("Signature doesn't unique in H2's history Signature list");
				return false;
			}
		}

		// Check if every Transaction is valid
		for (Transaction transaction : eqcBlock.getTransactions().getNewTransactionList()) {
			if (transaction.isCoinBase()) {
				
			} else {
				try {
					if (!transaction.isValid(accountsMerkleTree, AddressShape.READABLE)) {
						Log.Error("Every Transaction should valid");
						return false;
					}
				} catch (NoSuchFieldException | IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					Log.Error("Every Transaction should valid: " + e.getMessage());
					return false;
				}
			}
		}

		return true;
	}

	public void buildTransactionsForVerify() {
		// Only have CoinBase Transaction just return
		if (transactions.getNewTransactionList().size() == 1) {
			Transaction transaction = transactions.getNewTransactionList().get(0);
			// Set Address for every Transaction
			// Set TxOut Address
			for (TxOut txOut : transaction.getTxOutList()) {
				txOut.getAddress().setReadableAddress(Util.getAddress(txOut.getAddress().getID(), this));
			}
			return;
		}

		// Set Signature for every Transaction
		// Bug fix change to verify if every Transaction's signature is equal to
		// Signatures
		for (int i = 1; i < signatures.getSignatureList().size(); ++i) {
			transactions.getNewTransactionList().get(i).setSignature(signatures.getSignatureList().get(i));
		}

		for (int i = 1; i < transactions.getNewTransactionList().size(); ++i) {
			Transaction transaction = transactions.getNewTransactionList().get(i);
			// Set PublicKey for every Transaction
			// Bug fix before add in Transactions every transaction should have signature &
			// PublicKey.
			transaction.setPublickey(Util.getPublicKey(transaction.getTxIn().getAddress().getID(), this));
			// Set Address for every Transaction
			// Set TxIn Address
			transaction.getTxIn().getAddress()
					.setReadableAddress(Util.getAddress(transaction.getTxIn().getAddress().getID(), this));
			// Set TxOut Address
			for (TxOut txOut : transaction.getTxOutList()) {
				txOut.getAddress().setReadableAddress(Util.getAddress(txOut.getAddress().getID(), this));
			}
		}
	}

	public boolean isEveryAddressExists() {
		for (Transaction transaction : transactions.getNewTransactionList()) {
			// Check if TxIn Address exists
			if (!transaction.isCoinBase()) {
				if (Util.getAddress(transaction.getTxIn().getAddress().getID(), this) == null) {
					return false;
				}
			}

			// Check if All TxOut Address exists
			for (TxOut txOut : transaction.getTxOutList()) {
				if (Util.getAddress(txOut.getAddress().getID(), this) == null) {
					return false;
				}
			}
		}
		return true;
	}

	public boolean isEveryPublicKeyExists() {
		if (transactions.getNewTransactionList().size() == 1) {
			return true;
		}
		for (int i = 1; i < transactions.getSize(); ++i) {
			return false;
		}
		return true;
	}

	@Override
	public byte[] getBytes() {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			os.write(eqcHeader.getBin());
			os.write(root.getBin());
			os.write(transactions.getBin());
			os.write(signatures.getBin());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return os.toByteArray();
	}

	@Override
	public byte[] getBin() {
		return EQCType.bytesToBIN(getBytes());
	}

	public int getSize() {
		return getBytes().length;
	}

	/**
	 * @return the root
	 */
	public Root getRoot() {
		return root;
	}

	/**
	 * @param root the root to set
	 */
	public void setRoot(Root root) {
		this.root = root;
	}

	public byte[] getHash() {
		return eqcHeader.getHash();
	}

	@Override
	public boolean isSanity(AddressShape... addressShape) {
		if (addressShape.length != 0) {
			return false;
		}
		if (eqcHeader == null || root == null || transactions == null) {
			return false;
		}
		if (transactions.getNewTransactionList().size() == 0) {
			return false;
		}
		if (!transactions.getNewTransactionList().get(0).isCoinBase()) {
			return false;
		}
		if (transactions.getNewTransactionList().size() == 1) {
			if (signatures != null) {
				return false;
			}
		} else {
			for (int i = 1; i < transactions.getNewAccountList().size(); ++i) {
				if (transactions.getNewTransactionList().get(i).isCoinBase()) {
					return false;
				}
			}
			if (transactions.getNewTransactionList().size() != (signatures.getSignatureList().size() + 1)) {
				return false;
			}
		}
		return true;
	}

	public byte[] getTransactionsMerkelTreeRoot() {
		Vector<byte[]> transactionsMerkelTreeRootList = new Vector<>();
		byte[] bytes = null;
		transactionsMerkelTreeRootList.add(transactions.getNewTransactionListMerkelTreeRoot());
		
		if ((bytes = signatures.getSignaturesMerkelTreeRoot()) != null) {
			transactionsMerkelTreeRootList.add(bytes);
		}
		else {
			transactionsMerkelTreeRootList.add(Util.NULL_HASH);
		}
		
		if ((bytes = transactions.getNewAccountListMerkelTreeRoot()) != null) {
			transactionsMerkelTreeRootList.add(bytes);
		}
		else {
			transactionsMerkelTreeRootList.add(Util.NULL_HASH);
		}
		
		if ((bytes = transactions.getNewPublickeyListMerkelTreeRoot()) != null) {
			transactionsMerkelTreeRootList.add(bytes);
		}
		else {
			transactionsMerkelTreeRootList.add(Util.NULL_HASH);
		}
		return Util.EQCCHA_MULTIPLE_FIBONACCI_MERKEL(Util.getMerkleTreeRoot(transactionsMerkelTreeRootList), Util.THOUSANDPLUS,
				false);
	}

	@Override
	public byte[] getBytes(AddressShape addressShape) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public byte[] getBin(AddressShape addressShape) {
		// TODO Auto-generated method stub
		return null;
	}

//	public boolean isAddressListAddressUnique() {
//		for (int i = 0; i < transactions.getAddressList().size(); ++i) {
//			for (int j = i + 1; j < transactions.getAddressList().size(); ++j) {
//				if (transactions.getAddressList().get(i).equals(transactions.getAddressList().get(j))) {
//					return false;
//				}
//			}
//		}
//		return true;
//	}

}
