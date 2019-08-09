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
package com.eqchains.blockchain.transaction;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Vector;

import org.rocksdb.RocksDBException;
import org.rocksdb.WriteBatch;

import com.eqchains.blockchain.account.Account;
import com.eqchains.blockchain.account.Passport;
import com.eqchains.blockchain.account.Asset;
import com.eqchains.blockchain.account.AssetAccount;
import com.eqchains.blockchain.account.CoinAsset;
import com.eqchains.blockchain.account.Passport.AddressShape;
import com.eqchains.blockchain.accountsmerkletree.AccountsMerkleTree;
import com.eqchains.persistence.EQCBlockChainH2;
import com.eqchains.persistence.EQCBlockChainRocksDB;
import com.eqchains.persistence.EQCBlockChainRocksDB.TABLE;
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
	public final static int MIN_TXOUT = 1;
	public final static int MAX_TXOUT = 3;
	
	private void init() {
		txIn = null;
		solo = SOLO;
	}
	
	public CoinbaseTransaction() {
		super(TransactionType.COINBASE);
		init();
	}

	public CoinbaseTransaction(byte[] bytes, Passport.AddressShape addressShape)
			throws NoSuchFieldException, IOException, IllegalStateException {
		super(TransactionType.COINBASE);
		EQCType.assertNotNull(bytes);
		init();
		ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		parseHeader(is, addressShape);
		parseBody(is, addressShape);
		EQCType.assertNoRedundantData(is);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.eqzip.eqcoin.blockchain.Transaction#getBytes(com.eqzip.eqcoin.util.Util.
	 * AddressShape)
	 */
	@Override
	public byte[] getBytes(Passport.AddressShape addressShape) {
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
	public byte[] getBin(Passport.AddressShape addressShape) {
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
	public boolean isValid(AccountsMerkleTree accountsMerkleTree, AddressShape addressShape) throws NoSuchFieldException, IllegalStateException, RocksDBException, IOException, Exception {
		if(!isSanity(addressShape)) {
			return false;
		}
		if (accountsMerkleTree.getHeight().compareTo(Util.getMaxCoinbaseHeight(accountsMerkleTree.getHeight())) < 0) {
			if(!txOutList.get(0).getPassport().getID().equals(ID.ONE)) {
				return false;
			}
			if(!txOutList.get(1).getPassport().getID().equals(ID.TWO)) {
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
		return txOutList.size() == MAX_TXOUT;
	}

	@Override
	public boolean isSanity(AddressShape addressShape) {
		if(transactionType == null || txIn != null || nonce != null || txOutList == null) {
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
		for(TxOut txOut : txOutList) {
			if(!txOut.isSanity(addressShape)) {
				Log.Error("TxOut's sanity test failed");
				return false;
			}
			if(txOut.getPassport().getAddressType() != AddressType.T1 && txOut.getPassport().getAddressType() != AddressType.T2) {
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
	public void prepareAccounting(AccountsMerkleTree accountsMerkleTree, ID initID) throws NoSuchFieldException, IllegalStateException, RocksDBException, IOException, Exception {
		// Update TxOut's Address' isNew status if need
		for (TxOut txOut : txOutList) {
			if (!accountsMerkleTree.isAccountExists(txOut.getPassport(), true)) {
				txOut.getPassport().setID(initID);
				txOut.setNew(true);
				initID = initID.getNextID();
			} else {
				// For security issue need retrieve and fill in every Address' ID
				// according to it's AddressAI
				txOut.getPassport().setID(accountsMerkleTree.getAddressID(txOut.getPassport()));
			}
		}
	}
	
	public void update(AccountsMerkleTree accountsMerkleTree) throws Exception {
		Account account = null;
		WriteBatch writeBatch = new WriteBatch();
		// Update current Transaction's TxOut Account
		for (TxOut txOut : txOutList) {
			if (txOut.isNew()) {
				account = new AssetAccount();
				account.setCreateHeight(accountsMerkleTree.getHeight());
				account.setVersion(ID.ZERO);
				account.setVersionUpdateHeight(accountsMerkleTree.getHeight());
				account.setPassport(txOut.getPassport());
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
				accountsMerkleTree.increaseTotalAccountNumbers();
			} else {
				account = accountsMerkleTree.getAccount(txOut.getPassport().getID());
			}
			account.getAsset(getAssetID()).deposit(new ID(txOut.getValue()));
			account.getAsset(getAssetID()).setBalanceUpdateHeight(accountsMerkleTree.getHeight());
			account.setUpdateHeight(accountsMerkleTree.getHeight());
			writeBatch.put(accountsMerkleTree.getFilter().getTableHandle(TABLE.ACCOUNT), account.getID().getEQCBits(), account.getBytes());
			writeBatch.put(accountsMerkleTree.getFilter().getTableHandle(TABLE.ACCOUNT_AI), account.getPassport().getAddressAI(),
					account.getID().getEQCBits());
		}
		
		accountsMerkleTree.getFilter().batchUpdate(writeBatch);
		
	}
	
	protected String toInnerJson() {
		return

		"\"CoinbaseTransaction\":" + "\n{\n" + TxIn.coinBase() + ",\n"
		+ "\"TxOutList\":" + "\n{\n" + "\"Size\":" + "\"" + txOutList.size() + "\"" + ",\n"
		+ "\"List\":" + "\n" + getTxOutString() + "\n}";
	}

	/* (non-Javadoc)
	 * @see com.eqzip.eqcoin.blockchain.transaction.Transaction#getBillingSize()
	 */
	@Override
	public int getBillingSize() {
		int size = 0;

		// Transaction's ID format's size which storage in the EQC Blockchain
		size += getBin(AddressShape.ID).length;
		Log.info("ID size: " + size);

		// Transaction's AddressList size which storage the new Address
		for (TxOut txOut : txOutList) {
			if (txOut.isNew()) {
				size += txOut.getPassport().getBin(AddressShape.AI).length;
				Log.info("New TxOut: " + txOut.getPassport().getBin(AddressShape.AI).length);
			}
		}
		
		Log.info("Total size: " + size);
		return size;
	}

	public void parseBody(ByteArrayInputStream is) throws NoSuchFieldException, IOException {
	}

	public void parseBody(ByteArrayInputStream is, AddressShape addressShape) throws NoSuchFieldException, IOException {
		byte txOutValidCount = 0;
		while (txOutValidCount < MAX_TXOUT && !EQCType.isInputStreamEnd(is)) {
			txOutList.add(new TxOut(is, addressShape));
		}
	}
	
	public byte[] getBodyBytes(AddressShape addressShape) {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
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
	
}
