/**
 * EQcoin core - EQcoin Federation's EQcoin core library
 * @copyright 2018-present EQcoin Federation All rights reserved...
 * Copyright of all works released by EQcoin Federation or jointly released by
 * EQcoin Federation with cooperative partners are owned by EQcoin Federation
 * and entitled to protection available from copyright law by country as well as
 * international conventions.
 * Attribution — You must give appropriate credit, provide a link to the license.
 * Non Commercial — You may not use the material for commercial purposes.
 * No Derivatives — If you remix, transform, or build upon the material, you may
 * not distribute the modified material.
 * For any use of above stated content of copyright beyond the scope of fair use
 * or without prior written permission, EQcoin Federation reserves all rights to
 * take any legal action and pursue any right or remedy available under applicable
 * law.
 * https://www.eqcoin.org
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

import com.eqchains.blockchain.accountsmerkletree.Filter.Mode;
import com.eqchains.blockchain.hive.EQCHive;
import com.eqchains.blockchain.passport.Asset;
import com.eqchains.blockchain.passport.AssetPassport;
import com.eqchains.blockchain.passport.AssetSubchainHeader;
import com.eqchains.blockchain.passport.AssetSubchainPassport;
import com.eqchains.blockchain.passport.EQcoinSubchainPassport;
import com.eqchains.blockchain.passport.Lock;
import com.eqchains.blockchain.passport.Passport;
import com.eqchains.blockchain.passport.Lock.LockShape;
import com.eqchains.blockchain.transaction.CompressedPublickey;
import com.eqchains.blockchain.transaction.Transaction;
import com.eqchains.configuration.Configuration;
import com.eqchains.crypto.MerkleTree;
import com.eqchains.persistence.EQCBlockChainH2;
import com.eqchains.serialization.EQCTypable;
import com.eqchains.serialization.EQCType;
import com.eqchains.util.ID;
import com.eqchains.util.Log;
import com.eqchains.util.Util;

/**
 * The height of PassportsMerkleTree is previous EQCBlock's height when 
 * build new block or verify new block.
 * If create from the early height need fill all the relevant Passport's 
 * Snapshot(In current height) from H2 in Filter.
 * 
 * @author Xun Wang
 * @date Mar 11, 2019
 * @email 10509759@qq.com
 */
public class PassportsMerkleTree {
	public final static int MAX_PASSPORTS_PER_TREE = 1024;
	private Vector<byte[]> passportsMerkleTreeRootList;
	/**
	 * Current EQCHive's height which is the base for the new EQCHive
	 */
	private ID height;
	private byte[] passportsMerkleTreeRoot;
	private ID totalPassportNumbers;
	private ID previousTotalPassportNumbers;
	private Filter filter;
	private Vector<MerkleTree> passportsMerkleTree;

	public PassportsMerkleTree(ID height, Filter filter) throws Exception {
		super();
		EQcoinSubchainPassport eQcoinSubchainPassport = null;
		
		filter.setAccountsMerkleTree(this);
		this.height = height;
		// When recoverySingularityStatus the No.0 EQCHive doesn't exist so here need special operation
//		if (height.equals(ID.ZERO)) {
//			try {
//				eQcoinSubchainAccount = (EQcoinSubchainAccount) Util.DB().getAccount(ID.ONE, height);
//			}
//			catch (Exception e) {
//				Log.info("Height is zero and Account No.1 doesn't exists: " + e.getMessage());
//			}
//		} else {
//			totalAccountNumbers = Util.DB().getTotalAccountNumbers(height.getPreviousID());
//		}
		
		eQcoinSubchainPassport = (EQcoinSubchainPassport) Util.DB().getPassport(ID.ONE, height);
		if (eQcoinSubchainPassport != null) {
			totalPassportNumbers = eQcoinSubchainPassport.getAssetSubchainHeader().getTotalPassportNumbers();
		} else {
			totalPassportNumbers = ID.ZERO;
		}
		
		previousTotalPassportNumbers = totalPassportNumbers;
		this.filter = filter;
		passportsMerkleTreeRootList = new Vector<>();
	}
	
	/**
	 * Check if Passport exists according to Lock's AddressAI.
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
	 * @param lock
	 * @param isFiltering When need searching in Filter table just set it to true
	 * @return true if Account exists
	 * @throws Exception 
	 */
	public synchronized boolean isPassportExists(Lock lock, boolean isFiltering) throws Exception {
		boolean isExists = false;
		if(isFiltering && filter.isAccountExists(lock)) {
			isExists = true;
		}
		else {
			Passport passport = Util.DB().getPassport(lock.getAddressAI(), Mode.GLOBAL);
			if(passport != null && passport.getCreateHeight().compareTo(height) < 0 && passport.getLockCreateHeight().compareTo(height) < 0 && passport.getId().compareTo(previousTotalPassportNumbers) <= 0) {
				isExists = true;
			}
		}
		return  isExists;
	}
	
	public synchronized Passport getPassport(ID id, boolean isFiltering) throws Exception {
//		EQCType.assertNotBigger(id, previousTotalAccountNumbers); // here need do more job to determine if need this check
		return filter.getPassport(id, isFiltering);
	}
	
	public synchronized Passport getPassport(Lock key, boolean isFiltering) throws Exception {
		return filter.getPassport(key, isFiltering);
	}
	
	/**
	 * Save current Passport in Filter
	 * @param passport
	 * @return true if save successful
	 * @throws Exception 
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 */
	public synchronized void savePassport(Passport account) throws ClassNotFoundException, SQLException, Exception {
//		Log.info(account.toString());
		filter.savePassport(account);
	}
	
	public synchronized void increaseTotalPassportNumbers() {
		totalPassportNumbers = totalPassportNumbers.add(BigInteger.ONE);
		Log.info("increaseTotalAccountNumbers: " + totalPassportNumbers);
	}
	
	/**
	 * @return the totalPassportNumber
	 */
	public synchronized ID getTotalPassportNumbers() {
		return totalPassportNumbers;
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
		Passport account = null;
		Vector<byte[]> accountsHash = new Vector<>();
		long remainder = totalPassportNumbers.longValue();
		int begin = 0, end = 0;
		byte[] accountHash = null;
		
		for (int i = 0; i <= totalPassportNumbers.longValue() / MAX_PASSPORTS_PER_TREE; ++i) {
			begin = BigInteger.valueOf(i * MAX_PASSPORTS_PER_TREE)
					.add(BigInteger.valueOf(Util.INIT_ADDRESS_SERIAL_NUMBER)).intValue();
			if ((remainder - MAX_PASSPORTS_PER_TREE) > 0) {
				end = MAX_PASSPORTS_PER_TREE + Util.INIT_ADDRESS_SERIAL_NUMBER;
				remainder -= MAX_PASSPORTS_PER_TREE;
			} else {
				end = (int) remainder + Util.INIT_ADDRESS_SERIAL_NUMBER; 
			}
			for (int j = begin; j < end; ++j) {
				account = filter.getPassport(new ID(j), true);
				// If account == null which means error occur
				if (account == null) {
					throw new IllegalStateException("Account shouldn't be null");
				}
//				Log.info(account.toString());
//				Log.info(Util.dumpBytes(account.getHash(), 16));
				account.setSaveHash(true);
				accountHash = account.getHash();
				filter.savePassport(account);
				accountsHash.add(accountHash);
//				Log.info("No." + j + "'s "+ Util.dumpBytes(accountHash, 16));
				accountHash = null;
			}
			MerkleTree merkleTree = new MerkleTree(accountsHash);
			merkleTree.generateRoot();
			passportsMerkleTreeRootList.add(merkleTree.getRoot());
			merkleTree = null;
			accountsHash.clear();
		}
//		System.gc();
	}
	
	public void generateRoot() {
		MerkleTree merkleTree = new MerkleTree(passportsMerkleTreeRootList);
		merkleTree.generateRoot();
		passportsMerkleTreeRoot = merkleTree.getRoot();
	}
	
	public byte[] getRoot() {
		return passportsMerkleTreeRoot;
	}
	
	public Vector<byte[]> getAccountsMerkleTreeRootList(){
		Vector<byte[]> bytes = new Vector<byte[]>();
		for(MerkleTree merkleTree : passportsMerkleTree) {
			bytes.add(merkleTree.getRoot());
		}
		return bytes;
	}

	public void merge() throws Exception {
		filter.merge();
	}
	
	public void clear() throws ClassNotFoundException, SQLException, Exception {
		filter.clear();
	}
	
//	public ID getPassportID(Passport address) throws ClassNotFoundException, SQLException, Exception {
//		return filter.getPassportID(address);
//	}

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
//		Account account = getAccount(transaction.getTxIn().getAddress().getId());
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
		
		public boolean isValid(PassportsMerkleTree accountsMerkleTree) throws Exception {
			for(AssetStatistics assetStatistics : assetStatisticsList) {
				AssetSubchainPassport assetSubchainAccount = (AssetSubchainPassport) accountsMerkleTree.getPassport(assetStatistics.assetID, true);
				AssetSubchainHeader assetSubchainHeader = assetSubchainAccount.getAssetSubchainHeader();
				if(assetSubchainHeader.getTotalPassportNumbers().compareTo(assetStatistics.totalAccountNumbers) != 0) {
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
		Passport account;
		Statistics statistics = new Statistics();
		for(int i=1; i<=totalPassportNumbers.longValue(); ++i) {
			account = filter.getPassport(ID.valueOf(i), true);
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
	 * @return the previousTotalAccountNumbers
	 */
	public ID getPreviousTotalAccountNumbers() {
		return previousTotalPassportNumbers;
	}
	
	public void updateGlobalState() throws Exception {
		// Save the snapshot of current tail height's changed Accounts from Account Table to Snapshot Table
		takeSnapshot();
		merge();
		clear();
	}
	
	public ID getTotalCoinbaseTransactionNumbers() {
		if (height.compareTo(Util.getMaxCoinbaseHeight(height)) < 0) {
			return height.getNextID();
		}
		else {
			return Util.getMaxCoinbaseHeight(height);
		}
	}
	
}
