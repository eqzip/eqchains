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
package com.eqchains.blockchain.transaction;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Vector;

import com.eqchains.blockchain.accountsmerkletree.PassportsMerkleTree;
import com.eqchains.blockchain.passport.Asset;
import com.eqchains.blockchain.passport.AssetPassport;
import com.eqchains.blockchain.passport.CoinAsset;
import com.eqchains.blockchain.passport.Lock;
import com.eqchains.blockchain.passport.Passport;
import com.eqchains.blockchain.passport.Lock.LockShape;
import com.eqchains.blockchain.subchain.EQCSubchain;
import com.eqchains.blockchain.subchain.EQcoinSubchain;
import com.eqchains.persistence.EQCBlockChainH2;
import com.eqchains.persistence.EQCBlockChainH2.TRANSACTION_OP;
import com.eqchains.serialization.EQCType;
import com.eqchains.util.ID;
import com.eqchains.util.Log;
import com.eqchains.util.Util;
import com.eqchains.util.Util.AddressTool;
import com.eqchains.util.Util.AddressTool.AddressType;

/**
 * @author Xun Wang
 * @date Mar 21, 2019
 * @email 10509759@qq.com
 */
public class CoinbaseTransaction extends TransferTransaction {
	public final static int REWARD_NUMBERS = 3;
	
	private void init() {
		txIn = null;
		solo = SOLO;
	}
	
	public CoinbaseTransaction() {
		super(TransactionType.COINBASE);
		init();
	}

	public CoinbaseTransaction(byte[] bytes, Lock.LockShape addressShape)
			throws NoSuchFieldException, IOException, IllegalStateException {
		super(TransactionType.COINBASE);
		EQCType.assertNotNull(bytes);
		init();
		ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		parseHeader(is, addressShape);
		parseBody(is, addressShape);
		EQCType.assertNoRedundantData(is);
	}
	
	public CoinbaseTransaction(ResultSet resultSet)
			throws NoSuchFieldException, IOException, IllegalStateException, SQLException {
		super(TransactionType.COINBASE);
		Objects.requireNonNull(resultSet);
		init();
		// Parse Header
		solo = SOLO;
		transactionType = TransactionType.COINBASE;
		// Parse Body without nonce
		while(resultSet.next()) {
			if(resultSet.getByte("op") == TRANSACTION_OP.TXOUT.ordinal()) {
				TxOut txOut = getTxOut(ID.valueOf(resultSet.getLong("id")));
				if(txOut != null) {
					txOut.setValue(resultSet.getLong("value"));
				}
				else {
					txOut = new TxOut();
					Lock key = new Lock();
					key.setID(ID.valueOf(resultSet.getLong("id")));
					txOut.setKey(key);
					txOut.setValue(resultSet.getLong("value"));
					addTxOut(txOut);
				}
			}
			else if(resultSet.getByte("op") == TRANSACTION_OP.PASSPORT.ordinal()) {
				TxOut txOut = getTxOut(ID.valueOf(resultSet.getLong("id")));
				if(txOut != null) {
					txOut.setNew(true);
					txOut.getKey().setReadableLock(AddressTool.AIToAddress(resultSet.getBytes("object")));
				}
				else {
					txOut = new TxOut();
					txOut.setNew(true);
					Lock key = new Lock();
					key.setID(ID.valueOf(resultSet.getLong("id")));
					key.setReadableLock(AddressTool.AIToAddress(resultSet.getBytes("object")));
					txOut.setKey(key);
					addTxOut(txOut);
				}
			} else {
				throw new IllegalStateException("Invalid CoinBase Transaction OP type");
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.eqzip.eqcoin.blockchain.Transaction#getBytes(com.eqzip.eqcoin.util.Util.
	 * AddressShape)
	 */
	@Override
	public byte[] getBytes(Lock.LockShape addressShape) {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			// Serialization Header
			os.write(getHeaderBytes(addressShape));
			// Serialization Body
			os.write(getBodyBytes(addressShape));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return os.toByteArray();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.eqzip.eqcoin.blockchain.Transaction#getBin(com.eqzip.eqcoin.util.Util.
	 * AddressShape)
	 */
	@Override
	public byte[] getBin(Lock.LockShape addressShape) {
		return EQCType.bytesToBIN(getBytes(addressShape));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.eqzip.eqcoin.blockchain.Transaction#isValid(com.eqzip.eqcoin.blockchain.
	 * AccountsMerkleTree)
	 */
	@Override
	public boolean isValid(PassportsMerkleTree accountsMerkleTree, LockShape addressShape) throws NoSuchFieldException, IllegalStateException, IOException, Exception {
		if(!isSanity(addressShape)) {
			return false;
		}
		if(!nonce.equals(accountsMerkleTree.getHeight().getNextID())) {
			return false;
		}
		if (accountsMerkleTree.getHeight().compareTo(Util.getMaxCoinbaseHeight(accountsMerkleTree.getHeight())) < 0) {
			if(!txOutList.get(0).getKey().getId().equals(ID.ONE)) {
				return false;
			}
			if(!txOutList.get(1).getKey().getId().equals(ID.TWO)) {
				return false;
			}
			if(txOutList.get(2).getKey().getId().equals(ID.ONE) || txOutList.get(2).getKey().getId().equals(ID.TWO)) {
				Log.Error("No.3 Coinbase Reward's ID shouldn't equal to 1 or 2");
				return false;
			}
			if(txOutList.get(0).getValue() != Util.EQC_FOUNDATION_COINBASE_REWARD) {
				return false;
			}
			if(txOutList.get(1).getValue() != Util.EQZIP_COINBASE_REWARD) {
				return false;
			}
			if(txOutList.get(2).getValue() != Util.MINER_COINBASE_REWARD) {
				return false;
			}
		} else {
			throw new IllegalStateException("After MaxCoinbaseHeight haven't any CoinBase reward");
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see com.eqzip.eqcoin.blockchain.transaction.Transaction#isTxOutNumberValid()
	 */
	@Override
	public boolean isTxOutNumberValid() {
		return txOutList.size() == REWARD_NUMBERS;
	}

	@Override
	public boolean isSanity(LockShape addressShape) {
		if(transactionType == null || txIn != null || nonce == null || txOutList == null) {
			Log.Error("Some member variables is null");
			return false;
		}
		if (transactionType != TransactionType.COINBASE) {
			Log.Error("Transaction type:" +  transactionType + " isn't COINBASE");
			return false;
		}
		if(!getAssetID().equals(Asset.EQCOIN)) {
			Log.Error("AssetID:" + getAssetID() + " isn't EQCOIN");
			return false;
		}
		if (!isTxOutNumberValid()) {
			Log.Error("Total TxOut numbers is invalid");
			return false;
		}
		if(!nonce.isSanity()) {
			Log.Error("Nonce is invalid");
			return false;
		}
		for(TxOut txOut : txOutList) {
			if(!txOut.isSanity(addressShape)) {
				Log.Error("TxOut's sanity test failed");
				return false;
			}
			if(txOut.getKey().getAddressType() != AddressType.T1 && txOut.getKey().getAddressType() != AddressType.T2) {
				Log.Error("TxOut's AddressType is invalid");
				return false;
			}
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see com.eqzip.eqcoin.blockchain.Transaction#prepare(com.eqzip.eqcoin.blockchain.AccountsMerkleTree, com.eqzip.eqcoin.util.ID)
	 */
	@Override
	public void prepareAccounting(PassportsMerkleTree accountsMerkleTree, ID initID) throws NoSuchFieldException, IllegalStateException, IOException, Exception {
		// Update TxOut's Address' isNew status if need
		for (TxOut txOut : txOutList) {
			if (!accountsMerkleTree.isPassportExists(txOut.getKey(), true)) {
				txOut.getKey().setID(initID);
				txOut.setNew(true);
				initID = initID.getNextID();
			} else {
				// For security issue need retrieve and fill in every Address' ID according to it's AddressAI
				txOut.getKey().setID(accountsMerkleTree.getPassport(txOut.getKey(), true).getId());
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see com.eqchains.blockchain.transaction.TransferTransaction#prepareVerify(com.eqchains.blockchain.accountsmerkletree.AccountsMerkleTree, com.eqchains.blockchain.subchain.EQCSubchain)
	 */
	@Override
	public void prepareVerify(PassportsMerkleTree accountsMerkleTree, EQCSubchain eqcSubchain) throws Exception {
		EQcoinSubchain eQcoinSubchain = (EQcoinSubchain) eqcSubchain;
		Passport account = null;
		// Update TxOut's Address' isNew status if need
		for (TxOut txOut : txOutList) {
			account = accountsMerkleTree.getPassport(txOut.getKey().getId(), true);
			if (account == null) {
				                                                                              // Maybe here need reflacter
				txOut.getKey().setReadableLock(eQcoinSubchain.getPassport(txOut.getKey().getId()).getReadableLock());
				txOut.setNew(true);
			} else {
				// For security issue need retrieve and fill in every Address' AddressAI according to it's ID
				txOut.getKey().setReadableLock(account.getKey().getReadableLock());
			}
		}
	}

	public void update(PassportsMerkleTree accountsMerkleTree) throws Exception {
		Passport account = null;
		// Update current Transaction's TxOut Account
		for (TxOut txOut : txOutList) {
			if (txOut.isNew()) {
				account = new AssetPassport();
				account.setCreateHeight(accountsMerkleTree.getHeight());
				account.setVersion(ID.ZERO);
				account.setVersionUpdateHeight(accountsMerkleTree.getHeight());
				account.setKey(txOut.getKey());
				account.setLockCreateHeight(accountsMerkleTree.getHeight());
				Asset asset = new CoinAsset();
				asset.setVersion(ID.ZERO);
				asset.setVersionUpdateHeight(accountsMerkleTree.getHeight());
				asset.setAssetID(Asset.EQCOIN);
				asset.setCreateHeight(accountsMerkleTree.getHeight());
				asset.deposit(ID.ZERO);
				asset.setBalanceUpdateHeight(accountsMerkleTree.getHeight());
				asset.setNonce(ID.ZERO);
				asset.setNonceUpdateHeight(accountsMerkleTree.getHeight());
				account.setAsset(asset);
				Log.info("increaseTotalAccountNumbers");
				accountsMerkleTree.increaseTotalPassportNumbers();
			} else {
				account = accountsMerkleTree.getPassport(txOut.getKey().getId(), true);
			}
			account.getAsset(getAssetID()).deposit(new ID(txOut.getValue()));
			account.getAsset(getAssetID()).setBalanceUpdateHeight(accountsMerkleTree.getHeight());
			account.setUpdateHeight(accountsMerkleTree.getHeight());
			accountsMerkleTree.savePassport(account);
		}
	}
	
	public String toInnerJson() {
		return

		"\"CoinbaseTransaction\":" + "\n{\n" + TxIn.coinBase() + ",\n"
		+ "\"TxOutList\":" + "\n{\n" + "\"Size\":" + "\"" + txOutList.size() + "\"" + ",\n"
		+ "\"List\":" + "\n" + getTxOutString() + ",\n" 
		+ "\"Nonce\":" + "\"" + nonce + "\"" + 
		"\n}";
	}

	/* (non-Javadoc)
	 * @see com.eqzip.eqcoin.blockchain.transaction.Transaction#getBillingSize()
	 */
	@Override
	public int getBillingSize() {
		int size = 0;

		// Transaction's ID format's size which storage in the EQC Blockchain
		size += getBin(LockShape.ID).length;
		Log.info("ID size: " + size);

		// Transaction's AddressList size which storage the new Address
		for (TxOut txOut : txOutList) {
			if (txOut.isNew()) {
				size += txOut.getKey().getBin(LockShape.AI).length;
				Log.info("New TxOut: " + txOut.getKey().getBin(LockShape.AI).length);
			}
		}
		
		Log.info("Total size: " + size);
		return size;
	}

	public void parseBody(ByteArrayInputStream is) throws NoSuchFieldException, IOException {
	}

	public void parseBody(ByteArrayInputStream is, LockShape addressShape) throws NoSuchFieldException, IOException {
		nonce = EQCType.parseID(is);
		byte txOutValidCount = 0;
		while (txOutValidCount++ < MAX_TXOUT && !EQCType.isInputStreamEnd(is)) {
			txOutList.add(new TxOut(is, addressShape));
		}
	}
	
	public byte[] getBodyBytes(LockShape addressShape) {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			// Serialization nonce
			os.write(EQCType.bigIntegerToEQCBits(nonce));
			// Serialization TxOut
			for (TxOut txOut : txOutList) {
				os.write(txOut.getBytes(addressShape));
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return os.toByteArray();
	}

	/* (non-Javadoc)
	 * @see com.eqchains.blockchain.transaction.Transaction#compare(com.eqchains.blockchain.transaction.Transaction)
	 */
	@Override
	public boolean compare(Transaction transaction) {
		if(transactionType != transaction.getTransactionType()) {
			return false;
		}
		for(int i=0; i<REWARD_NUMBERS; ++i) {
			if(!txOutList.get(i).compare(transaction.getTxOutList().get(i))) {
				return false;
			}
		}
		if(!nonce.equals(transaction.getNonce())) {
			return false;
		}
		return true;
	}
	
	
	
}
