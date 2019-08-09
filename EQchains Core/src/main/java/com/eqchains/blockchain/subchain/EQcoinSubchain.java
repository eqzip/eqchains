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
package com.eqchains.blockchain.subchain;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Vector;

import org.rocksdb.WriteBatch;

import com.eqchains.blockchain.account.Account;
import com.eqchains.blockchain.account.Asset;
import com.eqchains.blockchain.account.AssetAccount;
import com.eqchains.blockchain.account.AssetSubchainAccount;
import com.eqchains.blockchain.account.CoinAsset;
import com.eqchains.blockchain.account.EQcoinSubchainAccount;
import com.eqchains.blockchain.account.Passport;
import com.eqchains.blockchain.account.Publickey;
import com.eqchains.blockchain.account.Passport.AddressShape;
import com.eqchains.blockchain.accountsmerkletree.AccountsMerkleTree;
import com.eqchains.blockchain.accountsmerkletree.AccountsMerkleTree.Statistics;
import com.eqchains.blockchain.transaction.CoinbaseTransaction;
import com.eqchains.blockchain.transaction.CompressedPublickey;
import com.eqchains.blockchain.transaction.OperationTransaction;
import com.eqchains.blockchain.transaction.Transaction;
import com.eqchains.blockchain.transaction.TransferTransaction;
import com.eqchains.blockchain.transaction.TxOut;
import com.eqchains.persistence.EQCBlockChainRocksDB;
import com.eqchains.persistence.EQCBlockChainRocksDB.TABLE;
import com.eqchains.serialization.EQCType;
import com.eqchains.serialization.EQCType.ARRAY;
import com.eqchains.util.ID;
import com.eqchains.util.Log;
import com.eqchains.util.Util;
import com.eqchains.util.Util.AddressTool;

/**
 * @author Xun Wang
 * @date July 31, 2019
 * @email 10509759@qq.com
 */
public class EQcoinSubchain extends EQCSubchain {
	private Vector<Passport> newPassportList;
	private Vector<CompressedPublickey> newCompressedPublickeyList;
	
	/* (non-Javadoc)
	 * @see com.eqchains.blockchain.subchain.EQCSubchain#init()
	 */
	@Override
	public void init() {
		super.init();
		newPassportList = new Vector<>();
		newCompressedPublickeyList = new Vector<>();
		subchainHeader = new EQcoinSubchainHeader();
	}

	public EQcoinSubchain() {
		init();
	}

	public EQcoinSubchain(byte[] bytes, boolean isSegwit) throws Exception {
		init();
		EQCType.assertNotNull(bytes);
		ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		parseHeader(is);
		parseBody(is);
		EQCType.assertNoRedundantData(is);
	}

	public void addTransaction(Transaction transaction) {
		if (transaction.getAssetID().equals(subchainHeader.getID()) && !isTransactionExists(transaction)) {
			// Add Transaction
			newTransactionList.add(transaction);
			newTransactionListLength += transaction.getBytes(AddressShape.ID).length;
			// Add Signature
			newSignatureList.add(transaction.getSignature());
			// Add Publickey
			if (transaction.getCompressedPublickey().isNew()) {
				newCompressedPublickeyList.add(transaction.getCompressedPublickey());
			}
			// Add new Address
			for (TxOut txOut : transaction.getTxOutList()) {
				if (txOut.isNew()) {
					newPassportList.add(txOut.getPassport());
				}
			}
		}
	}
	
	public void addCoinbaseTransaction(CoinbaseTransaction coinbaseTransaction) {
		// Add Coinbase Transaction
		((EQcoinSubchainHeader) subchainHeader).setCoinbaseTransaction(coinbaseTransaction);
		// Add new Address
		for (TxOut txOut : coinbaseTransaction.getTxOutList()) {
			if (txOut.isNew()) {
				newPassportList.add(txOut.getPassport());
			}
		}
	}
	
	public EQcoinSubchainHeader getEQcoinSubchainHeader() {
		return (EQcoinSubchainHeader) subchainHeader;
	}
	
	public ID getNewPassportID(AccountsMerkleTree accountsMerkleTree) {
		ID initID = null;
		if (newPassportList.size() == 0) {
			initID = new ID(accountsMerkleTree.getPreviousTotalAccountNumbers()
					.add(BigInteger.valueOf(Util.INIT_ADDRESS_SERIAL_NUMBER)));
		} else {
			initID = newPassportList.lastElement().getID().getNextID();
		}
		return initID;
	}
	
	public Passport getPassport(ID id) {
		Passport passport = null;
		for(Passport passport2:newPassportList) {
			if(passport2.getID().equals(id)) {
				passport = passport2;
				break;
			}
		}
		return passport;
	}
	
	public CompressedPublickey getCompressedPublickey(ID id) {
		CompressedPublickey compressedPublickey = null;
		for(CompressedPublickey compressedPublickey2:newCompressedPublickeyList) {
			if(compressedPublickey2.getID().equals(id)) {
				compressedPublickey = compressedPublickey2;
				break;
			}
		}
		return compressedPublickey;
	}

	/**
	 * @return the newPassportList
	 */
	public Vector<Passport> getNewPassportList() {
		return newPassportList;
	}

	/**
	 * @param newPassportList the newPassportList to set
	 */
	public void setNewPassportList(Vector<Passport> newPassportList) {
		this.newPassportList = newPassportList;
	}

	/**
	 * @return the newCompressedPublickeyList
	 */
	public Vector<CompressedPublickey> getNewCompressedPublickeyList() {
		return newCompressedPublickeyList;
	}

//	public boolean isEveryAddressExists() throws ClassNotFoundException, SQLException {
//		for (Transaction transaction : transactions.getNewTransactionList()) {
//			// Check if TxIn Address exists
//			if (!transaction.isCoinBase()) {
//				if (Util.getAddress(transaction.getTxIn().getPassport().getID(), this) == null) {
//					return false;
//				}
//			}
//
//			// Check if All TxOut Address exists
//			for (TxOut txOut : transaction.getTxOutList()) {
//				if (Util.getAddress(txOut.getPassport().getID(), this) == null) {
//					return false;
//				}
//			}
//		}
//		return true;
//	}
//
//	public boolean isEveryPublicKeyExists() {
//		if (transactions.getNewTransactionList().size() == 1) {
//			return true;
//		}
//		for (int i = 1; i < transactions.getSize(); ++i) {
//			return false;
//		}
//		return true;
//	}
	
	@Override
	public boolean isValid(AccountsMerkleTree accountsMerkleTree) {
		try {
			// Check if Transactions' size < 1 MB
			if (newTransactionListLength > Util.ONE_MB) {
				Log.Error("EQcoinSubchain's length is invalid");
				return false;
			}

			// Check if NewPassportList is valid
			if (!isNewPassportListValid(accountsMerkleTree)) {
				Log.Error("EQcoinSubchain's NewPassportList is invalid");
				return false;              
			}

			// Check if NewCompressedPublickeyList is valid
			if (!isNewCompressedPublickeyListValid(accountsMerkleTree)) {
				Log.Error("EQcoinSubchain's NewCompressedPublickeyList is invalid");
				return false;
			}

			// Check if Signatures' size and Transaction size is equal
			if (newTransactionList.size() != newSignatureList.size()) {
				Log.Error("EQcoinSubchain's newTransactionList's size doesn't equal to newSignatureList's size");
				return false;
			}

			long totalTxFee = 0;
			Transaction transaction = null;
			for (int i = 0; i < newTransactionList.size(); ++i) {
				transaction = newTransactionList.get(i);
				// Fill in Signature
				transaction.setSignature(newSignatureList.get(i));
				
				// Check if TxIn exists in previous block
				if(!accountsMerkleTree.isAccountExists(transaction.getTxIn().getPassport())) {
					Log.Error("Transaction Account doesn't exist in previous block have to exit");
					return false;
				}
				
				try {
					// Here exists one bug
					transaction.prepareVerify(accountsMerkleTree);
				} catch (IllegalStateException e) {
					Log.Error(e.getMessage());
					return false;
				}

				// Check if the Transaction's type is correct
				if (!(transaction instanceof TransferTransaction || transaction instanceof OperationTransaction)) {
					Log.Error("TransactionType is invalid have to exit");
					return false;
				}

				if (!transaction.isValid(accountsMerkleTree, AddressShape.READABLE)) {
					Log.info("Transaction is invalid: " + transaction);
					return false;
				} else {
					// Update AccountsMerkleTree relevant Account's status
					transaction.update(accountsMerkleTree);
					totalTxFee += transaction.getTxFee();
				}
			}

			// Verify CoinBaseTransaction
			CoinbaseTransaction coinbaseTransaction = getEQcoinSubchainHeader().getCoinbaseTransaction();
			coinbaseTransaction.prepareVerify(accountsMerkleTree);
			if (!coinbaseTransaction.isValid(accountsMerkleTree, AddressShape.READABLE)) {
				Log.info("CoinBaseTransaction is invalid: " + coinbaseTransaction);
				return false;
			}
			else {
				coinbaseTransaction.update(accountsMerkleTree);
			}
			
			// Verify TxFee
			if(subchainHeader.getTotalTxFee().longValue() != totalTxFee) {
				Log.Error("Total TxFee is invalid.");
				return false;
			}
			else {
				EQcoinSubchainAccount eQcoinSubchainAccount = (EQcoinSubchainAccount) accountsMerkleTree.getAccount(ID.ONE);
				eQcoinSubchainAccount.getAsset(Asset.EQCOIN).deposit(subchainHeader.getTotalTxFee());
			}
			
			// Update EQcoin Subchain's Header
			AssetSubchainAccount eqcoin = (AssetSubchainAccount) accountsMerkleTree.getAccount(ID.ONE);
			eqcoin.getAssetSubchainHeader().setTotalSupply(ID.valueOf(Util.cypherTotalSupply(accountsMerkleTree.getHeight())));
			eqcoin.getAssetSubchainHeader().setTotalAccountNumbers(eqcoin.getAssetSubchainHeader().getTotalAccountNumbers()
					.add(ID.valueOf(newPassportList.size())));
			eqcoin.getAssetSubchainHeader().setTotalTransactionNumbers(eqcoin.getAssetSubchainHeader()
					.getTotalTransactionNumbers().add(ID.valueOf(newTransactionList.size())));
			// Save EQcoin Subchain's Header
			accountsMerkleTree.saveAccount(eqcoin);

		} catch (Exception e) {
			Log.Error("EQCHive is invalid: " + e.getMessage());
			return false;
		}

		return true;
	}
	
	public boolean isNewPassportListValid(AccountsMerkleTree accountsMerkleTree) throws Exception {
//		if(newPassportList.size() == 0) {
//			// Here exists one bug need check if current Transactions contain any new Passport
//			return true;
//		}
		if (!newPassportList.isEmpty() && !newPassportList.get(0).getID().getPreviousID()
				.equals(accountsMerkleTree.getPreviousTotalAccountNumbers())) {
			return false;
		}
		// Get the new Account's ID list from Transactions
		Vector<ID> newAccounts = new Vector<>();
		for (Transaction transaction : newTransactionList) {
			for (TxOut txOut : transaction.getTxOutList()) {
				if (txOut.getPassport().getID().compareTo(accountsMerkleTree.getPreviousTotalAccountNumbers()) > 0) {
					if (!newAccounts.contains(txOut.getPassport().getID())) {
						newAccounts.add(txOut.getPassport().getID());
					}
				}
			}
		}
		if (newPassportList.size() != newAccounts.size()) {
			return false;
		}
		for (int i = 0; i < newPassportList.size(); ++i) {
			if (!newPassportList.get(i).getID().equals(newAccounts.get(i))) {
				return false;
			}
		}
		for (int i = 0; i < newPassportList.size(); ++i) {
			// Check if Address already exists and if exists duplicate Address in
			// newPassportList
			if (accountsMerkleTree.isAccountExists(newPassportList.get(i), true)) {
				return false;
			} else {
				// Check if ID is valid
				if ((i + 1) < newPassportList.size()) {
					if (!newPassportList.get(i).getID().getNextID().equals(newPassportList.get(i + 1))) {
						return false;
					}
				}
				// Save new Account in Filter
				Account account = new AssetAccount();
				account.setCreateHeight(accountsMerkleTree.getHeight());
				account.setVersion(ID.ZERO);
				account.setVersionUpdateHeight(accountsMerkleTree.getHeight());
				account.setPassport(newPassportList.get(i));
				account.setLockCreateHeight(accountsMerkleTree.getHeight());
				Asset asset = new CoinAsset();
				asset.setVersion(ID.ZERO);
				asset.setVersionUpdateHeight(accountsMerkleTree.getHeight());
				asset.setAssetID(Asset.EQCOIN);
				asset.setCreateHeight(accountsMerkleTree.getHeight());
				asset.deposit(ID.ZERO);
				asset.setBalanceUpdateHeight(accountsMerkleTree.getHeight());
				asset.setNonce(ID.ZERO);
				asset.setNonceUpdateHeight(accountsMerkleTree.getHeight());
				account.setAsset(asset);
				account.setUpdateHeight(accountsMerkleTree.getHeight());
				accountsMerkleTree.saveAccount(account);
//					Log.info("Original Account Numbers: " + accountsMerkleTree.getTotalAccountNumbers());
//					accountsMerkleTree.increaseTotalAccountNumbers();
//					Log.info("New Account Numbers: " + accountsMerkleTree.getTotalAccountNumbers());
			}
		}
		return true;
	}
	
	public boolean isNewCompressedPublickeyListValid(AccountsMerkleTree accountsMerkleTree) throws Exception {
		WriteBatch writeBatch = null;
		if(newCompressedPublickeyList.size() > 0) {
			writeBatch = new WriteBatch();
		}
		// Get the new Publickey's ID list from Transactions
		Vector<ID> newPublickeys = new Vector<>();
		for(Transaction transaction:newTransactionList) {
			Account account = accountsMerkleTree.getAccount(transaction.getTxIn().getPassport().getID());
				if(!account.isPublickeyExists()) {
					if(!newPublickeys.contains(transaction.getTxIn().getPassport().getID())) {
						newPublickeys.add(transaction.getTxIn().getPassport().getID());
					}
				}
		}
		if(newCompressedPublickeyList.size() != newPublickeys.size()) {
			return false;
		}
		for(CompressedPublickey compressedPublickey:newCompressedPublickeyList) {
			Passport passport = new Passport(AddressTool.generateAddress(compressedPublickey.getCompressedPublickey(), AddressTool.getAddressType(compressedPublickey.getCompressedPublickey())));
			compressedPublickey.setID(accountsMerkleTree.getAccount(passport).getID());
		}
		for(int i=0; i<newCompressedPublickeyList.size(); ++i) {
			if(!newCompressedPublickeyList.get(i).getID().equals(newPublickeys.get(i))) {
				return false;
			}
		}
		for(CompressedPublickey compressedPublickey : newCompressedPublickeyList) {
			// Already do this check in previous op check the order of buddy
//			if(!isPublickeyIDExistsInTransactions(publicKey.getID())) {
//				return false;
//			}
			if(accountsMerkleTree.isPublicKeyExists(compressedPublickey)) {
				return false;
			}
			else {
				Account account = accountsMerkleTree.getAccount(compressedPublickey.getID());
				if(!AddressTool.verifyAddressPublickey(account.getPassport().getReadableAddress(), compressedPublickey.getCompressedPublickey())) {
					return false;
				}
				else {
					Publickey publickey1 = new Publickey();
					publickey1.setCompressedPublickey(compressedPublickey.getCompressedPublickey());
					publickey1.setPublickeyCreateHeight(accountsMerkleTree.getHeight());
					account.setPublickey(publickey1);
					writeBatch.put(accountsMerkleTree.getFilter().getTableHandle(TABLE.ACCOUNT), account.getID().getEQCBits(),
							account.getBytes());
					writeBatch.put(accountsMerkleTree.getFilter().getTableHandle(TABLE.ACCOUNT_AI),
							account.getPassport().getAddressAI(), account.getID().getEQCBits());
				}
			}
		}
		if(writeBatch != null) {
			accountsMerkleTree.getFilter().batchUpdate(writeBatch);
		}
		return true;
	}
	

	/* (non-Javadoc)
	 * @see com.eqchains.blockchain.subchain.EQCSubchain#parseHeader(java.io.ByteArrayInputStream)
	 */
	@Override
	public void parseHeader(ByteArrayInputStream is) throws Exception {
		subchainHeader = new EQcoinSubchainHeader(is);
	}

	/* (non-Javadoc)
	 * @see com.eqchains.blockchain.subchain.EQCSubchain#parseBody(java.io.ByteArrayInputStream)
	 */
	@Override
	public void parseBody(ByteArrayInputStream is) throws Exception {
		super.parseBody(is);
		// Parse NewPassportList
		ARRAY passports = null;
		passports = EQCType.parseARRAY(is);
		Passport passport = null;
		if (!passports.isNULL()) {
			ByteArrayInputStream is1 = new ByteArrayInputStream(passports.elements);
			while (!EQCType.isInputStreamEnd(is1)) {
				passport = new Passport(is1);
				newPassportList.add(passport);
			}
			EQCType.assertEqual(passports.size, newPassportList.size());
		}
		// Parse NewCompressedPublickeyList
		ARRAY compressedPublickeys = null;
		compressedPublickeys = EQCType.parseARRAY(is);
		CompressedPublickey compressedPublickey = null;
		if (!compressedPublickeys.isNULL()) {
			ByteArrayInputStream is1 = new ByteArrayInputStream(compressedPublickeys.elements);
			while (!EQCType.isInputStreamEnd(is1)) {
				compressedPublickey = new CompressedPublickey(is1);
				newCompressedPublickeyList.add(compressedPublickey);
			}
			EQCType.assertEqual(compressedPublickeys.size, newCompressedPublickeyList.size());
		}
	}

	/* (non-Javadoc)
	 * @see com.eqchains.blockchain.subchain.EQCSubchain#getBodyBytes()
	 */
	@Override
	public byte[] getBodyBytes() throws Exception {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		os.write(super.getBodyBytes());
		os.write(getNewPassportListARRAY());
		os.write(getNewCompressedPublickeyListARRAY());
		return os.toByteArray();
	}

	/* (non-Javadoc)
	 * @see com.eqchains.blockchain.subchain.EQCSubchain#isSanity()
	 */
	@Override
	public boolean isSanity() {
		if(subchainHeader == null || newSignatureList == null || newSignatureList == null || newPassportList == null || newCompressedPublickeyList == null) {
			return false;
		}
		if(!subchainHeader.isSanity()) {
			return false;
		}
		if(!(newSignatureList.isEmpty() && newSignatureList.isEmpty() && newTransactionListLength == 0 && newCompressedPublickeyList.isEmpty())) {
			return false;
		}
		if(newTransactionList.size() != newSignatureList.size()) {
			return false;
		}
		long newTransactionListLength = 0;
		for(Transaction transaction:newTransactionList) {
			newTransactionListLength += transaction.getBin(AddressShape.ID).length;
		}
		if(this.newTransactionListLength != newTransactionListLength) {
			return false;
		}
		return true;
	}
	
	private byte[] getNewPassportListARRAY() {
		if (newPassportList.isEmpty()) {
			return EQCType.NULL_ARRAY;
		} else {
			Vector<byte[]> passports = new Vector<byte[]>();
			for (Passport passport : newPassportList) {
				passports.add(passport.getBytes());
			}
			return EQCType.bytesArrayToARRAY(passports);
		}
	}
	
	private byte[] getNewCompressedPublickeyListARRAY() {
		if (newCompressedPublickeyList.isEmpty()) {
			return EQCType.NULL_ARRAY;
		} else {
			Vector<byte[]> compressedPublickeys = new Vector<byte[]>();
			for (CompressedPublickey compressedPublickey : newCompressedPublickeyList) {
				compressedPublickeys.add(compressedPublickey.getBin());
			}
			return EQCType.bytesArrayToARRAY(compressedPublickeys);
		}
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
	
	protected String toInnerJson() {
		return null;
	}
	
}
