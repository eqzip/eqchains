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

import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Vector;

import org.rocksdb.ColumnFamilyHandle;
import org.rocksdb.CompressionType;
import org.rocksdb.MutableColumnFamilyOptions;
import org.rocksdb.RocksDBException;
import org.rocksdb.RocksIterator;
import org.rocksdb.WriteBatch;
import org.rocksdb.WriteOptions;

import com.eqchains.blockchain.Account.Asset;
import com.eqchains.blockchain.Account.Publickey;
import com.eqchains.blockchain.transaction.Address;
import com.eqchains.blockchain.transaction.Transaction;
import com.eqchains.blockchain.transaction.Address.AddressShape;
import com.eqchains.configuration.Configuration;
import com.eqchains.crypto.MerkleTree;
import com.eqchains.persistence.h2.EQCBlockChainH2;
import com.eqchains.persistence.rocksdb.EQCBlockChainRocksDB;
import com.eqchains.persistence.rocksdb.EQCBlockChainRocksDB.TABLE;
import com.eqchains.serialization.EQCTypable;
import com.eqchains.util.ID;
import com.eqchains.util.Log;
import com.eqchains.util.Util;

/**
 * The height of AccountsMerkleTree is previous EQCBlock's height when 
 * build new block or verify new block.
 * If create from the early height need fill all the relevant Account's 
 * Snapshot(In current height) from H2 in Filter.
 * 
 * @author Xun Wang
 * @date Mar 11, 2019
 * @email 10509759@qq.com
 */
public class AccountsMerkleTree {
	public final static int MAX_ACCOUNTS_PER_TREE = 1024;
	private Vector<byte[]> accountsMerkleTreeRootList;
	private static ID height;
	private byte[] accountsMerkleTreeRoot;
	private static ID totalAccountNumbers;
	private Filter filter;
	// stub
	private Vector<MerkleTree> accountsMerkleTree;

	public AccountsMerkleTree(ID height, Filter filter) {
		super();
		
//		if(height.subtract(val))
		
		this.height = height;
		// When generate SingularityBlock the zero height EQCBlock doesn't exist
		// so here need special operation
		if(height.equals(ID.ZERO)) {
			if(Configuration.getInstance().isInitSingularityBlock()) {
				EQCBlock eqcBlock = Util.DB().getEQCBlock(height, true);
				totalAccountNumbers = eqcBlock.getRoot().getTotalAccountNumbers();
			}
			else {
				totalAccountNumbers = ID.ZERO;
			}
		}
		else {
			totalAccountNumbers = Util.DB().getTotalAccountNumbers(height);
		}
		this.filter = filter;
		if(height.compareTo(EQCBlockChainRocksDB.getInstance().getEQCBlockTailHeight()) < 0) {
			for(long i=1; i<totalAccountNumbers.longValue(); ++i) {
				Account account = EQCBlockChainH2.getInstance().getAccountSnapshot(new ID(i), height);
				if(account != null) {
					Log.info(account.toString());
					filter.saveAccount(account);
				}
			}
		}
		accountsMerkleTreeRootList = new Vector<>();
	}
	
	/**
	 * When check TxIn Account doesn't need searching in filter just set isFiltering to false
	 * Check if TxIn Account exists in EQC blockchain's Accounts table
	 * and it's create height less than current AccountsMerkleTree's
	 * height.
	 * 
	 * When check TxOut Account need searching in filter just set isFiltering to true
	 * Check if TxOut Address exists in Filter table or EQC blockchain's Accounts table
	 * and it's create height less than current AccountsMerkleTree's
	 * height.
	 * 
	 * @param address
	 * @param isFiltering When need searching in Filter table just set it to true
	 * @return true if Address exists
	 */
	public synchronized boolean isAccountExists(Address address, boolean isFiltering) {
		boolean isExists = false;
		if(isFiltering && filter.isAddressExists(address)) {
			isExists = true;
		}
		else if(Util.DB().isAddressExists(address)) {
			Account account = Util.DB().getAccount(address.getAddressAI());
			if(account != null && account.getKey().getAddressCreateHeight().compareTo(height) <= 0) {
				isExists = true;
			}
		}
		return  isExists;
	}
	
	public synchronized Account getAccount(ID id) {
		return filter.getAccount(id, true);
	}
	
	public synchronized Account getAccount(Address address) {
		return filter.getAccount(address, true);
	}
	
	/**
	 * Save current Account in Filter
	 * @param account
	 * @return true if save successful
	 */
	public synchronized boolean saveAccount(Account account) {
		return filter.saveAccount(account);
	}
	
	public synchronized void increaseTotalAccountNumbers() {
		totalAccountNumbers = totalAccountNumbers.add(BigInteger.ONE);
	}
	
	/**
	 * @return the totalAccountNumber
	 */
	public synchronized ID getTotalAccountNumbers() {
		return totalAccountNumbers;
	}
	
	/**
	 * @return the height
	 */
	public synchronized ID getHeight() {
		return height;
	}

	/**
	 * @param height the height to set
	 */
	public synchronized void setHeight(ID height) {
		this.height = height;
	}

	public void buildAccountsMerkleTree() {
		Account account = null;
		Vector<byte[]> accountsHash = new Vector<>();
		long remainder = totalAccountNumbers.longValue();
		int begin = 0, end = 0;

		for (int i = 0; i <= totalAccountNumbers.longValue() / MAX_ACCOUNTS_PER_TREE; ++i) {
			begin = BigInteger.valueOf(i * MAX_ACCOUNTS_PER_TREE)
					.add(BigInteger.valueOf(Util.INIT_ADDRESS_SERIAL_NUMBER)).intValue();
			if ((remainder - MAX_ACCOUNTS_PER_TREE) > 0) {
				end = MAX_ACCOUNTS_PER_TREE + Util.INIT_ADDRESS_SERIAL_NUMBER;
				remainder -= MAX_ACCOUNTS_PER_TREE;
			} else {
				end = (int) remainder + Util.INIT_ADDRESS_SERIAL_NUMBER;
			}
			for (int j = begin; j < end; ++j) {
				account = filter.getAccount(new ID(j), false);
				// If account == null which means error occur
				if (account == null) {
					throw new IllegalStateException("Account shouldn't be null");
				}
				Log.info(account.toString());
				Log.info(Util.dumpBytes(account.getHash(), 16));
				accountsHash.add(account.getHash());
			}
			MerkleTree merkleTree = new MerkleTree(accountsHash);
			merkleTree.generateRoot();
			accountsMerkleTreeRootList.add(merkleTree.getRoot());
			merkleTree = null;
			accountsHash.clear();
		}
		System.gc();
	}
	
	public void generateRoot() {
		MerkleTree merkleTree = new MerkleTree(accountsMerkleTreeRootList);
		merkleTree.generateRoot();
		accountsMerkleTreeRoot = Util.EQCCHA_MULTIPLE_FIBONACCI_MERKEL(merkleTree.getRoot(), Util.THOUSANDPLUS, false);
	}
	
	public byte[] getRoot() {
		return accountsMerkleTreeRoot;
	}
	
	public Vector<byte[]> getAccountsMerkleTreeRootList(){
		Vector<byte[]> bytes = new Vector<byte[]>();
		for(MerkleTree merkleTree : accountsMerkleTree) {
			bytes.add(merkleTree.getRoot());
		}
		return bytes;
	}

	public boolean merge() {
		boolean isSucc = true;
		if(!filter.merge()) {
			isSucc = false;
		}
		if(!filter.close()) {
			isSucc = false;
		}
		return isSucc;
	}
	
	public boolean close() {
		return filter.close();
	}
	
	public static class Filter {
		private byte[] columnFamilyName = null;
//		private ColumnFamilyHandle columnFamilyHandle;
//		private HashMap<byte[], ColumnFamilyHandle> columnFamilyHandles;
		private Vector<ColumnFamilyHandle> columnFamilyHandles;
		
		public Filter(byte[] columnFamilyName) {
			if(EQCBlockChainRocksDB.getInstance().isDefaultColumnFamily(columnFamilyName)) {
				new IllegalArgumentException("Filter's columnFamilyName can't the same as defaultColumnFamilyName");
			}
			this.columnFamilyName = columnFamilyName;
			columnFamilyHandles  = new Vector();
			columnFamilyHandles.add(EQCBlockChainRocksDB.createTable(columnFamilyName));
			columnFamilyHandles.add(EQCBlockChainRocksDB.createTable(EQCBlockChainRocksDB.addSuffix(columnFamilyName, EQCBlockChainRocksDB.SUFFIX_AI)));
//			columnFamilyHandles.add(EQCBlockChainRocksDB.createTable(EQCBlockChainRocksDB.addSuffix(columnFamilyName, EQCBlockChainRocksDB.SUFFIX_HASH)));
			
			MutableColumnFamilyOptions mutableColumnFamilyOptions = MutableColumnFamilyOptions.builder().setCompressionType(CompressionType.NO_COMPRESSION).build();
			try {
				for(ColumnFamilyHandle columnFamilyHandle : columnFamilyHandles) {
					EQCBlockChainRocksDB.getRocksDB().setOptions(columnFamilyHandle, mutableColumnFamilyOptions);
					// In case before close the app crashed so here just clear the table
//					Log.info("Before cleartable: " + EQCBlockChainRocksDB.getTableItemNumbers(columnFamilyHandle));
					EQCBlockChainRocksDB.clearTable(columnFamilyHandle);
//					Log.info("After cleartable: " + EQCBlockChainRocksDB.getTableItemNumbers(columnFamilyHandle));
				}
			} catch (RocksDBException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.Error(e.getMessage());
			}
		}
		
		public Account getAccount(ID id, boolean isLoadInFilter) {
			return getAccount(id.getEQCBits(), isLoadInFilter);
		}
		
		public Account getAccount(Address address, boolean isLoadInFilter) {
			ID id = null;
			id = getAddressID(address);
			if(id == null) {
				return null;
			}
			return getAccount(id, isLoadInFilter);
		}
		
		public boolean isAddressExists(Address address) {
			boolean isSucc = false;
			try {
				// For security issue only support search address via AddressAI
				if((EQCBlockChainRocksDB.getRocksDB().get(columnFamilyHandles.get(1), address.getAddressAI()) != null)) {
					isSucc = true;
				}
			} catch (RocksDBException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.Error("During isAddressExists " + address + " error occur: " + e.getMessage());
			}
			return isSucc;
		}
		
		public boolean isAddressExists(Address address, ID height) {
			boolean isSucc = false;
			byte[] bytes = null;
			try {
				// For security issue only support search address via AddressAI
				if((bytes = EQCBlockChainRocksDB.getRocksDB().get(columnFamilyHandles.get(1), address.getAddressAI())) != null) {
					// Here maybe still exists some issue
					if(new AssetAccount(bytes).getKey().getAddressCreateHeight().compareTo(height) <= 0)
					isSucc = true;
				}
			} catch (RocksDBException | NoSuchFieldException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.Error("During isAddressExists " + address + " error occur: " + e.getMessage());
			}
			return isSucc;
		}
		
		public boolean saveAccount(Account account) {
			boolean isSucc = true;
			try {
				WriteBatch writeBatch = new WriteBatch();
				writeBatch.put(columnFamilyHandles.get(0), account.getIDEQCBits(), account.getBytes());
				writeBatch.put(columnFamilyHandles.get(1), account.getKey().getAddress().getAddressAI(), account.getIDEQCBits());
//				writeBatch.put(columnFamilyHandles.get(2), account.getIDEQCBits(), account.getHash());
				EQCBlockChainRocksDB.getRocksDB().write(EQCBlockChainRocksDB.getWriteOptions(), writeBatch);
			} catch (RocksDBException e) {
				isSucc = false;
				e.printStackTrace();
				Log.Error(e.getMessage());
			}
			return isSucc;
		}

		public Account getAccount(byte[] id, boolean isLoadInFilter) {
			Account account = null;
			byte[] bytes = null;
			try {
				bytes = EQCBlockChainRocksDB.getRocksDB().get(columnFamilyHandles.get(0), id);
				if (bytes != null) {
					account = new AssetAccount(bytes);
				} else {
					if (height.compareTo(EQCBlockChainRocksDB.getInstance().getEQCBlockTailHeight()) < 0) {
						account = EQCBlockChainH2.getInstance().getAccountSnapshot(new ID(id), height);
					}
					if (account == null) {
						bytes = EQCBlockChainRocksDB.get(TABLE.ACCOUNT, id);
						if (bytes != null) {
							account = new AssetAccount(bytes);
						}
					} 
					if (isLoadInFilter && account != null) {
						saveAccount(account);
					}
				}
			} catch (RocksDBException | NoSuchFieldException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.Error("During getAccount " + id + " error occur: " + e.getMessage());
			}
			return account;
		}
		
		@Deprecated
		public byte[] getAccountHash(ID id) {
			byte[] bytes = null;
			try {
				bytes = EQCBlockChainRocksDB.getRocksDB().get(columnFamilyHandles.get(2),
						id.getEQCBits());
				if(bytes == null) {
					bytes = EQCBlockChainRocksDB.get(TABLE.ACCOUNT_HASH, id.getEQCBits());
				}
			} catch (RocksDBException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.Error("During getAccountHash " + id + " error occur: " + e.getMessage());
			}
			return bytes;
		}
		
		public boolean merge() {
			boolean isSucc = true;
			Account account = null;
			RocksIterator rocksIterator = EQCBlockChainRocksDB.getRocksDB().newIterator(columnFamilyHandles.get(0));
			rocksIterator.seekToFirst();
			while(rocksIterator.isValid()) {
				try {
//					WriteBatch writeBatch = new WriteBatch();
//					Log.info("Key: " + Util.dumpBytes(rocksIterator.key(), 16) + " Value: " + Util.dumpBytes(rocksIterator.value(), 16));
//					writeBatch.put(EQCBlockChainRocksDB.getTableHandle(TABLE.ACCOUNT), rocksIterator.key(), rocksIterator.value());
//					rocksIterator.next();
//					Log.info("Key: " + Util.dumpBytes(rocksIterator.key(), 16) + " Value: " + Util.dumpBytes(rocksIterator.value(), 16));
//					writeBatch.put(EQCBlockChainRocksDB.getTableHandle(TABLE.ACCOUNT), rocksIterator.key(), rocksIterator.value());
//					rocksIterator.next();
//					Log.info("Key: " + Util.dumpBytes(rocksIterator.key(), 16) + " Value: " + Util.dumpBytes(rocksIterator.value(), 16));
//					writeBatch.put(EQCBlockChainRocksDB.getTableHandle(TABLE.ACCOUNT), rocksIterator.key(), rocksIterator.value());
//					EQCBlockChainRocksDB.getRocksDB().write(EQCBlockChainRocksDB.getWriteOptions(), writeBatch);
					account = new AssetAccount(rocksIterator.value());
					// At here calculate Account's Hash is faster than retrieve it from HD
					EQCBlockChainRocksDB.getInstance().saveAccount(account);
					rocksIterator.next();
				} catch (NoSuchFieldException | IOException e) {
					isSucc = false;
					e.printStackTrace();
					Log.Error("During merge " + columnFamilyHandles + " error occur: " + e.getMessage());
				}
			}
			return isSucc;
		}
		
		public boolean takeSnapshot() {
			boolean isSucc = true;
			RocksIterator rocksIterator = EQCBlockChainRocksDB.getRocksDB().newIterator(columnFamilyHandles.get(0));
			rocksIterator.seekToFirst();
			while (rocksIterator.isValid()) {
				try {
					Log.info("Key: " + Util.dumpBytes(rocksIterator.key(), 16) + " Value: " + Util.dumpBytes(rocksIterator.value(), 16));
					Account account = new AssetAccount(rocksIterator.value());
					if (account.getID()
							.compareTo(Util.DB().getEQCBlock(height, true).getRoot().getTotalAccountNumbers()) <= 0) {
						if (!EQCBlockChainH2.getInstance().saveAccountSnapshot(EQCBlockChainRocksDB.getInstance().getAccount(account.getID()), height)) {
							isSucc = false;
							Log.Error("During takeSnapshot error occur saveAccountSnapshot failed.");
							break;
						}
					}
//					rocksIterator.next();
//					Log.info("Key: " + Util.dumpBytes(rocksIterator.key(), 16) + " Value: " + Util.dumpBytes(rocksIterator.value(), 16));
//					rocksIterator.next();
//					Log.info("Key: " + Util.dumpBytes(rocksIterator.key(), 16) + " Value: " + Util.dumpBytes(rocksIterator.value(), 16));
					rocksIterator.next();
				} catch (NoSuchFieldException | IOException e) {
					isSucc = false;
					e.printStackTrace();
					Log.Error("During merge " + columnFamilyHandles + " error occur: " + e.getMessage());
				}
			}
			return isSucc;
		}
		
		public ID getAddressID(Address address) {
			ID id = null;
			byte[] bytes = null;
			try {
				bytes = EQCBlockChainRocksDB.getRocksDB().get(columnFamilyHandles.get(1), address.getAddressAI());
				if(bytes != null) {
					id = new ID(bytes);
				}
				else {
					bytes = EQCBlockChainRocksDB.get(TABLE.ACCOUNT_AI, address.getAddressAI());
					if(bytes != null) {
							id = new ID(bytes);
							byte[] data = EQCBlockChainRocksDB.get(TABLE.ACCOUNT, bytes);
							Account account = new AssetAccount(data);
							saveAccount(account);
					}
				}
			} catch (RocksDBException | NoSuchFieldException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.Error("During getAddressSerialNumber " + address + " error occur: " + e.getMessage());
			}
			return id;
		}

		public Address getAddress(ID id) {
			return getAccount(id, true).getKey().getAddress();
		}

		public PublicKey getPublicKey(ID id) {
			PublicKey publicKey = null;
			if(getAccount(id, true).getKey().getPublickey() != null) {
				publicKey = new PublicKey();
				publicKey.setPublicKey(getAccount(id, true).getKey().getPublickey().getPublickey());
				publicKey.setID(id);
			}
			return publicKey;
		}

		public boolean savePublicKey(PublicKey publicKey, ID height) {
			Account account = getAccount(publicKey.getID(), true);
			Publickey publickey2 = new Publickey();
			publickey2.setPublickey(publicKey.getPublicKey());
			publickey2.setPublickeyCreateHeight(height);
			account.getKey().setPublickey(publickey2);
			return saveAccount(account);
		}

		public boolean isPublicKeyExists(PublicKey publicKey) {
			boolean isSucc = false;
			Account account = getAccount(publicKey.getID(), true);
			if(account.getKey().getPublickey() != null) {
				if(Arrays.equals(account.getKey().getPublickey().getPublickey(), publicKey.getPublicKey())) {
					isSucc = true;
				}
			}
			return isSucc;
		}

		public boolean deletePublicKey(PublicKey publicKey) {
			Account account = getAccount(publicKey.getID(), true);
			account.getKey().setPublickey(null);
			return saveAccount(account);
		}

		public boolean close() {
			boolean isSucc = true;
			for (ColumnFamilyHandle columnFamilyHandle : columnFamilyHandles) {
				if (!EQCBlockChainRocksDB.clearTable(columnFamilyHandle) || !EQCBlockChainRocksDB.dropTable(columnFamilyHandle)) {
					isSucc = false;
				}
			}
			return isSucc;
		}
		
	}

	public ID getAddressID(Address address) {
		return filter.getAddressID(address);
	}

	public Address getAddress(ID id) {
		return filter.getAddress(id);
	}

	public PublicKey getPublicKey(ID id) {
		return filter.getPublicKey(id);
	}

	public boolean savePublicKey(PublicKey publicKey, ID height) {
		return filter.savePublicKey(publicKey, height);
	}

	public boolean isPublicKeyExists(PublicKey publicKey) {
		return filter.isPublicKeyExists(publicKey);
	}

	public boolean deletePublicKey(PublicKey publicKey) {
		return filter.deletePublicKey(publicKey);
	}

	public byte[] getEQCHeaderHash(ID id) {
		byte[] hash = null;
		Account account = null;
		account = getAccount(id);
		hash = EQCBlockChainRocksDB.getInstance().getEQCHeaderHash(account.getKey().getAddressCreateHeight());
		return hash;
	}
	
	public EQCBlock getEQCBlock(ID height, boolean isSegwit) {
		return Util.DB().getEQCBlock(height, true);
	}

//	/**
//	 * Update current Transaction's TxIn Account's Nonce&Balance
//	 * 
//	 * @param transaction
//	 * @return
//	 */
//	public boolean updateAccount(Transaction transaction){
//		// Update current Transaction's TxIn Account's Nonce&Balance
//		Account account = getAccount(transaction.getTxIn().getAddress().getID());
//		// Update current Transaction's TxIn Account's Nonce
//		account.increaseNonce();
//		// Update current Transaction's TxIn Account's Balance
//		account.updateBalance(-transaction.getBillingValue());
//		return saveAccount(account);
//	}
//	
//	/**
//	 * Update current Transaction's TxIn Publickey if need
//	 * 
//	 * @param transaction
//	 * @return
//	 */
//	public boolean updatePublickey(Transaction transaction) {
//		boolean isSucc = false;
//		// Update current Transaction's TxIn Publickey if need
//		if (transaction.getPublickey().isNew()) {
//			isSucc = savePublicKey(transaction.getPublickey(), height.getNextID());
//		}
//		return isSucc;
//	}
	
	public class Statistics {
		
		private Vector<AssetStatistics> assetStatisticsList;
		
		public Statistics() {
			assetStatisticsList = new Vector<>();
		}
		
		public AssetStatistics getAssetStatistics(ID assetID) {
			for(AssetStatistics assetStatistics : assetStatisticsList) {
				if(assetStatistics.assetID.equals(assetID)) {
					return assetStatistics;
				}
			}
			return null;
		}
		
		public boolean isAssetExists(ID assetID) {
			for(AssetStatistics assetStatistics : assetStatisticsList) {
				if(assetStatistics.assetID.equals(assetID)) {
					return true;
				}
			}
			return false;
		}
		
		public boolean isValid(AccountsMerkleTree accountsMerkleTree) {
			
			return true;
		}
		
		public void update(Vector<Asset> assets) {
			AssetStatistics assetStatistics = null;
			for(Asset asset : assets) {
				if(!isAssetExists(asset.getAssetID())) {
					assetStatistics = new AssetStatistics();
					assetStatistics.assetID = asset.getAssetID();
					assetStatisticsList.add(assetStatistics);
				}
				else {
					assetStatistics = getAssetStatistics(asset.getAssetID());
				}
				assetStatistics.totalAccountNumbers = assetStatistics.totalAccountNumbers.getNextID();
				assetStatistics.totalSupply += asset.getBalance();
				assetStatistics.totalTransactionNumbers = assetStatistics.totalTransactionNumbers.add(asset.getNonce());
			}
		}
		
		public class AssetStatistics{
			public ID assetID;
			public ID totalAccountNumbers;
			public long totalSupply;
			public ID totalTransactionNumbers;
		}
	}
	
	public Statistics getStatistics() {
		Account account;
		Statistics statistics = new Statistics();
		for(int i=1; i<=totalAccountNumbers.longValue(); ++i) {
			account = filter.getAccount(new ID(i).getEQCBits(), false);
			Log.info(account.toString());
			statistics.update(account.getAssetList());
		}
		// Calculate Coinbase Transaction's numbers
//		Log.info("height: " + height);
//		statistics.totalTransactionNumbers = statistics.totalTransactionNumbers.add(height.add(BigInteger.TWO));
		return statistics;
	}
	
	public boolean takeSnapshot() {
		return filter.takeSnapshot();
	}
	
}
