/**
 * EQchains core - EQchains Foundation's EQchains core library
 * @copyright 2018-present EQchains Foundation All rights reserved...
 * Copyright of all works released by EQchains Foundation or jointly released by
 * EQchains Foundation with cooperative partners are owned by EQchains Foundation
 * and entitled to protection available from copyright law by country as well as
 * international conventions.
 * Attribution — You must give appropriate credit, provide a link to the license.
 * Non Commercial — You may not use the material for commercial purposes.
 * No Derivatives — If you remix, transform, or build upon the material, you may
 * not distribute the modified material.
 * For any use of above stated content of copyright beyond the scope of fair use
 * or without prior written permission, EQchains Foundation reserves all rights to
 * take any legal action and pursue any right or remedy available under applicable
 * law.
 * https://www.eqchains.com
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
package com.eqchains.blockchain.hive;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Vector;

import javax.naming.InitialContext;
import javax.print.attribute.Size2DSyntax;

import org.rocksdb.RocksDBException;

import com.eqchains.blockchain.account.Passport;
import com.eqchains.avro.O;
import com.eqchains.blockchain.EQChains;
import com.eqchains.blockchain.account.Asset;
import com.eqchains.blockchain.account.AssetSubchainAccount;
import com.eqchains.blockchain.account.Passport.AddressShape;
import com.eqchains.blockchain.accountsmerkletree.AccountsMerkleTree;
import com.eqchains.blockchain.accountsmerkletree.AccountsMerkleTree.Statistics;
import com.eqchains.blockchain.subchain.EQCSignatures;
import com.eqchains.blockchain.subchain.EQCSubchain;
import com.eqchains.blockchain.subchain.EQcoinSubchain;
import com.eqchains.blockchain.subchain.EQcoinSubchainHeader;
import com.eqchains.blockchain.transaction.CoinbaseTransaction;
import com.eqchains.blockchain.transaction.OperationTransaction;
import com.eqchains.blockchain.transaction.Transaction;
import com.eqchains.blockchain.transaction.TransferTransaction;
import com.eqchains.blockchain.transaction.TxIn;
import com.eqchains.blockchain.transaction.TxOut;
import com.eqchains.blockchain.transaction.operation.UpdateAddressOperation;
import com.eqchains.crypto.MerkleTree;
import com.eqchains.keystore.Keystore;
import com.eqchains.persistence.EQCBlockChainH2;
import com.eqchains.persistence.EQCBlockChainRocksDB;
import com.eqchains.serialization.EQCTypable;
import com.eqchains.serialization.EQCType;
import com.eqchains.serialization.EQCType.ARRAY;
import com.eqchains.service.MinerService;
import com.eqchains.util.ID;
import com.eqchains.util.Log;
import com.eqchains.util.Util;
import com.eqchains.util.Util.AddressTool.AddressType;

/**
 * @author Xun Wang
 * @date Oct 1, 2018
 * @email 10509759@qq.com
 */
public class EQCHive implements EQCTypable {
	private EQCHeader eqcHeader;
	private EQCRoot eqcRoot;
	private Vector<EQCSubchain> subchainList;

//	private EQChains transactions;
//	private EQCSignatures signatures;
	// private txReceipt;
	// The min size of the EQCHeader's is 142 bytes.
	private int size = 142;

	public EQCHive(byte[] bytes, boolean isSegwit) throws Exception {
		EQCType.assertNotNull(bytes);
		ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		parse(is, isSegwit);
		EQCType.assertNoRedundantData(is);
	}

	public EQCHive(O o, boolean isSegwit) throws Exception {
		byte[] bytes = o.getO().array();
		EQCType.assertNotNull(bytes);
		ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		parse(is, isSegwit);
		EQCType.assertNoRedundantData(is);
	}

	private void parse(ByteArrayInputStream is, boolean isSegwit) throws Exception {
		// Parse EqcHeader
		eqcHeader = new EQCHeader(EQCType.parseBIN(is));

		// Parse Root
		eqcRoot = new EQCRoot(EQCType.parseBIN(is));

		// Parse Subchains
		ARRAY subchains = EQCType.parseARRAY(is);
		subchainList = new Vector<>();
		ByteArrayInputStream is1 = new ByteArrayInputStream(subchains.elements);
		while (!EQCType.isInputStreamEnd(is1)) {
			subchainList.add(EQCSubchain.parse(EQCType.parseBIN(is1), isSegwit));
		}
		EQCType.assertEqual(subchains.size, subchainList.size());
	}

	public EQCHive() {
		init();
	}

	private void init() {
		eqcHeader = new EQCHeader();
		eqcRoot = new EQCRoot();
		subchainList = new Vector<>();
		subchainList.add(new EQcoinSubchain());
	}

	public EQCHive(ID currentBlockHeight, byte[] previousBlockHeaderHash) throws RocksDBException, Exception {
		init();
		// Create EQC block header
		eqcHeader.setPreHash(previousBlockHeaderHash);
		eqcHeader.setHeight(currentBlockHeight);
		eqcHeader.setTarget(Util.cypherTarget(currentBlockHeight));
		eqcHeader.setTimestamp(new ID(System.currentTimeMillis()));
		eqcHeader.setNonce(ID.ZERO);
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

		"\"EQCHive\":{\n" + eqcHeader.toInnerJson() + ",\n" + eqcRoot.toInnerJson() + ",\n" +
		"\"EQCSubchainList\":" + 
		"\n{\n" +
		"\"Size\":\"" + subchainList.size() + "\",\n" +
		"\"List\":" + 
			getEQCSubchainList() + "\n}\n" +
		 "}";
	}
	
	private String getEQCSubchainList() {
		String tx = null;
		if (subchainList != null && subchainList.size() > 0) {
			tx = "\n[\n";
			if (subchainList.size() > 1) {
				for (int i = 0; i < subchainList.size() - 1; ++i) {
					tx += subchainList.get(i) + ",\n";
				}
			}
			tx += subchainList.get(subchainList.size() - 1);
			tx += "\n]";
		} else {
			tx = "[]";
		}
		return tx;
	}

	public void accountingEQCHive(AccountsMerkleTree accountsMerkleTree) throws Exception {
		/**
		 * Heal Protocol If height equal to a specific height then update the ID No.1's
		 * Address to a new Address the more information you can reference to
		 * https://github.com/eqzip/eqchains
		 */

		/**
		 * Begin handle EQcoinSubchain
		 */
		Vector<Transaction> pendingTransactionList = new Vector<Transaction>();
		EQcoinSubchain eQcoinSubchain = (EQcoinSubchain) subchainList.get(0);

		if (accountsMerkleTree.getHeight().compareTo(Util.getMaxCoinbaseHeight(accountsMerkleTree.getHeight())) < 0) {
			// Create CoinBase Transaction
			Passport passport = new Passport();
			passport.setReadableAddress(Util.SINGULARITY_C);
			CoinbaseTransaction coinbaseTransaction = Util.generateCoinBaseTransaction(passport, accountsMerkleTree);
			// Check if CoinBase isValid and update CoinBase's Account
			if (!coinbaseTransaction.isValid(accountsMerkleTree, AddressShape.READABLE)) {
				throw new IllegalStateException("CoinbaseTransaction is invalid: " + coinbaseTransaction);
			} else {
				coinbaseTransaction.prepareAccounting(accountsMerkleTree,
						eQcoinSubchain.getNewPassportID(accountsMerkleTree));
				coinbaseTransaction.update(accountsMerkleTree);
			}
			// Add CoinBase into EQcoinSubchainHeader
			eQcoinSubchain.addCoinbaseTransaction(coinbaseTransaction);
		}

		// Get EQcoinSubchain Transaction list till now only handle this but in the
		// future will handle all Transactions together to meet balance less
		pendingTransactionList.addAll(EQCBlockChainH2.getInstance().getTransactionListInPool());
		Log.info("Current have " + pendingTransactionList.size() + " pending Transactions.");

		// Handle every pending Transaction
		for (Transaction transaction : pendingTransactionList) {
			// If Transaction's TxIn is null or TxIn doesn't exists in Accounts just
			// continue
			if (transaction.getTxIn() == null
					|| !accountsMerkleTree.isAccountExists(transaction.getTxIn().getPassport(), false)) {
				EQCBlockChainH2.getInstance().deleteTransactionInPool(transaction);
				Log.Error("TxIn is null or TxIn doesn't exist in Accounts this is invalid just discard it: "
						+ transaction.toString());
				continue;
			}

			// If Transaction already in the EQcoinSubchain just continue
			if (eQcoinSubchain.isTransactionExists(transaction)) {
				EQCBlockChainH2.getInstance().deleteTransactionInPool(transaction);
				Log.Error("Transaction already exists this is invalid just discard it: " + transaction.toString());
				continue;
			}

			// Prepare Transaction
			transaction.prepareAccounting(accountsMerkleTree, eQcoinSubchain.getNewPassportID(accountsMerkleTree));

			// Check if Transaction is valid
			if (!transaction.isValid(accountsMerkleTree, AddressShape.READABLE)) {
				EQCBlockChainH2.getInstance().deleteTransactionInPool(transaction);
				Log.Error("Transaction is invalid: " + transaction);
				continue;
			}

			// Add Transaction into EQcoinSubchain
			if ((eQcoinSubchain.getNewTransactionListLength()
					+ transaction.getBin(AddressShape.ID).length) <= Util.MAX_BLOCK_SIZE) {
				Log.info("Add new Transaction which TxFee is: " + transaction.getTxFee());
				eQcoinSubchain.addTransaction(transaction);
				// Update Transaction
				transaction.update(accountsMerkleTree);
				// Update the TxFee
				eQcoinSubchain.getEQcoinSubchainHeader().depositTxFee(transaction.getTxFee());
			} else {
				Log.info("Exceed EQcoinSubchain's MAX_BLOCK_SIZE just stop accounting transaction");
				break;
			}
		}

		// Update EQcoinSubchain's Header
		EQcoinSubchainHeader preEQcoinSubchainHeader = accountsMerkleTree.getEQCBlock(accountsMerkleTree.getHeight().getPreviousID(), true).getEQcoinSubchain().getEQcoinSubchainHeader();
		eQcoinSubchain.getEQcoinSubchainHeader().setTotalAccountNumbers(preEQcoinSubchainHeader.getTotalAccountNumbers().add(ID.valueOf(eQcoinSubchain.getNewPassportList().size())));
		eQcoinSubchain.getEQcoinSubchainHeader().setTotalTransactionNumbers(preEQcoinSubchainHeader.getTotalTransactionNumbers().add(ID.valueOf(eQcoinSubchain.getNewTransactionList().size())));
		
		// Update EQcoin AssetSubchainAccount's Header
		AssetSubchainAccount eqcoin = (AssetSubchainAccount) accountsMerkleTree.getAccount(ID.ONE, true);
		eqcoin.getAssetSubchainHeader().setTotalSupply(new ID(Util.cypherTotalSupply(eqcHeader.getHeight())));
		eqcoin.getAssetSubchainHeader().setTotalAccountNumbers(eqcoin.getAssetSubchainHeader().getTotalAccountNumbers()
				.add(BigInteger.valueOf(eQcoinSubchain.getNewPassportList().size())));
		eqcoin.getAssetSubchainHeader().setTotalTransactionNumbers(eqcoin.getAssetSubchainHeader()
				.getTotalTransactionNumbers().add(BigInteger.valueOf(eQcoinSubchain.getNewTransactionList().size())));
		eqcoin.getAsset(Asset.EQCOIN).deposit(eQcoinSubchain.getEQcoinSubchainHeader().getTotalTxFee());
		// Save EQcoin Subchain's Header
		accountsMerkleTree.saveAccount(eqcoin);
		
		if (!eQcoinSubchain.getEQcoinSubchainHeader().getTotalAccountNumbers()
				.equals(eqcoin.getAssetSubchainHeader().getTotalAccountNumbers())) {
			throw new IllegalStateException("TotalAccountNumbers is invalid");
		}
		
		if (!eQcoinSubchain.getEQcoinSubchainHeader().getTotalTransactionNumbers()
				.equals(eqcoin.getAssetSubchainHeader().getTotalTransactionNumbers())) {
			throw new IllegalStateException("TotalTransactionNumbers is invalid");
		}

		/**
		 * Begin handle MiscSmartContractTransaction
		 */

		/**
		 * Begin handle EQCSubchainTransaction
		 */

		// Check Statistics
		Statistics statistics = accountsMerkleTree.getStatistics();
		if (!statistics.isValid(accountsMerkleTree)) {
			throw new IllegalStateException("Statistics is invalid!");
		}

		// Build AccountsMerkleTree and generate Root
		accountsMerkleTree.buildAccountsMerkleTree();
		accountsMerkleTree.generateRoot();

		// Initial Root
		eqcRoot.setAccountsMerkelTreeRoot(accountsMerkleTree.getRoot());
		eqcRoot.setSubchainsMerkelTreeRoot(getSubchainsMerkelTreeRoot());
		// Set EQCHeader's Root's hash
		eqcHeader.setRootHash(eqcRoot.getHash());

	}

	public boolean isMeetPreconditions(Object... objects) throws Exception {
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

	/**
	 * Deprecated use public boolean isValid(AccountsMerkleTree accountsMerkleTree)
	 * instead of
	 * 
	 * @param eqcBlock
	 * @param accountsMerkleTree
	 * @return
	 * @throws Exception
	 */
//	@Deprecated
	// Keep this only for reference&double check after used it then removed it
//	public static boolean verify(EQCHive eqcBlock, AccountsMerkleTree accountsMerkleTree) throws Exception {
//		// Check if EQCHeader is valid
//		BigInteger target = Util.targetBytesToBigInteger(Util.cypherTarget(eqcBlock.getHeight()));
//		if (new BigInteger(1,
//				Util.EQCCHA_MULTIPLE_FIBONACCI_MERKEL(eqcBlock.getEqcHeader().getBytes(), Util.HUNDRED_THOUSAND))
//						.compareTo(target) > 0) {
//			Log.Error("EQCHeader is invalid");
//			return false;
//		}
//
//		// Check if Transactions size is less than 1 MB
////		if (eqcBlock.getTransactions().getSize() > Util.ONE_MB) {
////			Log.Error("Transactions size  is invalid, size: " + eqcBlock.getTransactions().getSize());
////			return false;
////		}
//
//		// Check if AddressList is correct the first Address' Serial Number is equal to
//		// previous block's last Address' Serial Number + 1
//		// Every Address should be unique in current AddressList and doesn't exists in
//		// the history AddressList in H2
//		// Every Address's Serial Number should equal to previous Address'
//		// getNextSerialNumber
//
//		// Check if PublicKeyList is correct the first PublicKey's Serial Number is
//		// equal to previous block's last PublicKey's getNextSerialNumber
//		// Every PublicKey should be unique in current PublicKeyList and doesn't exists
//		// in the history PublicKeyList in H2
//		// Every PublicKey's Serial Number should equal to previous PublicKey's
//		// getNextSerialNumber
//
//		// Get Transaction list and Signature list
//		Vector<Transaction> transactinList = eqcBlock.getTransactions().getNewTransactionList();
//		Vector<byte[]> signatureList = eqcBlock.getSignatures().getSignatureList();
//
//		// In addition to the CoinBase transaction, the following checks are made for
//		// all other transactions.
//		// Check if every Transaction's PublicKey is exists
//		if (!eqcBlock.isEveryPublicKeyExists()) {
//			Log.Error("Every Transaction's PublicKey should exists");
//			return false;
//		}
//		// Check if every Transaction's Address is exists
//		if (!eqcBlock.isEveryAddressExists()) {
//			Log.Error("Every Transaction's Address should exists");
//			return false;
//		}
//
//		// Fill in every Transaction's PublicKey, Signature, relevant Address for verify
//		// Bad methods need change to every Transaction use itself's prepareVerify
////		eqcBlock.buildTransactionsForVerify();
//
//		// Check if only have CoinBase Transaction
//		if (signatureList == null) {
//			if (transactinList.size() != 1) {
//				Log.Error(
//						"Only have CoinBase Transaction but the number of Transaction isn't equal to 1, current size: "
//								+ transactinList.size());
//				return false;
//			}
//		} else {
//			// Check if every Transaction has it's Signature
//			if ((transactinList.size() - 1) != signatureList.size()) {
//				Log.Error("Transaction's number: " + (transactinList.size() - 1)
//						+ " doesn't equal to Signature's number: " + signatureList.size());
//				return false;
//			}
//		}
//
//		// Check if CoinBase is correct - CoinBase's Address&Value is valid
//		if (!transactinList.get(0).isCoinBase()) {
//			Log.Error("The No.0 Transaction isn't CoinBase");
//			return false;
//		}
//		// Check if CoinBase's TxOut Address is valid
//		if (!transactinList.get(0).isTxOutAddressValid()) {
//			Log.Error("The CoinBase's TxOut's Address is invalid: "
//					+ transactinList.get(0).getTxOutList().get(0).toString());
//			return false;
//		}
//		// Check if CoinBase's value is valid
//		long totalTxFee = 0;
//		for (int i = 1; i < transactinList.size(); ++i) {
//			totalTxFee += transactinList.get(i).getTxFee();
//		}
//		long coinBaseValue = 0;
//		if (eqcBlock.getHeight().compareTo(Util.getMaxCoinbaseHeight(eqcBlock.getHeight())) < 0) {
//			coinBaseValue = Util.COINBASE_REWARD + totalTxFee;
//			if (transactinList.get(0).getTxOutValues() != coinBaseValue) {
//				Log.Error("CoinBase's value: " + transactinList.get(0).getTxOutValues()
//						+ " doesn't equal to COINBASE_REWARD + totalTxFee: " + (Util.COINBASE_REWARD + totalTxFee));
//				return false;
//			}
//		} else {
//			coinBaseValue = totalTxFee;
//			if (transactinList.get(0).getTxOutValues() != coinBaseValue) {
//				Log.Error("CoinBase's value: " + transactinList.get(0).getTxOutValues()
//						+ " doesn't equal to totalTxFee: " + totalTxFee);
//				return false;
//			}
//		}
//
//		// Check if only have one CoinBase
//		for (int i = 1; i < transactinList.size(); ++i) {
//			if (transactinList.get(i).isCoinBase()) {
//				Log.Error("Every EQCBlock should has only one CoinBase but No. " + i + " is also CoinBase.");
//				return false;
//			}
//		}
//
//		// Check if Signature is unique in current Signatures and doesn't exists in the
//		// history Signature table in H2
//		for (int i = 0; i < signatureList.size(); ++i) {
//			for (int j = i + 1; j < signatureList.size(); ++j) {
//				if (Arrays.equals(signatureList.get(i), signatureList.get(j))) {
//					Log.Error("Signature doesn't unique in current  Signature list");
//					return false;
//				}
//			}
//		}
//
//		for (byte[] signature : signatureList) {
//			if (EQCBlockChainH2.getInstance().isSignatureExists(signature)) {
//				Log.Error("Signature doesn't unique in H2's history Signature list");
//				return false;
//			}
//		}
//
//		// Check if every Transaction is valid
//		for (Transaction transaction : eqcBlock.getTransactions().getNewTransactionList()) {
//			if (transaction.isCoinBase()) {
//
//			} else {
//				if (!transaction.isValid(accountsMerkleTree, AddressShape.READABLE)) {
//					Log.Error("Every Transaction should valid");
//					return false;
//				}
//			}
//		}
//
//		return true;
//	}

	@Override
	public byte[] getBytes() {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			os.write(eqcHeader.getBin());
			os.write(eqcRoot.getBin());
			os.write(getEQCSubchainArray());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return os.toByteArray();
	}

	private byte[] getEQCSubchainArray() throws Exception {
		Vector<byte[]> subchains = new Vector<>();
		for (EQCSubchain eqcSubchain : subchainList) {
			subchains.add(eqcSubchain.getBin());
		}
		return EQCType.bytesArrayToARRAY(subchains);
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
	public EQCRoot getRoot() {
		return eqcRoot;
	}

	/**
	 * @param root the root to set
	 */
	public void setRoot(EQCRoot root) {
		this.eqcRoot = root;
	}

	public byte[] getHash() {
		return eqcHeader.getHash();
	}

	@Override
	public boolean isSanity() {
		if (eqcHeader == null || eqcRoot == null || subchainList == null) {
			return false;
		}
		if (subchainList.size() != 1) {
			return false;
		}
		if (!(subchainList.get(0) instanceof EQcoinSubchain)) {
			return false;
		}
		if (!getEQcoinSubchain().isSanity()) {
			return false;
		}
//		if (transactions.getNewTransactionList().size() == 0) {
//			return false;
//		}
//		if (!transactions.getNewTransactionList().get(0).isCoinBase()) {
//			return false;
//		}
//		if (transactions.getNewTransactionList().size() == 1) {
//			if (signatures != null) {
//				return false;
//			}
//		} else {
//			for (int i = 1; i < transactions.getNewPassportList().size(); ++i) {
//				if (transactions.getNewTransactionList().get(i).isCoinBase()) {
//					return false;
//				}
//			}
//			if (transactions.getNewTransactionList().size() != (signatures.getSignatureList().size() + 1)) {
//				return false;
//			}
//		}
		return true;
	}

//	public byte[] getTransactionsMerkelTreeRoot() {
//		Vector<byte[]> transactionsMerkelTreeRootList = new Vector<>();
//		byte[] bytes = null;
//		transactionsMerkelTreeRootList.add(transactions.getNewTransactionListMerkelTreeRoot());
//
//		if ((signatures != null) && (bytes = signatures.getSignaturesMerkelTreeRoot()) != null) {
//			transactionsMerkelTreeRootList.add(bytes);
//		}
////		else {
////			transactionsMerkelTreeRootList.add(Util.NULL_HASH);
////		}
//
//		if ((bytes = transactions.getNewPassportListMerkelTreeRoot()) != null) {
//			transactionsMerkelTreeRootList.add(bytes);
//		}
////		else {
////			transactionsMerkelTreeRootList.add(Util.NULL_HASH);
////		}
//
//		if ((bytes = transactions.getNewCompressedPublickeyListMerkelTreeRoot()) != null) {
//			transactionsMerkelTreeRootList.add(bytes);
//		}
////		else {
////			transactionsMerkelTreeRootList.add(Util.NULL_HASH);
////		}
//		return Util.EQCCHA_MULTIPLE_DUAL(Util.getMerkleTreeRoot(transactionsMerkelTreeRootList), Util.ONE, false,
//				false);
//	}

	public byte[] getSubchainsMerkelTreeRoot() throws Exception {
		Vector<byte[]> subchainsRootList = new Vector<>();
		for (EQCSubchain eqcSubchain : subchainList) {
			subchainsRootList.add(eqcSubchain.getBytes());
		}
		return Util.EQCCHA_MULTIPLE_DUAL(Util.getMerkleTreeRoot(subchainsRootList), Util.ONE, true, false);
	}

	/**
	 * Auditing the EQCHive
	 * <p>
	 * @param accountsMerkleTree
	 * @return
	 * @throws Exception
	 */
	@Override
	public boolean isValid(AccountsMerkleTree accountsMerkleTree) {
		try {

			/**
			 * Heal Protocol If height equal to a specific height then update the ID No.1's
			 * Address to a new Address the more information you can reference to
			 * https://github.com/eqzip/eqchains
			 */

			// Check if Target is valid
			if (!eqcHeader.isDifficultyValid(accountsMerkleTree)) {
				Log.Error("EQCHeader difficulty is invalid.");
				return false;
			}

			// Check if EQcoinSubchain is valid
			if (!getEQcoinSubchain().isValid(accountsMerkleTree)) {
				Log.Error("EQcoinSubchain is invalid.");
				return false;
			}

			// Verify Statistics
			Statistics statistics = accountsMerkleTree.getStatistics();
			if (!statistics.isValid(accountsMerkleTree)) {
				Log.Error("Statistics data is invalid.");
				return false;
			}

			// Build AccountsMerkleTree and generate Root and Statistics
			accountsMerkleTree.buildAccountsMerkleTree();
			accountsMerkleTree.generateRoot();

			// Verify Root
//		// Check total supply
//		if (statistics.totalSupply != Util.cypherTotalSupply(eqcHeader.getHeight())) {
//			Log.Error("Total supply is invalid doesn't equal cypherTotalSupply.");
//			return false;
//		}
//		if(statistics.totalSupply != root.getTotalSupply()){
//			Log.Error("Total supply is invalid doesn't equal root.");
//			return false;
//		}

//			EQCHive previousBlock = EQCBlockChainRocksDB.getInstance()
//					.getEQCBlock(eqcHeader.getHeight().getPreviousID(), true);
			// Check total Account numbers
//		if (!previousBlock.getRoot().getTotalAccountNumbers()
//				.add(BigInteger.valueOf(transactions.getNewAccountList().size()))
//				.equals(accountsMerkleTree.getTotalAccountNumbers())) {
//			Log.Error("Total Account numbers is invalid doesn't equal accountsMerkleTree.");
//			return false;
//		}
//		if(!root.getTotalAccountNumbers().equals(accountsMerkleTree.getTotalAccountNumbers())) {
//			Log.Error("Total Account numbers is invalid doesn't equal root.");
//			return false;
//		}

//		// Check total Transaction numbers
//		if (!previousBlock.getRoot().getTotalTransactionNumbers()
//				.add(BigInteger.valueOf(transactions.getNewTransactionList().size()))
//				.equals(statistics.totalTransactionNumbers)) {
//			Log.Error("Total Transaction numbers is invalid doesn't equal transactions.getNewTransactionList.");
//			return false;
//		}
//		if(!statistics.totalTransactionNumbers.equals(root.getTotalTransactionNumbers())) {
//			Log.Error("Total Transaction numbers is invalid doesn't equal root.getTotalTransactionNumbers.");
//			return false;
//		}
			// Check AccountsMerkelTreeRoot
			if (!Arrays.equals(eqcRoot.getAccountsMerkelTreeRoot(), accountsMerkleTree.getRoot())) {
				Log.Error("EQCAccountsMerkelTreeRoot is invalid!");
				return false;
			}
			// Check TransactionsMerkelTreeRoot
			if (!Arrays.equals(eqcRoot.getSubchainsMerkelTreeRoot(), getSubchainsMerkelTreeRoot())) {
				Log.Error("EQCSubchainsMerkelTreeRoot is invalid!");
				return false;
			}
			// Verify EQCHeader
			if (!eqcHeader.isValid(accountsMerkleTree, eqcRoot.getHash())) {
				Log.Error("EQCHeader is invalid!");
				return false;
			}

			// Merge shouldn't be done at here
//		// Merge AccountsMerkleTree relevant Account's status
//		if(!accountsMerkleTree.merge()) {
//			Log.Error("Merge AccountsMerkleTree relevant Account's status error occur");
//			return false;
//		}
		} catch (Exception e) {
			Log.Error("EQCHive is invalid: " + e.getMessage());
			return false;
		}

		return true;
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

	public O getO() {
		return new O(ByteBuffer.wrap(this.getBytes()));
	}

	/**
	 * @return the subchainList
	 */
	public Vector<EQCSubchain> getSubchainList() {
		return subchainList;
	}

	public EQcoinSubchain getEQcoinSubchain() {
		return (EQcoinSubchain) subchainList.get(0);
	}

	public EQCSubchain getEQCSubchain(ID id) {
		EQCSubchain eqcSubchain = null;
		for (EQCSubchain eqcSubchain2 : subchainList) {
			if (eqcSubchain2.getSubchainHeader().getID().equals(id)) {
				eqcSubchain = eqcSubchain2;
			}
		}
		return eqcSubchain;
	}

//	/**
//	 * @return the transactions
//	 */
//	public EQChains getTransactions() {
//		return transactions;
//	}
//
//	/**
//	 * @return the signatures
//	 */
//	public EQCSignatures getSignatures() {
//		return signatures;
//	}

}
