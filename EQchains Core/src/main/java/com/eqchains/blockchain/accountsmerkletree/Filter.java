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
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Vector;

import org.rocksdb.ColumnFamilyHandle;
import org.rocksdb.RocksDBException;
import org.rocksdb.RocksIterator;
import org.rocksdb.WriteBatch;
import org.rocksdb.WriteOptions;

import com.eqchains.blockchain.account.Account;
import com.eqchains.blockchain.account.Passport;
import com.eqchains.blockchain.hive.EQCHive;
import com.eqchains.blockchain.transaction.CompressedPublickey;
import com.eqchains.blockchain.account.AssetSubchainAccount;
import com.eqchains.persistence.EQCBlockChainH2;
import com.eqchains.persistence.EQCBlockChainRocksDB;
import com.eqchains.persistence.EQCBlockChainRocksDB.TABLE;
import com.eqchains.serialization.EQCType;
import com.eqchains.util.ID;
import com.eqchains.util.Log;
import com.eqchains.util.Util;

/**
 * @author Xun Wang
 * @date Jun 16, 2019
 * @email 10509759@qq.com
 */
public class Filter {
	private AccountsMerkleTree accountsMerkleTree;
	private Mode mode;

	public enum Mode {
		MINERING, VALID
	}

	public Filter(Mode mode) throws RocksDBException {
		this.mode = mode;
		// In case before close the app crashed or abnormal interruption so here just
		// clear the table
		clear();
	}

	public TABLE getFilterTable(TABLE table) {
		TABLE filterTable = null;
		if (mode == Mode.MINERING) {
			if (table == TABLE.ACCOUNT) {
				filterTable = TABLE.ACCOUNT_MINERING;
			} else if (table == TABLE.ACCOUNT_AI) {
				filterTable = TABLE.ACCOUNT_MINERING_AI;
			} else if (table == TABLE.ACCOUNT_HASH) {
				filterTable = TABLE.ACCOUNT_MINERING_HASH;
			}
		} else if (mode == Mode.VALID) {
			if (table == TABLE.ACCOUNT) {
				filterTable = TABLE.ACCOUNT_VALID;
			} else if (table == TABLE.ACCOUNT_AI) {
				filterTable = TABLE.ACCOUNT_VALID_AI;
			} else if (table == TABLE.ACCOUNT_HASH) {
				filterTable = TABLE.ACCOUNT_VALID_HASH;
			}
		}
		return filterTable;
	}

	public ColumnFamilyHandle getTableHandle(TABLE table) throws RocksDBException {
		return EQCBlockChainRocksDB.getInstance().getTableHandle(getFilterTable(table));
	}

	public Account getAccount(ID id, boolean isLoadInFilter) throws Exception {
		// Here maybe exists some bugs need do more test
		// Test find during verify block due to before Transaction.update the total
		// account numbers will not increase so the new account's id will exceed the
		// total account numbers
//		EQCType.assertNotBigger(id, accountsMerkleTree.getTotalAccountNumbers());
		return getAccount(id.getEQCBits(), isLoadInFilter);
	}

	public Account getAccount(Passport address, boolean isLoadInFilter) throws Exception {
		ID id = null;
		id = getAddressID(address);
		if (id == null) {
			return null;
		}
		return getAccount(id, isLoadInFilter);
	}

	/**
	 * For security issue only support search address via AddressAI
	 * <p>
	 * 
	 * @param passport
	 * @return
	 * @throws RocksDBException
	 */
	public boolean isAccountExists(Passport passport) throws RocksDBException {
		boolean isSucc = false;
		// For security issue only support search address via AddressAI
		if ((EQCBlockChainRocksDB.getInstance().get(getFilterTable(TABLE.ACCOUNT), passport.getAddressAI()) != null)) {
			isSucc = true;
		}
		return isSucc;
	}

	public boolean isAddressExists(Passport passport, ID height)
			throws NoSuchFieldException, RocksDBException, IOException {
		boolean isSucc = false;
		byte[] bytes = null;
		Account account = null;
		// For security issue only support search address via AddressAI
		if ((bytes = EQCBlockChainRocksDB.getInstance().get(getFilterTable(TABLE.ACCOUNT),
				passport.getAddressAI())) != null) {
			// Here maybe still exists some issue
			account = Account.parseAccount(bytes);
			if (account.getLockCreateHeight().compareTo(height) <= 0)
				isSucc = true;
		}
		return isSucc;
	}

	public void saveAccount(Account account) throws RocksDBException {
		WriteBatch writeBatch = new WriteBatch();
		writeBatch.put(EQCBlockChainRocksDB.getInstance().getTableHandle(getFilterTable(TABLE.ACCOUNT)),
				account.getID().getEQCBits(), account.getBytes());
		writeBatch.put(EQCBlockChainRocksDB.getInstance().getTableHandle(getFilterTable(TABLE.ACCOUNT_AI)),
				account.getPassport().getAddressAI(), account.getID().getEQCBits());
		EQCBlockChainRocksDB.getInstance().batchUpdate(writeBatch);
	}

	public void saveAccountHash(Account account, byte[] accountHash) throws RocksDBException {
		WriteBatch writeBatch = new WriteBatch();
		writeBatch.put(EQCBlockChainRocksDB.getInstance().getTableHandle(getFilterTable(TABLE.ACCOUNT_HASH)),
				account.getID().getEQCBits(), accountHash);
		EQCBlockChainRocksDB.getInstance().batchUpdate(writeBatch);
	}

	public void batchUpdate(WriteBatch writeBatch) throws RocksDBException {
		EQCBlockChainRocksDB.getInstance().batchUpdate(writeBatch);
	}

	public Account getAccount(byte[] id, boolean isLoadInFilter) throws Exception {
		Account account = null;
		byte[] bytes = null;
		// Check if Account already loading in filter
		bytes = EQCBlockChainRocksDB.getInstance().get(getFilterTable(TABLE.ACCOUNT), id);
		if (bytes != null) {
			account = Account.parseAccount(bytes);
		} else {
			// The first time loading account need loading the previous block's snapshot but
			// doesn't include No.0 EQCHive
			if (accountsMerkleTree.getHeight().compareTo(ID.ZERO) > 0) {
				ID tailHeight = Util.DB().getEQCBlockTailHeight();
				if (accountsMerkleTree.getHeight().isNextID(tailHeight)) {
					bytes = EQCBlockChainRocksDB.getInstance().get(TABLE.ACCOUNT, id);
					if (bytes != null) {
						account = Account.parseAccount(bytes);
					}
				} else if (accountsMerkleTree.getHeight().compareTo(tailHeight) <= 0) {
					// Load relevant Account from snapshot
					account = EQCBlockChainH2.getInstance().getAccountSnapshot(new ID(id),
							accountsMerkleTree.getHeight().getPreviousID());
				} else {
					throw new IllegalStateException("Wrong height " + accountsMerkleTree.getHeight() + " tail height "
							+ Util.DB().getEQCBlockTailHeight());
				}
			} else {
				account = EQCBlockChainH2.getInstance().getAccountSnapshot(new ID(id), ID.ZERO);
			}
//			if (accountsMerkleTree.getHeight()
//					.compareTo(Util.DB().getEQCBlockTailHeight()) < 0) {
//				// Load relevant 
//				account = EQCBlockChainH2.getInstance().getAccountSnapshot(new ID(id), accountsMerkleTree.getHeight().getPreviousID());
//			}
//			else {
//				bytes = EQCBlockChainRocksDB.getInstance().get(TABLE.ACCOUNT, id);
//				if (bytes != null) {
//					account = Account.parseAccount(bytes);
//				}
//			}
			if (account == null) {
				throw new IllegalStateException("Account No. " + new ID(id) + " doesn't exists.");
			}
			if (isLoadInFilter) {
				saveAccount(account);
			}
		}
		return account;
	}

	@Deprecated
	public byte[] getAccountHash(ID id) throws RocksDBException {
		byte[] bytes = null;
		bytes = EQCBlockChainRocksDB.getInstance().get(getFilterTable(TABLE.ACCOUNT_HASH), id.getEQCBits());
		if (bytes == null) {
			bytes = EQCBlockChainRocksDB.getInstance().get(TABLE.ACCOUNT_HASH, id.getEQCBits());
		}
		return bytes;
	}

	public void merge(EQCHive eqcHive) throws Exception {
		Account account = null;
		boolean isChanged = false;
		WriteBatch writeBatch = new WriteBatch();
		RocksIterator rocksIterator = EQCBlockChainRocksDB.getInstance()
				.getRocksIterator(getFilterTable(TABLE.ACCOUNT));
		rocksIterator.seekToFirst();
		isChanged = rocksIterator.isValid();
		// Update relevant Account's status to Global Accounts table
		while (rocksIterator.isValid()) {
			account = Account.parseAccount(rocksIterator.value());
			writeBatch.put(EQCBlockChainRocksDB.getInstance().getTableHandle(TABLE.ACCOUNT), rocksIterator.key(),
					rocksIterator.value());
//				Log.info(account.toString());
//				Log.info(Util.dumpBytes(account.getAddress().getAddressAI(), 16));
			writeBatch.put(EQCBlockChainRocksDB.getInstance().getTableHandle(TABLE.ACCOUNT_AI),
					account.getPassport().getAddressAI(), account.getID().getEQCBits());
			writeBatch.put(EQCBlockChainRocksDB.getInstance().getTableHandle(TABLE.ACCOUNT_HASH),
					account.getID().getEQCBits(), EQCBlockChainRocksDB.getInstance()
							.get(getFilterTable(TABLE.ACCOUNT_HASH), account.getID().getEQCBits()));
//				EQCBlockChainRocksDB.getInstance().getInstance().saveAccount(account);
			rocksIterator.next();
		}
		if (eqcHive != null) {
			isChanged = true;
			// Save EQCHive
			writeBatch.put(EQCBlockChainRocksDB.getInstance().getTableHandle(TABLE.EQCBLOCK), eqcHive.getHeight().getEQCBits(), eqcHive.getBytes());
			writeBatch.put(EQCBlockChainRocksDB.getInstance().getTableHandle(TABLE.EQCBLOCK_HASH), eqcHive.getHeight().getEQCBits(),
					eqcHive.getEqcHeader().getHash());
			// Save EQCHive tail height
			writeBatch.put(EQCBlockChainRocksDB.getInstance().getTableHandle(TABLE.MISC), EQCBlockChainRocksDB.EQCBLOCK_TAIL_HEIGHT,
					eqcHive.getHeight().getEQCBits());
		}
		// Batch update
		if (isChanged) {
			EQCBlockChainRocksDB.getInstance().batchUpdate(writeBatch);
		}
	}

	public void takeSnapshot() throws Exception {
		RocksIterator rocksIterator = EQCBlockChainRocksDB.getInstance()
				.getRocksIterator(getFilterTable(TABLE.ACCOUNT));
		rocksIterator.seekToFirst();
//		AssetSubchainAccount assetSubchainAccount = (AssetSubchainAccount) Util.DB().getAccount(ID.ONE);

//		// Prepare Snapshot height
//		ID snapshotHeight = ID.ZERO;
//		if(accountsMerkleTree.getHeight().compareTo(ID.ZERO) > 0) {
//			snapshotHeight = accountsMerkleTree.getHeight().getNextID();
//		}

		while (rocksIterator.isValid()) {
//				Log.info("Key: " + Util.dumpBytes(rocksIterator.key(), 16) + " Value: " + Util.dumpBytes(rocksIterator.value(), 16));
			Account account = Account.parseAccount(rocksIterator.value());
			EQCBlockChainH2.getInstance().saveAccountSnapshot(account, accountsMerkleTree.getHeight());
//				if (account.getID()
//						.compareTo(assetSubchainAccount.getAssetSubchainHeader().getTotalAccountNumbers()) <= 0) {
//					if (!EQCBlockChainH2.getInstance().saveAccountSnapshot(account, accountsMerkleTree.getHeight())) {
//						isSucc = false;
//						Log.Error("During takeSnapshot error occur saveAccountSnapshot failed.");
//						break;
//					}
//				}
//				rocksIterator.next();
//				Log.info("Key: " + Util.dumpBytes(rocksIterator.key(), 16) + " Value: " + Util.dumpBytes(rocksIterator.value(), 16));
//				rocksIterator.next();
//				Log.info("Key: " + Util.dumpBytes(rocksIterator.key(), 16) + " Value: " + Util.dumpBytes(rocksIterator.value(), 16));
			rocksIterator.next();
		}
	}

	/**
	 * Use this to fill in the relevant Transaction's TxIn or TxOut's ID which can't
	 * be null
	 * 
	 * @param address
	 * @return
	 * @throws RocksDBException
	 * @throws NoSuchFieldException
	 * @throws IllegalStateException
	 * @throws IOException
	 */
	public ID getAddressID(Passport passport)
			throws RocksDBException, NoSuchFieldException, IllegalStateException, IOException {
		ID id = null;
		byte[] bytes = null;
		bytes = EQCBlockChainRocksDB.getInstance().get(getFilterTable(TABLE.ACCOUNT_AI), passport.getAddressAI());
		if (bytes != null) {
			id = new ID(bytes);
		} else {
			bytes = EQCBlockChainRocksDB.getInstance().get(TABLE.ACCOUNT_AI, passport.getAddressAI());
			if (bytes != null) {
				id = new ID(bytes);
			}
		}
		EQCType.assertNotNull(id);
		return id;
	}

	public Passport getAddress(ID id) throws Exception {
		return getAccount(id, true).getPassport();
	}

	public CompressedPublickey getPublicKey(ID id) throws Exception {
		CompressedPublickey publicKey = null;
		if (getAccount(id, true).getPublickey() != null) {
			publicKey = new CompressedPublickey();
			publicKey.setCompressedPublickey(getAccount(id, true).getPublickey().getCompressedPublickey());
			publicKey.setID(id);
		}
		return publicKey;
	}

	public void savePublicKey(CompressedPublickey publicKey, ID height) throws Exception {
		Account account = getAccount(publicKey.getID(), true);
		com.eqchains.blockchain.account.Publickey publickey2 = new com.eqchains.blockchain.account.Publickey();
		publickey2.setCompressedPublickey(publicKey.getCompressedPublickey());
		publickey2.setPublickeyCreateHeight(height);
		account.setPublickey(publickey2);
		saveAccount(account);
	}

	public boolean isPublicKeyExists(CompressedPublickey publicKey) throws Exception {
		boolean isSucc = false;
		Account account = getAccount(publicKey.getID(), true);
		if (account.getPublickey() != null) {
			if (Arrays.equals(account.getPublickey().getCompressedPublickey(), publicKey.getCompressedPublickey())) {
				isSucc = true;
			}
		}
		return isSucc;
	}

	@Deprecated
	public void deletePublicKey(CompressedPublickey publicKey) throws Exception {
		Account account = getAccount(publicKey.getID(), true);
		account.setPublickey(null);
		saveAccount(account);
	}

	public void clear() throws RocksDBException {
		if (mode == Mode.MINERING) {
			EQCBlockChainRocksDB.getInstance()
					.clearTable(EQCBlockChainRocksDB.getInstance().getTableHandle(TABLE.ACCOUNT_MINERING));
			EQCBlockChainRocksDB.getInstance()
					.clearTable(EQCBlockChainRocksDB.getInstance().getTableHandle(TABLE.ACCOUNT_MINERING_AI));
			EQCBlockChainRocksDB.getInstance()
					.clearTable(EQCBlockChainRocksDB.getInstance().getTableHandle(TABLE.ACCOUNT_MINERING_HASH));
		} else if (mode == Mode.VALID) {
			EQCBlockChainRocksDB.getInstance()
					.clearTable(EQCBlockChainRocksDB.getInstance().getTableHandle(TABLE.ACCOUNT_VALID));
			EQCBlockChainRocksDB.getInstance()
					.clearTable(EQCBlockChainRocksDB.getInstance().getTableHandle(TABLE.ACCOUNT_VALID_AI));
			EQCBlockChainRocksDB.getInstance()
					.clearTable(EQCBlockChainRocksDB.getInstance().getTableHandle(TABLE.ACCOUNT_VALID_HASH));
		}
	}

	/**
	 * @param accountsMerkleTree the accountsMerkleTree to set
	 */
	public void setAccountsMerkleTree(AccountsMerkleTree accountsMerkleTree) {
		this.accountsMerkleTree = accountsMerkleTree;
	}

}
