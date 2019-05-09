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
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Signature;
import java.security.SignatureException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Vector;

import com.eqchains.blockchain.Account;
import com.eqchains.blockchain.AccountsMerkleTree;
import com.eqchains.blockchain.PublicKey;
import com.eqchains.blockchain.transaction.Address.AddressShape;
import com.eqchains.crypto.EQCPublicKey;
import com.eqchains.keystore.Keystore.ECCTYPE;
import com.eqchains.persistence.h2.EQCBlockChainH2;
import com.eqchains.serialization.EQCTypable;
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
public abstract class Transaction implements Comparator<Transaction>, Comparable<Transaction>, EQCTypable {
	public enum TransactionType {
		COINBASE, OPERATION, SOLIDITYCONTRACT, INVALID, TRANSFER;
		public static TransactionType get(int ordinal) {
			TransactionType transactionType = null;
			switch (ordinal) {
			case 0:
				transactionType = TransactionType.COINBASE;
				break;
			case 1:
				transactionType = TransactionType.OPERATION;
				break;
			case 2:
				transactionType = TransactionType.SOLIDITYCONTRACT;
				break;
			default:
				transactionType = TransactionType.INVALID;
				break;
			}
			return transactionType;
		}
	}

	public enum TXFEE_RATE {
		POSTPONEVIP(9), POSTPONE0(8), POSTPONE20(4), POSTPONE40(2), POSTPONE60(1);
		private TXFEE_RATE(int rate) {
			this.rate = rate;
		}

		private int rate;

		public int getRate() {
			return rate;
		}

		public static TXFEE_RATE get(int rate) {
			TXFEE_RATE txfee_rate = null;
			switch (rate) {
			case 1:
				txfee_rate = TXFEE_RATE.POSTPONE60;
				break;
			case 2:
				txfee_rate = TXFEE_RATE.POSTPONE40;
				break;
			case 4:
				txfee_rate = TXFEE_RATE.POSTPONE20;
				break;
			case 8:
				txfee_rate = TXFEE_RATE.POSTPONE0;
				break;
			case 9:
				txfee_rate = TXFEE_RATE.POSTPONEVIP;
				break;
			default:
				txfee_rate = TXFEE_RATE.POSTPONE20;
				break;
			}
			return txfee_rate;
		}
	}

	protected TransactionType transactionType;
	protected ID nonce;
	protected TxIn txIn;
	protected Vector<TxOut> txOutList;
	protected PublicKey publickey;
	protected byte[] signature;
	public final static int MAX_TXOUT = 10;
	public final static int MIN_TXOUT = 1;
	public final static int SOLO = 0;

	/*
	 * VERIFICATION_COUNT equal to the number of member variables of the class to be
	 * verified.
	 */
	protected final static byte VERIFICATION_MIN_COUNT = 4;
	protected final static byte VERIFICATION_MAX_COUNT = 13;

	public Transaction(TransactionType transactionType) {
		txIn = new TxIn();
		txOutList = new Vector<TxOut>();
		nonce = ID.ZERO;
		publickey = new PublicKey();
		this.transactionType = transactionType;
	}

	/**
	 * @return the txIn
	 */
	public TxIn getTxIn() {
		return txIn;
	}

	/**
	 * @param txIn the txIn to set
	 */
	public void setTxIn(TxIn txIn) {
		this.txIn = txIn;
	}

	/**
	 * @return the txOutList
	 */
	public Vector<TxOut> getTxOutList() {
		return txOutList;
	}

	/**
	 * @param txOutList the txOutList to set
	 */
	public void setTxOutList(Vector<TxOut> txOutList) {
		this.txOutList = txOutList;
	}

	/**
	 * @return the signature
	 */
	public byte[] getSignature() {
		return signature;
	}

	/**
	 * @param signature the signature to set
	 */
	public void setSignature(byte[] signature) {
		this.signature = signature;
	}

	/**
	 * @return the publickey
	 */
	public PublicKey getPublickey() {
		return publickey;
	}

	/**
	 * @return the nonce
	 */
	public BigInteger getNonce() {
		return nonce;
	}

	/**
	 * @param nonce the nonce to set
	 */
	public void setNonce(ID nonce) {
		this.nonce = nonce;
	}

	/**
	 * @param publickey the publickey to set
	 */
	public void setPublickey(PublicKey publickey) {
		this.publickey = publickey;
	}

	@Override
	public int compareTo(Transaction o) {
		// TODO Auto-generated method stub
		int nResult = 0;
		if ((nResult = this.txIn.getAddress().getReadableAddress().compareTo(o.getTxIn().getAddress().getReadableAddress())) == 0) {
			nResult = this.nonce.compareTo(o.getNonce());
		}
		return nResult;
	}

	@Override
	public int compare(Transaction o1, Transaction o2) {
		// TODO Auto-generated method stub
		int nResult = 0;
		if ((nResult = o1.getTxIn().getAddress().getReadableAddress().compareTo(o2.getTxIn().getAddress().getReadableAddress())) == 0) {
			nResult = o1.getNonce().compareTo(o2.getNonce());
		}
		return nResult;
	}

	public byte[] getBytes(Address.AddressShape addressShape) {
		return null;
	}

	public byte[] getRPCBytes() {
		return null;
	}

	public void addTxOut(TxOut txOut) {
		if (txOutList.size() >= MAX_TXOUT) {
			throw new UnsupportedOperationException("The number of TxOut cannot exceed 10.");
		}
		if (!isTxOutAddressExists(txOut)) {
			if (!isTxOutAddressEqualsTxInAddress(txOut)) {
				txOutList.add(txOut);
			} else {
				Log.Error(txOut + " equal to TxIn Address: " + txIn + " just ignore it.");
			}
		} else {
			Log.Error(txOut + " already exists in txOutList just ignore it.");
		}
	}

	public int getTxOutNumber() {
		return txOutList.size();
	}

	public long getTxOutValues() {
		long totalTxOut = 0;
		for (TxOut txOut : txOutList) {
			totalTxOut += txOut.getValue();
		}
		return totalTxOut;
	}

	public boolean isNoncePositive() {
		return nonce.signum() >= 0;
	}

	public boolean isTxOutValueLessThanTxInValue() {
		return getTxOutValues() < txIn.getValue();
	}

	public boolean isTxOutAddressIncludeTxInAddress() {
		for (TxOut txOut : txOutList) {
			if (isTxOutAddressEqualsTxInAddress(txOut)) {
				return true;
			}
		}
		return false;
	}

	private boolean isTxOutAddressEqualsTxInAddress(TxOut txOut) {
		if(txIn == null) {
			return false;
		}
		return txOut.getAddress().getReadableAddress().equals(txIn.getAddress().getReadableAddress());
	}

	public boolean isTxOutNumberValid() {
		return (txOutList.size() >= MIN_TXOUT) && (txOutList.size() <= MAX_TXOUT);
	}

	public boolean isAllValueValid() {
		if ((txIn.getValue() < Util.MIN_EQC) || (txIn.getValue() >= Util.MAX_EQC)) {
			return false;
		}

		for (TxOut txOut : txOutList) {
			if ((txOut.getValue() < Util.MIN_EQC) || (txOut.getValue() >= Util.MAX_EQC)) {
				return false;
			}
		}

		return true;
	}

	public long getTxFeeLimit() {
		return txIn.getValue() - getTxOutValues();
	}

	public boolean isTxFeeLimitValid() {
		Log.info("getTxFeeLimit(): " + getTxFeeLimit());
		Log.info("getMaxTxFeeLimit(): " + getMaxTxFeeLimit());
		Log.info("getDefaultTxFeeLimit(): " + getDefaultTxFeeLimit());
		boolean boolIsValid = true;
		if (getTxFeeLimit() < getDefaultTxFeeLimit()) {
			boolIsValid = false;
		} else if ((getTxFeeLimit() <= getMaxTxFeeLimit()) && (getTxFeeLimit() % getDefaultTxFeeLimit()) != 0) {
			boolIsValid = false;
		}
		return boolIsValid;
	}

	public long getMaxTxFeeLimit() {
		return (getMaxBillingSize() * TXFEE_RATE.POSTPONE0.getRate() * Util.TXFEE_UNIT);
	}

	public long getDefaultTxFeeLimit() {
		return (getMaxBillingSize() * Util.TXFEE_UNIT);
	}

	public int getMaxBillingSize() {
		return 0;
	}

	public long getTxFee() {
		long txFee = 0;
		if (getTxFeeLimit() > getMaxTxFeeLimit()) {
			txFee = getTxFeeLimit();
		} else {
			txFee = getBillingSize() * getQos().getRate() * Util.TXFEE_UNIT;
		}
		return txFee;
	}

	public long getBillingValue() {
		Log.info("TxFee: " + getTxFee() + " TxOutValues: " + getTxOutValues() + " TxFeeLimit: " + getTxFeeLimit());
		return getTxFee() + getTxOutValues();
	}

	public void setTxFeeLimit(TXFEE_RATE txfee_rate) {
		txIn.setValue(getTxOutValues() + cypherTxFeeLimit(txfee_rate));
	}

	public long cypherTxFeeLimit(TXFEE_RATE txfee_rate) {
		return (getMaxBillingSize() * txfee_rate.getRate() * Util.TXFEE_UNIT);
	}

	public void cypherTxInValue(TXFEE_RATE txfee_rate) {
		txIn.setValue(getTxOutValues() + cypherTxFeeLimit(txfee_rate));
	}
	
	public boolean isTxOutAddressExists(TxOut txOut) {
		boolean boolIsExists = false;
		for (TxOut txOut2 : txOutList) {
			if (txOut2.getAddress().getReadableAddress().equals(txOut.getAddress().getReadableAddress())) {
				boolIsExists = true;
//				Log.info("TxOutAddressExists" + " a: " + txOut2.getAddress().getAddress() + " b: " + txOut.getAddress().getAddress());
				break;
			}
		}
		return boolIsExists;
	}

	public boolean isTxOutAddressUnique() {
		for (int i = 0; i < txOutList.size(); ++i) {
			for (int j = i + 1; j < txOutList.size(); ++j) {
				if (txOutList.get(i).getAddress().equals(txOutList.get(j).getAddress())) {
					return false;
				}
			}
		}
		return true;
	}

	public int getBillingSize() {
		int size = 0;

		// Transaction's ID format's size which storage in the EQC Blockchain
		size += getBin().length;
		Log.info("ID size: " + size);

		// Transaction's AddressList size which storage the new Address
		for (TxOut txOut : txOutList) {
			if (txOut.isNew()) {
				size += txOut.getBin().length;
				Log.info("New TxOut: " + txOut.getBin().length);
			}
		}

		// Transaction's PublickeyList size
		if (publickey.isNew()) {
			size += publickey.getBin().length;
			Log.info("New Publickey: " + publickey.getBin().length);
		}

		// Transaction's Signature size
		size += EQCType.bytesToBIN(signature).length;
		Log.info("Signature size: " + EQCType.bytesToBIN(signature).length);
		Log.info("Total size: " + size);
		return size;
	}

	public boolean isTxOutAddressValid() {
		for (TxOut txOut : txOutList) {
			if (!txOut.getAddress().isGood(null)) {
				return false;
			}
		}
		return true;
	}

	public TXFEE_RATE getQos() {
		int rate = 1;
		if (getTxFeeLimit() > getMaxTxFeeLimit()) {
			rate = TXFEE_RATE.POSTPONEVIP.getRate();
		} else {
			rate = (int) (getTxFeeLimit() / (getMaxBillingSize() * Util.TXFEE_UNIT));
		}
		return TXFEE_RATE.get(rate);
	}
	
	public long getQosRate() {
		long rate = 1;
		if (getTxFeeLimit() > getMaxTxFeeLimit()) {
			rate = TXFEE_RATE.POSTPONEVIP.getRate() + getTxFeeLimit() - getMaxTxFeeLimit();
		} else {
			rate = (int) (getTxFeeLimit() / (getMaxBillingSize() * Util.TXFEE_UNIT));
		}
		return rate;
	}

	public boolean verify(AccountsMerkleTree accountsMerkleTree) {
		boolean isValid = false;
		if (accountsMerkleTree.isPublicKeyExists(publickey) || verifyPublickey()) {
			isValid = verifySignature(accountsMerkleTree.getEQCHeaderHash(txIn.getAddress().getID()), publickey.getPublicKey());
		}
		return isValid;
	}

	public boolean verifyPublickey() {
		boolean isValid = true;
		if (txIn.getAddress().getType() == AddressType.T1 || txIn.getAddress().getType() == AddressType.T2) {
			isValid = AddressTool.verifyAddressPublickey(txIn.getAddress().getReadableAddress(), publickey.getPublicKey());
		}
		return isValid;
	}

	/**
	 * Get the Transaction's bytes for storage it in the EQC block chain For save
	 * the space the Address' shape is Serial Number which is the EQCBits type.
	 * 
	 * @return byte[]
	 */
	@Override
	public byte[] getBytes() {
		// TODO Auto-generated method stub
		return getBytes(Address.AddressShape.ID);
	}

	public byte[] getBin(Address.AddressShape addressShape) {
		return null;
	}

	/**
	 * Get the Transaction's BIN for storage it in the EQC block chain. For save the
	 * space the Address' shape is Serial Number which is the EQCBits type.
	 * 
	 * @return byte[]
	 */
	@Override
	public byte[] getBin() {
		return getBin(Address.AddressShape.ID);
	}

	protected String getTxOutString() {
		String tx = "[\n";
		if (txOutList.size() > 0) {
			for (int i = 0; i < txOutList.size() - 1; ++i) {
				tx += txOutList.get(i) + ",\n";
			}
			tx += txOutList.get(txOutList.size() - 1);
		} else {
			tx += null;
		}
		tx += "\n]";
		return tx;
	}

	public boolean isValid(AccountsMerkleTree accountsMerkleTree, AddressShape addressShape) throws NoSuchFieldException, IOException {
		return false;
	}

	public static boolean isValid(byte[] bytes, AddressShape addressShape) throws NoSuchFieldException, IOException {
		boolean isValid = false;
		TransactionType transactionType = parseTransactionType(bytes);
		try {
			if (transactionType == TransactionType.COINBASE) {
				isValid = CoinbaseTransaction.isValid(bytes, addressShape);
			} else if (transactionType == TransactionType.TRANSFER) {
				isValid = TransferTransaction.isValid(bytes, addressShape);
			} else if (transactionType == TransactionType.OPERATION) {
				isValid = OperationTransaction.isValid(bytes, addressShape);
			} 
		} catch (NoSuchFieldException | UnsupportedOperationException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return isValid;
	}

	public boolean isCoinBase() {
		boolean isSucc = false;
		if (transactionType == TransactionType.COINBASE) {
			isSucc = true;
		}
		return isSucc;
	}

	/**
	 * @return the transactionType
	 */
	public TransactionType getTransactionType() {
		return transactionType;
	}

	public static TransactionType parseTransactionType(byte[] bytes) {
		TransactionType transactionType = TransactionType.INVALID;
		ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		byte[] data = null;
		int solo = -1, txType = -1;
		try {
			if ((data = EQCType.parseEQCBits(is)) != null) {
				solo = EQCType.eqcBitsToInt(data);
			}
			if (solo == SOLO) {
				if ((data = EQCType.parseEQCBits(is)) != null) {
					txType = EQCType.eqcBitsToInt(data);
				}
				transactionType = TransactionType.get(txType);
			} else {
				transactionType = TransactionType.TRANSFER;
			}
		} catch (NoSuchFieldException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return transactionType;
	}

	public static Transaction parseTransaction(byte[] bytes, Address.AddressShape addressShape) {
		Transaction transaction = null;
		TransactionType transactionType = parseTransactionType(bytes);

		try {
			if (transactionType == TransactionType.COINBASE) {
				transaction = new CoinbaseTransaction(bytes, addressShape);
			} else if (transactionType == TransactionType.TRANSFER) {
				transaction = new TransferTransaction(bytes, addressShape);
			} else if (transactionType == TransactionType.OPERATION) {
				transaction = new OperationTransaction(bytes, addressShape);
			} 
		} catch (NoSuchFieldException | UnsupportedOperationException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return transaction;
	}

	public boolean verifySignature(byte[] TXIN_HEADER_HASH, byte[] publicKey) {
		boolean isTransactionValid = false;
		Signature signature = null;

		// Verify Signature
		try {
			signature = Signature.getInstance("SHA1withECDSA", "SunEC");
			ECCTYPE eccType = null;
			if (txIn.getAddress().getType() == AddressType.T1) {
				eccType = ECCTYPE.P256;
			} else if (txIn.getAddress().getType() == AddressType.T2) {
				eccType = ECCTYPE.P521;
			}
			EQCPublicKey eqPublicKey = new EQCPublicKey(eccType);
			// Create EQPublicKey according to java Publickey
			eqPublicKey.setECPoint(publickey.getPublicKey());
//			Log.info(Util.dumpBytesBigEndianBinary(eqPublicKey.getEncoded()));
			signature.initVerify(eqPublicKey);
			signature.update(TXIN_HEADER_HASH);
			signature.update(publicKey);
			signature.update(getBytes(Address.AddressShape.READABLE));
			isTransactionValid = signature.verify(this.signature);
		} catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidKeyException | SignatureException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return isTransactionValid;
	}

	public byte[] sign(Signature ecdsa, byte[] TXIN_HEADER_HASH, byte[] publicKey) {
		try {
			ecdsa.update(TXIN_HEADER_HASH);
			ecdsa.update(publicKey);
			ecdsa.update(getBytes(Address.AddressShape.READABLE));
			this.publickey.setPublicKey(publicKey);
			signature = ecdsa.sign();
		} catch (SignatureException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return signature;
	}

	public void prepareAccounting(AccountsMerkleTree accountsMerkleTree, ID initID) {
		// Fill in Publickey's Serial Number
		publickey.setID(txIn.getAddress().getID());

		// Update Publickey's isNew status if need
			if (!accountsMerkleTree.isPublicKeyExists(getPublickey())) {
				getPublickey().setNew(true);
			}

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
	}
	
	public boolean isAllAddressIDValid(AccountsMerkleTree accountsMerkleTree) {
		if(txIn.getAddress().getID().compareTo(accountsMerkleTree.getTotalAccountNumbers()) > 0) {
			return false;
		}
		for(TxOut txOut : txOutList) {
			if(txOut.getAddress().getID().compareTo(accountsMerkleTree.getTotalAccountNumbers()) > 0) {
				return false;
			}
		}
		return true;
	}
	
	public void prepareVerify(AccountsMerkleTree accountsMerkleTree, byte[] signature) throws IllegalStateException {
		// Fill in TxIn's ReadableAddress
		if(transactionType != TransactionType.COINBASE) {
			Account account = accountsMerkleTree.getAccount(txIn.getAddress().getID());
			txIn.getAddress().setReadableAddress(account.getAddress().getReadableAddress());
			// Check if Publickey is exists and fill in Publickey's ID and Publickey
			if(!account.isPublickeyExists()) {
				throw new IllegalStateException(account.toString() + "'s Publickey shouldn't be empty.");
			}
			else {
				publickey.setID(txIn.getAddress().getID());
				publickey.setPublicKey(accountsMerkleTree.getPublicKey(txIn.getAddress().getID()).getPublicKey());
			}
		}
		
		// Fill in TxOut's Address' ReadableAddress
		for (TxOut txOut : getTxOutList()) {
			txOut.getAddress().setReadableAddress(accountsMerkleTree.getAddress(txOut.getAddress().getID()).getReadableAddress());
		}
		
		// Fill in Signature
		this.signature = signature;
	}

	public boolean isNonceCorrect(AccountsMerkleTree accountsMerkleTree) {
		// Check if Nonce's value is correct
		if (!nonce.isNextID(accountsMerkleTree.getAccount(txIn.getAddress().getID()).getNonce())) {
			return false;
		}
		return true;
	}

	@Override
	public boolean isSanity(AddressShape... addressShape) {
		return false;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((nonce == null) ? 0 : nonce.hashCode());
		result = prime * result + ((publickey == null) ? 0 : publickey.hashCode());
		result = prime * result + Arrays.hashCode(signature);
		result = prime * result + ((transactionType == null) ? 0 : transactionType.hashCode());
		result = prime * result + ((txIn == null) ? 0 : txIn.hashCode());
		result = prime * result + ((txOutList == null) ? 0 : txOutList.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Transaction other = (Transaction) obj;
		if (nonce == null) {
			if (other.nonce != null)
				return false;
		} else if (!nonce.equals(other.nonce))
			return false;
		if (publickey == null) {
			if (other.publickey != null)
				return false;
		} else if (!publickey.equals(other.publickey))
			return false;
		if (!Arrays.equals(signature, other.signature))
			return false;
		if (transactionType != other.transactionType)
			return false;
		if (txIn == null) {
			if (other.txIn != null)
				return false;
		} else if (!txIn.equals(other.txIn))
			return false;
		if (txOutList == null) {
			if (other.txOutList != null)
				return false;
		} else if (!txOutList.equals(other.txOutList))
			return false;
		return true;
	}
	
	public void update(AccountsMerkleTree accountsMerkleTree) {
		// Update current Transaction's relevant Account's AccountsMerkleTree's data
		// Update current Transaction's TxIn Publickey if need
		if (publickey.isNew()) {
			accountsMerkleTree.savePublicKey(publickey, accountsMerkleTree.getHeight().getNextID());
		}

		// Update current Transaction's TxIn Account's Nonce&Balance
		Account account = accountsMerkleTree.getAccount(txIn.getAddress().getID());
		// Update current Transaction's TxIn Account's Nonce
		account.increaseNonce();
		// Update current Transaction's TxIn Account's Balance
		account.updateBalance(-getBillingValue());
		accountsMerkleTree.saveAccount(account);

		// Update current Transaction's TxOut Account
		for (TxOut txOut : txOutList) {
			account = null;
			if (txOut.isNew()) {
				account = new Account();
				account.setAddress(txOut.getAddress());
				account.setAddressCreateHeight(accountsMerkleTree.getHeight().getNextID());
			} else {
				account = accountsMerkleTree.getAccount(txOut.getAddress().getID());
			}
			account.updateBalance(txOut.getValue());
			account.setBalanceUpdateHeight(accountsMerkleTree.getHeight().getNextID());
			if(accountsMerkleTree.saveAccount(account) && txOut.isNew()) {
				accountsMerkleTree.increaseTotalAccountNumbers();
			}
		}
		
		// If is OperationTransaction
		if (this instanceof OperationTransaction) {
			OperationTransaction operationTransaction = (OperationTransaction) this;
			operationTransaction.execute(accountsMerkleTree, txIn.getAddress().getID());
		}
	}
}
