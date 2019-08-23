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
package com.eqchains.blockchain;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Vector;

import org.rocksdb.RocksDBException;
import org.rocksdb.WriteBatch;

import com.eqchains.blockchain.account.Account;
import com.eqchains.blockchain.account.Passport;
import com.eqchains.blockchain.account.Asset;
import com.eqchains.blockchain.account.AssetAccount;
import com.eqchains.blockchain.account.AssetSubchainAccount;
import com.eqchains.blockchain.account.CoinAsset;
import com.eqchains.blockchain.account.Publickey;
import com.eqchains.blockchain.account.Passport.AddressShape;
import com.eqchains.blockchain.accountsmerkletree.AccountsMerkleTree;
import com.eqchains.blockchain.transaction.CoinbaseTransaction;
import com.eqchains.blockchain.transaction.OperationTransaction;
import com.eqchains.blockchain.transaction.CompressedPublickey;
import com.eqchains.blockchain.transaction.Transaction;
import com.eqchains.blockchain.transaction.TxIn;
import com.eqchains.blockchain.transaction.TxOut;
import com.eqchains.persistence.EQCBlockChainRocksDB;
import com.eqchains.persistence.EQCBlockChainRocksDB.TABLE;
import com.eqchains.serialization.EQCTypable;
import com.eqchains.serialization.EQCType;
import com.eqchains.serialization.EQCType.ARRAY;
import com.eqchains.util.ID;
import com.eqchains.util.Log;
import com.eqchains.util.Util;
import com.eqchains.util.Util.AddressTool;


/**
 * @author Xun Wang
 * @date Oct 1, 2018
 * @email 10509759@qq.com
 */
public class EQChains implements EQCTypable {
	private Vector<Transaction> newTransactionList;
	private long newTransactionListSize;
	private Vector<Passport> newPassportList;
	private long newPassportListSize;
	private Vector<CompressedPublickey> newCompressedPublickeyList;
	private long newCompressedPublickeyListSize;
	
	public EQChains() {
		super();
		init();
	}
	
	public void init() {
		newTransactionList = new Vector<>();
		newTransactionListSize = 0;
		newPassportList = new Vector<>();
		newPassportListSize = 0;
		newCompressedPublickeyList = new Vector<>();
		newCompressedPublickeyListSize = 0;
	}
	
	public EQChains(ByteBuffer byteBuffer) throws NoSuchFieldException, IOException {
		parseTransactions(byteBuffer.array());
	}
	
	public EQChains(byte[] bytes) throws NoSuchFieldException, IOException {
		parseTransactions(bytes);
	}
	
	private void parseTransactions(byte[] bytes) throws NoSuchFieldException, IOException {
		ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		byte[] data = null;
		init();
		
		// Parse TransactionList
		ARRAY array = EQCType.parseARRAY(is);
		if (!array.isNULL()) {
			newTransactionListSize = array.size;
			ByteArrayInputStream iStream = new ByteArrayInputStream(array.elements);
			for (int i=0; i<newTransactionListSize; ++i) {
				newTransactionList.add(Transaction.parseTransaction(EQCType.parseBIN(iStream), Passport.AddressShape.ID));
			}
			EQCType.assertNoRedundantData(iStream);
		}
		else {
			throw EQCType.NULL_OBJECT_EXCEPTION;
		}

		// Parse Accounts
		array = EQCType.parseARRAY(is);
		if (!array.isNULL()) {
			newPassportListSize = array.size;
			ByteArrayInputStream iStream = new ByteArrayInputStream(array.elements);
			for(int i=0; i<newPassportListSize; ++i) {
				newPassportList.add(new Passport(iStream));
			}
			EQCType.assertNoRedundantData(iStream);
		}
		
		// Parse PublicKeys
		array = EQCType.parseARRAY(is);
		if (!array.isNULL()) {
			newCompressedPublickeyListSize = array.size;
			ByteArrayInputStream iStream = new ByteArrayInputStream(array.elements);
			for(int i=0; i<newCompressedPublickeyListSize; ++i) {
				newCompressedPublickeyList.add(new CompressedPublickey(iStream));
			}
			EQCType.assertNoRedundantData(iStream);
		}
	}
	
//	private void parseTransactionList(byte[] bytes) throws NoSuchFieldException, IOException {
//		ARRAY array = EQCType.parseARRAY(bytes);
//		newTransactionListSize = array.length;
//		for (byte[] transaction : array.elements) {
//			newTransactionList.add(Transaction.parseTransaction(transaction, Address.AddressShape.ID));
//		}
//	}
//	
//	
//	private void parsePublicKeys(byte[] bytes) throws NoSuchFieldException, IOException{
//		ARRAY publickeys = EQCType.parseARRAY(bytes);
//		newCompressedPublickeyListSize = publickeys.length;
//		for(byte[] publickey : publickeys.elements) {
//			newCompressedPublickeyList.add(new PublicKey(publickey));
//		}
//	}
	
//	private static boolean isPublicKeysValid(ARRAY publickeys) throws NoSuchFieldException, IOException {
//		boolean boolIsPublicKeysValid = false;
//		if(publickeys.length == publickeys.elements.size()) {
//			boolIsPublicKeysValid = true;
//		}
//		for(byte[] publickey : publickeys.elements) {
//			if(!PublicKey.isValid(publickey)) {
//				boolIsPublicKeysValid = false;
//				break;
//			}
//		}
//		return boolIsPublicKeysValid;
//	}
	
//	private void parseAccounts(byte[] bytes) throws NoSuchFieldException, IOException{
//		ARRAY array = EQCType.parseARRAY(bytes);
//		newPassportListSize = (int) array.length;
//		for(byte[] address : array.elements) {
//			newPassportList.add(new Address(address));
//		}
//	}
	
	public void addTransaction(Transaction transaction) {
		if(!isTransactionExists(transaction)) {
			// Add Publickey
			if(!transaction.isCoinBase() && transaction.getCompressedPublickey().isNew()) {
				newCompressedPublickeyList.add(transaction.getCompressedPublickey());
			}
			// Add new Address
			for(TxOut txOut : transaction.getTxOutList()) {
				if(txOut.isNew()) {
					newPassportList.add(txOut.getPassport());
				}
			}
			// Add Transaction
			newTransactionList.add(transaction);
		}
	}
	
	public boolean isTransactionExists(Transaction transaction) {
		return newTransactionList.contains(transaction);
	}
	
	@Deprecated
	public boolean isAccountExists(Passport address) {
		return newPassportList.contains(address);
	}
	
	@Deprecated
	public boolean isAddressExists(String address) {
		boolean isExists = false;
		for(Passport address2 : newPassportList) {
			if(address2.getReadableAddress().equals(address)) {
				isExists = true;
				break;
			}
		}
		return isExists;
	}
	
	@Deprecated
	public boolean isPublicKeyExists(byte[] compressedPublickey) {
		boolean isExists = false;
		for(CompressedPublickey compressedPublickey2 : newCompressedPublickeyList) {
			if(Arrays.equals(compressedPublickey, compressedPublickey2.getCompressedPublickey())) {
				isExists = true;
				break;
			}
		}
		return isExists;
	}
	
	/**
	 * Get the Transactions' bytes for storage it in the EQC block chain
	 * For save the space the Address' shape is Serial Number which is the EQCBits type.
	 * @return byte[]
	 */
	@Override
	public byte[] getBytes() {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			os.write(getTransactionsArray());
			os.write(getAccountsArray());
			os.write(getPublicKeysArray());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return os.toByteArray();
	}

	public ByteBuffer getByteBuffer() {
		return ByteBuffer.wrap(getBytes());
	}
	
	private byte[] getAccountsArray() {
		if (newPassportList.size() == 0) {
			return EQCType.bytesToBIN(null);
		}
		else {
			Vector<byte[]> addresses = new Vector<byte[]>();
			for (Passport address : newPassportList) {
				addresses.add(address.getBytes());
			}
			return EQCType.bytesArrayToARRAY(addresses);
//			ByteArrayOutputStream os = new ByteArrayOutputStream();
//			for (Address address : AccountList) {
//				try {
//					os.write(address.getAIBin());
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//					Log.Error(e.getMessage());
//				}
//			}
//			return EQCType.bytesToBIN(os.toByteArray());
		}
	}
	
	private byte[] getPublicKeysArray() {
		if (newCompressedPublickeyList.size() == 0) {
			return EQCType.bytesToBIN(null);
		} 
		else {
			Vector<byte[]> compressedPublickeys = new Vector<byte[]>();
			for (CompressedPublickey compressedPublickey : newCompressedPublickeyList) {
				compressedPublickeys.add(compressedPublickey.getBytes());
			}
			return EQCType.bytesArrayToARRAY(compressedPublickeys);
		}
	}
	
	private byte[] getTransactionsArray() {
		if (newTransactionList.size() == 0) {
//			throw new IllegalArgumentException("At a minimum should include a transaction.");
			return EQCType.bytesArrayToARRAY(null);
		} 
		else {
			Vector<byte[]> transactions = new Vector<byte[]>();
			for(Transaction transaction: newTransactionList) {
				transactions.add(transaction.getBin(AddressShape.ID));
			}
			return EQCType.bytesArrayToARRAY(transactions);
//			ByteArrayOutputStream os = new ByteArrayOutputStream();
//			for (Transaction transaction : transactionList) {
//				try {
//					os.write(transaction.getBin());
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//					Log.Error(e.getMessage());
//				}
//			}
//			return EQCType.bytesToBIN(os.toByteArray());
		}
	}
	
	/**
	 * Get the Transactions' BIN for storage it in the EQC block chain.
	 * For save the space the Address' shape is Serial Number which is the EQCBits type.
	 * @return byte[]
	 */
	@Override
	public byte[] getBin() {
		// TODO Auto-generated method stub
		return EQCType.bytesToBIN(getBytes());
	}
	
	private String _getnewPassportList() {
		String tx = null;
		if (newPassportList != null && newPassportList.size() > 0) {
			tx = "\n[\n";
			if (newPassportList.size() > 1) {
				for (int i = 0; i < newPassportList.size() - 1; ++i) {
					tx += newPassportList.get(i) + ",\n";
				}
			}
			tx += newPassportList.get(newPassportList.size() - 1);
			tx += "\n]";
		} else {
			tx = "[]";
		}
		return tx;
	}
	
	private String _getnewCompressedPublickeyList() {
		String tx = null;
		if (newCompressedPublickeyList != null && newCompressedPublickeyList.size() > 0) {
			tx = "\n[\n";
			if (newCompressedPublickeyList.size() > 1) {
				for (int i = 0; i < newCompressedPublickeyList.size() - 1; ++i) {
					tx += newCompressedPublickeyList.get(i) + ",\n";
				}
			}
			tx += newCompressedPublickeyList.get(newCompressedPublickeyList.size() - 1);
			tx += "\n]";
		} else {
			tx = "[]";
		}
		return tx;
	}
	
	private String _getNewTransactionList() {
		String tx = null;
		if (newTransactionList != null && newTransactionList.size() > 0) {
			tx = "\n[\n";
			if (newTransactionList.size() > 1) {
				for (int i = 0; i < newTransactionList.size() - 1; ++i) {
					tx += newTransactionList.get(i) + ",\n";
				}
			}
			tx += newTransactionList.get(newTransactionList.size() - 1);
			tx += "\n]";
		} else {
			tx = "[]";
		}
		return tx;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return 
		
				"{\n" +
				toInnerJson() +
				"\n}";
		
	}
	
	public String toInnerJson() {
		return 
		
				"\"Transactions\":" + 
				"\n{\n" +
					"\"NewPassportList\":" + 
					"\n{\n" +
					"\"Size\":\"" + newPassportListSize + "\",\n" +
					"\"List\":" + 
						_getnewPassportList() + "\n},\n" +
						"\"NewCompressedPublickeyList\":" + 
						"\n{\n" +
						"\"Size\":\"" + newCompressedPublickeyListSize + "\",\n" +
						"\"List\":" + 
							_getnewCompressedPublickeyList() + "\n},\n" +
								"\"NewTransactionList\":" + 
								"\n{\n" +
								"\"Size\":\"" + newTransactionListSize + "\",\n" +
								"\"List\":" + 
									_getNewTransactionList() + "\n}\n" +
				"}";
		
	}
	
	public int getSize() {
		return getBytes().length;
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

	/**
	 * @param newCompressedPublickeyList the newCompressedPublickeyList to set
	 */
	public void setNewCompressedPublickeyList(Vector<CompressedPublickey> newCompressedPublickeyList) {
		this.newCompressedPublickeyList = newCompressedPublickeyList;
	}

	/**
	 * @return the newTransactionList
	 */
	public Vector<Transaction> getNewTransactionList() {
		return newTransactionList;
	}

	/**
	 * @param newTransactionList the newTransactionList to set
	 */
	public void setNewTransactionList(Vector<Transaction> newTransactionList) {
		this.newTransactionList = newTransactionList;
	}

	@Deprecated
	public void deleteTransactionTail() {
		Transaction transaction = newTransactionList.lastElement();
		for(TxOut txOut : transaction.getTxOutList()) {
			if(newPassportList.contains(txOut.getPassport())) {
				newPassportList.removeElement(txOut.getPassport());
			}
		}
		if(newCompressedPublickeyList.contains(transaction.getCompressedPublickey())) {
			newCompressedPublickeyList.removeElement(transaction.getCompressedPublickey());
		}
		newTransactionList.remove(transaction);
	}
	
	public byte[] getNewTransactionListMerkelTreeRoot() {
		Vector<byte[]> transactions = new Vector<byte[]>();
		for (Transaction transaction : newTransactionList) {
			transactions.add(transaction.getBytes(AddressShape.ID));
		}
		return Util.getMerkleTreeRoot(transactions);
	}
	
	public byte[] getNewCompressedPublickeyListMerkelTreeRoot() {
		if(newCompressedPublickeyList.size() == 0) {
			return null;
		}
		Vector<byte[]> compressedPublickeys = new Vector<byte[]>();
		for (CompressedPublickey compressedPublickey : newCompressedPublickeyList) {
			compressedPublickeys.add(compressedPublickey.getBytes());
		}
		return Util.getMerkleTreeRoot(compressedPublickeys);
	}
	
	public byte[] getNewPassportListMerkelTreeRoot() {
		if(newPassportList.size() == 0) {
			return null;
		}
		Vector<byte[]> addresses = new Vector<byte[]>();
		for (Passport address : newPassportList) {
			addresses.add(address.getBytes());
		}
		return Util.getMerkleTreeRoot(addresses);
	}
	
	public byte[] getHash() {
		return  Util.EQCCHA_MULTIPLE_DUAL(getBytes(), Util.ONE, true, false);
	}

	@Override
	public boolean isSanity() {
		for(Passport address : newPassportList) {
			if(!address.isSanity(null)) {
				return false;
			}
		}
		for(CompressedPublickey compressedPublickey : newCompressedPublickeyList) {
			if(!compressedPublickey.isSanity()) {
				return false;
			}
		}
		for(Transaction transaction : newTransactionList) {
			if(!transaction.isSanity(AddressShape.ID)) {
				return false;
			}
		}
		return false;
	}

	public long getTxFee() {
		long txFee = 0;
		for(int i=1; i<newTransactionList.size(); ++i) {
			txFee += newTransactionList.get(i).getTxFee();
		}
		return txFee;
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
//				account.setPublickeyCreateHeight(accountsMerkleTree.getHeight());
				accountsMerkleTree.saveAccount(account);
//					Log.info("Original Account Numbers: " + accountsMerkleTree.getTotalAccountNumbers());
//					accountsMerkleTree.increaseTotalAccountNumbers();
//					Log.info("New Account Numbers: " + accountsMerkleTree.getTotalAccountNumbers());
			}
		}
		return true;
	}
	
	public boolean isPublickeyIDExistsInTransactions(ID id) {
		for(Transaction transaction : newTransactionList) {
			if(!transaction.isCoinBase()) {
			if(transaction.getTxIn().getPassport().getID().equals(id)) {
				return true;
			}
			}
		}
		return false;
	}
	
	public boolean isnewCompressedPublickeyListValid(AccountsMerkleTree accountsMerkleTree) throws Exception {
		WriteBatch writeBatch = null;
		if(newCompressedPublickeyList.size() > 0) {
			writeBatch = new WriteBatch();
		}
		// Get the new Publickey's ID list from Transactions
		Vector<ID> newPublickeys = new Vector<>();
		for(int i=1; i<newTransactionList.size(); ++i) {
			Account account = accountsMerkleTree.getAccount(newTransactionList.get(i).getTxIn().getPassport().getID(), true);
				if(!account.isPublickeyExists()) {
					if(!newPublickeys.contains(newTransactionList.get(i).getTxIn().getPassport().getID())) {
						newPublickeys.add(newTransactionList.get(i).getTxIn().getPassport().getID());
					}
				}
		}
		if(newCompressedPublickeyList.size() != newPublickeys.size()) {
			return false;
		}
		for(CompressedPublickey compressedPublickey:newCompressedPublickeyList) {
//			compressedPublickey.setID(accountsMerkleTree.getAccount(AddressTool.publickeyToAI(compressedPublickey.getCompressedPublickey())).getID());
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
			if(false) {//accountsMerkleTree.isPublicKeyExists(compressedPublickey)) {
				return false;
			}
			else {
				Account account = accountsMerkleTree.getAccount(compressedPublickey.getID(), true);
				if(!AddressTool.verifyAddressPublickey(account.getPassport().getReadableAddress(), compressedPublickey.getCompressedPublickey())) {
					return false;
				}
				else {
					Publickey publickey1 = new Publickey();
					publickey1.setCompressedPublickey(compressedPublickey.getCompressedPublickey());
					publickey1.setPublickeyCreateHeight(accountsMerkleTree.getHeight());
					account.setPublickey(publickey1);
//					writeBatch.put(EQCBlockChainRocksDB.getInstance().getTableHandle(accountsMerkleTree.getFilter().getFilterTable(TABLE.ACCOUNT)), account.getID().getEQCBits(),
//							account.getBytes());
//					writeBatch.put(EQCBlockChainRocksDB.getInstance().getTableHandle(accountsMerkleTree.getFilter().getFilterTable(TABLE.ACCOUNT_AI)),
//							account.getPassport().getAddressAI(), account.getID().getEQCBits());
				}
			}
		}
//		if(writeBatch != null) {
//			accountsMerkleTree.getFilter().batchUpdate(writeBatch);
//		}
		return true;
	}
	
	public boolean isAllTxInPublickeyExists(AccountsMerkleTree accountsMerkleTree) throws Exception {
		for(int i=1; i<newTransactionList.size(); ++i) {
			if(!accountsMerkleTree.getAccount(newTransactionList.get(i).getTxIn().getPassport().getID(), true).isPublickeyExists()) {
				return false;
			}
		}
		return true;
	}
	
	public CoinbaseTransaction getCoinbaseTransaction() {
		return (CoinbaseTransaction) newTransactionList.get(0);
	}
	
	public ID getNewPassportID(AccountsMerkleTree accountsMerkleTree) {
		ID initID = null;
		if (newPassportList.size() == 0) {
			initID = new ID(accountsMerkleTree.getTotalAccountNumbers()
					.add(BigInteger.valueOf(Util.INIT_ADDRESS_SERIAL_NUMBER)));
		} else {
			initID = newPassportList.lastElement().getID().getNextID();
		}
		return initID;
	}

	@Override
	public boolean isValid(AccountsMerkleTree accountsMerkleTree) {
		// TODO Auto-generated method stub
		return false;
	}

}
