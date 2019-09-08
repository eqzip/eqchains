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

import java.awt.Window.Type;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Objects;
import java.util.Vector;

import org.rocksdb.RocksDBException;
import org.rocksdb.WriteBatch;

import com.eqchains.blockchain.account.Account;
import com.eqchains.blockchain.account.Asset;
import com.eqchains.blockchain.account.Passport;
import com.eqchains.blockchain.account.AssetAccount;
import com.eqchains.blockchain.account.CoinAsset;
import com.eqchains.blockchain.account.Publickey;
import com.eqchains.blockchain.account.Passport.AddressShape;
import com.eqchains.blockchain.accountsmerkletree.AccountsMerkleTree;
import com.eqchains.blockchain.subchain.EQCSubchain;
import com.eqchains.blockchain.subchain.EQcoinSubchain;
import com.eqchains.blockchain.transaction.Transaction.TXFEE_RATE;
import com.eqchains.blockchain.transaction.Transaction.TransactionType;
import com.eqchains.persistence.EQCBlockChainH2;
import com.eqchains.persistence.EQCBlockChainRocksDB;
import com.eqchains.persistence.EQCBlockChainH2.TRANSACTION_OP;
import com.eqchains.persistence.EQCBlockChainRocksDB.TABLE;
import com.eqchains.serialization.EQCTypable;
import com.eqchains.serialization.EQCType;
import com.eqchains.util.ID;
import com.eqchains.util.Log;
import com.eqchains.util.Util;
import com.eqchains.util.Util.AddressTool;
import com.eqchains.util.Util.AddressTool.AddressType;

/**
 * @author Xun Wang
 * @date Sep 28, 2018
 * @email 10509759@qq.com
 */
public class TransferTransaction extends Transaction {
	protected Vector<TxOut> txOutList;
	public final static int MIN_TXOUT = 1;
	public final static int MAX_TXOUT = 10;

	private void init() {
		txOutList = new Vector<TxOut>();
	}

	public TransferTransaction(TransactionType transactionType) {
		super(transactionType);
		init();
	}

	public TransferTransaction(byte[] bytes, Passport.AddressShape addressShape) throws NoSuchFieldException,
			IOException, UnsupportedOperationException, NoSuchFieldException, IllegalStateException {
		super(TransactionType.TRANSFER);
		EQCType.assertNotNull(bytes);
		init();
		ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		parseBody(is, addressShape);
		EQCType.assertNoRedundantData(is);
	}
	
	public boolean parseBody(ResultSet resultSet)
			throws NoSuchFieldException, IOException, NoSuchFieldException, IllegalStateException, SQLException {
			boolean isHandled = false;
			if (resultSet.getByte("op") == TRANSACTION_OP.TXOUT.ordinal()) {
				TxOut txOut = getTxOut(ID.valueOf(resultSet.getLong("id")));
				if(txOut != null) {
					txOut.setValue(resultSet.getLong("value"));
				}
				else {
					txOut = new TxOut();
					Passport passport = new Passport();
					passport.setID(ID.valueOf(resultSet.getLong("id")));
					txOut.setPassport(passport);
					txOut.setValue(resultSet.getLong("value"));
					addTxOut(txOut);
				}
				isHandled = true;
			} 
			else if (resultSet.getByte("op") == TRANSACTION_OP.PASSPORT.ordinal()) {
				TxOut txOut = getTxOut(ID.valueOf(resultSet.getLong("id")));
				if(txOut != null) {
					txOut.setNew(true);
					txOut.getPassport().setReadableAddress(AddressTool.AIToAddress(resultSet.getBytes("object")));
				}
				else {
					txOut = new TxOut();
					txOut.setNew(true);
					Passport passport = new Passport();
					passport.setID(ID.valueOf(resultSet.getLong("id")));
					passport.setReadableAddress(AddressTool.AIToAddress(resultSet.getBytes("object")));
					txOut.setPassport(passport);
					addTxOut(txOut);
				}
				isHandled = true;
			}
			else if (resultSet.getByte("op") == TRANSACTION_OP.PUBLICKEY.ordinal()) {
				compressedPublickey.setNew(true);
				compressedPublickey.setCompressedPublickey(resultSet.getBytes("object")); 
				compressedPublickey.setID(ID.valueOf(resultSet.getLong("id")));
				isHandled = true;
			}
			else if (resultSet.getByte("op") == TRANSACTION_OP.TXIN.ordinal()) {
				TxIn txIn = new TxIn();
				Passport passport = new Passport();
				passport.setID(ID.valueOf(resultSet.getLong("id")));
				txIn.setPassport(passport);
				txIn.setValue(resultSet.getLong("value"));
				isHandled = true;
			}
			return isHandled;
	}
	
	public TransferTransaction(ResultSet resultSet) throws NoSuchFieldException, IOException,
			UnsupportedOperationException, NoSuchFieldException, IllegalStateException, SQLException {
		super(TransactionType.TRANSFER);
		Objects.requireNonNull(resultSet);
		init();
		// Parse Body without nonce
		while (resultSet.next()) {
			parseBody(resultSet);
		}
	}

	public TransferTransaction() {
		super(TransactionType.TRANSFER);
		init();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.eqzip.eqcoin.blockchain.Transaction#getRPCBytes()
	 */
	@Override
	public byte[] getRPCBytes() {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			os.write(EQCType.bytesToBIN(getBytes(Passport.AddressShape.READABLE)));
			os.write(EQCType.bytesToBIN(compressedPublickey.getCompressedPublickey()));
			os.write(EQCType.bytesToBIN(signature));
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
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(signature);
		result = prime * result + ((txIn == null) ? 0 : txIn.hashCode());
		result = prime * result + ((txOutList == null) ? 0 : txOutList.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
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
		TransferTransaction other = (TransferTransaction) obj;
		if (!Arrays.equals(signature, other.signature))
			return false;
		if (txIn == null) {
			if (other.txIn != null)
				return false;
		} else if (!txIn.equals(other.txIn))
			return false;
		if (txOutList == null) {
			if (other.txOutList != null)
				return false;
		} else {
			// Temporarily save a copy of TxOut in order not to change the order in which
			// the user enters TxOut.
			Vector<TxOut> originalTxOutList = new Vector<TxOut>();
			for (TxOut txOut : txOutList) {
				originalTxOutList.add(txOut);
			}
			// Sort the temporarily saved TxOut in alphabetical dictionary order.
			Collections.sort(originalTxOutList);
			// Temporarily save a copy of TxOut in order not to change the order in which
			// the user enters TxOut.
			Vector<TxOut> targetTxOutList = new Vector<TxOut>();
			for (TxOut txOut : other.txOutList) {
				targetTxOutList.add(txOut);
			}
			// Sort the temporarily saved TxOut in alphabetical dictionary order.
			Collections.sort(targetTxOutList);
			// Compare temporarily saved TxOut collections sorted alphabetically in
			// alphabetical order.
			if (!originalTxOutList.equals(targetTxOutList))
				return false;
		}
		return true;
	}

	public String toInnerJson() {
		return

		"\"TransferTransaction\":" + "\n{\n" + txIn.toInnerJson() + ",\n" + "\"TxOutList\":" + "\n{\n" + "\"Size\":"
				+ "\"" + txOutList.size() + "\"" + ",\n" + "\"List\":" + "\n" + getTxOutString() + "\n},\n"
				+ "\"Nonce\":" + "\"" + nonce + "\"" + ",\n" + "\"Signature\":"
				+ ((signature == null) ? null : "\"" + Util.getHexString(signature) + "\"") + ",\n" + "\"Publickey\":"
				+ ((compressedPublickey == null || compressedPublickey.getCompressedPublickey() == null) ? null
						: "\"" + Util.getHexString(compressedPublickey.getCompressedPublickey()) + "\"")
				+ "\n" + "}";
	}

	/**
	 * 0. 验证Transaction的完整性： 0.1 对于CoinBase transaction至少包括一个TxOut。 0.2 对于非CoinBase
	 * transaction至少包括一个TxOut&一个TxIn。 1. 验证TxIn余额是否足够？
	 * 之前高度的余额减去当前EQCBlock中之前的交易记录中已经花费的余额。 2. 验证TxIn address是否有效&和公钥是否一致？ 3.
	 * 验证TxIn‘s block header‘s hash+bin(TxIn+TxOut）的签名能否通过？ 4.
	 * 验证TxHash是否在之前的区块中不存在，也即此交易是唯一的交易。防止重放攻击。
	 * 验证Signature在之前的区块中&当前的EQCBlock中不存在。防止重放攻击。 5. 验证TxOut的数量是不是大于等于1&小于等于10。 6.
	 * 验证TxOut的地址是不是唯一的存在？也即每个TxOut地址只能出现一次。 7. 验证TxOut是否小于TxIn？ 8.
	 * 验证是否TxFee大于零，验证是否所有的Txin&TxOut的Value大于零。 // 9. 验证TxFee是否足够？
	 * 
	 * @return
	 * @throws RocksDBException
	 * @throws IllegalStateException
	 * @throws IOException
	 * @throws NoSuchFieldException
	 * @throws Exception
	 */
	public boolean isValid(AccountsMerkleTree accountsMerkleTree, AddressShape addressShape)
			throws NoSuchFieldException, IllegalStateException, RocksDBException, IOException, Exception {

		if (!isSanity(addressShape)) {
			Log.Error("Sanity test failed");
			return false;
		}

		// Check if TxIn's relevant Account exists in history Hive
		if (!accountsMerkleTree.isAccountExists(txIn.getPassport(), false)) {
			Log.Error("TxIn's relevant Account doesn't exists");
			return false;
		}

		// Check Nonce is positive
		if (!isNoncePositive()) {
			Log.Error("isNoncePositive failed");
			return false;
		}

		// Check if Nonce is correct
		if (!isNonceCorrect(accountsMerkleTree)) {
			// In MVP phase just directly delete the Transaction which has the wrong nonce
			EQCBlockChainH2.getInstance().deleteTransactionInPool(this);
			Log.Error("Nonce doesn't correct, current: " + nonce + " expect: " + accountsMerkleTree
					.getAccount(txIn.getPassport().getID(), true).getAsset(getAssetID()).getNonce().getNextID());
			return false;
		}

		// Check if Publickey's ID is equal to TxIn's ID
		if (!compressedPublickey.getID().equals(txIn.getPassport().getID())) {
			Log.Error("Publickey's ID:" + compressedPublickey.getID() + " doesn't equal to TxIn's ID:"
					+ txIn.getPassport().getID());
			return false;
		}

		// Check if Publickey exists in Account and equal to current Publickey
		Account txInAccount = accountsMerkleTree.getAccount(txIn.getPassport(), true);
		if (txInAccount.isPublickeyExists()) {
			if (!Arrays.equals(txInAccount.getPublickey().getCompressedPublickey(),
					compressedPublickey.getCompressedPublickey())) {
				Log.Error("Publickey doesn't equal to current Publickey");
				return false;
			}
		} else {
			// Verify Publickey
			if (!AddressTool.verifyAddressPublickey(txIn.getPassport().getReadableAddress(),
					compressedPublickey.getCompressedPublickey())) {
				Log.Error("Verify Publickey failed");
				return false;
			}
		}

		// Check balance from previous height's Account history
		if (txIn.getValue() + Util.MIN_EQC > accountsMerkleTree.getAccount(txIn.getPassport(), false).getAsset(getAssetID()).getBalance().longValue()) {
			Log.Error("Balance isn't enough");
			return false;
		}

		// Check if the number of TxOut is greater than MIN_TXOUT and less than or equal
		// to MAX_TXOUT
		if (!isTxOutNumberValid()) {
			Log.Error("TxOut numbers:" + txOutList.size() + " is invalid");
			return false;
		}

		// Check if the TxOut's Address is unique
		if (!isTxOutAddressUnique()) {
			Log.Error("TxOut Address isn't unique");
			return false;
		}

		// Check if TxOut's Address is valid
		if (!isTxOutAddressValid()) {
			Log.Error("TxOut's Address is invalid");
			return false;
		}

		// Check if TxOut's Address doesn't include TxIn
		if (isTxOutAddressIncludeTxInAddress()) {
			Log.info("Txout's Address include TxIn this is invalid");
			return false;
		}

		// Check if all TxIn and TxOut's value is valid
		if (!isAllValueValid()) {
			Log.Error("isAllValueValid failed");
			return false;
		}

		// Check if TxFeeLimit is valid
		if (!isTxFeeLimitValid()) {
			Log.Error("isTxFeeLimitValid failed");
			return false;
		}

		// Verify if Transaction's signature can pass
		if (!verify(accountsMerkleTree)) {
			Log.Error("Transaction's signature verify failed");
			return false;
		}

		return true;
	}

	@Override
	public int getMaxBillingLength() {
		int length = 0;

		/**
		 * Transaction.getBytes(AddressShape.ID)'s length
		 */
		// Nonce
		length += EQCType.bigIntegerToEQCBits(nonce).length;
		// TxIn Serial Number length
		length += Util.BASIC_SERIAL_NUMBER_LEN;
		// TxIn value's length
		length += Util.BASIC_VALUE_NUMBER_LEN;
		// TxOut length
		for (TxOut txOut : txOutList) {
			length += Util.BASIC_SERIAL_NUMBER_LEN;
			length += EQCType.longToEQCBits(txOut.getValue()).length;
		}
		// For Binxx's overhead length
		length += EQCType.getEQCTypeOverhead(length);

		/**
		 * Transaction's relevant Passport length
		 */
		for (TxOut txOut : txOutList) {
			length += txOut.getPassport().getBillingSize();
		}

		/**
		 * Transaction's compressed Publickey length
		 */
		length += compressedPublickey.getBillingSize();

		/**
		 * Transaction's Signature length
		 */
		if (txIn.getPassport().getAddressType() == AddressType.T1) {
			length += Util.P256_BASIC_SIGNATURE_LEN;
		} else if (txIn.getPassport().getAddressType() == AddressType.T2) {
			length += Util.P521_BASIC_SIGNATURE_LEN;
		}
//		Log.info("Total length: " + length);
		return length;
	}

//	@Override
//	public int getBillingSize() {
//		int size = 0;
//
//		// Transaction's Serial Number format's size which storage in the EQC Blockchain
//		size += getBin().length;
//		Log.info("ID size: " + size);
//
//		// Transaction's AddressList size which storage the new Address
//		for (TxOut txOut : txOutList) {
//			if (txOut.isNew()) {
//				size += txOut.getAddress().getBin(AddressShape.AI).length;
//				Log.info("New TxOut: " + txOut.getAddress().getBin(AddressShape.AI).length);
//			}
//		}
//
//		// Transaction's PublickeyList size
//		if (publickey.isNew()) {
//			size += publickey.getBin().length;
//			Log.info("New Publickey: " + publickey.getBin().length);
//		}
//
//		// Transaction's Signature size
//		size += EQCType.bytesToBIN(signature).length;
//		Log.info("Signature size: " + EQCType.bytesToBIN(signature).length);
//
//		Log.info("Total size: " + size);
//		return size;
//	}

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
			// Serialization body
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

	protected boolean isBasicSanity(AddressShape addressShape) {
		if (transactionType == null || nonce == null || txIn == null || txOutList == null || compressedPublickey == null
				|| signature == null) {
			return false;
		}

		if (!getAssetID().equals(Asset.EQCOIN)) {
			return false;
		}

		if (!compressedPublickey.isSanity()) {
			return false;
		}

		if (!isTxOutNumberValid()) {
			return false;
		}

		if (!txIn.isSanity(addressShape)) {
			return false;
		} else {
			if (!txIn.getPassport().isGood()) {
				return false;
			}
			if (txIn.getPassport().getAddressType() != AddressType.T1
					&& txIn.getPassport().getAddressType() != AddressType.T2) {
				return false;
			}
		}
		for (TxOut txOut : txOutList) {
			if (!txOut.isSanity(addressShape)) {
				return false;
			}
			if (txOut.getPassport().getAddressType() != AddressType.T1
					&& txOut.getPassport().getAddressType() != AddressType.T2) {
				return false;
			}
		}

		return true;
	}

	@Override
	public boolean isSanity(AddressShape addressShape) {
		if (!isBasicSanity(addressShape)) {
			return false;
		}
		if (transactionType != TransactionType.TRANSFER) {
			return false;
		}
		return true;
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
		if (txIn == null) {
			return false;
		}
		return txOut.getPassport().getReadableAddress().equals(txIn.getPassport().getReadableAddress());
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
//		Log.info("getTxFeeLimit(): " + getTxFeeLimit());
//		Log.info("getMaxTxFeeLimit(): " + getMaxTxFeeLimit());
//		Log.info("getDefaultTxFeeLimit(): " + getDefaultTxFeeLimit());
		boolean boolIsValid = true;
		if (getTxFeeLimit() < getDefaultTxFeeLimit()) {
			boolIsValid = false;
		}
//		else if ((getTxFeeLimit() <= getMaxTxFeeLimit()) && (getTxFeeLimit() % getDefaultTxFeeLimit()) != 0) {
//			boolIsValid = false;
//		}
		return boolIsValid;
	}

	public long getBillingValue() {
		Log.info("TxFee: " + getTxFee() + " TxOutValues: " + getTxOutValues() + " TxFeeLimit: " + getTxFeeLimit());
		return getTxFee() + getTxOutValues();
	}

	public void setTxFeeLimit(TXFEE_RATE txfee_rate) {
		txIn.setValue(getTxOutValues() + cypherTxFeeLimit(txfee_rate));
	}

	public void cypherTxInValue(TXFEE_RATE txfee_rate) {
		txIn.setValue(getTxOutValues() + cypherTxFeeLimit(txfee_rate));
	}

	public boolean isTxOutAddressExists(TxOut txOut) {
		boolean boolIsExists = false;
		for (TxOut txOut2 : txOutList) {
			if (txOut2.getPassport().getReadableAddress().equals(txOut.getPassport().getReadableAddress())) {
				boolIsExists = true;
//				Log.info("TxOutAddressExists" + " a: " + txOut2.getAddress().getAddress() + " b: " + txOut.getAddress().getAddress());
				break;
			}
		}
		return boolIsExists;
	}
	
	public TxOut getTxOut(ID id) {
		TxOut txOut = null;
		for (TxOut txOut2 : txOutList) {
			if (txOut2.getPassport().getID().equals(id)) {
				txOut = txOut2;
				break;
			}
		}
		return txOut;
	}

	public boolean isTxOutAddressUnique() {
		for (int i = 0; i < txOutList.size(); ++i) {
			for (int j = i + 1; j < txOutList.size(); ++j) {
				if (txOutList.get(i).getPassport().equals(txOutList.get(j).getPassport())) {
					return false;
				}
			}
		}
		return true;
	}

	public int getBillingSize() {
		int size = 0;

		// Transaction's ID format's size which storage in the EQC Blockchain
		size += getBin(AddressShape.ID).length;
//		Log.info("ID size: " + size);

		// Transaction's AddressList size which storage the new Address
		for (TxOut txOut : txOutList) {
			if (txOut.isNew()) {
				size += txOut.getBin(AddressShape.AI).length;
//				Log.info("New TxOut: " + txOut.getBin(AddressShape.AI).length);
			}
		}

		// Transaction's PublickeyList size
		if (compressedPublickey.isNew()) {
			size += compressedPublickey.getBin().length;
//			Log.info("New Publickey: " + publickey.getBin().length);
		}

		// Transaction's Signature size
		size += EQCType.bytesToBIN(signature).length;
//		Log.info("Signature size: " + EQCType.bytesToBIN(signature).length);
//		Log.info("Total size: " + size);
		return size;
	}

	public boolean isTxOutAddressValid() {
		for (TxOut txOut : txOutList) {
			if (!txOut.getPassport().isGood(null)) {
				return false;
			}
		}
		return true;
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

	public boolean isAllAddressIDValid(AccountsMerkleTree accountsMerkleTree) {
		if (!super.isAllAddressIDValid(accountsMerkleTree)) {
			return false;
		}
		for (TxOut txOut : txOutList) {
			if (txOut.getPassport().getID().compareTo(accountsMerkleTree.getTotalAccountNumbers()) > 0) {
				return false;
			}
		}
		return true;
	}

	public void prepareAccounting(AccountsMerkleTree accountsMerkleTree, ID initID) throws Exception {
		Account txInAccount = accountsMerkleTree.getAccount(txIn.getPassport(), true);
		// Fill in TxIn's Address
		txIn.getPassport().setID(txInAccount.getID());
		// Fill in Publickey's Serial Number
		compressedPublickey.setID(txIn.getPassport().getID());

		// Update Publickey's isNew status if need
		if (!txInAccount.isPublickeyExists()) {
			compressedPublickey.setNew(true);
		}

		// Update TxOut's Address' isNew status if need
		for (TxOut txOut : txOutList) {
			if (!accountsMerkleTree.isAccountExists(txOut.getPassport(), true)) {
				txOut.getPassport().setID(initID);
				txOut.setNew(true);
				initID = initID.getNextID();
			} else {
				// For security issue need retrieve and fill in every Address' ID
				// according to it's AddressAI
				txOut.getPassport().setID(accountsMerkleTree.getAccount(txOut.getPassport(), true).getID());
			}
		}
	}

	public void prepareVerify(AccountsMerkleTree accountsMerkleTree, EQCSubchain eqcSubchain) throws Exception {
		EQcoinSubchain eQcoinSubchain = (EQcoinSubchain) eqcSubchain;
		Account txInAccount = accountsMerkleTree.getAccount(txIn.getPassport().getID(), true);
		// Fill in TxIn's ReadableAddress
		txIn.getPassport().setReadableAddress(txInAccount.getPassport().getReadableAddress());

		// Update Publickey's isNew status if need
		if (!txInAccount.isPublickeyExists()) {
			compressedPublickey.setNew(true);
			compressedPublickey.setID(txIn.getPassport().getID());
			compressedPublickey.setCompressedPublickey(
					eQcoinSubchain.getCompressedPublickey(txIn.getPassport().getID()).getCompressedPublickey());
		} else {
			compressedPublickey.setID(txIn.getPassport().getID());
			compressedPublickey.setCompressedPublickey(txInAccount.getPublickey().getCompressedPublickey());
		}

		// Update TxOut's Address' isNew status if need
		Account account = null;
		for (TxOut txOut : txOutList) {
			account = accountsMerkleTree.getAccount(txOut.getPassport().getID(), true);
			if (account == null) {
				txOut.getPassport().setReadableAddress(
						eQcoinSubchain.getPassport(txOut.getPassport().getID()).getReadableAddress());
				txOut.setNew(true);
			} else {
				// For security issue need retrieve and fill in every Address' AddressAI
				// according to it's ID
				txOut.getPassport().setReadableAddress(account.getPassport().getReadableAddress());
			}
		}
	}

	public void update(AccountsMerkleTree accountsMerkleTree) throws Exception {
		// Update current Transaction's relevant Account's AccountsMerkleTree's data
		// Update current Transaction's TxIn Account's relevant Asset's Nonce&Balance
		Account account = accountsMerkleTree.getAccount(txIn.getPassport().getID(), true);
		// Update current Transaction's TxIn Account's relevant Asset's Nonce
		account.getAsset(getAssetID()).increaseNonce();
		// Update current Transaction's TxIn Account's relevant Asset's Balance
		account.getAsset(getAssetID()).withdraw(new ID(getBillingValue()));
		account.getAsset(getAssetID()).setBalanceUpdateHeight(accountsMerkleTree.getHeight());
		// Update current Transaction's TxIn Publickey if need
		if (compressedPublickey.isNew()) {
			Publickey publickey = new Publickey();
			publickey.setCompressedPublickey(compressedPublickey.getCompressedPublickey());
			publickey.setPublickeyCreateHeight(accountsMerkleTree.getHeight());
			account.setPublickey(publickey);
		}
		account.setUpdateHeight(accountsMerkleTree.getHeight());
		accountsMerkleTree.saveAccount(account);

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
				account = accountsMerkleTree.getAccount(txOut.getPassport().getID(), true);
			}
			account.getAsset(getAssetID()).deposit(new ID(txOut.getValue()));
			account.getAsset(getAssetID()).setBalanceUpdateHeight(accountsMerkleTree.getHeight());
			account.setUpdateHeight(accountsMerkleTree.getHeight());
			accountsMerkleTree.saveAccount(account);
		}
	}

	public void parseBody(ByteArrayInputStream is, AddressShape addressShape)
			throws NoSuchFieldException, IOException, NoSuchFieldException, IllegalStateException {

		super.parseBody(is, addressShape);

		byte txOutCount = 0;
		// Parse TxOut
		while (txOutCount < MAX_TXOUT && !EQCType.isInputStreamEnd(is)) {
			TxOut txOut = new TxOut(is, addressShape);
			// Add TxOut
			txOutList.add(txOut);
		}
	}

	@Override
	public byte[] getBodyBytes(AddressShape addressShape) {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			// Serialization Super
			os.write(super.getBodyBytes(addressShape));
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
