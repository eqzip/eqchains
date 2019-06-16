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
import com.eqchains.blockchain.account.Asset;
import com.eqchains.blockchain.account.AssetAccount;
import com.eqchains.blockchain.account.CoinAsset;
import com.eqchains.blockchain.accountsmerkletree.AccountsMerkleTree;
import com.eqchains.blockchain.transaction.Address.AddressShape;
import com.eqchains.serialization.EQCType;
import com.eqchains.util.ID;
import com.eqchains.util.Log;
import com.eqchains.util.Util;
import com.eqchains.util.Util.AddressTool;

/**
 * @author Xun Wang
 * @date Mar 21, 2019
 * @email 10509759@qq.com
 */
public class CoinbaseTransaction extends TransferTransaction {
	private long txFee;
	public final static int MIN_TXOUT = 1;
	public final static int MAX_TXOUT = 3;
//	protected final static byte BODY_VERIFICATION_MIN_COUNT = MIN_TXOUT + 1;
//	protected final static byte BODY_VERIFICATION_MAX_COUNT = MAX_TXOUT + 1;
	
	private void init() {
		txIn = null;
		txFee = 0;
		solo = SOLO;
	}
	
	public CoinbaseTransaction() {
		super(TransactionType.COINBASE);
		init();
	}

	public CoinbaseTransaction(byte[] bytes, Address.AddressShape addressShape)
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
	public byte[] getBytes(Address.AddressShape addressShape) {
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
	public byte[] getBin(Address.AddressShape addressShape) {
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
		long eqcFoundationCoinBaseValue = 0;
		long eqzipCoinBaseValue = 0;
		long minerCoinBaseValue = 0;
		if(!isSanity(addressShape)) {
			return false;
		}
		if (accountsMerkleTree.getHeight().getNextID().compareTo(Util.getMaxCoinbaseHeight(accountsMerkleTree.getHeight().getNextID())) < 0) {
			if(txOutList.size() != MAX_TXOUT) {
				return false;
			}
			if(!txOutList.get(0).getAddress().getID().equals(ID.ONE)) {
				return false;
			}
			if(!txOutList.get(1).getAddress().getID().equals(ID.TWO)) {
				return false;
			}
			if (accountsMerkleTree.isAccountExists(txOutList.get(2).getAddress(), false)) {
				if (nonce.getPreviousID().compareTo(
						accountsMerkleTree.getAccount(txOutList.get(2).getAddress().getID()).getAsset(getAssetID()).getNonce()) != 0) {
					return false;
				}
			} else {
				if(nonce.compareTo(ID.ONE) != 0) {
					return false;
				}
			}
			eqcFoundationCoinBaseValue = txOutList.get(0).getValue();
			eqzipCoinBaseValue = txOutList.get(1).getValue();
			minerCoinBaseValue =  txOutList.get(2).getValue();
			if(eqcFoundationCoinBaseValue != Util.EQC_FOUNDATION_COINBASE_REWARD + txFee) {
				return false;
			}
			if(eqzipCoinBaseValue != Util.EQZIP_COINBASE_REWARD) {
				return false;
			}
			if(minerCoinBaseValue != Util.MINER_COINBASE_REWARD) {
				return false;
			}
		} else {
			if(txOutList.size() != MIN_TXOUT) {
				return false;
			}
			if(!txOutList.get(0).getAddress().getID().equals(ID.ONE)) {
				return false;
			}
			if(nonce.getPreviousID().compareTo(accountsMerkleTree.getAccount(txOutList.get(0).getAddress().getID()).getAsset(getAssetID()).getNonce()) != 0) {
				return false;
			}
			eqcFoundationCoinBaseValue = txOutList.get(0).getValue();
			if (eqcFoundationCoinBaseValue != txFee) {
				return false;
			}
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see com.eqzip.eqcoin.blockchain.transaction.Transaction#isTxOutNumberValid()
	 */
	@Override
	public boolean isTxOutNumberValid() {
		// TODO Auto-generated method stub
		return txOutList.size() >= MIN_TXOUT && txOutList.size() <= MAX_TXOUT;
	}

	@Override
	public boolean isSanity(AddressShape addressShape) {
		if(transactionType == null || txIn != null || nonce == null || txOutList == null) {
			return false;
		}
		if (transactionType != TransactionType.COINBASE) {
			return false;
		}
		if (!isTxOutNumberValid()) {
			return false;
		}
		for(TxOut txOut : txOutList) {
			if(!txOut.isSanity(addressShape)) {
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
			if (!accountsMerkleTree.isAccountExists(txOut.getAddress(), true)) {
				txOut.getAddress().setID(initID);
				txOut.setNew(true);
				initID = initID.getNextID();
			} else {
				// For security issue need retrieve and fill in every Address' ID
				// according to it's AddressAI
				txOut.getAddress().setID(accountsMerkleTree.getAddressID(txOut.getAddress()));
			}
		}
		// Set Nonce
		if (accountsMerkleTree.getHeight().getNextID().compareTo(Util.getMaxCoinbaseHeight(accountsMerkleTree.getHeight().getNextID())) < 0) {
			if (accountsMerkleTree.isAccountExists(txOutList.get(2).getAddress(), true)) {
				nonce = accountsMerkleTree.getAccount(txOutList.get(2).getAddress()).getAsset(Asset.EQCOIN).getNonce().getNextID();
			} else {
				nonce = ID.ONE;
			}
		} else {
			nonce = accountsMerkleTree.getAccount(txOutList.get(0).getAddress().getID()).getAsset(Asset.EQCOIN).getNonce().getNextID();
		}
	}
	
	public void updateTxFee(long txFee) {
		txOutList.get(0).addValue(txFee);
	}
	
	public void update(AccountsMerkleTree accountsMerkleTree) throws RocksDBException, NoSuchFieldException, IllegalStateException, IOException, ClassNotFoundException, SQLException {
		Account account = null;
		WriteBatch writeBatch = new WriteBatch();
		// Update current Transaction's TxOut Account
		for (TxOut txOut : txOutList) {
			if (txOut.isNew()) {
				account = new AssetAccount();
				account.getKey().setAddress(txOut.getAddress());
				account.getKey().setAddressCreateHeight(accountsMerkleTree.getHeight().getNextID());
				Asset asset = new CoinAsset();
				asset.setAssetID(getAssetID());
				asset.setBalanceUpdateHeight(accountsMerkleTree.getHeight().getNextID());
				asset.setNonce(ID.ZERO);
				account.setAsset(asset);
				accountsMerkleTree.increaseTotalAccountNumbers();
			} else {
				account = accountsMerkleTree.getAccount(txOut.getAddress().getID());
			}
			account.getAsset(getAssetID()).setBalance(account.getAsset(getAssetID()).getBalance().add(new ID(txOut.getValue())));; 
			account.getAsset(getAssetID()).setBalanceUpdateHeight(accountsMerkleTree.getHeight().getNextID());
			// Update Nonce
			if(accountsMerkleTree.getHeight().compareTo(Util.getMaxCoinbaseHeight(accountsMerkleTree.getHeight().getNextID())) < 0) {
				if(account.getKey().getAddress().getID().compareTo(ID.TWO) > 0) {
					account.getAsset(getAssetID()).increaseNonce();
				}
			}
			else {
				if(account.getKey().getAddress().getID().compareTo(ID.ONE) == 0) {
					account.getAsset(getAssetID()).increaseNonce();
				}
			}
			writeBatch.put(accountsMerkleTree.getFilter().getColumnFamilyHandles().get(0), account.getIDEQCBits(), account.getBytes());
			writeBatch.put(accountsMerkleTree.getFilter().getColumnFamilyHandles().get(1), account.getKey().getAddress().getAddressAI(),
					account.getIDEQCBits());
		}
		
		accountsMerkleTree.getFilter().batchUpdate(writeBatch);
		
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return

		"{\n" + toInnerJson() + "\n}";

	}

	public String toInnerJson() {
		return

		"\"CoinbaseTransaction\":" + "\n{\n" + TxIn.coinBase() + ",\n"
		+ "\"TxOutList\":" + "\n{\n" + "\"Size\":" + "\"" + txOutList.size() + "\"" + ",\n"
		+ "\"List\":" + "\n" + getTxOutString() + "\n},\n" + "\"Nonce\":" + "\"" + nonce + "\"" + "\n}";
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
				size += txOut.getAddress().getBin(AddressShape.AI).length;
				Log.info("New TxOut: " + txOut.getAddress().getBin(AddressShape.AI).length);
			}
		}
		
		Log.info("Total size: " + size);
		return size;
	}

	/**
	 * @return the txFee
	 */
	public long getTxFee() {
		return txFee;
	}

	/**
	 * @param txFee the txFee to set
	 */
	public void setTxFee(long txFee) {
		this.txFee = txFee;
	}

	public void parseBody(ByteArrayInputStream is) throws NoSuchFieldException, IOException {
	}

	public void parseBody(ByteArrayInputStream is, AddressShape addressShape) throws NoSuchFieldException, IOException {
		byte txOutValidCount = 0;
		if (addressShape == Address.AddressShape.ID) {
			// Parse nonce
			nonce = new ID(EQCType.parseEQCBits(is));
			// Parse TxOut
			while (txOutValidCount < MAX_TXOUT) {
				TxOut txOut = new TxOut();
				// Parse TxOut address
				txOut.getAddress().setID(new ID(EQCType.parseEQCBits(is)));
				// Parse TxOut value
				txOut.setValue(EQCType.eqcBitsToLong(EQCType.parseEQCBits(is)));
				// Add TxOut
				txOutList.add(txOut);
				++txOutValidCount;
			}
		} else if (addressShape == Address.AddressShape.READABLE || addressShape == Address.AddressShape.AI) {
			// Parse nonce
			nonce = new ID(EQCType.parseEQCBits(is));
			// Parse TxOut
			while (txOutValidCount < MAX_TXOUT) {
				TxOut txOut = new TxOut(EQCType.parseBIN(is), addressShape);
				// Add TxOut
				txOutList.add(txOut);
				++txOutValidCount;
			}
		}
	}
	
	public byte[] getBodyBytes(AddressShape addressShape) {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			// Serialization nonce
			os.write(nonce.getEQCBits());
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
