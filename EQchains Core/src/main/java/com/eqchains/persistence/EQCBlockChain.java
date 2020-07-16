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
package com.eqchains.persistence;

import java.sql.SQLException;
import java.util.Vector;

import com.eqchains.blockchain.accountsmerkletree.Filter.Mode;
import com.eqchains.blockchain.hive.EQCHive;
import com.eqchains.blockchain.passport.Lock;
import com.eqchains.blockchain.passport.Passport;
import com.eqchains.blockchain.subchain.EQCSubchain;
import com.eqchains.blockchain.transaction.Transaction;
import com.eqchains.persistence.EQCBlockChainH2.NODETYPE;
import com.eqchains.rpc.Balance;
import com.eqchains.rpc.IPList;
import com.eqchains.rpc.MaxNonce;
import com.eqchains.rpc.Nest;
import com.eqchains.rpc.SignHash;
import com.eqchains.rpc.TransactionIndex;
import com.eqchains.rpc.TransactionIndexList;
import com.eqchains.rpc.TransactionList;
import com.eqchains.util.ID;

/**
 * @author Xun Wang
 * @date Oct 2, 2018
 * @email 10509759@qq.com
 */
public interface EQCBlockChain<T> {

	// Lock relevant interface for H2, avro(optional).
	public boolean saveLock(Lock lock, Mode mode) throws Exception;

	/**
	 * Get Lock from Global state DB according to it's ID which is the latest
	 * status.
	 * <p>
	 * 
	 * @param id
	 * @return
	 * @throws Exception
	 */
	public Lock getLock(ID id, Mode mode) throws Exception;

	/**
	 * Get Lock from the specific height if which doesn't exists will return null.
	 * If the height equal to current tail's height will retrieve the Lock from
	 * LOCK_GLOBAL table otherwise will try retrieve it from Lock snapshot table.
	 * <p>
	 * 
	 * @param id
	 * @param height
	 * @return
	 * @throws Exception
	 */
	public Lock getLock(ID id, ID height) throws Exception;

	public Lock getLock(String readableLock, Mode mode) throws Exception;

	public boolean deleteLock(ID id, Mode mode) throws Exception;

	public boolean clearLock(Mode mode) throws Exception;

	// Passport relevant interface for H2, avro(optional).
	public boolean savePassport(Passport account, Mode mode) throws Exception;

	/**
	 * Get Passport from Global state DB according to it's ID which is the latest
	 * status.
	 * <p>
	 * 
	 * @param id
	 * @return
	 * @throws Exception
	 */
	public Passport getPassport(ID id, Mode mode) throws Exception;

	/**
	 * Get Passport from the specific height if which doesn't exists will return
	 * null. If the height equal to current tail's height will retrieve the Account
	 * from ACCOUNT_GLOBAL table otherwise will try retrieve it from Account
	 * snapshot table.
	 * <p>
	 * 
	 * @param id
	 * @param height
	 * @return
	 * @throws Exception
	 */
	public Passport getPassport(ID id, ID height) throws Exception;

	public Passport getPassport(byte[] addressAI, Mode mode) throws Exception;

	public boolean deletePassport(ID id, Mode mode) throws Exception;

	public boolean clearPassport(Mode mode) throws Exception;

	// relevant interface for for avro, H2(optional).
	public boolean isEQCHiveExists(ID height) throws Exception;

	public boolean saveEQCHive(EQCHive eqcHive) throws Exception;

	public EQCHive getEQCHive(ID height, boolean isSegwit) throws Exception;

	public boolean deleteEQCHive(ID height) throws Exception;

	// TransactionPool relevant interface for H2, avro.
	public boolean isTransactionExistsInPool(Transaction transaction) throws SQLException;

	public boolean isTransactionExistsInPool(TransactionIndex transactionIndex) throws SQLException;

	public boolean saveTransactionInPool(Transaction transaction) throws SQLException;

	public boolean deleteTransactionInPool(Transaction transaction) throws SQLException;

	public boolean deleteTransactionsInPool(EQCHive eqcHive) throws SQLException, ClassNotFoundException, Exception;

	public Vector<Transaction> getTransactionListInPool() throws SQLException, Exception;

	public Vector<Transaction> getPendingTransactionListInPool(Nest nest) throws SQLException, Exception;

	@Deprecated
	public boolean isTransactionMaxNonceExists(Nest nest) throws SQLException;

	@Deprecated
	public boolean saveTransactionMaxNonce(Nest nest, MaxNonce maxNonce) throws SQLException;

	public MaxNonce getTransactionMaxNonce(Nest nest) throws SQLException, Exception;

	@Deprecated
	public boolean deleteTransactionMaxNonce(Nest nest) throws SQLException;

	public Balance getBalance(Nest nest) throws SQLException, Exception;

	public TransactionIndexList getTransactionIndexListInPool(long previousSyncTime, long currentSyncTime)
			throws SQLException, Exception;

	public TransactionList<byte[]> getTransactionListInPool(TransactionIndexList<byte[]> transactionIndexList)
			throws SQLException, Exception;

	// For sign and verify Transaction need use relevant TxIn's EQC block header's
	// hash via this function to get it from xxx.EQC.
	public byte[] getEQCHeaderHash(ID height) throws Exception;

	public byte[] getEQCHeaderBuddyHash(ID height, ID currentTailHeight) throws Exception;

	public ID getEQCBlockTailHeight() throws Exception;

	public boolean saveEQCBlockTailHeight(ID height) throws Exception;

	@Deprecated
	public ID getTotalAccountNumbers(ID height) throws Exception;

	public SignHash getSignHash(ID id) throws Exception;

	// MinerNetwork and FullNodeNetwork relevant interface for H2, avro.
	public boolean isIPExists(String ip, NODETYPE nodeType) throws SQLException;

	public boolean isMinerExists(String ip) throws SQLException, Exception;

	public boolean saveMiner(String ip) throws SQLException, Exception;

	public boolean deleteMiner(String ip) throws SQLException, Exception;

	public boolean saveMinerSyncTime(String ip, long syncTime) throws SQLException, Exception;

	public long getMinerSyncTime(String ip) throws SQLException, Exception;

	public boolean saveIPCounter(String ip, int counter) throws SQLException, Exception;

	public int getIPCounter(String ip) throws SQLException, Exception;

	public IPList getMinerList() throws SQLException, Exception;

	public boolean isFullNodeExists(String ip) throws SQLException, Exception;

	public boolean saveFullNode(String ip) throws SQLException, Exception;

	public boolean deleteFullNode(String ip) throws SQLException, Exception;

	public IPList getFullNodeList() throws SQLException, Exception;

	// Release the relevant database resource
	public boolean close() throws SQLException, Exception;

	// Clear the relevant database table
	public boolean dropTable() throws Exception, SQLException;

	// Take Lock's snapshot
	public Passport getLockSnapshot(ID lockID, ID height) throws SQLException, Exception;

	public Passport getLockSnapshot(byte[] addressAI, ID height) throws SQLException, Exception;

	public boolean saveLockSnapshot(Lock lock, ID height) throws SQLException, Exception;

	public boolean deleteLockSnapshotFrom(ID height, boolean isForward) throws SQLException, Exception;

	/**
	 * After verify the new block's state. Merge the new Lock states from Miner
	 * or Valid to Global
	 * 
	 * @param mode
	 * @return
	 * @throws SQLException
	 * @throws Exception
	 */
	public boolean mergeLock(Mode mode) throws SQLException, Exception;

	/**
	 * After verify the new block's state. Take the changed Lock's snapshot from
	 * Miner or Valid.
	 * 
	 * @param mode
	 * @param height
	 * @return
	 * @throws SQLException
	 * @throws Exception
	 */
	public boolean takeLockSnapshot(Mode mode, ID height) throws SQLException, Exception;

	// Take Passport's snapshot
	public Passport getPassportSnapshot(ID passportID, ID height) throws SQLException, Exception;

	public Passport getPassportSnapshot(byte[] addressAI, ID height) throws SQLException, Exception;

	public boolean savePassportSnapshot(Passport passport, ID height) throws SQLException, Exception;

	public boolean deletePassportSnapshotFrom(ID height, boolean isForward) throws SQLException, Exception;

	/**
	 * After verify the new block's state. Merge the new Passport states from Miner
	 * or Valid to Global
	 * 
	 * @param mode
	 * @return
	 * @throws SQLException
	 * @throws Exception
	 */
	public boolean mergePassport(Mode mode) throws SQLException, Exception;

	/**
	 * After verify the new block's state. Take the changed Passport's snapshot from
	 * Miner or Valid.
	 * 
	 * @param mode
	 * @param height
	 * @return
	 * @throws SQLException
	 * @throws Exception
	 */
	public boolean takePassportSnapshot(Mode mode, ID height) throws SQLException, Exception;

	// Audit layer relevant interface for H2
	public ID isTransactionExists(Transaction transaction, Mode mode) throws Exception;

	public boolean saveTransaction(Transaction transaction, ID height, ID index, ID sn, Mode mode) throws Exception;

	public boolean saveTransactions(EQCSubchain eqcSubchain, ID height, Mode mode) throws Exception;

	public boolean deleteTransaction(Transaction transaction, Mode mode) throws Exception;

	public boolean deleteTransactionFrom(ID height, Mode mode) throws Exception;

	public boolean saveTransactions(EQCHive eqcHive, Mode mode) throws Exception;

	public ID getTotalTransactionNumbers(ID height, ID assetID, Mode mode) throws Exception;

}
