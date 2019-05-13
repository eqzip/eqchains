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
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

import com.eqchains.blockchain.Account;
import com.eqchains.blockchain.Account.Asset;
import com.eqchains.blockchain.AccountsMerkleTree;
import com.eqchains.blockchain.transaction.Address.AddressShape;
import com.eqchains.persistence.h2.EQCBlockChainH2;
import com.eqchains.persistence.rocksdb.EQCBlockChainRocksDB;
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

	private void init() {
		assetID = Asset.EQCOIN;
	}
	
	public TransferTransaction(TransactionType transactionType) {
		super(transactionType);
		init();
	}

	public TransferTransaction(byte[] bytes, Address.AddressShape addressShape)
			throws NoSuchFieldException, IOException, UnsupportedOperationException {
		super(TransactionType.TRANSFER);
		init();
		ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		byte[] data = null;

		if (addressShape == Address.AddressShape.ID) {
			// Parse nonce
			if ((data = EQCType.parseEQCBits(is)) != null && !EQCType.isNULL(data)) {
				nonce = new ID(data);
			}

			// Parse TxIn
			data = null;
			// Parse TxIn address
			if ((data = EQCType.parseEQCBits(is)) != null && !EQCType.isNULL(data)) {
				txIn.getAddress().setID(new ID(data));
			}
			// Parse TxIn value
			data = null;
			if ((data = EQCType.parseEQCBits(is)) != null && !EQCType.isNULL(data)) {
				txIn.setValue(EQCType.eqcBitsToLong(data));
			}

			// Parse TxOut
			data = null;
			while ((data = EQCType.parseEQCBits(is)) != null && !EQCType.isNULL(data)) {
				TxOut txOut = new TxOut();
				// Parse TxOut address
				txOut.getAddress().setID(new ID(EQCType.eqcBitsToBigInteger(data)));
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
			// Parse nonce
			data = null;
			if ((data = EQCType.parseEQCBits(is)) != null && !EQCType.isNULL(data)) {
				nonce = new ID(data);
			}

			// Parse TxIn
			// Parse TxIn address
			data = null;
			if ((data = EQCType.parseBIN(is)) != null && !EQCType.isNULL(data)) {
//				if (addressShape == Address.AddressShape.READABLE) {
//					Log.info(Util.dumpBytes(data, 16));
//					Log.info("data Len: " + data.length);
//					txIn.getAddress().setReadableAddress(EQCType.bytesToASCIISting(data));
//					Log.info(txIn.getAddress().getReadableAddress());
//					Log.info("" + txIn.getAddress().getReadableAddress().length());
//				} else if (addressShape == Address.AddressShape.AI) {
//					txIn.getAddress().setReadableAddress(Util.AddressTool.AIToAddress(data));
//				}
				txIn = new TxIn(data, addressShape);
//				Log.info(txIn.getAddress().getReadableAddress());
//				Log.info("" + txIn.getAddress().getReadableAddress().length());
			}
//			// Parse TxIn value
//			data = null;
//			if ((data = EQCType.parseEQCBits(is)) != null && !EQCType.isNULL(data)) {
//				txIn.setValue(EQCType.eqcBitsToLong(data));
//			}

			// Parse TxOut
			data = null;
			while ((data = EQCType.parseBIN(is)) != null && !EQCType.isNULL(data)) {
				TxOut txOut = new TxOut(data, addressShape);
//				// Parse TxOut address
//				if (addressShape == Address.AddressShape.READABLE) {
//					txOut.getAddress().setReadableAddress(EQCType.bytesToASCIISting(data));
//				} else if (addressShape == Address.AddressShape.AI) {
//					txOut.getAddress().setReadableAddress(Util.AddressTool.AIToAddress(data));
//				}
//				// Parse TxOut value
//				data = null;
//				if ((data = EQCType.parseEQCBits(is)) != null && !EQCType.isNULL(data)) {
//					txOut.setValue(EQCType.eqcBitsToLong(data));
//				}
				// Add TxOut
				txOutList.add(txOut);
				data = null;
			}
		}
	}

	public TransferTransaction() {
		super(TransactionType.TRANSFER);
	}

	public static boolean isValid(byte[] bytes, AddressShape addressShape) throws NoSuchFieldException, IOException {
		ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		byte[] data;
		byte txOutValidCount = 0;
		boolean isTxInValid = false, isTxOutValid = false, isNonceValid = false;

		if (addressShape == Address.AddressShape.ID) {
			// Parse nonce
			if ((data = EQCType.parseEQCBits(is)) != null && !EQCType.isNULL(data)) {
				isNonceValid = true;
			}

			// Parse TxIn
			// Parse TxIn address
			data = null;
			if ((data = EQCType.parseEQCBits(is)) != null && !EQCType.isNULL(data)) {
				// Parse TxIn value
				data = null;
				if ((data = EQCType.parseEQCBits(is)) != null && !EQCType.isNULL(data)) {
					isTxInValid = true;
				}
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
				// Add TxOut
				data = null;
			}
		} else if (addressShape == Address.AddressShape.READABLE || addressShape == Address.AddressShape.AI) {
			// Parse nonce
			data = null;
			if ((data = EQCType.parseEQCBits(is)) != null && !EQCType.isNULL(data)) {
				isNonceValid = true;
			}

			// Parse TxIn
			data = null;
			if ((data = EQCType.parseBIN(is)) != null && !EQCType.isNULL(data)) {
//				// Parse TxIn address
//				// Parse TxIn value
//				data = null;
//				if ((data = EQCType.parseEQCBits(is)) != null && !EQCType.isNULL(data)) {
//					isTxInValid = true;
//				}
				if(TxIn.isValid(data, addressShape)) {
					isTxInValid = true;
				}
			}

			// Parse TxOut
			data = null;
			while ((data = EQCType.parseBIN(is)) != null && !EQCType.isNULL(data)) {
//				// Parse TxOut address
//				isTxOutValid = false;
//				// Parse TxOut value
//				data = null;
//				if ((data = EQCType.parseEQCBits(is)) != null && !EQCType.isNULL(data)) {
//					isTxOutValid = true;
//					++txOutValidCount;
//				}
//				// Add TxOut
//				data = null;
				if(TxOut.isValid(data, addressShape)) {
					isTxOutValid = true;
					++txOutValidCount;
				}
				else {
					isTxOutValid = false;
					break;
				}
			}
		}

		return isTxInValid && (txOutValidCount >= MIN_TXOUT) && (txOutValidCount <= MAX_TXOUT) && isTxOutValid
				&& isNonceValid && EQCType.isInputStreamEnd(is);
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
			os.write(EQCType.bytesToBIN(getBytes(Address.AddressShape.READABLE)));
			os.write(EQCType.bytesToBIN(publickey.getPublicKey()));
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

		"\"TransferTransaction\":" + "\n{\n" + txIn.toInnerJson() + ",\n"
				+ "\"TxOutList\":" + "\n{\n" + "\"Size\":" + "\"" + txOutList.size() + "\"" + ",\n"
				+ "\"List\":" + "\n" + getTxOutString() + "\n},\n"
				+ "\"Nonce\":" + "\"" + nonce + "\"" + ",\n"
				+ "\"Signature\":" + ((signature == null) ? null : "\"" + Util.getHexString(signature) + "\"") + ",\n" + "\"Publickey\":" 
				+ ((publickey.getPublicKey() == null) ? null : "\"" + Util.getHexString(publickey.getPublicKey()) + "\"")+ "\n" + "}";
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
	 * @throws IOException
	 * @throws NoSuchFieldException
	 */
	public boolean isValid(AccountsMerkleTree accountsMerkleTree, AddressShape addressShape) throws NoSuchFieldException, IOException {
		
		Account txInAccount = accountsMerkleTree.getAccount(txIn.getAddress());
		
		if(!isSanity(addressShape)) {
			return false;
		}

		// Check Nonce is positive
		if (!isNoncePositive()) {
			return false;
		}

		// Check if Publickey's ID is equal to TxIn's ID
		if (!publickey.getID().equals(txIn.getAddress().getID())) {
			return false;
		}

		// Check if Publickey exists in Account and equal to current Publickey
		if(txInAccount.isPublickeyExists()) {
			if(!Arrays.equals(txInAccount.getKey().getPublickey().getPublickey(), publickey.getPublicKey())) {
				return false;
			}
		}
		else {
			// Verify Publickey
			if (AddressTool.verifyAddressPublickey(txIn.getAddress().getReadableAddress(), publickey.getPublicKey())) {
				return false;
			}
		}
		
		// Check balance
		if (txIn.getValue() + Util.MIN_EQC > txInAccount.getAsset(Asset.EQCOIN).getBalance()) {
			return false;
		}

		// Check if the number of TxOut is greater than 0 and less than or equal to 10
		if (!isTxOutNumberValid()) {
			return false;
		}

		// Check if the TxOut's Address is unique
		if (!isTxOutAddressUnique()) {
			return false;
		}

		// Check if TxOut's Address is valid
		if (!isTxOutAddressValid()) {
			return false;
		}

		// Check if TxOut's Address doesn't include TxIn
		if (isTxOutAddressIncludeTxInAddress()) {
			return false;
		}

		// Check if all TxIn and TxOut's value is valid
		if (!isAllValueValid()) {
			return false;
		}

		// Check if TxFeeLimit is valid
		if (!isTxFeeLimitValid()) {
			return false;
		}

		// Verify if Transaction's signature can pass
		if (!verify(accountsMerkleTree)) {
			return false;
		}

		return true;
	}

	@Override
	public int getMaxBillingSize() {
		int size = 0;

		// Nonce
		size += EQCType.bigIntegerToEQCBits(nonce).length;

		// TxIn Serial Number size
		size += Util.BASIC_SERIAL_NUMBER_LEN;//txIn.getAddress().getID().getEQCBits().length;
		// TxIn value's size
		size += Util.BASIC_VALUE_NUMBER_LEN;

		// TxOut size
		for (TxOut txOut : txOutList) {
			size += Util.BASIC_SERIAL_NUMBER_LEN;
			size += EQCType.longToEQCBits(txOut.getValue()).length;
		}

		// For Binxx's overhead size
		size += EQCType.getEQCTypeOverhead(size);

		// Transaction's AddressList size which storage the Address
		for (TxOut txOut : txOutList) {
			size += txOut.getAddress().getBillingSize();
		}

		// Transaction's PublickeyList size
		size += publickey.getBillingSize();

		// Transaction's Signature size
		if(txIn.getAddress().getType() == AddressType.T1) {
			size += Util.P256_BASIC_SIGNATURE_LEN;
		}
		else if(txIn.getAddress().getType() == AddressType.T2) {
			size += Util.P521_BASIC_SIGNATURE_LEN;
		}
		Log.info("Total size: " + size);
		return size;
	}

	@Override
	public int getBillingSize() {
		int size = 0;

		// Transaction's Serial Number format's size which storage in the EQC Blockchain
		size += getBin().length;
		Log.info("ID size: " + size);

		// Transaction's AddressList size which storage the new Address
		for (TxOut txOut : txOutList) {
			if (txOut.isNew()) {
				size += txOut.getAddress().getBin(AddressShape.AI).length;
				Log.info("New TxOut: " + txOut.getAddress().getBin(AddressShape.AI).length);
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
			// Serialization nonce
			os.write(EQCType.bigIntegerToEQCBits(nonce));
			// Serialization TxIn
//			Log.info(Util.dumpBytes(txIn.getBin(addressShape), 16));
//			Log.info("Len: " + txIn.getBin(addressShape).length);
			os.write(txIn.getBin(addressShape));
			// Serialization TxOut
			for (TxOut txOut : txOutList) {
				os.write(txOut.getBin(addressShape));
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
	 * @see
	 * com.eqzip.eqcoin.blockchain.Transaction#getBin(com.eqzip.eqcoin.util.Util.
	 * AddressShape)
	 */
	@Override
	public byte[] getBin(Address.AddressShape addressShape) {
		return EQCType.bytesToBIN(getBytes(addressShape));
	}

	@Override
	public boolean isSanity(AddressShape... addressShape) {
		if (transactionType == null || nonce == null || txIn == null || txOutList == null || publickey == null
				|| signature == null) {
			return false;
		}

		if(!publickey.isSanity(addressShape)) {
			return false;
		}
		
		if (transactionType != TransactionType.TRANSFER) {
			return false;
		}

		if (!isTxOutNumberValid()) {
			return false;
		}
		
		if (!txIn.isSanity(addressShape)) {
			return false;
		} else {
			if (!txIn.getAddress().isGood()) {
				return false;
			}
		}
		for (TxOut txOut : txOutList) {
			if (!txOut.isSanity(addressShape)) {
				return false;
			}
		}

		return true;
	}

}
