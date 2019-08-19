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
package com.eqchains.persistence;

import java.io.IOException;
import java.math.BigInteger;
import java.sql.SQLException;
import java.util.Vector;

import org.rocksdb.RocksDBException;

import com.eqchains.blockchain.account.Account;
import com.eqchains.blockchain.account.Passport;
import com.eqchains.blockchain.accountsmerkletree.Filter.Mode;
import com.eqchains.blockchain.hive.EQCHive;
import com.eqchains.blockchain.transaction.Transaction;
import com.eqchains.persistence.EQCBlockChainH2.NODETYPE;
import com.eqchains.rpc.Nest;
import com.eqchains.rpc.SignHash;
import com.eqchains.rpc.TransactionIndex;
import com.eqchains.rpc.TransactionIndexList;
import com.eqchains.rpc.TransactionList;
import com.eqchains.rpc.Balance;
import com.eqchains.rpc.IPList;
import com.eqchains.rpc.MaxNonce;
import com.eqchains.util.ID;

/**
 * @author Xun Wang
 * @date Oct 2, 2018
 * @email 10509759@qq.com
 */
public interface EQCBlockChain {
	
	// Account relevant interface for H2, avro(optional).
	public boolean saveAccount(Account account) throws Exception;
	
	/**
	 * Get Account according to it's ID.
	 * <p>
	 * @param id
	 * @return
	 * @throws Exception
	 */
	public Account getAccount(ID id) throws Exception;
	
	public Account getAccount(byte[] addressAI) throws Exception;
	
	public boolean deleteAccount(ID id) throws Exception;
	
	@Deprecated
	public void deleteAccountFromTo(ID fromID, ID toID) throws Exception;
	
	// Block relevant interface for for avro, H2(optional).
	public boolean saveEQCHive(EQCHive eqcHive) throws Exception;
	
	public EQCHive getEQCHive(ID height, boolean isSegwit) throws Exception;
	
	public boolean deleteEQCHive(ID height) throws Exception;
	
	@Deprecated
	public void deleteEQCHiveFromTo(ID fromHeight, ID toHeight) throws Exception;
	
	// TransactionPool relevant interface for H2, avro.
	public boolean isTransactionExistsInPool(Transaction transaction) throws SQLException;
	
	public boolean isTransactionExistsInPool(TransactionIndex transactionIndex) throws SQLException;
	
	public boolean saveTransactionInPool(Transaction transaction) throws SQLException;
	
	public boolean deleteTransactionInPool(Transaction transaction) throws SQLException;
	
	public boolean deleteTransactionsInPool(EQCHive eqcHive) throws SQLException, ClassNotFoundException, RocksDBException, Exception;
	
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
	
	public TransactionIndexList getTransactionIndexListInPool(long previousSyncTime, long currentSyncTime) throws SQLException, Exception;
	
	public TransactionList getTransactionListInPool(TransactionIndexList transactionIndexList) throws SQLException, Exception;
	
	// For sign and verify Transaction need use relevant TxIn's EQC block header's hash via this function to get it from xxx.EQC.
	public byte[] getEQCHeaderHash(ID height) throws Exception;
	
	public byte[] getEQCHeaderBuddyHash(ID height, ID currentTailHeight) throws Exception;
	
	public ID getEQCBlockTailHeight() throws Exception;
	
	public boolean saveEQCBlockTailHeight(ID height) throws Exception;
	
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
	
	// Take Account's snapshot
	public Account getAccountSnapshot(ID accountID, ID height) throws SQLException, Exception;
	
	public Account getAccountSnapshot(byte[] addressAI, ID height) throws SQLException, Exception;
	
	public boolean saveAccountSnapshot(Account account, ID height) throws SQLException, Exception;
	
	public boolean deleteAccountSnapshotFrom(ID height, boolean isForward) throws SQLException, Exception;
	
	public boolean merge(Mode mode) throws SQLException, Exception;
	
	public boolean takeSnapshot(Mode mode, ID height) throws SQLException, Exception;
	
	// Filter relevant interface for H2
	public boolean saveAccount(Account account, Mode mode) throws Exception;
	
	public Account getAccount(ID id, Mode mode) throws Exception;
	
	public Account getAccount(byte[] addressAI, Mode mode) throws Exception;
	
	public boolean clear(Mode mode) throws Exception;
	
}
