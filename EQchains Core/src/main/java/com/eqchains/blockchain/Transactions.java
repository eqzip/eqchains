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
import java.util.Arrays;
import java.util.Vector;

import com.eqchains.blockchain.account.Account;
import com.eqchains.blockchain.account.AssetAccount;
import com.eqchains.blockchain.account.Account.Asset;
import com.eqchains.blockchain.transaction.Address;
import com.eqchains.blockchain.transaction.CoinbaseTransaction;
import com.eqchains.blockchain.transaction.OperationTransaction;
import com.eqchains.blockchain.transaction.Transaction;
import com.eqchains.blockchain.transaction.TxOut;
import com.eqchains.blockchain.transaction.Address.AddressShape;
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
public class Transactions implements EQCTypable {
	private Vector<Transaction> newTransactionList;
	private long newTransactionListSize;
	private Vector<Address> newAccountList;
	private long newAccountListSize;
	private Vector<PublicKey> newPublicKeyList;
	private long newPublickeyListSize;
	/*
	 * VERIFICATION_COUNT equal to the number of member variables of the class to be verified.
	 */
	private static byte VERIFICATION_COUNT = 3;
	
	public Transactions() {
		super();
		init();
	}
	
	public void init() {
		newTransactionList = new Vector<Transaction>();
		newTransactionListSize = 0;
		newAccountList = new Vector<Address>();
		newAccountListSize = 0;
		newPublicKeyList = new Vector<PublicKey>();
		newPublickeyListSize = 0;
	}
	
	public Transactions(ByteBuffer byteBuffer) throws NoSuchFieldException, IOException {
		parseTransactions(byteBuffer.array());
	}
	
	public Transactions(byte[] bytes) throws NoSuchFieldException, IOException {
		parseTransactions(bytes);
	}
	
	private void parseTransactions(byte[] bytes) throws NoSuchFieldException, IOException {
		ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		byte[] data = null;
		init();
		
		// Parse TransactionList
		ARRAY array = null;
		if ((array = EQCType.parseARRAY(is)) != null) {
			newTransactionListSize = array.length;
			for (byte[] transaction : array.elements) {
				newTransactionList.add(Transaction.parseTransaction(transaction, Address.AddressShape.ID));
			}
		}

		// Parse Accounts
		array = null;
		if ((array = EQCType.parseARRAY(is)) != null) {
			newAccountListSize = (int) array.length;
			for(byte[] address : array.elements) {
				newAccountList.add(new Address(address));
			}
		}
		
		// Parse PublicKeys
		array = null;
		if ((array = EQCType.parseARRAY(is)) != null) {
			newPublickeyListSize = array.length;
			for(byte[] publickey : array.elements) {
				newPublicKeyList.add(new PublicKey(publickey));
			}
		}
	}
	
	public static boolean isValid(ByteBuffer byteBuffer) throws NoSuchFieldException, IOException {
		return isValid(byteBuffer.array());
	}
	
	public static boolean isValid(byte[] bytes) throws NoSuchFieldException, IOException {
		ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		byte[] data = null;
		byte validCount = 0;
		
		// Parse TransactionList
		ARRAY array = EQCType.parseARRAY(is);
		if ((array != null) && isTransactionListValid(array)) {
				++validCount;
		}
		
		// Parse Accounts
		array = EQCType.parseARRAY(is);
		if ((array == null) || ((array != null) && isAccountsValid(array))) {
				++validCount;
		}
		
		// Parse PublicKeys
		array = EQCType.parseARRAY(is);
		if ((array == null) || ((array != null) && isPublicKeysValid(array))) {
				++validCount;
		}
		
		return (validCount >= VERIFICATION_COUNT) && EQCType.isInputStreamEnd(is);
	}
	
	private void parseTransactionList(byte[] bytes) throws NoSuchFieldException, IOException {
		ARRAY array = EQCType.parseARRAY(bytes);
		newTransactionListSize = array.length;
		for (byte[] transaction : array.elements) {
			newTransactionList.add(Transaction.parseTransaction(transaction, Address.AddressShape.ID));
		}
	}
	
	private static boolean isTransactionListValid(ARRAY transactions) throws NoSuchFieldException, IOException {
		boolean boolIsTransactionListValid = false;
		if(transactions.length == transactions.elements.size()) {
			boolIsTransactionListValid = true;
		}
		for(byte[] transaction : transactions.elements) {
			if(!Transaction.isValid(transaction, Address.AddressShape.ID)) {
				boolIsTransactionListValid = false;
				break;
			}
		}
		return boolIsTransactionListValid;
	}
	
	private void parsePublicKeys(byte[] bytes) throws NoSuchFieldException, IOException{
		ARRAY publickeys = EQCType.parseARRAY(bytes);
		newPublickeyListSize = publickeys.length;
		for(byte[] publickey : publickeys.elements) {
			newPublicKeyList.add(new PublicKey(publickey));
		}
	}
	
	private static boolean isPublicKeysValid(ARRAY publickeys) throws NoSuchFieldException, IOException {
		boolean boolIsPublicKeysValid = false;
		if(publickeys.length == publickeys.elements.size()) {
			boolIsPublicKeysValid = true;
		}
		for(byte[] publickey : publickeys.elements) {
			if(!PublicKey.isValid(publickey)) {
				boolIsPublicKeysValid = false;
				break;
			}
		}
		return boolIsPublicKeysValid;
	}
	
	private void parseAccounts(byte[] bytes) throws NoSuchFieldException, IOException{
		ARRAY array = EQCType.parseARRAY(bytes);
		newAccountListSize = (int) array.length;
		for(byte[] address : array.elements) {
			newAccountList.add(new Address(address));
		}
	}
	
	private static boolean isAccountsValid(ARRAY addresses) throws NoSuchFieldException, IOException {
		boolean boolIsAccountsValid = false;
		if(addresses.length == addresses.elements.size()) {
			boolIsAccountsValid = true;
		}
		for(byte[] address : addresses.elements) {
			if(!Address.isValid(address)) {
				boolIsAccountsValid = false;
				break;
			}
		}
		return boolIsAccountsValid;
	}
	
	public void addTransaction(Transaction transaction) {
		if(!isTransactionExists(transaction)) {
			// Add Publickey
			if(transaction.getPublickey().isNew()) {
				newPublicKeyList.add(transaction.getPublickey());
			}
			// Add new Address
			for(TxOut txOut : transaction.getTxOutList()) {
				if(txOut.isNew()) {
					newAccountList.add(txOut.getAddress());
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
	public boolean isAddressExists(Address address) {
		return newAccountList.contains(address);
	}
	
	@Deprecated
	public boolean isAddressExists(String address) {
		boolean isExists = false;
		for(Address address2 : newAccountList) {
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
		if (newAccountList.size() == 0) {
			return EQCType.bytesToBIN(null);
		}
		else {
			Vector<byte[]> addresses = new Vector<byte[]>();
			for (Address address : newAccountList) {
				addresses.add(address.getBytes(AddressShape.AI));
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
				transactions.add(transaction.getBytes());
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
	
	private String _getNewAccountList() {
		String tx = null;
		if (newAccountList != null && newAccountList.size() > 0) {
			tx = "\n[\n";
			if (newAccountList.size() > 1) {
				for (int i = 0; i < newAccountList.size() - 1; ++i) {
					tx += newAccountList.get(i) + ",\n";
				}
			}
			tx += newAccountList.get(newAccountList.size() - 1);
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
					"\"NewAccountList\":" + 
					"\n{\n" +
					"\"Size\":\"" + newAccountListSize + "\",\n" +
					"\"List\":" + 
						_getNewAccountList() + "\n},\n" +
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
	 * @return the newAccountList
	 */
	public Vector<Address> getNewAccountList() {
		return newAccountList;
	}

	/**
	 * @param newAccountList the newAccountList to set
	 */
	public void setNewAccountList(Vector<Address> newAccountList) {
		this.newAccountList = newAccountList;
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
			if(newAccountList.contains(txOut.getAddress())) {
				newAccountList.removeElement(txOut.getAddress());
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
			transactions.add(transaction.getBytes());
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
	
	public byte[] getNewAccountListMerkelTreeRoot() {
		if(newAccountList.size() == 0) {
			return null;
		}
		Vector<byte[]> addresses = new Vector<byte[]>();
		for (Address address : newAccountList) {
			addresses.add(address.getBytes());
		}
		return Util.getMerkleTreeRoot(addresses);
	}
	
	public byte[] getHash() {
		return  Util.EQCCHA_MULTIPLE_DUAL(getBytes(), Util.ONE, true, false);
	}

	@Override
	public boolean isSanity(AddressShape... addressShape) {
		if(addressShape.length != 0) {
			return false;
		}
		for(Address address : newAccountList) {
			if(!address.isSanity()) {
				return false;
			}
		}
		for(PublicKey publicKey : newPublicKeyList) {
			if(!publicKey.isSanity()) {
				return false;
			}
		}
		for(Transaction transaction : newTransactionList) {
			if(!transaction.isSanity(addressShape)) {
				return false;
			}
		}
		return false;
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
	
	public long getTxFee() {
		long txFee = 0;
		for(int i=1; i<newTransactionList.size(); ++i) {
			txFee += newTransactionList.get(i).getTxFee();
		}
		return txFee;
	}
	
	public boolean isAccountListValid(AccountsMerkleTree accountsMerkleTree) {
		if (!newAccountList.get(0).getID().subtract(accountsMerkleTree
						.getEQCBlock(accountsMerkleTree.getHeight(), true).getRoot().getTotalAccountNumbers())
				.equals(ID.ONE)) {
			return false;
		}else {
			for(int i=0; i<newAccountList.size(); ++i) {
				// Check if Address already exists
				if(accountsMerkleTree.isAccountExists(newAccountList.get(i), true)) {
					return false;
				}
				else {
					// Save new Account in Filter
					Account account = new AssetAccount();
					account.getKey().setAddress(newAccountList.get(i));
					account.getKey().setAddressCreateHeight(accountsMerkleTree.getHeight().getNextID());
					Asset asset = new Asset();
					asset.setAssetID(Asset.EQCOIN);
					asset.setBalanceUpdateHeight(account.getKey().getAddressCreateHeight());
					asset.setNonce(ID.ZERO);
					account.setAsset(asset);
					accountsMerkleTree.saveAccount(account);
					Log.info("Numbers: " + accountsMerkleTree.getTotalAccountNumbers());
					accountsMerkleTree.increaseTotalAccountNumbers();
					Log.info("Numbers: " + accountsMerkleTree.getTotalAccountNumbers());
				}
				// Check if ID is valid
				if ((newAccountList.size() > 1) && ((i + 1) < newAccountList.size())) {
					if (!newAccountList.get(i).getID().getNextID().equals(newAccountList.get(i + 1))) {
						return false;
					}
				}
			}
		}
		return true;
	}
	
	public boolean isPublickeyIDExistsInTransactions(ID id) {
		for(Transaction transaction : newTransactionList) {
			if(transaction.getTxIn().getAddress().getID().equals(id)) {
				return true;
			}
		}
		return false;
	}
	
	public boolean isPublickeyListValid(AccountsMerkleTree accountsMerkleTree) {
		for(PublicKey publicKey : newPublicKeyList) {
			if(!isPublickeyIDExistsInTransactions(publicKey.getID())) {
				return false;
			}
			if(accountsMerkleTree.isPublicKeyExists(publicKey)) {
				return false;
			}
			else {
				Address address = accountsMerkleTree.getAccount(publicKey.getID()).getKey().getAddress();
				if(!AddressTool.verifyAddressPublickey(address.getReadableAddress(), publicKey.getPublicKey())) {
					return false;
				}
				else {
					accountsMerkleTree.savePublicKey(publicKey, accountsMerkleTree.getHeight().getNextID());
				}
			}
		}
		return true;
	}
	
	public boolean isAllTxInPublickeyExists(AccountsMerkleTree accountsMerkleTree) {
		for(int i=1; i<newTransactionList.size(); ++i) {
			if(!accountsMerkleTree.getAccount(newTransactionList.get(i).getTxIn().getAddress().getID()).isPublickeyExists()) {
				return false;
			}
		}
		return true;
	}
	
	public CoinbaseTransaction getCoinbaseTransaction() {
		return (CoinbaseTransaction) newTransactionList.get(0);
	}
	
	public ID getAccountListInitId(AccountsMerkleTree accountsMerkleTree) {
		ID initID = null;
		if (newAccountList.size() == 0) {
			initID = new ID(accountsMerkleTree.getTotalAccountNumbers()
					.add(BigInteger.valueOf(Util.INIT_ADDRESS_SERIAL_NUMBER)));
		} else {
			initID = newAccountList.lastElement().getID().getNextID();
		}
		return initID;
	}
	
}
