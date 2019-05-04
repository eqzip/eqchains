/**
 * EQCoin core - EQCOIN Foundation's EQCoin core library
 * @copyright 2018-present EQCOIN Foundation All rights reserved...
 * Copyright of all works released by EQCOIN Foundation or jointly released by
 * EQCOIN Foundation with cooperative partners are owned by EQCOIN Foundation
 * and entitled to protection available from copyright law by country as well as
 * international conventions.
 * Attribution — You must give appropriate credit, provide a link to the license.
 * Non Commercial — You may not use the material for commercial purposes.
 * No Derivatives — If you remix, transform, or build upon the material, you may
 * not distribute the modified material.
 * For any use of above stated content of copyright beyond the scope of fair use
 * or without prior written permission, EQCOIN Foundation reserves all rights to
 * take any legal action and pursue any right or remedy available under applicable
 * law.
 * https://www.eqzip.com
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

import java.math.BigInteger;
import java.util.Vector;

import com.eqchains.blockchain.transaction.Address;
import com.eqchains.blockchain.transaction.Transaction;
import com.eqchains.util.ID;

/**
 * @author Xun Wang
 * @date Oct 2, 2018
 * @email 10509759@qq.com
 */
public interface EQCBlockChain {
	
	// Address relevant interface for H2, avro(optional).
	public ID getAddressID(Address address);
	
	public Address getAddress(ID serialNumber);
	
//	public boolean appendAddress(Address address, SerialNumber address_create_height);
	
	public boolean isAddressExists(Address address);
	
//	public boolean deleteAddress(Address address);
	
//	public boolean deleteAddressFromHeight(SerialNumber height);
	
//	public SerialNumber getLastAddressSerialNumber();
	
//	public long getAddressTotalNumbers();
	
//	public boolean addAllAddress(EQCBlock eqcBlock);
	
	// Account relevant interface for H2, avro(optional).
//	public Vector<Account> getAllAccounts(SerialNumber height);
	
//	public Vector<Account> getAccounts(SerialNumber begin, SerialNumber end, SerialNumber height);
	
//	public ID getTotalAccountNumber(ID height);
	
	public boolean saveAccount(Account account);
	
	public Account getAccount(ID serialNumber);
	
	public Account getAccount(byte[] addressAI);
	
	public boolean deleteAccount(ID serialNumber);
	
	// Public key relevant interface for H2, avro(optional).
//	public SerialNumber getPublicKeySerialNumber(Address address);
	
	public PublicKey getPublicKey(ID serialNumber);
	
	public boolean savePublicKey(PublicKey publicKey, ID height);
	
	public boolean isPublicKeyExists(PublicKey publicKey);
	
	public boolean deletePublicKey(PublicKey publicKey);
	
//	public boolean deletePublicKeyFromHeight(SerialNumber height);
//	
//	public SerialNumber getLastPublicKeySerialNumber();
//	
//	public long getPublicKeyTotalNumbers();
//	
//	public boolean addAllPublicKeys(EQCBlock eqcBlock);
	
	// Block relevant interface for for avro, H2(optional).
	public EQCBlock getEQCBlock(ID height, boolean isSegwit);
	
	public boolean isEQCBlockExists(ID height);
	
//	public boolean isEQCBlockExists(EQCBlock eqcBlock);
	
	public boolean saveEQCBlock(EQCBlock eqcBlock);
	
	public boolean deleteEQCBlock(ID height);
	
//	public EQCHeader getEQCHeader(SerialNumber height);
	
	// Transactions relevant interface for H2, avro.
	public byte[] getTransactionsHash(ID height);
	
	// Balance relevant interface
//	public long getBalance(Address address);
//	
//	public long getBalance(Address address, SerialNumber height);
	
	// TransactionPool relevant interface for H2, avro.
	public Vector<Transaction> getTransactionListInPool();
	
//	public Vector<Transaction> getTransactionList(Address address, SerialNumber height);
	
	public boolean addTransactionInPool(Transaction transaction);
	
	public boolean deleteTransactionInPool(Transaction transaction);
	
	public boolean deleteTransactionsInPool(EQCBlock eqcBlock);
	
	public boolean isTransactionExistsInPool(Transaction transaction);
	
	// Transaction relevant interface for H2, avro. Save all the Transaction record in the EQC block chain.
//	public Vector<Transaction> getTransactionList(Address address);
//	
//	public Vector<Transaction> getTransactionList(Address address, SerialNumber height);
//	
//	public boolean addTransaction(Transaction transaction, SerialNumber height, int trans_sn);
//	
//	public boolean addAllTransactions(EQCBlock eqcBlock);
//	
//	public boolean deleteTransactionFromHeight(SerialNumber height);
//	
//	public boolean isTransactionExists(Transaction transaction);
	
	// Get the TxIn's Address's EQC block height which record the TxIn's Address.
//	public SerialNumber getTxInHeight(Address txInAddress);
	
	// For sign and verify Transaction need use relevant TxIn's EQC block header's hash via this function to get it from xxx.EQC.
	public byte[] getEQCHeaderHash(ID height);
	
//	public int getTransactionNumbersIn24hours(Address address, SerialNumber currentHeight);
	
//	public byte[] getTxInBlockHeaderHash(Address txInAddress);
//	
//	public boolean addTxInBlockHeaderHash(byte[] hash, SerialNumber addressSerialNumber, SerialNumber height);
	
	public ID getEQCBlockTailHeight();
	
	public boolean saveEQCBlockTailHeight(ID height);
	
	public ID getTotalAccountNumbers(ID height);
	
	public boolean saveTotalAccountNumbers(ID numbers);
	
	// Signature relevant interface for H2, avro.
//	public boolean isSignatureExists(byte[] signature);
	
//	public boolean appendSignature(SerialNumber height, int trans_sn, SerialNumber txin_sn, byte[] signature);
	
//	public boolean addAllSignatures(EQCBlock eqcBlock);
	
	// Account relevant interface for H2, avro.
//	public long getBalanceFromAccount(Address address);
	
//	public boolean updateBalanceInAccount(Address address, long balance);
	
//	public int getNonce(Address address);
	
//	public boolean updateNonce(Address address, int nonce);
	
	// Release the relevant database resource
	public boolean close();
	// Clear the relevant database table
	public void dropTable();
	
	// Take Account's snapshot
	public Account getAccountSnapshot(ID accountID, ID height);
	
	public boolean saveAccountSnapshot(Account account, ID height);
	
	public boolean deleteAccountSnapshot(ID height, boolean isForward);
	
}
