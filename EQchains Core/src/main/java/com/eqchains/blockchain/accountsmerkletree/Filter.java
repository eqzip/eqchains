/**
 * EQchains core - EQchains Federation's EQchains core library
 * @copyright 2018-present EQchains Federation All rights reserved...
 * Copyright of all works released by EQchains Federation or jointly released by
 * EQchains Federation with cooperative partners are owned by EQchains Federation
 * and entitled to protection available from copyright law by country as well as
 * international conventions.
 * Attribution — You must give appropriate credit, provide a link to the license.
 * Non Commercial — You may not use the material for commercial purposes.
 * No Derivatives — If you remix, transform, or build upon the material, you may
 * not distribute the modified material.
 * For any use of above stated content of copyright beyond the scope of fair use
 * or without prior written permission, EQchains Federation reserves all rights to
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
import java.util.Objects;
import java.util.Vector;

import com.eqchains.blockchain.hive.EQCHive;
import com.eqchains.blockchain.passport.AssetSubchainPassport;
import com.eqchains.blockchain.passport.Lock;
import com.eqchains.blockchain.passport.Passport;
import com.eqchains.blockchain.transaction.CompressedPublickey;
import com.eqchains.persistence.EQCBlockChainH2;
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
	private PassportsMerkleTree accountsMerkleTree;
	private Mode mode;

	public enum Mode {
		GLOBAL, MINING, VALID
	}

	public Filter(Mode mode) throws ClassNotFoundException, SQLException, Exception {
		this.mode = mode;
		// In case before close the app crashed or abnormal interruption so here just
		// clear the table
		clear();
	}

//	public Account getAccount(ID id, boolean isLoadInFilter) throws Exception {
//		// Here maybe exists some bugs need do more test
//		// Test find during verify block due to before Transaction.update the total
//		// account numbers will not increase so the new account's id will exceed the
//		// total account numbers
////		EQCType.assertNotBigger(id, accountsMerkleTree.getTotalAccountNumbers());
//		return getAccount(id, isLoadInFilter);
//	}

	/**
	 * Search the Account in Filter table
	 * For security issue only support search address via AddressAI
	 * <p>
	 * 
	 * @param key
	 * @return
	 * @throws Exception 
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 */
	public boolean isAccountExists(Lock key) throws ClassNotFoundException, SQLException, Exception {
		boolean isSucc = false;
		// For security issue only support search address via AddressAI
		if (Util.DB().getPassport(key.getAddressAI(), mode) != null) {
			isSucc = true;
		}
		return isSucc;
	}

	public void savePassport(Passport account) throws ClassNotFoundException, SQLException, Exception {
		Util.DB().savePassport(account, mode);
	}

	public Passport getPassport(ID id, boolean isFiltering) throws Exception {
		Passport account = null;
		// Check if Account already loading in filter
		if(isFiltering) {
			account = Util.DB().getPassport(id, mode);
		}
		if (account == null)  {
			// The first time loading account need loading the previous block's snapshot but
			// doesn't include No.0 EQCHive
			if (accountsMerkleTree.getHeight().compareTo(ID.ZERO) > 0) {
				ID tailHeight = Util.DB().getEQCBlockTailHeight();
				if (accountsMerkleTree.getHeight().isNextID(tailHeight)) {
					account = Util.DB().getPassport(id, Mode.GLOBAL);
					if(!(account != null && account.getCreateHeight().compareTo(accountsMerkleTree.getHeight()) < 0 && account.getLockCreateHeight().compareTo(accountsMerkleTree.getHeight()) < 0 && account.getID().compareTo(accountsMerkleTree.getPreviousTotalAccountNumbers()) <= 0)) {
						Log.Error("Account exists but doesn't valid" + account);
						account = null;
					}
				} else if (accountsMerkleTree.getHeight().compareTo(tailHeight) <= 0) {
					// Load relevant Account from snapshot
					account = EQCBlockChainH2.getInstance().getPassportSnapshot(new ID(id),
							accountsMerkleTree.getHeight().getPreviousID());
				} else {
					throw new IllegalStateException("Wrong height " + accountsMerkleTree.getHeight() + " tail height "
							+ Util.DB().getEQCBlockTailHeight());
				}
			} else {
				account = EQCBlockChainH2.getInstance().getPassportSnapshot(new ID(id), ID.ZERO);
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
//			if (account == null) {
//				throw new IllegalStateException("Account No. " + new ID(id) + " doesn't exists.");
//			}
//			if (isLoadInFilter) {
//				saveAccount(account);
//			}
		}
		return account;
	}
	
	public Passport getPassport(Lock key, boolean isFiltering) throws Exception {
		Passport account = null;
		// Check if Account already loading in filter
		if(isFiltering) {
			account = Util.DB().getPassport(key.getAddressAI(), mode);
		}
		if (account == null)  {
			// The first time loading account need loading the previous block's snapshot but
			// doesn't include No.0 EQCHive
			if (accountsMerkleTree.getHeight().compareTo(ID.ZERO) > 0) {
				ID tailHeight = Util.DB().getEQCBlockTailHeight();
				if (accountsMerkleTree.getHeight().isNextID(tailHeight)) {
					account = Util.DB().getPassport(key.getAddressAI(), Mode.GLOBAL);
					if(!(account != null && account.getCreateHeight().compareTo(accountsMerkleTree.getHeight()) < 0 && account.getLockCreateHeight().compareTo(accountsMerkleTree.getHeight()) < 0 && account.getID().compareTo(accountsMerkleTree.getPreviousTotalAccountNumbers()) <= 0)) {
						Log.Error("Account exists but doesn't valid" + account);
						account = null;
					}
				} else if (accountsMerkleTree.getHeight().compareTo(tailHeight) <= 0) {
					// Load relevant Account from snapshot
					account = EQCBlockChainH2.getInstance().getPassportSnapshot(key.getAddressAI(),
							accountsMerkleTree.getHeight().getPreviousID());
				} else {
					throw new IllegalStateException("Wrong height " + accountsMerkleTree.getHeight() + " tail height "
							+ Util.DB().getEQCBlockTailHeight());
				}
			} else {
				account = EQCBlockChainH2.getInstance().getPassportSnapshot(key.getAddressAI(), ID.ZERO);
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
//			if (account == null) {
//				throw new IllegalStateException("Account No. " + new ID(id) + " doesn't exists.");
//			}
//			if (isLoadInFilter) {
//				saveAccount(account);
//			}
		}
		return account;
	}

	public void merge() throws Exception {
		Util.DB().mergePassport(mode);
	}

	public void takeSnapshot() throws Exception {
		Util.DB().takePassportSnapshot(mode, accountsMerkleTree.getHeight());
	}

//	/**
//	 * Use this to fill in the relevant Transaction's TxIn or TxOut's ID which can't
//	 * be null
//	 * 
//	 * @param address
//	 * @return
//	 * @throws Exception 
//	 * @throws SQLException 
//	 * @throws ClassNotFoundException 
//	 */
//	public ID getPassportID(Passport passport)
//			throws ClassNotFoundException, SQLException, Exception {
//		ID id = null;
//		Account account = null;
//		account = Util.DB().getAccount(passport.getAddressAI(), mode);
//		if (account != null) {
//			id = account.getID();
//		} else {
//			account = Util.DB().getAccount(passport.getAddressAI());
//			if (account != null) {
//				id = account.getID();
//			}
//		}
//		Objects.requireNonNull(id);
//		return id;
//	}

	public void clear() throws ClassNotFoundException, SQLException, Exception {
		Util.DB().clearPassport(mode);
	}

	/**
	 * @param accountsMerkleTree the accountsMerkleTree to set
	 */
	public void setAccountsMerkleTree(PassportsMerkleTree accountsMerkleTree) {
		this.accountsMerkleTree = accountsMerkleTree;
	}

	/**
	 * @return the mode
	 */
	public Mode getMode() {
		return mode;
	}

}
