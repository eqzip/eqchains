/**
 * EQCoin core - EQCOIN Foundation's EQCoin core library
 * @copyright 2018-present EQCOIN Foundation All rights reserved...
 * Copyright of all works released by EQCOIN Foundation or jointly released by
 * EQCOIN Foundation with cooperative partners are owned by EQCOIN Foundation
 * and entitled to protection available from copyright law by country as well as
 * international conventions.
 * Attribution — You must give appropriate credit, provide a link to the license.
 * Non Commercial — You may not use the material for commercial purposes.
 * No Derivatives — If you remix, transform, or build upon the material, you may
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
package com.eqzip.eqcoin.blockchain;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Vector;

import javax.naming.InitialContext;
import javax.print.attribute.Size2DSyntax;

import com.eqzip.eqcoin.blockchain.Account.Publickey;
import com.eqzip.eqcoin.keystore.Keystore;
import com.eqzip.eqcoin.persistence.avro.EQCBlockAvro;
import com.eqzip.eqcoin.persistence.h2.EQCBlockChainH2;
import com.eqzip.eqcoin.persistence.rocksdb.EQCBlockChainRocksDB;
import com.eqzip.eqcoin.serialization.EQCTypable;
import com.eqzip.eqcoin.serialization.EQCType;
import com.eqzip.eqcoin.service.MinerService;
import com.eqzip.eqcoin.util.Log;
import com.eqzip.eqcoin.util.SerialNumber;
import com.eqzip.eqcoin.util.Util;
import com.eqzip.eqcoin.util.Util.AddressShape;

/**
 * @author Xun Wang
 * @date Oct 1, 2018
 * @email 10509759@qq.com
 */
public class EQCBlock implements EQCTypable {
	private EQCHeader eqcHeader;
	private Root root;
//	private Index index;
	private Transactions transactions;
	// The following is Transactions' Segregated Witness members it's hash will be
	// recorded in the TransactionHeader
	private Signatures signatures;
	// private txReceipt;
	// The min size of the EQCHeader's is 142 bytes.
	private int size = 142;
//	private SerialNumber height;
	/*
	 * VERIFICATION_COUNT equal to the number of member variables of the class to be
	 * verified.
	 */
	private static byte VERIFICATION_COUNT = 5;

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

//		// Parse Index
//		if ((data = EQCType.parseBIN(is)) != null) {
//			if (!Index.isValid(data)) {
//				throw new ClassCastException("Index bytes are invalid.");
//			}
//			index = new Index(data);
//		}

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
			if ((data = EQCType.parseBIN(is)) != null) {
				if (!Signatures.isValid(data)) {
					throw new ClassCastException("Signatures bytes are invalid.");
				}
				signatures = new Signatures(data);
			}
		}
	}

	public static boolean isValid(byte[] bytes, boolean isSegwit) throws NoSuchFieldException, IOException {
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

		// Parse Index
		if ((data = EQCType.parseBIN(is)) != null) {
			if (Index.isValid(data)) {
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

		if (!isSegwit) {
			// Parse Signatures
			data = null;
			if ((data = EQCType.parseBIN(is)) != null) {
				if (Signatures.isValid(data)) {
					++validCount;
				}
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
//		index = new Index();
		transactions = new Transactions();
		signatures = new Signatures();
	}

	public EQCBlock(SerialNumber currentBlockHeight, byte[] previousBlockHeaderHash) {

		init();

//		height = currentBlockHeight;
		// Create EQC block header
		eqcHeader.setNonce(SerialNumber.ZERO);
		eqcHeader.setPreHash(previousBlockHeaderHash);
		eqcHeader.setTarget(Util.cypherTarget(currentBlockHeight));
		eqcHeader.setTimestamp(new SerialNumber(System.currentTimeMillis()));

		root.setHeight(currentBlockHeight);

		// Create TransactionsHeader
		TransactionsHeader transactionsHeader = new TransactionsHeader();
		transactionsHeader.setSignaturesHash(null);
		transactions.setTransactionsHeader(transactionsHeader);

	}

	// 這個主要用來構造SingularBlock但是依然有問題，因爲有很多個CoinBase交易。Need do more job
	public void addTransaction(Transaction transaction) {
		if (!isTransactionExists(transaction)) {
			if (transaction.isCoinBase()) {
				if (transactions.getTransactionNumbers() != 0) {
					throw new IllegalArgumentException("CoinBase should be the first transaction.");
				}
				transactions.addTransaction(transaction);
				// need do more job to fix this when h2 is ready this will be ok. 
				// Already fixed due to have add Nonce and AccountsMerkleTree so we can make sure every Transaction is unique
//				signatures.addSignature(transaction.getSignature());
			} else {
				transactions.addTransaction(transaction);
				signatures.addSignature(transaction.getSignature());
			}
		}
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

	public SerialNumber getHeight() {
		return root.getHeight();
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

		"\"EQCBlock\":{\n" + eqcHeader.toInnerJson() + ",\n" + transactions.toInnerJson() + ",\n"
				+ signatures.toInnerJson() + "\n" + "}";

	}

	public void buildTransactions(SerialNumber height, AccountsMerkleTree accountsMerkleTree) throws NoSuchFieldException, IllegalStateException, IOException {
		Vector<Transaction> pendingTransactionList = new Vector<Transaction>();
		Transaction transaction = null;
		if (!height.isNextSerialNumber(accountsMerkleTree.getHeight())){
			throw new IllegalStateException("Current block's height is wrong isn't previous block's next height!");
		}
		accountsMerkleTree.setHeight(height);
		// Create CoinBase Transaction
		Address address = new Address();
		address.setAddress(Keystore.getInstance().getUserAccounts().get(5).getAddress());

		// Add CoinBase into Transactions
		pendingTransactionList.add(Util.generateCoinBaseTransaction(address, height));

		// Get Transaction list
		pendingTransactionList.addAll(EQCBlockChainH2.getInstance().getTransactionListInPool());

		// Handle every pending Transaction
		for (int i = 0; i < pendingTransactionList.size(); ++i) {
			transaction = pendingTransactionList.get(i);
			
			// Check CoinBase is valid
			if((i == 0) && !transaction.isCoinBase()) {
				throw new IllegalArgumentException("No.0 Transaction must be CoinBase Transaction");
			}
			else {
				if(!transaction.getTxOutList().get(0).getAddress().getSerialNumber().equals(SerialNumber.TWO) && (transaction.getTxOutList().get(0).getValue() != Util.EQC_FOUNDATION_COINBASE_REWARD))
				throw new IllegalArgumentException("CoinBase Transaction's first Address must be EQC Foundation's Address which Serial Number should be No.2 and it's value should be " + Util.EQC_FOUNDATION_COINBASE_REWARD);
			}
			
			// If Transaction already in this.transactions just continue
			if (this.transactions.isTransactionExists(transaction)) {
				EQCBlockChainH2.getInstance().deleteTransactionInPool(transaction);
				Log.Error("Transaction already exists this is invalid just discard it: "
						+ transaction.toString());
				continue;
			}
			// Only No.0 can be CoinBase Transaction
			if ((i > 0) && transaction.isCoinBase()) {
				EQCBlockChainH2.getInstance().deleteTransactionInPool(transaction);
				Log.Error("Only No. 0 can be CoinBase Transaction but No." + i + " is also CoinBase this is invalid just discard it: "
						+ transaction.toString());
				continue;
			}

			// If isn't CoinBase Transaction
			if (!transaction.isCoinBase()) {
				
				// Check if TxIn's Address exists and fill in TxIn's Serial Number
				if (!accountsMerkleTree.isAddressExists(transaction.getTxIn().getAddress())) {
					// TxIn doesn't exist illegal Transaction just discard it
					// Delete this Transaction in Transaction List & TransactionPool
					pendingTransactionList.remove(transaction);
					// Delete relevant Transaction in pool
					EQCBlockChainH2.getInstance().deleteTransactionInPool(transaction);
					Log.Error("Transaction's TxIn doesn't exists this is invalid just discard it: "
							+ transaction.toString());
					continue;
				} else {
					// Fill in TxIn's Address' Serial Number
					transaction.getTxIn().getAddress().setSerialNumber(accountsMerkleTree
							.getAddressSerialNumber(transaction.getTxIn().getAddress()));
					transaction.getPublickey().setSerialNumber(transaction.getTxIn().getAddress().getSerialNumber());
				}

				// Check if Transaction is valid
				if (!transaction.isValid(accountsMerkleTree)) {
					EQCBlockChainH2.getInstance().deleteTransactionInPool(transaction);
					Log.Error("Transaction is invalid: " + transaction.toString());
					continue;
				} else {
					
					// Check if Transaction's Nonce is correct
					if (!transaction.getNonce().subtract(accountsMerkleTree.getAccount(transaction.getTxIn().getAddress().getSerialNumber()).getNonce())
							.equals(BigInteger.ONE)) {
						// Can't delte it need do more job to mark this add new flag -
						// Nonce doesn't continuous if more than 24 Hours just delete the relevant
						// transaction in pool
//						EQCBlockChainH2.getInstance().deleteTransactionInPool(transaction);
						Log.Error("Transaction is invalid: " + transaction.toString());
						continue;
					} 
//					else {
//						
//						// Update AccountsMerkleTree's MiningState's TxIn Account & it's Nonce
//						accountStatusTxIn = null;
//						accountStatusTxIn = AccountsMerkleTreeService.getInstance().getAccountsMerkleTree()
//								.getAccountStatus(transaction.getTxIn().getAddress().getSerialNumber());
//						if (accountStatusTxIn.getAccount(ACCOUNT_STATUS.MINING) == null) {
//							accountStatusTxIn.setMiningAccount(accountStatusTxIn.getAccount(ACCOUNT_STATUS.FIXED));
//						}
//						accountStatusTxIn.getAccount(ACCOUNT_STATUS.MINING).getNonce().add(BigInteger.ONE);
//						accountStatusTxIn.getAccount(ACCOUNT_STATUS.MINING).setNonceChanged(true);
//					}
				}

				// Add Publickey if any
				if (!transaction.isCoinBase()) {
					// Check if PublicKey is exists in PublicKeyList if not then add it in current
					// block's PublicKeyList
					// if PublicKeyList's size is zero then use
					// EQCBlockChainH2.getInstance().getLastPublicKeySerialNumber() get the last
					// PublicKey' Serial Number
					// otherwise get the Serial Number from the PublicKeyList's last PublicKey.
					if (!accountsMerkleTree.isPublicKeyExists(transaction.getPublickey())) {
//						transaction.getPublickey().setSerialNumber(transaction.getTxIn().getAddress().getSerialNumber());
						transaction.getPublickey().setNew(true);
//						transactions.getPublicKeyList().add(transaction.getPublickey());
//						Publickey publickey = new Publickey();
//						publickey.setPublickey(transaction.getPublickey().getPublicKey());
//						publickey.setPublickeyCreateHeight(accountsMerkleTree.getHeight(ACCOUNT_STATUS.MINING));
//						accountsMerkleTree.getAccountStatus(transaction.getTxIn().getAddress().getSerialNumber())
//								.getAccount(ACCOUNT_STATUS.MINING).setPublickey(publickey);
//						accountsMerkleTree.getAccountStatus(transaction.getTxIn().getAddress().getSerialNumber())
//								.getAccount(ACCOUNT_STATUS.MINING).setPublickeyChanged(true);
					}
				}
			}
			
			// Add new Address if any
			// Check if current Transaction's all address is exists in AddressList if not
			// then add it in current block's AddressList
			// if AddressList's size is zero then use
			// AccountsMerkleTreeService.getInstance().getAccountsMerkleTree().getTotalAccountNumber()
			// get the
			// Total Account Number then calculate Serial Number according to it
			// otherwise get the Serial Number from the AddressList's last Address.
			for (TxOut txOut : transaction.getTxOutList()) {
				if (!accountsMerkleTree.isAddressExists(txOut.getAddress())) {
					if (transactions.getAddressList().size() == 0) {
						// Need test this if totalAccountNumberStatus can be modified in
						// getTotalAccountNumber @2019-2-23. 2-26 have tested the original Biginteger can't be modified 
						SerialNumber initSerialNumber = new SerialNumber(
								accountsMerkleTree.getTotalAccountNumber()
										.add(BigInteger.valueOf(Util.INIT_ADDRESS_SERIAL_NUMBER)));
						txOut.getAddress().setSerialNumber(initSerialNumber);
					} else {
						txOut.getAddress()
								.setSerialNumber(transactions.getAddressList().lastElement().getSerialNumber().getNextSerialNumber());
					}
					txOut.setNew(true);
//					transactions.getAddressList().add(txOut.getAddress());
//					// Due to already setAddressChanged(true) in addAccount so here does nothing
//					accountsMerkleTree.addNewAccount(txOut.getAddress(), ACCOUNT_STATUS.MINING);
				} else {
					// For security issue need retrieve and fill in every Address' Serial Number according to it's AddressAI
					txOut.getAddress().setSerialNumber(
							accountsMerkleTree.getAddressSerialNumber(txOut.getAddress()));
				}
			}

			// Add Transaction into Transactions due to the extra Signature's space maybe
			// here will waste some space but this isn't a bug
			if ((transactions.getSize() + transaction.getBillingSize()) <= Util.ONE_MB) {
				this.transactions.addTransaction(transaction);
//				Account accountStatus = null;
				// If current Transaction isn't CoinBase
				if (!transaction.isCoinBase()) {
					// Add signature
					signatures.addSignature(transaction.getSignature());

					// Update current Transaction's relevant Account's AccountsMerkleTree's data
					// Update current Transaction's TxIn Publickey if need
					if (transaction.getPublickey().isNew()) {
//						Publickey publickey = new Publickey();
//						publickey.setPublickey(transaction.getPublickey().getPublicKey());
//						publickey.setPublickeyCreateHeight(accountsMerkleTree.getHeight());
						accountsMerkleTree.savePublicKey(transaction.getPublickey(), height);
//						accountsMerkleTree.getAccountStatus(transaction.getTxIn().getAddress().getSerialNumber())
//								.getAccount(ACCOUNT_STATUS.MINING).setPublickey(publickey);
//						accountsMerkleTree.getAccountStatus(transaction.getTxIn().getAddress().getSerialNumber())
//								.getAccount(ACCOUNT_STATUS.MINING).setPublickeyChanged(true);
					}
					
//					// Update current Transaction's TxIn Account if need
//					accountStatus = accountsMerkleTree.getAccountStatus(transaction.getTxIn().getAddress().getSerialNumber());
//					if (accountStatus.getAccount(ACCOUNT_STATUS.MINING) == null) {
//						accountStatus.setMiningAccount(accountStatus.getAccount(ACCOUNT_STATUS.FIXED));
//					}
					
					Account account = accountsMerkleTree.getAccount(transaction.getTxIn().getAddress().getSerialNumber());
					// Update current Transaction's TxIn Account's Nonce
//					accountStatus.getAccount(ACCOUNT_STATUS.MINING).getNonce().add(BigInteger.ONE);
//					accountStatus.getAccount(ACCOUNT_STATUS.MINING).setNonceChanged(true);
					account.increaseNonce();
					
					//Update current Transaction's TxIn Account's balance
//					accountStatus.getMiningAccount().updateBalance(-transaction.getBillingValue());
//					accountStatus.getMiningAccount().setBalanceUpdateHeight(height);
					account.updateBalance(-transaction.getBillingValue());
					accountsMerkleTree.saveAccount(account);
					
					// Update the CoinBase's TxFee
					long value = this.transactions.getTransactionList().get(0).getTxOutList().get(1).getValue();
					this.transactions.getTransactionList().get(0).getTxOutList().get(1)
							.setValue(value + transaction.getTxFee());
				}
//				// Update current Transaction's TxOut if need
//				for(TxOut txOut : transaction.getTxOutList()) {
//					if(txOut.isNew()) {
//						Account account = new Account();
//						account.setAddress(txOut.getAddress());
//						account.setAddressCreateHeight(height);
//						accountsMerkleTree.saveAccount(account);
//					}
//				}
				// Update current Transaction's TxOut Account
				for(TxOut txOut : transaction.getTxOutList()) {
					Account account = null;
					if(txOut.isNew()) {
						account = new Account();
						account.setAddress(txOut.getAddress());
						account.setAddressCreateHeight(height);
					}
					else {
						account = accountsMerkleTree.getAccount(txOut.getAddress().getSerialNumber());
					}
					account.updateBalance(txOut.getValue());
					account.setBalanceUpdateHeight(height);
					accountsMerkleTree.saveAccount(account);
				}
			} else {
				break;
			}
		}
		
		// Update CoinBase's Account
		Account coinbase = accountsMerkleTree.getAccount(transactions.getTransactionList().get(0).getTxOutList().get(1).getAddress().getSerialNumber());
		coinbase.updateBalance(transactions.getTransactionList().get(0).getTxOutList().get(1).getValue());
		coinbase.setBalanceUpdateHeight(height);
		accountsMerkleTree.saveAccount(coinbase);
		
		// Build AccountsMerkleTree and generate Root
		accountsMerkleTree.buildAccountsMerkleTree();
		accountsMerkleTree.generateRoot();

//		// Initial Index
//		index.setTotalSupply(Util.cypherTotalSupply(root.getHeight()));
//		EQCBlock previousBlock = EQCBlockChainH2.getInstance()
//				.getEQCBlock(new SerialNumber(root.getHeight().getSerialNumber().subtract(BigInteger.ONE)), true);
//		index.setTotalAccountNumbers(previousBlock.getIndex().getTotalAccountNumbers()
//				.add(BigInteger.valueOf(transactions.getAddressList().size())));
//		index.setTotalTransactionNumbers(previousBlock.getIndex().getTotalTransactionNumbers()
//				.add(BigInteger.valueOf(transactions.getTransactionNumbers())));
//		index.setAccountsMerkleTreeRootList(accountsMerkleTree.getAccountsMerkleTreeRootList());
//		index.setTransactionsHash(transactions.getHash());

		// Initial Root
		// Already set height in the Constructor
//		root.setIndexHash(index.getHash());
//		root.setAccountsHash(index.getAccountsMerkleTreeRootListRoot());
		root.setTotalSupply(Util.cypherTotalSupply(root.getHeight()));
		EQCBlock previousBlock = EQCBlockChainRocksDB.getInstance()
				.getEQCBlock(new SerialNumber(root.getHeight().subtract(BigInteger.ONE)), true);
		root.setTotalAccountNumbers(previousBlock.getRoot().getTotalAccountNumbers()
				.add(BigInteger.valueOf(transactions.getAddressList().size())));
		root.setTotalTransactionNumbers(previousBlock.getRoot().getTotalTransactionNumbers()
				.add(BigInteger.valueOf(transactions.getTransactionNumbers())));
		root.setTransactionsMerkelTreeRoot(transactions.getTransactionsMerkelTreeRoot());
		// Set EQCHeader's Root's hash
		eqcHeader.setRootHash(root.getHash());

	}

	public void buildTransactionsForVerify() {
		// Only have CoinBase Transaction just return
		if (transactions.getTransactionList().size() == 1) {
			Transaction transaction = transactions.getTransactionList().get(0);
			// Set Address for every Transaction
			// Set TxOut Address
			for (TxOut txOut : transaction.getTxOutList()) {
				txOut.getAddress().setAddress(Util.getAddress(txOut.getAddress().getSerialNumber(), this));
			}
			return;
		}

		// Set Signature for every Transaction
		// Bug fix change to verify if every Transaction's signature is equal to
		// Signatures
		for (int i = 1; i < signatures.getSignatureList().size(); ++i) {
			transactions.getTransactionList().get(i).setSignature(signatures.getSignatureList().get(i));
		}

		for (int i = 1; i < transactions.getTransactionList().size(); ++i) {
			Transaction transaction = transactions.getTransactionList().get(i);
			// Set PublicKey for every Transaction
			// Bug fix before add in Transactions every transaction should have signature &
			// PublicKey.
			transaction.setPublickey(Util.getPublicKey(transaction.getTxIn().getAddress().getSerialNumber(), this));
			// Set Address for every Transaction
			// Set TxIn Address
			transaction.getTxIn().getAddress()
					.setAddress(Util.getAddress(transaction.getTxIn().getAddress().getSerialNumber(), this));
			// Set TxOut Address
			for (TxOut txOut : transaction.getTxOutList()) {
				txOut.getAddress().setAddress(Util.getAddress(txOut.getAddress().getSerialNumber(), this));
			}
		}
	}

	public boolean isEveryAddressExists() {
		for (Transaction transaction : transactions.getTransactionList()) {
			// Check if TxIn Address exists
			if (!transaction.isCoinBase()) {
				if (Util.getAddress(transaction.getTxIn().getAddress().getSerialNumber(), this) == null) {
					return false;
				}
			}

			// Check if All TxOut Address exists
			for (TxOut txOut : transaction.getTxOutList()) {
				if (Util.getAddress(txOut.getAddress().getSerialNumber(), this) == null) {
					return false;
				}
			}
		}
		return true;
	}

	public boolean isEveryPublicKeyExists() {
		if (transactions.getTransactionList().size() == 1) {
			return true;
		}
		for (int i = 1; i < transactions.getSize(); ++i) {
			Transaction transaction = transactions.getTransactionList().get(i);
			if (Util.getPublicKey(transaction.getTxIn().getAddress().getSerialNumber(), this) == null) {
				return false;
			}
		}
		return true;
	}

	@Override
	public byte[] getBytes() {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			os.write(eqcHeader.getBin());
			os.write(root.getBin());
//			os.write(index.getBin());
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

//	/**
//	 * @return the index
//	 */
//	public Index getIndex() {
//		return index;
//	}
//
//	/**
//	 * @param index the index to set
//	 */
//	public void setIndex(Index index) {
//		this.index = index;
//	}

	public byte[] getHash() {
		return eqcHeader.getHash();
	}

}
