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
package com.eqchains.blockchain.transaction;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Vector;

import com.eqchains.blockchain.Account;
import com.eqchains.blockchain.AccountsMerkleTree;
import com.eqchains.blockchain.transaction.Address.AddressShape;
import com.eqchains.blockchain.transaction.Transaction.TransactionType;
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
public class CoinbaseTransaction extends Transaction {

	public CoinbaseTransaction() {
		super(TransactionType.COINBASE);
		txIn = null;
	}

	public CoinbaseTransaction(byte[] bytes, Address.AddressShape addressShape)
			throws NoSuchFieldException, IOException {
		super(TransactionType.COINBASE);
		txIn = null;
		ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		byte[] data = null;

		if (addressShape == Address.AddressShape.ID) {
			// Parse Solo
			if ((data = EQCType.parseEQCBits(is)) != null) {
				int solo = EQCType.eqcBitsToInt(data);
				if (solo != SOLO) {
					throw new IllegalStateException("CoinbaseTransaction's Solo is invalid: " + solo);
				}
			}

			// Parse Transaction type
			if ((data = EQCType.parseEQCBits(is)) != null) {
				int transactionType = (int) EQCType.eqcBitsToLong(data);
				if (transactionType != TransactionType.COINBASE.ordinal()) {
					throw new IllegalStateException(
							"CoinbaseTransaction's TransactionType is invalid: " + transactionType);
				} else {
					this.transactionType = TransactionType.COINBASE;
				}
			}
			
			// Parse nonce
			if ((data = EQCType.parseEQCBits(is)) != null && !EQCType.isNULL(data)) {
				nonce = new ID(data);
			}
			
			// Parse TxOut
			data = null;
			while ((data = EQCType.parseEQCBits(is)) != null && !EQCType.isNULL(data)) {
				TxOut txOut = new TxOut();
				// Parse TxOut address
				txOut.getAddress().setID(new ID(data));
				// Parse TxOut value
				data = null;
				if ((data = EQCType.parseEQCBits(is)) != null && !EQCType.isNULL(data)) {
					txOut.setValue(EQCType.eqcBitsToLong(data));
				}

				// Add TxOut
				txOutList.add(txOut);
				data = null;
			}
		} else if (addressShape == Address.AddressShape.READABLE || addressShape == Address.AddressShape.AI) {
			// Parse Solo
			if ((data = EQCType.parseEQCBits(is)) != null) {
				int solo = EQCType.eqcBitsToInt(data);
				if (solo != SOLO) {
					throw new IllegalStateException("CoinbaseTransaction's Solo is invalid: " + solo);
				}
			}

			// Parse Transaction type
			if ((data = EQCType.parseEQCBits(is)) != null) {
				int transactionType = EQCType.eqcBitsToInt(data);
				if (transactionType != TransactionType.COINBASE.ordinal()) {
					throw new IllegalStateException(
							"CoinbaseTransaction's TransactionType is invalid: " + transactionType);
				} else {
					this.transactionType = TransactionType.COINBASE;
				}
			}
			
			// Parse nonce
			data = null;
			if ((data = EQCType.parseEQCBits(is)) != null && !EQCType.isNULL(data)) {
				nonce = new ID(data);
			}

			// Parse TxOut
			data = null;
			while ((data = EQCType.parseBIN(is)) != null && !EQCType.isNULL(data)) {
				TxOut txOut = new TxOut();
				// Parse TxOut address
				if (addressShape == Address.AddressShape.READABLE) {
					txOut.getAddress().setReadableAddress(EQCType.bytesToASCIISting(data));
				} else if (addressShape == Address.AddressShape.AI) {
					txOut.getAddress().setReadableAddress(AddressTool.AIToAddress(data));
				}

				// Parse TxOut value
				data = null;
				if ((data = EQCType.parseEQCBits(is)) != null && !EQCType.isNULL(data)) {
					txOut.setValue(EQCType.eqcBitsToLong(data));
				}

				// Add TxOut
				txOutList.add(txOut);
				data = null;
			}
		}
	}

	public static boolean isValid(byte[] bytes, AddressShape addressShape) throws NoSuchFieldException, IOException {
		ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		byte[] data;
		byte txOutValidCount = 0;
		int type = -1;
		boolean isTxOutValid = false, isSoloValid = false, isTransactionTypeValid = false, isNonceValid = false;

		if (addressShape == Address.AddressShape.ID) {
			// Parse Solo
			if ((data = EQCType.parseEQCBits(is)) != null) {
				int solo = EQCType.eqcBitsToInt(data);
				if (solo == SOLO) {
					isSoloValid = true;
				}
			}

			// Parse Transaction type
			if ((data = EQCType.parseEQCBits(is)) != null) {
				int transactionType = (int) EQCType.eqcBitsToLong(data);
				if (transactionType == TransactionType.COINBASE.ordinal()) {
					isTransactionTypeValid = true;
				} 
			}
			
			// Parse nonce
			if ((data = EQCType.parseEQCBits(is)) != null && !EQCType.isNULL(data)) {
				isNonceValid = true;
			}

			// Parse TxOut
			data = null;
			while ((data = EQCType.parseEQCBits(is)) != null && !EQCType.isNULL(data)) {
				// Parse TxOut address
				isTxOutValid = false;
				// Parse TxOut value
				data = null;
				if ((data = EQCType.parseEQCBits(is)) != null && !EQCType.isNULL(data)) {
					isTxOutValid = true;
					++txOutValidCount;
				}
				data = null;
			}
		} else if (addressShape == Address.AddressShape.READABLE || addressShape == Address.AddressShape.AI) {
			// Parse Solo
			if ((data = EQCType.parseEQCBits(is)) != null) {
				int solo = EQCType.eqcBitsToInt(data);
				if (solo == SOLO) {
					isSoloValid = true;
				}
			}

			// Parse Transaction type
			if ((data = EQCType.parseEQCBits(is)) != null) {
				int transactionType = EQCType.eqcBitsToInt(data);
				if (transactionType == TransactionType.COINBASE.ordinal()) {
					isTransactionTypeValid = true;
				}
			}

			// Parse nonce
			if ((data = EQCType.parseEQCBits(is)) != null && !EQCType.isNULL(data)) {
				isNonceValid = true;
			}
			
			// Parse TxOut
			data = null;
			while ((data = EQCType.parseBIN(is)) != null && !EQCType.isNULL(data)) {
				// Parse TxOut address
				isTxOutValid = false;
				// Parse TxOut value
				data = null;
				if ((data = EQCType.parseEQCBits(is)) != null && !EQCType.isNULL(data)) {
					isTxOutValid = true;
					++txOutValidCount;
				}
				data = null;
			}
		}

		return isSoloValid && isTransactionTypeValid && isNonceValid && (txOutValidCount == Util.TWO) && isTxOutValid
				&& EQCType.isInputStreamEnd(is);
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
			// Serialization Solo
			os.write(EQCType.longToEQCBits(SOLO));
			// Serialization Transaction type
			os.write(EQCType.longToEQCBits(TransactionType.COINBASE.ordinal()));
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.eqzip.eqcoin.blockchain.Transaction#getBytes()
	 */
	@Override
	public byte[] getBytes() {
		return getBytes(Address.AddressShape.ID);
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
	 * @see com.eqzip.eqcoin.blockchain.Transaction#getBin()
	 */
	@Override
	public byte[] getBin() {
		return EQCType.bytesToBIN(getBytes());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.eqzip.eqcoin.blockchain.Transaction#isValid(com.eqzip.eqcoin.blockchain.
	 * AccountsMerkleTree)
	 */
	@Override
	public boolean isValid(AccountsMerkleTree accountsMerkleTree, AddressShape addressShape)
			throws NoSuchFieldException, IOException, IllegalArgumentException {
		return false;
	}

	/* (non-Javadoc)
	 * @see com.eqzip.eqcoin.blockchain.transaction.Transaction#isTxOutNumberValid()
	 */
	@Override
	public boolean isTxOutNumberValid() {
		// TODO Auto-generated method stub
		return txOutList.size() <= 2;
	}

	@Override
	public boolean isSanity(AddressShape ...addressShape) {
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
	public void prepareAccounting(AccountsMerkleTree accountsMerkleTree, ID initID) {
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
		if (accountsMerkleTree.getHeight().getNextID().compareTo(Util.MAX_COINBASE_HEIGHT) < 0) {
			if (accountsMerkleTree.isAccountExists(txOutList.get(1).getAddress(), true)) {
				nonce = accountsMerkleTree.getAccount(txOutList.get(1).getAddress()).getNonce().getNextID();
			} else {
				nonce = ID.ONE;
			}
		} else {
			nonce = accountsMerkleTree.getAccount(txOutList.get(0).getAddress().getID()).getNonce().getNextID();
		}
	}
	
	public boolean isCoinbaseValid(AccountsMerkleTree accountsMerkleTree, long txFee) {
		long eqcFoundationCoinBaseValue = 0;
		long minerCoinBaseValue = 0;
		if(!isSanity()) {
			return false;
		}
		if (accountsMerkleTree.getHeight().getNextID().compareTo(Util.MAX_COINBASE_HEIGHT) < 0) {
			if(txOutList.size() != Util.TWO) {
				return false;
			}
			if(!txOutList.get(0).getAddress().getID().equals(ID.ONE)) {
				return false;
			}
			if (accountsMerkleTree.isAccountExists(txOutList.get(1).getAddress(), true)) {
				if (nonce.getPreviousID().compareTo(
						accountsMerkleTree.getAccount(txOutList.get(1).getAddress()).getNonce()) != 0) {
					return false;
				}
			} else {
				if(nonce.compareTo(ID.ONE) != 0) {
					return false;
				}
			}
			eqcFoundationCoinBaseValue = txOutList.get(0).getValue();
			minerCoinBaseValue =  txOutList.get(1).getValue();
			if(eqcFoundationCoinBaseValue != Util.EQC_FOUNDATION_COINBASE_REWARD + txFee) {
				return false;
			}
			if(minerCoinBaseValue != Util.MINER_COINBASE_REWARD) {
				return false;
			}
		} else {
			if(txOutList.size() != Util.ONE) {
				return false;
			}
			if(!txOutList.get(0).getAddress().getID().equals(ID.ONE)) {
				return false;
			}
			if(nonce.getPreviousID().compareTo(accountsMerkleTree.getAccount(txOutList.get(0).getAddress().getID()).getNonce()) != 0) {
				return false;
			}
			eqcFoundationCoinBaseValue = txOutList.get(0).getValue();
			if (eqcFoundationCoinBaseValue != txFee) {
				return false;
			}
		}
		return true;
	}
	
	public void updateTxFee(long txFee) {
		txOutList.get(0).updateValue(txFee);
	}
	
	public void update(AccountsMerkleTree accountsMerkleTree) {
		Account account = null;
		// Update current Transaction's TxOut Account
		for (TxOut txOut : txOutList) {
			if (txOut.isNew()) {
				account = new Account();
				account.setAddress(txOut.getAddress());
				account.setAddressCreateHeight(accountsMerkleTree.getHeight().getNextID());
			} else {
				account = accountsMerkleTree.getAccount(txOut.getAddress().getID());
			}
			account.updateBalance(txOut.getValue());
			account.setBalanceUpdateHeight(accountsMerkleTree.getHeight().getNextID());
			// Update Nonce
			if(accountsMerkleTree.getHeight().compareTo(Util.MAX_COINBASE_HEIGHT) < 0) {
				if(account.getAddress().getID().compareTo(ID.ONE) > 0) {
					account.increaseNonce();
				}
			}
			else {
				if(account.getAddress().getID().compareTo(ID.ONE) == 0) {
					account.increaseNonce();
				}
			}
			if(accountsMerkleTree.saveAccount(account) && txOut.isNew()) {
				accountsMerkleTree.increaseTotalAccountNumbers();
			}
		}
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
		size += getBin().length;
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
	
//	public String toInnerJsons() {
//		return
//
//		"\"Transaction\":" + "\n{\n" + ((txIn == null) ? TxIn.coinBase() : txIn.toInnerJson()) + ",\n"
//				+ "\"txOutList\":" + "\n" + getTxOutString() + ",\n" + "\"Nonce\":" + "\"" + nonce + "\"" + ",\n"
//				+ "\"signature\":" + "\"" + Util.getHexString(signature) + "\"" + ",\n" + "\"publickey\":" + "\""
//				+ ((publickey == null) ? null : Util.getHexString(publickey.getPublicKey())) + "\"" + "\n" + "}";
//	}
	
}
