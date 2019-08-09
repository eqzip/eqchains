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
package com.eqchains.blockchain.accountsmerkletree;

import java.io.IOException;
import java.math.BigInteger;
import java.sql.SQLException;
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

import com.eqchains.blockchain.account.Account;
import com.eqchains.blockchain.account.Passport;
import com.eqchains.blockchain.account.Asset;
import com.eqchains.blockchain.account.AssetAccount;
import com.eqchains.blockchain.account.AssetSubchainAccount;
import com.eqchains.blockchain.account.AssetSubchainHeader;
import com.eqchains.blockchain.account.Passport.AddressShape;
import com.eqchains.blockchain.hive.EQCHive;
import com.eqchains.blockchain.transaction.CompressedPublickey;
import com.eqchains.blockchain.transaction.Transaction;
import com.eqchains.configuration.Configuration;
import com.eqchains.crypto.MerkleTree;
import com.eqchains.persistence.EQCBlockChainH2;
import com.eqchains.persistence.EQCBlockChainRocksDB;
import com.eqchains.persistence.EQCBlockChainRocksDB.TABLE;
import com.eqchains.serialization.EQCTypable;
import com.eqchains.serialization.EQCType;
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
	/**
	 * Current EQCHive's height which is the base for the new EQCHive
	 */
	private ID height;
	private byte[] accountsMerkleTreeRoot;
	private ID totalAccountNumbers;
	private ID previousTotalAccountNumbers;
	private Filter filter;
	private Vector<MerkleTree> accountsMerkleTree;

	public AccountsMerkleTree(ID height, Filter filter) throws Exception {
		super();
		AssetSubchainAccount assetSubchainAccount = null;
		
		filter.setAccountsMerkleTree(this);
		this.height = height;
		// When recoverySingularityStatus the No.0 EQCHive doesn't exist so here need special operation
		if (height.equals(ID.ZERO)) {
			try {
				assetSubchainAccount = (AssetSubchainAccount) Util.DB().getAccount(ID.ONE);
			}
			catch (Exception e) {
				Log.info("Height is zero and Account No.1 doesn't exists: " + e.getMessage());
			}
			if (assetSubchainAccount != null) {
				totalAccountNumbers = Util.DB().getTotalAccountNumbers(height);
			} else {
				totalAccountNumbers = ID.ZERO;
			}
		} else {
			totalAccountNumbers = Util.DB().getTotalAccountNumbers(height.getPreviousID());
		}
		previousTotalAccountNumbers = totalAccountNumbers;
		this.filter = filter;
		accountsMerkleTreeRootList = new Vector<>();
	}
	
	/**
	 * Check if Account exists according to Passport's AddressAI.
	 * <p>
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
	 * @return true if Account exists
	 * @throws Exception 
	 */
	public synchronized boolean isAccountExists(Passport address, boolean isFiltering) throws Exception {
		boolean isExists = false;
		if(isFiltering && filter.isAccountExists(address)) {
			isExists = true;
		}
		else {
			Account account = Util.DB().getAccount(address.getAddressAI());
			if(account != null && account.getLockCreateHeight().compareTo(height) < 0 && account.getID().compareTo(previousTotalAccountNumbers) <= 0) {
				isExists = true;
			}
		}
		return  isExists;
	}
	
	public synchronized boolean isAccountExists(Passport passport) throws Exception {
		boolean isExists = false;
			Account account = Util.DB().getAccount(passport.getID());
			if(account != null && account.getLockCreateHeight().compareTo(height) < 0 && account.getID().compareTo(previousTotalAccountNumbers) <= 0) {
				isExists = true;
			}
		return  isExists;
	}
	
	public synchronized Account getAccount(ID id) throws Exception {
		EQCType.assertNotBigger(id, previousTotalAccountNumbers);
		return filter.getAccount(id, true);
	}
	
	public synchronized Account getAccount(Passport address) throws Exception {
		return filter.getAccount(address, true);
	}
	
	/**
	 * Save current Account in Filter
	 * @param account
	 * @return true if save successful
	 * @throws RocksDBException 
	 */
	public synchronized void saveAccount(Account account) throws RocksDBException {
//		Log.info(account.toString());
		filter.saveAccount(account);
	}
	
	public synchronized void increaseTotalAccountNumbers() {
		totalAccountNumbers = totalAccountNumbers.add(BigInteger.ONE);
		Log.info("increaseTotalAccountNumbers: " + totalAccountNumbers);
	}
	
	/**
	 * @return the totalAccountNumber
	 */
	public synchronized ID getTotalAccountNumbers() {
		return totalAccountNumbers;
	}
	
	/**
	 * Get current EQCHive's height
	 * 
	 * @return the height
	 */
	public synchronized ID getHeight() {
 		return height;
	}

	/**
	 * Set current EQCHive's height
	 * 
	 * @param height the height to set
	 */
	public synchronized void setHeight(ID height) {
		this.height = height;
	}

	public void buildAccountsMerkleTree() throws Exception {
		Log.info("Begin buildAccountsMerkleTree");
		Account account = null;
		Vector<byte[]> accountsHash = new Vector<>();
		long remainder = totalAccountNumbers.longValue();
		int begin = 0, end = 0;
		byte[] accountHash = null;
		
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
//				Log.info(account.toString());
//				Log.info(Util.dumpBytes(account.getHash(), 16));
				accountHash = account.getHash();
				filter.saveAccountHash(account, accountHash);
				accountsHash.add(accountHash);
				accountHash = null;
			}
			MerkleTree merkleTree = new MerkleTree(accountsHash);
			merkleTree.generateRoot();
			accountsMerkleTreeRootList.add(merkleTree.getRoot());
			merkleTree = null;
			accountsHash.clear();
		}
//		System.gc();
	}
	
	public void generateRoot() {
		MerkleTree merkleTree = new MerkleTree(accountsMerkleTreeRootList);
		merkleTree.generateRoot();
		accountsMerkleTreeRoot = merkleTree.getRoot();
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

	public void merge(EQCHive eqcHive) throws Exception {
		filter.merge(eqcHive);
	}
	
	public void clear() throws RocksDBException {
		filter.clear();
	}
	
	public ID getAddressID(Passport address) throws NoSuchFieldException, IllegalStateException, RocksDBException, IOException {
		return filter.getAddressID(address);
	}

	public Passport getAddress(ID id) throws Exception {
		return filter.getAddress(id);
	}

	public CompressedPublickey getPublicKey(ID id) throws Exception {
		return filter.getPublicKey(id);
	}

	@Deprecated
	public void savePublicKey(CompressedPublickey publicKey, ID height) throws Exception {
		filter.savePublicKey(publicKey, height);
	}

	public boolean isPublicKeyExists(CompressedPublickey publicKey) throws Exception {
		return filter.isPublicKeyExists(publicKey);
	}

	@Deprecated
	public void deletePublicKey(CompressedPublickey publicKey) throws Exception {
		filter.deletePublicKey(publicKey);
	}

	public byte[] getEQCHeaderHash(ID height) throws Exception {
		return Util.DB().getEQCHeaderHash(height);
	}
	
	public byte[] getEQCHeaderBuddyHash(ID height) throws Exception {
		byte[] hash = null;
		EQCType.assertNotBigger(height, this.height);
		if(height.compareTo(this.height) < 0) {
			hash = Util.DB().getEQCHeaderHash(height);
		}
		else {
			hash = Util.DB().getEQCHeaderHash(height.getPreviousID());
		}
		return hash;
	}
	
	public EQCHive getEQCBlock(ID height, boolean isSegwit) throws Exception {
		return Util.DB().getEQCHive(height, true);
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
		
		public boolean isValid(AccountsMerkleTree accountsMerkleTree) throws Exception {
			for(AssetStatistics assetStatistics : assetStatisticsList) {
				AssetSubchainAccount assetSubchainAccount = (AssetSubchainAccount) accountsMerkleTree.getAccount(assetStatistics.assetID);
				AssetSubchainHeader assetSubchainHeader = assetSubchainAccount.getAssetSubchainHeader();
				if(assetSubchainHeader.getTotalAccountNumbers().compareTo(assetStatistics.totalAccountNumbers) != 0) {
					Log.Error("totalAccountNumbers is invalid.");
					return false;
				}
 				if(assetSubchainHeader.getTotalSupply().compareTo(assetStatistics.totalSupply) != 0) {
					Log.Error("totalSupply is invalid. Expect: " + assetSubchainHeader.getTotalSupply() + " actual: " + assetStatistics.totalSupply);
					return false;
				}
				if(assetSubchainHeader.getTotalTransactionNumbers().compareTo(assetStatistics.totalTransactionNumbers) != 0) {
					Log.Error("totalTransactionNumbers is invalid.");
					return false;
				}
			}
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
				assetStatistics.totalSupply = assetStatistics.totalSupply.add(asset.getBalance());
				assetStatistics.totalTransactionNumbers = assetStatistics.totalTransactionNumbers.add(asset.getNonce());
			}
		}
		
		public class AssetStatistics{
			public ID assetID;
			public ID totalAccountNumbers;
			public ID totalSupply;
			public ID totalTransactionNumbers;
			
			public AssetStatistics() {
				totalAccountNumbers = ID.ZERO;
				totalSupply = ID.ZERO;
				totalTransactionNumbers = ID.ZERO;
			}
			
		}
	}
	
	public Statistics getStatistics() throws Exception {
		Account account;
		Statistics statistics = new Statistics();
		for(int i=1; i<=totalAccountNumbers.longValue(); ++i) {
			account = filter.getAccount(new ID(i).getEQCBits(), false);
//			Log.info(account.toString());
			statistics.update(account.getAssetList());
		}
		// Calculate Coinbase Transaction's numbers
//		Log.info("height: " + height);
//		statistics.totalTransactionNumbers = statistics.totalTransactionNumbers.add(height.add(BigInteger.TWO));
		return statistics;
	}
	
	public void takeSnapshot() throws Exception {
		filter.takeSnapshot();
	}

	/**
	 * @return the filter
	 */
	public Filter getFilter() {
		return filter;
	}
	
	/**
	 * Retrieve the Account from specific height which maybe is current tail or history height 
	 * 
	 * @param addressAI
	 * @return
	 * @throws ClassNotFoundException
	 * @throws RocksDBException
	 * @throws SQLException
	 * @throws Exception
	 */
	public Account getAccount(byte[] addressAI)
			throws ClassNotFoundException, RocksDBException, SQLException, Exception {
		Account account = Util.DB().getAccount(addressAI);
		ID tail = Util.DB().getEQCBlockTailHeight();
		if (height.equals(tail) || height.equals(tail.getNextID())) {
			if (account != null && account.getLockCreateHeight().compareTo(height) <= 0
					&& account.getID().compareTo(previousTotalAccountNumbers) <= 0) {
				account = Util.DB().getAccount(addressAI);
			}
		}
		else if(height.compareTo(tail) <= 0) {
			account = EQCBlockChainH2.getInstance().getAccountSnapshot(addressAI, height.getPreviousID());
		}
		else {
			throw new IllegalStateException("Wrong height " + height + " tail height " + Util.DB().getEQCBlockTailHeight());
		}
		return account;
	}

	/**
	 * @return the previousTotalAccountNumbers
	 */
	public ID getPreviousTotalAccountNumbers() {
		return previousTotalAccountNumbers;
	}
	
}
