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
import com.eqchains.blockchain.transaction.Transaction;
import com.eqchains.blockchain.transaction.TxIn;
import com.eqchains.blockchain.transaction.TxOut;
import com.eqchains.serialization.EQCTypable;
import com.eqchains.serialization.EQCType;
import com.eqchains.serialization.EQCType.ARRAY;
import com.eqchains.util.ID;
import com.eqchains.util.Log;
import com.eqchains.util.Util;
import com.eqchains.util.Util.AddressTool;

import avro.shaded.com.google.common.base.Objects;


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
	private Vector<PublicKey> newPublicKeyList;
	private long newPublickeyListSize;
	
	public EQChains() {
		super();
		init();
	}
	
	public void init() {
		newTransactionList = new Vector<Transaction>();
		newTransactionListSize = 0;
		newPassportList = new Vector<Passport>();
		newPassportListSize = 0;
		newPublicKeyList = new Vector<PublicKey>();
		newPublickeyListSize = 0;
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
			newTransactionListSize = array.length;
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
			newPassportListSize = array.length;
			ByteArrayInputStream iStream = new ByteArrayInputStream(array.elements);
			for(int i=0; i<newPassportListSize; ++i) {
				newPassportList.add(new Passport(iStream));
			}
			EQCType.assertNoRedundantData(iStream);
		}
		
		// Parse PublicKeys
		array = EQCType.parseARRAY(is);
		if (!array.isNULL()) {
			newPublickeyListSize = array.length;
			ByteArrayInputStream iStream = new ByteArrayInputStream(array.elements);
			for(int i=0; i<newPublickeyListSize; ++i) {
				newPublicKeyList.add(new PublicKey(iStream));
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
//		newPublickeyListSize = publickeys.length;
//		for(byte[] publickey : publickeys.elements) {
//			newPublicKeyList.add(new PublicKey(publickey));
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
			if(!transaction.isCoinBase() && transaction.getPublickey().isNew()) {
				newPublicKeyList.add(transaction.getPublickey());
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
	public boolean isPublicKeyExists(byte[] publicKey) {
		boolean isExists = false;
		for(PublicKey publicKey2 : newPublicKeyList) {
			if(Arrays.equals(publicKey, publicKey2.getPublicKey())) {
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
		if (newPublicKeyList.size() == 0) {
			return EQCType.bytesToBIN(null);
		} 
		else {
			Vector<byte[]> publickys = new Vector<byte[]>();
			for (PublicKey publicKey : newPublicKeyList) {
				publickys.add(publicKey.getBytes());
			}
			return EQCType.bytesArrayToARRAY(publickys);
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
	
	private String _getNewPublicKeyList() {
		String tx = null;
		if (newPublicKeyList != null && newPublicKeyList.size() > 0) {
			tx = "\n[\n";
			if (newPublicKeyList.size() > 1) {
				for (int i = 0; i < newPublicKeyList.size() - 1; ++i) {
					tx += newPublicKeyList.get(i) + ",\n";
				}
			}
			tx += newPublicKeyList.get(newPublicKeyList.size() - 1);
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
						"\"NewPublicKeyList\":" + 
						"\n{\n" +
						"\"Size\":\"" + newPublickeyListSize + "\",\n" +
						"\"List\":" + 
							_getNewPublicKeyList() + "\n},\n" +
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
	 * @return the newPublicKeyList
	 */
	public Vector<PublicKey> getNewPublicKeyList() {
		return newPublicKeyList;
	}

	/**
	 * @param newPublicKeyList the newPublicKeyList to set
	 */
	public void setNewPublicKeyList(Vector<PublicKey> newPublicKeyList) {
		this.newPublicKeyList = newPublicKeyList;
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
		if(newPublicKeyList.contains(transaction.getPublickey())) {
			newPublicKeyList.removeElement(transaction.getPublickey());
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
	
	public byte[] getNewPublickeyListMerkelTreeRoot() {
		if(newPublicKeyList.size() == 0) {
			return null;
		}
		Vector<byte[]> publickeys = new Vector<byte[]>();
		for (PublicKey publicKey : newPublicKeyList) {
			publickeys.add(publicKey.getBytes());
		}
		return Util.getMerkleTreeRoot(publickeys);
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
		for(PublicKey publicKey : newPublicKeyList) {
			if(!publicKey.isSanity()) {
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
		AssetSubchainAccount eqcoin = (AssetSubchainAccount) accountsMerkleTree.getAccount(ID.ONE);
		if(newPassportList.size() == 0) {
			return true;
		}
		else if (!newPassportList.get(0).getID().subtract(eqcoin.getAssetSubchainHeader().getTotalAccountNumbers())
				.equals(ID.ONE)) {
			return false;
		}else {
			// Get the new Account's ID list from Transactions
			Vector<ID> newAccounts = new Vector<>();
			for(Transaction transaction : newTransactionList) {
				for(TxOut txOut : transaction.getTxOutList()) {
					if(txOut.getPassport().getID().compareTo(eqcoin.getAssetSubchainHeader().getTotalAccountNumbers()) > 0) {
						if(!newAccounts.contains(txOut.getPassport().getID())) {
							newAccounts.add(txOut.getPassport().getID());
						}
					}
				}
			}
			if(newPassportList.size() != newAccounts.size()) {
				return false;
			}
			for(int i=0; i<newPassportList.size(); ++i) {
				if(!newPassportList.get(i).getID().equals(newAccounts.get(i))) {
					return false;
				}
			}
			for(int i=0; i<newPassportList.size(); ++i) {
				// Check if Address already exists
				if(accountsMerkleTree.isAccountExists(newPassportList.get(i), true)) {
					return false;
				}
				else {
					// Check if ID is valid
					if ((i + 1) < newPassportList.size()) {
						if (!newPassportList.get(i).getID().getNextID().equals(newPassportList.get(i + 1))) {
							return false;
						}
					}
					// Save new Account in Filter
					Account account = new AssetAccount();
					account.setPassport(newPassportList.get(i));
					account.setLockCreateHeight(accountsMerkleTree.getHeight().getNextID());
					Asset asset = new CoinAsset();
					asset.setAssetID(Asset.EQCOIN);
					asset.setNonce(ID.ZERO);
					account.setAsset(asset);
					accountsMerkleTree.saveAccount(account);
					Log.info("Numbers: " + accountsMerkleTree.getTotalAccountNumbers());
					accountsMerkleTree.increaseTotalAccountNumbers();
					Log.info("Numbers: " + accountsMerkleTree.getTotalAccountNumbers());
				}
			}
		}
		return true;
	}
	
	public boolean isPublickeyIDExistsInTransactions(ID id) {
		for(Transaction transaction : newTransactionList) {
			if(transaction.getTxIn().getPassport().getID().equals(id)) {
				return true;
			}
		}
		return false;
	}
	
	public boolean isNewPublickeyListValid(AccountsMerkleTree accountsMerkleTree) throws NoSuchFieldException, IllegalStateException, RocksDBException, IOException, ClassNotFoundException, SQLException {
		WriteBatch writeBatch = null;
		if(newPublicKeyList.size() > 0) {
			writeBatch = new WriteBatch();
		}
		// Get the new Publickey's ID list from Transactions
		Vector<ID> newPublickeys = new Vector<>();
		for(int i=1; i<newTransactionList.size(); ++i) {
			Account account = accountsMerkleTree.getAccount(newTransactionList.get(i).getTxIn().getPassport());
				if(!account.isPublickeyExists()) {
					if(!newPublickeys.contains(newTransactionList.get(i).getTxIn().getPassport().getID())) {
						newPublickeys.add(newTransactionList.get(i).getTxIn().getPassport().getID());
					}
				}
		}
		if(newPublicKeyList.size() != newPublickeys.size()) {
			return false;
		}
		for(int i=0; i<newPublicKeyList.size(); ++i) {
			if(!newPublicKeyList.get(i).getID().equals(newPublickeys.get(i))) {
				return false;
			}
		}
		for(PublicKey publicKey : newPublicKeyList) {
			if(!isPublickeyIDExistsInTransactions(publicKey.getID())) {
				return false;
			}
			if(accountsMerkleTree.isPublicKeyExists(publicKey)) {
				return false;
			}
			else {
				Account account = accountsMerkleTree.getAccount(publicKey.getID());
				if(!AddressTool.verifyAddressPublickey(account.getPassport().getReadableAddress(), publicKey.getPublicKey())) {
					return false;
				}
				else {
					Publickey publickey1 = new Publickey();
					publickey1.setPublickey(publicKey.getPublicKey());
					publickey1.setPublickeyCreateHeight(accountsMerkleTree.getHeight().getNextID());
					account.setPublickey(publickey1);
					writeBatch.put(accountsMerkleTree.getFilter().getColumnFamilyHandles().get(0), account.getIDEQCBits(),
							account.getBytes());
					writeBatch.put(accountsMerkleTree.getFilter().getColumnFamilyHandles().get(1),
							account.getPassport().getAddressAI(), account.getIDEQCBits());
				}
			}
		}
		if(writeBatch != null) {
			accountsMerkleTree.getFilter().batchUpdate(writeBatch);
		}
		return true;
	}
	
	public boolean isAllTxInPublickeyExists(AccountsMerkleTree accountsMerkleTree) throws NoSuchFieldException, IllegalStateException, RocksDBException, IOException, ClassNotFoundException, SQLException {
		for(int i=1; i<newTransactionList.size(); ++i) {
			if(!accountsMerkleTree.getAccount(newTransactionList.get(i).getTxIn().getPassport().getID()).isPublickeyExists()) {
				return false;
			}
		}
		return true;
	}
	
	public CoinbaseTransaction getCoinbaseTransaction() {
		return (CoinbaseTransaction) newTransactionList.get(0);
	}
	
	public ID getNewPassportListInitId(AccountsMerkleTree accountsMerkleTree) {
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
