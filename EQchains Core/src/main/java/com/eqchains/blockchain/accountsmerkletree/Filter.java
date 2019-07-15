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

import com.eqchains.blockchain.PublicKey;
import com.eqchains.blockchain.account.Account;
import com.eqchains.blockchain.account.Passport;
import com.eqchains.blockchain.account.AssetSubchainAccount;
import com.eqchains.persistence.h2.EQCBlockChainH2;
import com.eqchains.persistence.rocksdb.EQCBlockChainRocksDB;
import com.eqchains.persistence.rocksdb.EQCBlockChainRocksDB.TABLE;
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
//	private byte[] columnFamilyName = null;
	private Vector<ColumnFamilyHandle> columnFamilyHandles;
	
	public enum Mode {
		MINERING, VALID
	}
	
	public Filter(Mode mode) throws RocksDBException {
		columnFamilyHandles = new Vector();
		if (mode == Mode.MINERING) {
			columnFamilyHandles.add(EQCBlockChainRocksDB.getTableHandle(TABLE.ACCOUNT_MINERING));
			columnFamilyHandles.add(EQCBlockChainRocksDB.getTableHandle(TABLE.ACCOUNT_MINERING_AI));
			columnFamilyHandles.add(EQCBlockChainRocksDB.getTableHandle(TABLE.ACCOUNT_MINERING_HASH));
		} else {
			columnFamilyHandles.add(EQCBlockChainRocksDB.getTableHandle(TABLE.ACCOUNT_VALID));
			columnFamilyHandles.add(EQCBlockChainRocksDB.getTableHandle(TABLE.ACCOUNT_VALID_AI));
			columnFamilyHandles.add(EQCBlockChainRocksDB.getTableHandle(TABLE.ACCOUNT_VALID_HASH));
		}
		
		for (ColumnFamilyHandle columnFamilyHandle : columnFamilyHandles) {
			// In case before close the app crashed or abnormal interruption so here just
			// clear the table
//				Log.info("Before cleartable: " + EQCBlockChainRocksDB.getTableItemNumbers(columnFamilyHandle));
			EQCBlockChainRocksDB.clearTable(columnFamilyHandle);
//				Log.info("After cleartable: " + EQCBlockChainRocksDB.getTableItemNumbers(columnFamilyHandle));
		}
	}
	
	public Account getAccount(ID id, boolean isLoadInFilter) throws NoSuchFieldException, IllegalStateException, RocksDBException, IOException, ClassNotFoundException, SQLException {
		return getAccount(id.getEQCBits(), isLoadInFilter);
	}
	
	public Account getAccount(Passport address, boolean isLoadInFilter) throws NoSuchFieldException, IllegalStateException, RocksDBException, IOException, ClassNotFoundException, SQLException {
		ID id = null;
		id = getAddressID(address);
		if(id == null) {
			return null;
		}
		return getAccount(id, isLoadInFilter);
	}
	
	public boolean isAccountExists(Passport passport) throws RocksDBException {
		boolean isSucc = false;
		// For security issue only support search address via AddressAI
		if ((EQCBlockChainRocksDB.getRocksDB().get(columnFamilyHandles.get(1), passport.getAddressAI()) != null)) {
			isSucc = true;
		}
		return isSucc;
	}
	
	public boolean isAddressExists(Passport address, ID height) throws NoSuchFieldException, RocksDBException, IOException {
		boolean isSucc = false;
		byte[] bytes = null;
		Account account = null;
			// For security issue only support search address via AddressAI
			if((bytes = EQCBlockChainRocksDB.getRocksDB().get(columnFamilyHandles.get(1), address.getAddressAI())) != null) {
				// Here maybe still exists some issue
				account = Account.parseAccount(bytes);
				if(account.getLockCreateHeight().compareTo(height) <= 0)
				isSucc = true;
			}
		return isSucc;
	}
	
	public void saveAccount(Account account) throws RocksDBException {
		WriteBatch writeBatch = new WriteBatch();
		writeBatch.put(columnFamilyHandles.get(0), account.getIDEQCBits(), account.getBytes());
		writeBatch.put(columnFamilyHandles.get(1), account.getPassport().getAddressAI(),
				account.getIDEQCBits());
		EQCBlockChainRocksDB.getRocksDB().write(EQCBlockChainRocksDB.getWriteOptions(), writeBatch);
	}
	
	public void saveAccountHash(Account account, byte[] accountHash) throws RocksDBException {
		WriteBatch writeBatch = new WriteBatch();
		writeBatch.put(columnFamilyHandles.get(2), account.getIDEQCBits(), accountHash);
		EQCBlockChainRocksDB.getRocksDB().write(EQCBlockChainRocksDB.getWriteOptions(), writeBatch);
	}
	
	public void batchUpdate(WriteBatch writeBatch) throws RocksDBException {
		EQCBlockChainRocksDB.getRocksDB().write(EQCBlockChainRocksDB.getWriteOptions(), writeBatch);
	}

	public Account getAccount(byte[] id, boolean isLoadInFilter) throws RocksDBException, NoSuchFieldException, IllegalStateException, IOException, ClassNotFoundException, SQLException {
		Account account = null;
		byte[] bytes = null;
			bytes = EQCBlockChainRocksDB.getRocksDB().get(columnFamilyHandles.get(0), id);
			if (bytes != null) {
				account = Account.parseAccount(bytes);
			} else {
				if (accountsMerkleTree.getHeight().compareTo(EQCBlockChainRocksDB.getInstance().getEQCBlockTailHeight()) < 0) {
					account = EQCBlockChainH2.getInstance().getAccountSnapshot(new ID(id), accountsMerkleTree.getHeight());
				}
				if (account == null) {
					bytes = EQCBlockChainRocksDB.get(TABLE.ACCOUNT, id);
					if (bytes != null) {
						account = Account.parseAccount(bytes);
					}
				} 
				if (isLoadInFilter && account != null) {
					saveAccount(account);
				}
			}
		return account;
	}
	
	@Deprecated
	public byte[] getAccountHash(ID id) throws RocksDBException {
		byte[] bytes = null;
			bytes = EQCBlockChainRocksDB.getRocksDB().get(columnFamilyHandles.get(2),
					id.getEQCBits());
			if(bytes == null) {
				bytes = EQCBlockChainRocksDB.get(TABLE.ACCOUNT_HASH, id.getEQCBits());
			}
		return bytes;
	}
	
	public void merge() throws Exception {
		Account account = null;
		WriteBatch writeBatch = null;
		RocksIterator rocksIterator = EQCBlockChainRocksDB.getRocksDB().newIterator(columnFamilyHandles.get(0));
		rocksIterator.seekToFirst();
		while(rocksIterator.isValid()) {
				account = Account.parseAccount(rocksIterator.value());
				writeBatch = new WriteBatch();
				writeBatch.put(EQCBlockChainRocksDB.getTableHandle(TABLE.ACCOUNT), rocksIterator.key(), rocksIterator.value());
//				Log.info(account.toString());
//				Log.info(Util.dumpBytes(account.getAddress().getAddressAI(), 16));
				writeBatch.put(EQCBlockChainRocksDB.getTableHandle(TABLE.ACCOUNT_AI), account.getPassport().getAddressAI(), account.getIDEQCBits());
				writeBatch.put(EQCBlockChainRocksDB.getTableHandle(TABLE.ACCOUNT_HASH), account.getIDEQCBits(), EQCBlockChainRocksDB.getRocksDB().get(columnFamilyHandles.get(2), account.getIDEQCBits()));
				EQCBlockChainRocksDB.getRocksDB().write(EQCBlockChainRocksDB.getWriteOptions(), writeBatch);
//				EQCBlockChainRocksDB.getInstance().saveAccount(account);
				rocksIterator.next();
		}
	}
	
	public void takeSnapshot() throws Exception {
		boolean isSucc = true;
		RocksIterator rocksIterator = EQCBlockChainRocksDB.getRocksDB().newIterator(columnFamilyHandles.get(0));
		rocksIterator.seekToFirst();
		AssetSubchainAccount assetSubchainAccount = (AssetSubchainAccount) Util.DB().getAccount(ID.ONE);
		
		while (rocksIterator.isValid()) {
//				Log.info("Key: " + Util.dumpBytes(rocksIterator.key(), 16) + " Value: " + Util.dumpBytes(rocksIterator.value(), 16));
				Account account = Account.parseAccount(rocksIterator.value());
				if (account.getID()
						.compareTo(assetSubchainAccount.getAssetSubchainHeader().getTotalAccountNumbers()) <= 0) {
					if (!EQCBlockChainH2.getInstance().saveAccountSnapshot(EQCBlockChainRocksDB.getInstance().getAccount(account.getID()), accountsMerkleTree.getHeight())) {
						isSucc = false;
						Log.Error("During takeSnapshot error occur saveAccountSnapshot failed.");
						break;
					}
				}
//				rocksIterator.next();
//				Log.info("Key: " + Util.dumpBytes(rocksIterator.key(), 16) + " Value: " + Util.dumpBytes(rocksIterator.value(), 16));
//				rocksIterator.next();
//				Log.info("Key: " + Util.dumpBytes(rocksIterator.key(), 16) + " Value: " + Util.dumpBytes(rocksIterator.value(), 16));
				rocksIterator.next();
		}
	}
	
	public ID getAddressID(Passport address) throws RocksDBException, NoSuchFieldException, IllegalStateException, IOException {
		ID id = null;
		byte[] bytes = null;
			bytes = EQCBlockChainRocksDB.getRocksDB().get(columnFamilyHandles.get(1), address.getAddressAI());
			if(bytes != null) {
				id = new ID(bytes);
			}
			else {
				bytes = EQCBlockChainRocksDB.get(TABLE.ACCOUNT_AI, address.getAddressAI());
				if(bytes != null) {
						id = new ID(bytes);
						byte[] data = EQCBlockChainRocksDB.get(TABLE.ACCOUNT, bytes);
						Account account = Account.parseAccount(data);
						saveAccount(account);
				}
			}
		return id;
	}

	public Passport getAddress(ID id) throws NoSuchFieldException, IllegalStateException, RocksDBException, IOException, ClassNotFoundException, SQLException {
		return getAccount(id, true).getPassport();
	}

	public PublicKey getPublicKey(ID id) throws NoSuchFieldException, IllegalStateException, RocksDBException, IOException, ClassNotFoundException, SQLException {
		PublicKey publicKey = null;
		if(getAccount(id, true).getPublickey() != null) {
			publicKey = new PublicKey();
			publicKey.setPublicKey(getAccount(id, true).getPublickey().getPublickey());
			publicKey.setID(id);
		}
		return publicKey;
	}

	public void savePublicKey(PublicKey publicKey, ID height) throws RocksDBException, NoSuchFieldException, IllegalStateException, IOException, ClassNotFoundException, SQLException {
		Account account = getAccount(publicKey.getID(), true);
		com.eqchains.blockchain.account.Publickey publickey2 = new com.eqchains.blockchain.account.Publickey();
		publickey2.setPublickey(publicKey.getPublicKey());
		publickey2.setPublickeyCreateHeight(height);
		account.setPublickey(publickey2);
		saveAccount(account);
	}

	public boolean isPublicKeyExists(PublicKey publicKey) throws NoSuchFieldException, IllegalStateException, RocksDBException, IOException, ClassNotFoundException, SQLException {
		boolean isSucc = false;
		Account account = getAccount(publicKey.getID(), true);
		if(account.getPublickey() != null) {
			if(Arrays.equals(account.getPublickey().getPublickey(), publicKey.getPublicKey())) {
				isSucc = true;
			}
		}
		return isSucc;
	}

	public void deletePublicKey(PublicKey publicKey) throws RocksDBException, NoSuchFieldException, IllegalStateException, IOException, ClassNotFoundException, SQLException {
		Account account = getAccount(publicKey.getID(), true);
		account.setPublickey(null);
		saveAccount(account);
	}

	public void clear() throws RocksDBException {
		for (ColumnFamilyHandle columnFamilyHandle : columnFamilyHandles) {
			EQCBlockChainRocksDB.clearTable(columnFamilyHandle);
//			EQCBlockChainRocksDB.dropTable(columnFamilyHandle);
//			// Bug fix for https://github.com/facebook/rocksdb/issues/189 which could cause JVM crash
//			columnFamilyHandle.close();
		}
	}

	/**
	 * @return the columnFamilyHandles
	 */
	public Vector<ColumnFamilyHandle> getColumnFamilyHandles() {
		return columnFamilyHandles;
	}

	/**
	 * @param accountsMerkleTree the accountsMerkleTree to set
	 */
	public void setAccountsMerkleTree(AccountsMerkleTree accountsMerkleTree) {
		this.accountsMerkleTree = accountsMerkleTree;
	}
	
}
