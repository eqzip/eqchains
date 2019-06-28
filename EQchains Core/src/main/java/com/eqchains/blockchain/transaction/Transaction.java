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
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Signature;
import java.security.SignatureException;
import java.security.acl.Owner;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Vector;

import org.rocksdb.RocksDBException;

import com.eqchains.blockchain.PublicKey;
import com.eqchains.blockchain.account.Account;
import com.eqchains.blockchain.account.Passport;
import com.eqchains.blockchain.account.Asset;
import com.eqchains.blockchain.account.AssetAccount;
import com.eqchains.blockchain.account.Passport.AddressShape;
import com.eqchains.blockchain.accountsmerkletree.AccountsMerkleTree;
import com.eqchains.blockchain.transaction.Transaction.TransactionType;
import com.eqchains.crypto.EQCPublicKey;
import com.eqchains.keystore.Keystore.ECCTYPE;
import com.eqchains.persistence.h2.EQCBlockChainH2;
import com.eqchains.serialization.EQCAddressShapeInheritable;
import com.eqchains.serialization.EQCAddressShapeTypable;
import com.eqchains.serialization.EQCInheritable;
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
public abstract class Transaction implements Comparator<Transaction>, Comparable<Transaction>, EQCAddressShapeTypable, EQCAddressShapeInheritable {
	/**
	 * Header
	 */
	protected ID solo;
	protected TransactionType transactionType;
	/**
	 * Body
	 */
	protected ID nonce;
	protected TxIn txIn;
	protected PublicKey publickey;
	protected byte[] signature;
	public final static ID SOLO = ID.ZERO;
	
	public enum TransactionType {
		COINBASE, TRANSFER, OPERATION, ASSETSUBCHAIN, MISCSMARTCONTRACT, INVALID;
		public static TransactionType get(int ordinal) {
			TransactionType transactionType = null;
			switch (ordinal) {
			case 0:
				transactionType = TransactionType.COINBASE;
				break;
			case 1:
				transactionType = TransactionType.TRANSFER;
				break;
			case 2:
				transactionType = TransactionType.OPERATION;
				break;
			case 3:
				transactionType = TransactionType.ASSETSUBCHAIN;
				break;
			case 4:
				transactionType = TransactionType.MISCSMARTCONTRACT;
				break;
			default:
				transactionType = TransactionType.INVALID;
				break;
			}
			return transactionType;
		}
		public byte[] getEQCBits() {
			return EQCType.intToEQCBits(this.ordinal());
		}
	}

	public ID getAssetID() {
		ID assetID = null;
		if (transactionType == TransactionType.COINBASE || transactionType == TransactionType.TRANSFER
				|| transactionType == TransactionType.OPERATION
				|| transactionType == TransactionType.MISCSMARTCONTRACT) {
			assetID = Asset.EQCOIN;
		} else if (transactionType == TransactionType.ASSETSUBCHAIN) {
			assetID = getSubchainID();
		}
		return assetID;
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

	public Transaction(TransactionType transactionType) {
		this.transactionType = transactionType;
	}
	
	public Transaction(byte[] bytes, Passport.AddressShape addressShape) throws NoSuchFieldException, IOException, NoSuchFieldException, IllegalStateException {
	}
	
	private ID getSubchainID() {
		ID subchainID = null;
		if(transactionType == TransactionType.TRANSFER) {
			subchainID = Asset.EQCOIN;
		}
		return subchainID;
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
	 * @param publickey the publickey to set
	 */
	public void setPublickey(PublicKey publickey) {
		this.publickey = publickey;
	}

	/**
	 * @return the nonce
	 */
	public ID getNonce() {
		return nonce;
	}

	/**
	 * @param nonce the nonce to set
	 * @throws NoSuchFieldException 
	 */
	public void setNonce(ID nonce) {
		this.nonce = nonce;
	}

	@Override
	public int compareTo(Transaction o) {
		// TODO Auto-generated method stub
		int nResult = 0;
		if ((nResult = this.getTransactionType().compareTo(o.getTransactionType())) == 0) {
			if ((nResult = this.getSubchainID().compareTo(o.getSubchainID())) == 0) {
				if ((nResult = this.getQosRate().compareTo(o.getQosRate())) == 0) {
					if ((nResult = this.txIn.getPassport().getID().compareTo(o.getTxIn().getPassport().getID())) == 0) {
						nResult = this.nonce.compareTo(o.getNonce());
					}
				}
			}
		}
		if (nResult != 0) {
			if (nResult < 0) {
				nResult = 1;
			} else {
				nResult = -1;
			}
		}
		return nResult;
	}

	@Override
	public int compare(Transaction o1, Transaction o2) {
		// TODO Auto-generated method stub
		int nResult = 0;
		if ((nResult = o1.getTransactionType().compareTo(o2.getTransactionType())) == 0) {
			if ((nResult = o1.getSubchainID().compareTo(o2.getSubchainID())) == 0) {
				if ((nResult = o1.getQosRate().compareTo(o2.getQosRate())) == 0) {
					if ((nResult = o1.txIn.getPassport().getID().compareTo(o2.getTxIn().getPassport().getID())) == 0) {
						nResult = o1.nonce.compareTo(o2.getNonce());
					}
				}
			}
		}
		if (nResult != 0) {
			if (nResult < 0) {
				nResult = 1;
			} else {
				nResult = -1;
			}
		}
		return nResult;
	}

	public byte[] getRPCBytes() {
		return null;
	}

	public boolean isNoncePositive() {
		return nonce.signum() >= 0;
	}
	
	public boolean isAllValueValid() {
		return false;
	}
	
	public long getTxFeeLimit() {
		return 0;
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
		return (getMaxBillingSize() * TXFEE_RATE.POSTPONE0.getRate() * Util.TXFEE_RATE);
	}

	public long getDefaultTxFeeLimit() {
		return (getMaxBillingSize() * Util.TXFEE_RATE);
	}

	public int getMaxBillingSize() {
		return 0;
	}

	public long getTxFee() {
		long txFee = 0;
		if (getTxFeeLimit() > getMaxTxFeeLimit()) {
			txFee = getTxFeeLimit();
		} else {
			txFee = getBillingSize() * getQos().getRate() * Util.TXFEE_RATE;
		}
		return txFee;
	}

	public long getBillingValue() {
		return 0;
	}

	public void setTxFeeLimit(TXFEE_RATE txfee_rate) {
	}

	public long cypherTxFeeLimit(TXFEE_RATE txfee_rate) {
		return (getMaxBillingSize() * txfee_rate.getRate() * Util.TXFEE_RATE);
	}

	public void cypherTxInValue(TXFEE_RATE txfee_rate) {
	}
	
	public int getBillingSize() {
		return 0;
	}

	public TXFEE_RATE getQos() {
		int rate = 1;
		if (getTxFeeLimit() > getMaxTxFeeLimit()) {
			rate = TXFEE_RATE.POSTPONEVIP.getRate();
		} else {
			rate = (int) (getTxFeeLimit() / (getMaxBillingSize() * Util.TXFEE_RATE));
		}
		return TXFEE_RATE.get(rate);
	}
	
	public ID getQosRate() {
		long rate = 1;
		if (getTxFeeLimit() > getMaxTxFeeLimit()) {
			rate = TXFEE_RATE.POSTPONEVIP.getRate() + getTxFeeLimit() - getMaxTxFeeLimit();
		} else {
			rate = (int) (getTxFeeLimit() / (getMaxBillingSize() * Util.TXFEE_RATE));
		}
		return new ID(rate);
	}

	public boolean verify(AccountsMerkleTree accountsMerkleTree) throws NoSuchFieldException, IllegalStateException, RocksDBException, IOException, ClassNotFoundException, SQLException {
		boolean isValid = false;
		if (publickey.isValid(accountsMerkleTree)) {
			isValid = verifySignature(accountsMerkleTree.getEQCHeaderHash(txIn.getPassport().getID()));
		}
		return isValid;
	}

	public boolean isValid(AccountsMerkleTree accountsMerkleTree, AddressShape addressShape) throws NoSuchFieldException, IllegalStateException, RocksDBException, IOException, Exception {
		return false;
	}

	public boolean isCoinBase() {
		return (transactionType == TransactionType.COINBASE);
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
		ID solo = null;
		try {
			if ((data = EQCType.parseEQCBits(is)) != null) {
				solo = EQCType.eqcBitsToID(data);
			}
			if (solo.equals(SOLO)) {
				if ((data = EQCType.parseEQCBits(is)) != null) {
					transactionType = TransactionType.get(EQCType.eqcBitsToInt(data));
				}
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

	public static Transaction parseTransaction(byte[] bytes, Passport.AddressShape addressShape) {
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
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return transaction;
	}

	public boolean verifySignature(byte[] signature_hash) {
		boolean isTransactionValid = false;
		Signature signature = null;

		// Verify Signature
		try {
			signature = Signature.getInstance("SHA1withECDSA", "SunEC");
			ECCTYPE eccType = null;
			if (txIn.getPassport().getAddressType() == AddressType.T1) {
				eccType = ECCTYPE.P256;
			} else if (txIn.getPassport().getAddressType() == AddressType.T2) {
				eccType = ECCTYPE.P521;
			}
			EQCPublicKey eqPublicKey = new EQCPublicKey(eccType);
			// Create EQPublicKey according to java Publickey
			eqPublicKey.setECPoint(publickey.getPublicKey());
//			Log.info(Util.dumpBytesBigEndianBinary(eqPublicKey.getEncoded()));
			signature.update(txIn.getPassport().getIDEQCBits());
			signature.update(Util.SINGULARITY);
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			os.write(signature_hash);
			os.write(getBytes(AddressShape.READABLE));
			signature.update(Util.EQCCHA_MULTIPLE_DUAL(os.toByteArray(), Util.HUNDREDPULS, true, false));
			signature.update(Util.SINGULARITY);
			signature.update(nonce.getEQCBits());
			isTransactionValid = signature.verify(this.signature);
		} catch (NoSuchAlgorithmException | NoSuchProviderException | SignatureException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return isTransactionValid;
	}

	public byte[] sign(Signature ecdsa, byte[] signature_hash) {
		try {
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			ecdsa.update(txIn.getPassport().getIDEQCBits());
			ecdsa.update(Util.SINGULARITY);
			os.write(signature_hash);
			os.write(getBytes(AddressShape.READABLE));
			ecdsa.update(Util.EQCCHA_MULTIPLE_DUAL(os.toByteArray(), Util.HUNDREDPULS, true, false));
			ecdsa.update(Util.SINGULARITY);
			ecdsa.update(nonce.getEQCBits());
			signature = ecdsa.sign();
		} catch (SignatureException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return signature;
	}

	public boolean isAllAddressIDValid(AccountsMerkleTree accountsMerkleTree) {
		if(txIn.getPassport().getID().compareTo(accountsMerkleTree.getTotalAccountNumbers()) > 0) {
			return false;
		}
		return true;
	}
	
	public void prepareVerify(AccountsMerkleTree accountsMerkleTree, byte[] signature) throws IllegalStateException, NoSuchFieldException, RocksDBException, IOException, ClassNotFoundException, SQLException {
		// Fill in TxIn's ReadableAddress
		if(transactionType != TransactionType.COINBASE) {
			Account account = accountsMerkleTree.getAccount(txIn.getPassport().getID());
			txIn.getPassport().setReadableAddress(account.getPassport().getReadableAddress());
			// Check if Publickey is exists and fill in Publickey's ID and Publickey
			if(!account.isPublickeyExists()) {
				throw new IllegalStateException(account.toString() + "'s Publickey shouldn't be empty.");
			}
			else {
				publickey.setID(txIn.getPassport().getID());
				publickey.setPublicKey(accountsMerkleTree.getPublicKey(txIn.getPassport().getID()).getPublicKey());
			}
		}
		// Fill in Signature
		this.signature = signature;
	}

	public boolean isNonceCorrect(AccountsMerkleTree accountsMerkleTree) throws NoSuchFieldException, IllegalStateException, RocksDBException, IOException, ClassNotFoundException, SQLException {
		// Check if Nonce's value is correct
		if (!nonce.isNextID(accountsMerkleTree.getAccount(txIn.getPassport().getID()).getAsset(getAssetID()).getNonce())) {
			return false;
		}
		return true;
	}

//	/* (non-Javadoc)
//	 * @see java.lang.Object#hashCode()
//	 */
//	@Override
//	public int hashCode() {
//		final int prime = 31;
//		int result = 1;
//		result = prime * result + ((nonce == null) ? 0 : nonce.hashCode());
//		result = prime * result + ((publickey == null) ? 0 : publickey.hashCode());
//		result = prime * result + Arrays.hashCode(signature);
//		result = prime * result + ((transactionType == null) ? 0 : transactionType.hashCode());
//		result = prime * result + ((txIn == null) ? 0 : txIn.hashCode());
//		result = prime * result + ((txOutList == null) ? 0 : txOutList.hashCode());
//		return result;
//	}
//
//	/* (non-Javadoc)
//	 * @see java.lang.Object#equals(java.lang.Object)
//	 */
//	@Override
//	public boolean equals(Object obj) {
//		if (this == obj)
//			return true;
//		if (obj == null)
//			return false;
//		if (getClass() != obj.getClass())
//			return false;
//		Transaction other = (Transaction) obj;
//		if (nonce == null) {
//			if (other.nonce != null)
//				return false;
//		} else if (!nonce.equals(other.nonce))
//			return false;
//		if (publickey == null) {
//			if (other.publickey != null)
//				return false;
//		} else if (!publickey.equals(other.publickey))
//			return false;
//		if (!Arrays.equals(signature, other.signature))
//			return false;
//		if (transactionType != other.transactionType)
//			return false;
//		if (txIn == null) {
//			if (other.txIn != null)
//				return false;
//		} else if (!txIn.equals(other.txIn))
//			return false;
//		if (txOutList == null) {
//			if (other.txOutList != null)
//				return false;
//		} else if (!txOutList.equals(other.txOutList))
//			return false;
//		return true;
//	}
	
	public void update(AccountsMerkleTree accountsMerkleTree) throws Exception {
		// Update current Transaction's relevant Account's AccountsMerkleTree's data
		// Update current Transaction's TxIn Publickey if need
		if (publickey.isNew()) {
			accountsMerkleTree.savePublicKey(publickey, accountsMerkleTree.getHeight().getNextID());
		}

		// Update current Transaction's TxIn Account's relevant Asset's Nonce&Balance
		Account account = accountsMerkleTree.getAccount(txIn.getPassport().getID());
		// Update current Transaction's TxIn Account's relevant Asset's Nonce
		account.getAsset(getAssetID()).increaseNonce();
		// Update current Transaction's TxIn Account's relevant Asset's Balance
		account.getAsset(getAssetID()).getBalance().subtract(new ID(getBillingValue()));
		accountsMerkleTree.saveAccount(account);
	}

	public void parseHeader(ByteArrayInputStream is, AddressShape addressShape) throws NoSuchFieldException, IOException {
		// Parse Solo
		solo = EQCType.eqcBitsToID(EQCType.parseEQCBits(is));
		// Parse Transaction type
		transactionType = TransactionType.get(EQCType.eqcBitsToInt(EQCType.parseEQCBits(is)));
	}
	public void parseBody(ByteArrayInputStream is) throws NoSuchFieldException, IOException {
	}

	@Override
	public void parseBody(ByteArrayInputStream is, AddressShape addressShape)
			throws NoSuchFieldException, IOException, NoSuchFieldException, IllegalStateException {
		// Parse nonce
		nonce = new ID(EQCType.parseEQCBits(is));
		// Parse TxIn
		txIn = new TxIn(is, addressShape);
	}
	
	public void prepareAccounting(AccountsMerkleTree accountsMerkleTree, ID accountListInitId) throws Exception {
		// TODO Auto-generated method stub
		
	}

	public Vector<TxOut> getTxOutList() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isTxOutAddressValid() {
		// TODO Auto-generated method stub
		return true;
	}

	public long getTxOutValues() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	public byte[] getBytes(AddressShape addressShape) {
		return null;
	}
	public byte[] getBin(AddressShape addressShape) {
		return null;
	}
	public boolean isSanity(AddressShape addressShape) {
		return false;
	}
	
	public byte[] getHeaderBytes(AddressShape addressShape) {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			// Serialization Solo
			os.write(solo.getEQCBits());
			// Serialization Transaction type
			os.write(transactionType.getEQCBits());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return os.toByteArray();
	}
	
	public byte[] getBodyBytes(AddressShape addressShape) {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			// Serialization nonce
			os.write(EQCType.bigIntegerToEQCBits(nonce));
			// Serialization TxIn
//			Log.info(Util.dumpBytes(txIn.getBin(addressShape), 16));
//			Log.info("Len: " + txIn.getBin(addressShape).length);
			os.write(txIn.getBytes(addressShape));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return os.toByteArray();
	}
	
	public byte[] getProof() {
		byte[] bytes = new byte[5];
		if(signature.length < Util.P256_BASIC_SIGNATURE_LEN) {
			bytes[0] = signature[9];
			bytes[1] = signature[33];
			bytes[2] = signature[41];
			bytes[3] = signature[51];
			bytes[4] = signature[65];
		}
		else {
			bytes[0] = signature[33];
			bytes[1] = signature[51];
			bytes[2] = signature[87];
			bytes[3] = signature[99];
			bytes[4] = signature[101];
		}
		return bytes;
	}
	
}
