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
package com.eqzip.eqcoin.blockchain;

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

import org.apache.commons.collections.functors.IfClosure;

import com.eqzip.eqcoin.persistence.h2.EQCBlockChainH2;
import com.eqzip.eqcoin.persistence.rocksdb.EQCBlockChainRocksDB;
import com.eqzip.eqcoin.serialization.EQCTypable;
import com.eqzip.eqcoin.serialization.EQCType;
import com.eqzip.eqcoin.util.Log;
import com.eqzip.eqcoin.util.SerialNumber;
import com.eqzip.eqcoin.util.Util;
import com.eqzip.eqcoin.util.Util.AddressShape;
import com.eqzip.eqcoin.util.Util.AddressTool;
import com.eqzip.eqcoin.util.Util.AddressTool.AddressType;
import com.eqzip.eqcoin.util.Util.AddressTool.P2SHAddress;
import com.eqzip.eqcoin.util.Util.AddressTool.P2SHAddress.Peer;
import com.eqzip.eqcoin.util.Util.AddressTool.P2SHAddress.PeerPublickeys;
import com.eqzip.eqcoin.util.Util.AddressTool.P2SHAddress.PeerSignatures;
import com.eqzip.eqcoin.util.Util.TXFEE_RATE;

/**
 * @author Xun Wang
 * @date Sep 28, 2018
 * @email 10509759@qq.com
 */
public class Transaction implements Comparator<Transaction>, Comparable<Transaction>, EQCTypable {
	private BigInteger version;
	private BigInteger nonce;
	private TxIn txIn;
	private Vector<TxOut> txOutList;
	private PublicKey publickey;
	private byte[] signature;
	public final static int MAX_TXOUT = 10;
	/*
	 * VERIFICATION_COUNT equal to the number of member variables of the class to be
	 * verified.
	 */
	private final static byte VERIFICATION_MIN_COUNT = 4;
	private final static byte VERIFICATION_MAX_COUNT = 13;

	public Transaction() {
		super();
		txOutList = new Vector<TxOut>();
		version = BigInteger.ZERO;
		nonce = BigInteger.ZERO;
		publickey = new PublicKey();
	}
	
//	/**
//	 * This is CoinBase constructor and only for CoinBase
//	 * @param coinBase
//	 */
//	public Transaction(byte[] coinBase) {
//		super();
//		txOutList = new Vector<TxOut>();
//		ByteArrayInputStream is = new ByteArrayInputStream(coinBase);
//		byte[] data = null;
//		
//		// Parse TxIn address
//		if (!EQCType.isCoinBaseTxIN(is)) {
//			
//		}
//
//		// Parse TxOut
//		data = null;
//		while ((data = EQCType.parseEQCBits(is)) != null) {
//			TxOut txOut = new TxOut();
//			// Parse TxOut address
//			Address address = new Address();
//			address.setSerialNumber(new SerialNumber(Util.eqcBitsToBigInteger(data)));
//			txOut.setAddress(address);
//
//			// Parse TxOut value
//			data = null;
//			if ((data = EQCType.parseEQCBits(is)) != null) {
//				txOut.setValue(Util.eqcBitsToLong(data));
//			}
//
//			// Add TxOut
//			txOutList.add(txOut);
//			data = null;
//		}
//	}

	public Transaction(byte[] bytes, AddressShape addressShape) throws NoSuchFieldException, IOException, UnsupportedOperationException {
		super();
		txOutList = new Vector<TxOut>();
		ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		byte[] data = null;
		
		if(addressShape == AddressShape.SERIALNUMBER) {
			// Parse Version
			if ((data = EQCType.parseEQCBits(is)) != null) {
				version = Util.eqcBitsToBigInteger(data);
			}
			
			// Parse nonce
			data = null;
			if ((data = EQCType.parseEQCBits(is)) != null) {
				version = Util.eqcBitsToBigInteger(data);
			}
			
			// Parse TxIn
			data = null;
			if ((data = EQCType.parseEQCBits(is)) != null && EQCType.isNULL(data)) {
				// This is CoinBase
				txIn = null;
			}
			else {
				// Parse TxIn address
				txIn = new TxIn();
				Address address = new Address();
				address.setSerialNumber(new SerialNumber(data));
				txIn.setAddress(address);
				// Parse TxIn value
				data = null;
				if ((data = EQCType.parseEQCBits(is)) != null) {
					txIn.setValue(Util.eqcBitsToLong(data));
				}
			}
			
//			if ((data = EQCType.parseEQCBits(is)) != null) {
//				// Address' Serial Number begin from one. If Transaction is CoinBase Transaction due to it's TxIn is null so set it value to zero.
//				BigInteger txType = Util.eqcBitsToBigInteger(data);
//				if (txType.compareTo(BigInteger.ZERO) == 0) {
//					// This transaction is CoinBase transaction and it doesn't need TxIn. Miners earn EQC by creating value-packaged deals and manufacturing blocks.
//					txIn = null;
//				} 
//				else if((txType.compareTo(BigInteger.ONE) >= 0) && (txType.compareTo(BigInteger.TEN) <= 0)) {
//					throw new UnsupportedOperationException("Current only have normal Tranaction type, the version number: " + txType + " is wrong...");
//				}
//				else {
//					// Parse TxIn address
//					txIn = new TxIn();
//					Address address = new Address();
//					address.setSerialNumber(new SerialNumber(txType));
//					txIn.setAddress(address);
//					// Parse TxIn value
//					data = null;
//					if ((data = EQCType.parseEQCBits(is)) != null) {
//						txIn.setValue(Util.eqcBitsToLong(data));
//					}
//				}
//			}

			// Parse TxOut
			data = null;
			while ((data = EQCType.parseEQCBits(is)) != null) {
				TxOut txOut = new TxOut();
				// Parse TxOut address
				Address address = new Address();
				address.setSerialNumber(new SerialNumber(Util.eqcBitsToBigInteger(data)));
				txOut.setAddress(address);

				// Parse TxOut value
				data = null;
				if ((data = EQCType.parseEQCBits(is)) != null) {
					txOut.setValue(Util.eqcBitsToLong(data));
				}

				// Add TxOut
				txOutList.add(txOut);
				data = null;
			}
		}
		else if(addressShape == AddressShape.ADDRESS || addressShape == AddressShape.AI) {
			// Parse Version
			if ((data = EQCType.parseEQCBits(is)) != null) {
				version = Util.eqcBitsToBigInteger(data);
			}
			
			// Parse nonce
			data = null;
			if ((data = EQCType.parseEQCBits(is)) != null) {
				version = Util.eqcBitsToBigInteger(data);
			}
						
			// Parse TxIn
			data = null;
			if (((data = EQCType.parseBIN(is)) != null) && (EQCType.isNULL(data))) {
				// This is CoinBase
				txIn = null;
			}
			else {
				// Parse TxIn address
				txIn = new TxIn();
				Address address = new Address();
				if(addressShape == AddressShape.ADDRESS) {
					address.setAddress(Util.bytesToASCIISting(data));
				}
				else if(addressShape == AddressShape.AI) {
					address.setAddress(Util.AddressTool.AIToAddress(data));
				}
				txIn.setAddress(address);
				// Parse TxIn value
				data = null;
				if ((data = EQCType.parseEQCBits(is)) != null) {
					txIn.setValue(Util.eqcBitsToLong(data));
				}
			}
			
			// Parse TxOut
			data = null;
			while ((data = EQCType.parseBIN(is)) != null) {
				TxOut txOut = new TxOut();
				// Parse TxOut address
				Address address = new Address();
				if(addressShape == AddressShape.ADDRESS) {
					address.setAddress(Util.bytesToASCIISting(data));
				}
				else if(addressShape == AddressShape.AI) {
					address.setAddress(Util.AddressTool.AIToAddress(data));
				}
				txOut.setAddress(address);

				// Parse TxOut value
				data = null;
				if ((data = EQCType.parseEQCBits(is)) != null) {
					txOut.setValue(Util.eqcBitsToLong(data));
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
		byte validCount = 0;
		
		if(addressShape == AddressShape.SERIALNUMBER) {
			// Parse Version
			if ((data = EQCType.parseEQCBits(is)) != null) {
				++validCount;
			}

			// Parse nonce
			data = null;
			if ((data = EQCType.parseEQCBits(is)) != null) {
				++validCount;
			}
				
			// Parse TxIn
			data = null;
			if ((data = EQCType.parseEQCBits(is)) != null && EQCType.isNULL(data)) {
				// This is CoinBase
				++validCount;
			} else {
				// Parse TxIn address
				// Parse TxIn value
				data = null;
				if ((data = EQCType.parseEQCBits(is)) != null) {
					++validCount;
				}
			}
						
			// Parse TxOut
			data = null;
			while ((data = EQCType.parseEQCBits(is)) != null) {
				// Parse TxOut address
				// Parse TxOut value
				data = null;
				if ((data = EQCType.parseEQCBits(is)) != null) {
					++validCount;
				}
				data = null;
			}
		}
		else if(addressShape == AddressShape.ADDRESS) {
			// Parse Version
			if ((data = EQCType.parseEQCBits(is)) != null) {
				++validCount;
			}
			
			// Parse nonce
			data = null;
			if ((data = EQCType.parseEQCBits(is)) != null) {
				++validCount;
			}
			
			// Parse TxIn
			data = null;
			if (((data = EQCType.parseBIN(is)) != null) && (EQCType.isNULL(data))) {
				// This is CoinBase
				++validCount;
			}
			else {
				// Parse TxIn address
				// Parse TxIn value
				data = null;
				if ((data = EQCType.parseEQCBits(is)) != null) {
					++validCount;
				}
			}
			
			// Parse TxOut
			data = null;
			while ((data = EQCType.parseBIN(is)) != null) {
				TxOut txOut = new TxOut();
				// Parse TxOut address
				// Parse TxOut value
				data = null;
				if ((data = EQCType.parseEQCBits(is)) != null) {
					++validCount;
				}
				data = null;
			}
		}

		return (validCount >= VERIFICATION_MIN_COUNT) &&  (validCount <= VERIFICATION_MAX_COUNT) && EQCType.isInputStreamEnd(is);
	}

	public byte[] getBytes(AddressShape addressShape) {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			// Serialization Version
			os.write(Util.bigIntegerToEQCBits(version));
//			if(addressShape == AddressShape.ADDRESS) {
//				// This Transaction is CoinBase so here just save a zero.
//				os.write(Util.bigIntegerToEQCBits(version));
//			}
			
			// Serialization nonce
			os.write(Util.bigIntegerToEQCBits(nonce));
			
			// Serialization TxIn
			if (txIn != null) {
				os.write(txIn.getBytes(addressShape));
			}
			else {
				os.write(EQCType.NULL);
//				if(addressShape == AddressShape.SERIALNUMBER) {
//					// This Transaction is CoinBase so here just save a zero.
//					os.write(0);
//				}
//				else if(addressShape == AddressShape.ADDRESS) {
//					// This Transaction is CoinBase so here just save a BIN7.
//					os.write(EQCType.BIN7);
//				}
			}
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

	public byte[] getRPCBytes() {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			os.write(EQCType.bytesToBIN(getBytes(AddressShape.ADDRESS)));
			os.write(EQCType.bytesToBIN(publickey.getPublicKey()));
			os.write(EQCType.bytesToBIN(signature));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return os.toByteArray();
	}

	public void addTxOut(TxOut txOut) {
		if (txOutList.size() >= MAX_TXOUT) {
			throw new UnsupportedOperationException("The number of TxOut cannot exceed 10.");
		}
		if (!isTxOutAddressExists(txOut)) {
			txOutList.add(txOut);
		}
		else {
			Log.Error(txOut.toString() + " already exists in txOutList just ignore it.");
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
	
	public boolean isTxFeePositive() {
		return getTxFeeLimit() > 0;
	}

	public boolean isTxOutNumberValid() {
		return (txOutList.size() > 0) && (txOutList.size() <= MAX_TXOUT);
	}
	
	public boolean isAllValueValid() {
		if(!isCoinBase()) {
			if((txIn.getValue( )<= 0) || (txIn.getValue() >= Util.MAX_EQC)) {
				return false;
			}
		}
		
		for(TxOut txOut :txOutList) {
			if((txOut.getValue( )<= 0) || (txOut.getValue() >= Util.MAX_EQC)) {
				return false;
			}
		}
			
		return true;
	}

	public long getTxFeeLimit() {
		return txIn.getValue() - getTxOutValues();
	}
	
	public boolean isTxFeeLimitValid() {
		boolean boolIsValid = true;
		if(getTxFeeLimit() < getDefaultTxFeeLimit()) {
			boolIsValid = false;
		}
		else if((getTxFeeLimit() < getMaxTxFeeLimit()) && (getTxFeeLimit() % getDefaultTxFeeLimit()) != 0) {
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
	
	public long getTxFee() {
		long txFee = 0;
		if(getTxFeeLimit() > getMaxTxFeeLimit()) {
			txFee = getTxFeeLimit();
		}
		else {
			txFee = getBillingSize() * getQos().getRate() * Util.TXFEE_UNIT;
		}
		return txFee;
	}
	
	public long getBillingValue() {
		return getTxFee() + getTxOutValues();
	}
	
	public void setTxFeeLimit(TXFEE_RATE txfee_rate) {
		txIn.setValue(getTxOutValues() + cypherTxFeeLimit(txfee_rate));
	}
	
	public long cypherTxFeeLimit(TXFEE_RATE txfee_rate) {
		return (getMaxBillingSize() * txfee_rate.getRate() * Util.TXFEE_UNIT);
	}
	
	public boolean isTxOutAddressExists(TxOut txOut) {
		boolean boolIsExists = false;
		for(TxOut txOut2 : txOutList) {
			if(txOut2.getAddress().getAddress().equals(txOut.getAddress().getAddress())) {
				boolIsExists = true;
//				Log.info("TxOutAddressExists" + " a: " + txOut2.getAddress().getAddress() + " b: " + txOut.getAddress().getAddress());
				break;
			}
		}
		return boolIsExists;
	}

	public boolean isTxOutAddressUnique() {
		boolean isUnique = true;
		for (int i = 0; i < txOutList.size(); ++i) {
			for (int j = i + 1; j < txOutList.size(); ++j) {
				if (txOutList.get(i).getAddress().getAddress().equals(txOutList.get(j).getAddress().getAddress())) {
					isUnique = false;
					break;
				}
			}
		}
		return isUnique;
	}
	
	public boolean isCoinBase() {
		return txIn == null && (txOutList.size() == 2) && txOutList.get(0).getAddress().getSerialNumber().equals(SerialNumber.TWO);
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
	 * @param publickey the publickey to set
	 */
	public void setPublickey(PublicKey publickey) {
		this.publickey = publickey;
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
		Transaction other = (Transaction) obj;
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
			// Temporarily save a copy of TxOut in order not to change the order in which the user enters TxOut.
			Vector<TxOut> originalTxOutList = new Vector<TxOut>();
			for(TxOut txOut : txOutList) {
				originalTxOutList.add(txOut);
			}
			// Sort the temporarily saved TxOut in alphabetical dictionary order.
			Collections.sort(originalTxOutList);
			// Temporarily save a copy of TxOut in order not to change the order in which the user enters TxOut.
			Vector<TxOut> targetTxOutList = new Vector<TxOut>();
			for(TxOut txOut : other.txOutList) {
				targetTxOutList.add(txOut);
			}
			// Sort the temporarily saved TxOut in alphabetical dictionary order.
			Collections.sort(targetTxOutList);
			// Compare temporarily saved TxOut collections sorted alphabetically in alphabetical order.
			if (!originalTxOutList.equals(targetTxOutList))
				return false;
		}
		return true;
	}
	
	public byte[] getBin(AddressShape addressShape) {
		byte[] bytes = null;
//		if(addressShape == AddressShape.SERIALNUMBER) {
//			bytes = EQCType.bytesToBIN(getBytes(AddressShape.SERIALNUMBER));
//		}
//		else if(addressShape == AddressShape.ADDRESS) {
//			bytes = EQCType.bytesToBIN(getBytes(AddressShape.ADDRESS));
//		}
//		else if(addressShape == AddressShape.ADDRESS) {
//			bytes = EQCType.bytesToBIN(getBytes(AddressShape.ADDRESS));
//		}
		bytes = EQCType.bytesToBIN(getBytes(addressShape));
		return bytes;
	}

	/**
	 * Get the Transaction's bytes for storage it in the EQC block chain
	 * For save the space the Address' shape is Serial Number which is the EQCBits type.
	 * 
	 * @return byte[]
	 */
	@Override
	public byte[] getBytes() {
		// TODO Auto-generated method stub
		return getBytes(AddressShape.SERIALNUMBER);
	}
	
	private String getTxOutString() {
		String tx = "[\n";
		if(txOutList.size() >= 1) {
			for(int i=0; i<txOutList.size()-1; ++i) {
				tx += txOutList.get(i) + ",\n";
			}
			tx += txOutList.get(txOutList.size()-1);
		}
		else {
			tx += null;
		}
		tx += "\n]";
		return tx;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return 
		
				"{\n" +
				toInnerJson() +
				"\n}";
		
	}
	
	public String toInnerJson() {
		return 
				
				"\"Transaction\":" + 
				"\n{\n" +
					"\"Version\":" + "\"" + version + "\"" + ",\n" +
					"\"Nonce\":" + "\"" + nonce + "\"" + ",\n" +
					((txIn==null)?TxIn.coinBase():txIn.toInnerJson()) + ",\n" +
					"\"txOutList\":" + 
					"\n" +
						getTxOutString() + ",\n" +
					"\"signature\":" + "\"" + Util.getHexString(signature) + "\"" + ",\n" +
					"\"publickey\":" + "\"" + ((publickey == null)?null:Util.getHexString(publickey.getPublicKey())) + "\"" + "\n" +
				"}";
	}

	/**
	 * Get the Transaction's BIN for storage it in the EQC block chain.
	 * For save the space the Address' shape is Serial Number which is the EQCBits type.
	 * 
	 * @return byte[]
	 */
	@Override
	public byte[] getBin() {
		return getBin(AddressShape.SERIALNUMBER);
	}
	
	public int getMaxBillingSize() {
		int size = 0;
		
		// Transaction's Serial Number format's size which storage in the EQC Blockchain
		// Version
		size += Util.bigIntegerToEQCBits(version).length;
		// Nonce
		size += Util.bigIntegerToEQCBits(nonce).length;
		
		// TxIn Serial Number size
		size += txIn.getAddress().getSerialNumber().getEQCBits().length;
		// TxIn value's size
		size += Util.BASIC_SERIAL_NUMBER_LEN;
		
		for(TxOut txOut : txOutList) {
			size += Util.BASIC_SERIAL_NUMBER_LEN;
			size += Util.longToEQCBits(txOut.getValue()).length;
		}
		
		// For Binx's overhead size
		size += EQCType.getEQCTypeOverhead(size);
		
		// Transaction's AddressList size which storage the new Address
		for(TxOut txOut : txOutList) {
			size += txOut.getAddress().getBillingSize();
		}
		
		// Transaction's PublickeyList size
		size += publickey.getBillingSize();
		
		// Transaction's Signature size
		size += EQCType.bytesToBIN(signature).length;
		
//		// Calculate Transaction getBin's size
//		Transaction transaction = new Transaction(getBytes(AddressShape.ADDRESS), AddressShape.ADDRESS);
//		transaction.setPublickey(publickey);
//		
//		// Fill in Transaction's Address' Serial Number for calculate it's size
//		SerialNumber currentSerialNumber = EQCBlockChainH2.getInstance().getLastAddressSerialNumber()
//				.getNextSerialNumber();
//		if(!isCoinBase()) {
//			if (!EQCBlockChainH2.getInstance().isAddressExists(transaction.getTxIn().getAddress())) {
//				transaction.getTxIn().getAddress().setSerialNumber(currentSerialNumber);
//				currentSerialNumber = currentSerialNumber.getNextSerialNumber();
//			}
//			else {
//				transaction.getTxIn().getAddress().setSerialNumber(EQCBlockChainH2.getInstance().getAddressSerialNumber(transaction.getTxIn().getAddress()));
//			}
//		}
//		for (TxOut txOut : transaction.getTxOutList()) {
//			if (!EQCBlockChainH2.getInstance().isAddressExists(txOut.getAddress())) {
//				txOut.getAddress().setSerialNumber(currentSerialNumber);
//				currentSerialNumber = currentSerialNumber.getNextSerialNumber();
//			}
//			else {
//				txOut.getAddress().setSerialNumber(EQCBlockChainH2.getInstance().getAddressSerialNumber(txOut.getAddress()));
//			}
//		}
//		size += transaction.getBin().length;
//		
//		// Calculate Transaction's all TxOut's new Address size
//		for (TxOut txOut : transaction.getTxOutList()) {
//			if (!EQCBlockChainH2.getInstance().isAddressExists(txOut.getAddress())) {
//				size += txOut.getAddress().getBin().length;
//			}
//		}
//		
//		if (!isCoinBase()) {
//			// Calculate Transaction's PublicKey size if doesn't saved
//			if (!EQCBlockChainH2.getInstance().isPublicKeyExists(transaction.getPublickey())) {
//				PublicKey publicKey = new PublicKey();
//				publicKey.setSerialNumber(transaction.getTxIn().getAddress().getSerialNumber());
//				if (transaction.getTxIn().getAddress().getType() == AddressType.T1) {
//					publicKey.setPublicKey(new byte[Util.P256_BASIC_PUBLICKEY_LEN]);
//				} else if (transaction.getTxIn().getAddress().getType() == AddressType.T2) {
//					publicKey.setPublicKey(new byte[Util.P521_BASIC_PUBLICKEY_LEN]); 
//				}
//				size += publicKey.getBin().length;
//			}
//
//			// Calculate Transaction's Signature size
//			if (transaction.getTxIn().getAddress().getType() == AddressType.T1) {
//				size += Util.P256_BASIC_SIGNATURE_LEN;
//			} else if (transaction.getTxIn().getAddress().getType() == AddressType.T2) {
//				size += Util.P521_BASIC_SIGNATURE_LEN;
//			}
//		}
		
		return size;
	}
	
	public int getBillingSize() {
		int size = 0;
		
		// Transaction's Serial Number format's size which storage in the EQC Blockchain
		size += getBin().length;
		
		// Transaction's AddressList size which storage the new Address
		for(TxOut txOut : txOutList) {
			if(txOut.isNew()) {
				size += txOut.getBin().length;
			}
		}
		
		// Transaction's PublickeyList size
		if(publickey.isNew()) {
			size += publickey.getBin().length;
		}
		
		// Transaction's Signature size
		size += EQCType.bytesToBIN(signature).length;
		
		return size;
	}
	
	public boolean isAllValuePositive() {
		if(!isCoinBase()) {
			if(txIn.getValue() < 0) {
				return false;
			}
		}
		for(TxOut txOut : txOutList) {
			if(txOut.getValue() < 0) {
				return false;
			}
		}
		return true;
	}
	
	public boolean isTxOutAddressValid() {
		for(TxOut txOut: txOutList) {
			if(!txOut.getAddress().isGood(null)) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * 0. 验证Transaction的完整性：
0.1 对于CoinBase transaction至少包括一个TxOut。
0.2 对于非CoinBase transaction至少包括一个TxOut&一个TxIn。
1. 验证TxIn余额是否足够？
之前高度的余额减去当前EQCBlock中之前的交易记录中已经花费的余额。
2. 验证TxIn address是否有效&和公钥是否一致？
3. 验证TxIn‘s block header‘s hash+bin(TxIn+TxOut）的签名能否通过？
4. 验证TxHash是否在之前的区块中不存在，也即此交易是唯一的交易。防止重放攻击。
验证Signature在之前的区块中&当前的EQCBlock中不存在。防止重放攻击。
5. 验证TxOut的数量是不是大于等于1&小于等于10。
6. 验证TxOut的地址是不是唯一的存在？也即每个TxOut地址只能出现一次。
7. 验证TxOut是否小于TxIn？
8. 验证是否TxFee大于零，验证是否所有的Txin&TxOut的Value大于零。
// 9. 验证TxFee是否足够？
	 * @return
	 * @throws IOException 
	 * @throws NoSuchFieldException 
	 */
	public boolean isValid(AccountsMerkleTree accountsMerkleTree) throws NoSuchFieldException, IOException {
		
		if(!Transaction.isValid(getBytes(), AddressShape.SERIALNUMBER)) {
			return false;
		}
		
		if (isCoinBase()) {
			// Check if CoinBase's TxIn is null
			if (txIn != null) {
				return false;
			}
			
			// Check if CoinBase only have two TxOut
			if(txOutList.size() != Util.INIT_ADDRESS_SERIAL_NUMBER) {
				return false;
			}
			
			// Check if the first reward Address is EQC FOUNDATION's Address
			if(!txOutList.get(0).getAddress().getSerialNumber().equals(SerialNumber.TWO)) {
				return false;
			}
		} 
		else {
			// Check if TxIn isn't null
			if(txIn == null) {
				return false;
			}

			// Check balance
			if (txIn.getValue() > accountsMerkleTree.getAccount(txIn.getAddress().getSerialNumber()).getBalance()) {
				return false;
			}

			// Check TxIn's Address
			if (!txIn.getAddress().isGood(publickey)) {
				return false;
			}

			// Verify if Transaction's signature can pass
			if (!Util.verifySignature(this.getTxIn().getAddress().getType(), this, EQCBlockChainRocksDB.getInstance().getEQCHeaderHash(accountsMerkleTree.getAccount(txIn.getAddress().getSerialNumber()).getAddressCreateHeight()))) {
				return false;
			}

//			// Check if current Transaction already stored in the history EQC block
//			if (EQCBlockChainH2.getInstance().isTransactionExists(this)) {
//				return false;
//			}

			// Check if the number of TxOut is greater than 0 and less than or equal to 10
			if (!isTxOutNumberValid()) {
				return false;
			}

			// Check if the TxOut's Address is unique
			if (!isTxOutAddressUnique()) {
				return false;
			}
			
			// Check if TxOut's Address is valid
			if(!isTxOutAddressValid()) {
				return false;
			}
			
			// Check if TxOut's Address doesn't include TxIn
			
			// Check if every TxOut's Value is bigger than 50 EQC

			// Check if the TxOut'a value is less than TxIn's value
			if (!isTxOutValueLessThanTxInValue()) {
				return false;
			}
			
			// Check if TxFee > 0
			if(!isTxFeePositive()) {
				return false;
			}
			
			// Check if TxFee is valid
			
			// Check if all TxIn and TxOut's value > 0
			if(!isAllValuePositive()) {
				return false;
			}
				
			// Check if TxFee is enough in case the zero point of the Serial Number
			// is reached, the value stored in the Serial Number will be increased by one
			// byte compared to the value calculated by the client, so the result value is
			// -1 to increase the redundancy of 25 bytes.
			long minTxFee = ((EQCBlockChainH2.getInstance().getTransactionNumbersIn24hours(txIn.getAddress(),
					EQCBlockChainH2.getInstance().getEQCBlockTailHeight()) / 10 + 1) * getMaxBillingSize() / 25) - 1;
			if (getTxFeeLimit() < minTxFee) {
				return false;
			}
		}
		return true;
	}
	
	public TXFEE_RATE getQos() {
		int rate = 1;
		if(getTxFeeLimit() > getMaxTxFeeLimit()) {
			rate = TXFEE_RATE.POSTPONEVIP.getRate();
		}
		else {
			rate = (int) (getTxFeeLimit() / (getMaxBillingSize() * Util.TXFEE_UNIT));
		}
		return TXFEE_RATE.getQos(rate);
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
	public void setNonce(BigInteger nonce) {
		this.nonce = nonce;
	}

	@Override
	public int compareTo(Transaction o) {
		// TODO Auto-generated method stub
		int nResult = 0;
		if((nResult = this.getTxIn().getAddress().getAddress().compareTo(o.getTxIn().getAddress().getAddress())) == 0) {
			nResult = this.getNonce().compareTo(o.getNonce());
		}
		return nResult;
	}

	@Override
	public int compare(Transaction o1, Transaction o2) {
		// TODO Auto-generated method stub
		int nResult = 0;
		if((nResult = o1.getTxIn().getAddress().getAddress().compareTo(o2.getTxIn().getAddress().getAddress())) == 0) {
			nResult = o1.getNonce().compareTo(o2.getNonce());
		}
		return nResult;
	}
	
	public boolean verify() {
		boolean isValid = true;
		if(txIn.getAddress().getType() == AddressType.T1 || txIn.getAddress().getType() == AddressType.T2) {
			if(verifyPublickey()) {
				isValid = Util.verifySignature(getTxIn().getAddress().getType(), this, Util.getBlockHeaderHash(this));
			}
			else {
				isValid = false;
			}
		}
		else if(txIn.getAddress().getType() == AddressType.T3) {
			// Verify Publickey
			if(verifyPublickey()) {
				// Verify Signature
				isValid = verifySignature();
			}
			else {
				isValid = false;
			}
		}
		return isValid;
	}
	
	public boolean verifyPublickey() {
		boolean isValid = true;
		if(txIn.getAddress().getType() == AddressType.T1 || txIn.getAddress().getType() == AddressType.T2) {
			isValid = AddressTool.verifyAddress(txIn.getAddress().getAddress(), publickey.getPublicKey());
		}
		else if(txIn.getAddress().getType() == AddressType.T3) {
			PeerPublickeys peerPublickeys = null;
			P2SHAddress pAddress = null;
			try {
				peerPublickeys = new PeerPublickeys(publickey.getPublicKey());
				pAddress = new P2SHAddress(txIn.getAddress().getCode());
				isValid = pAddress.isPeerPublickeysValid(peerPublickeys);
			} catch (NoSuchFieldException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				isValid = false;
				Log.Error("During verifyPublickey error occur: " + e.getMessage());
			}
			
		}
		return isValid;
	}
	
	public boolean verifySignature() {
		boolean isValid = true;
		if(txIn.getAddress().getType() == AddressType.T1 || txIn.getAddress().getType() == AddressType.T2) {
			isValid = Util.verifySignature(getTxIn().getAddress().getType(), this, Util.getBlockHeaderHash(this));
		}
		else if(txIn.getAddress().getType() == AddressType.T3) {
			PeerPublickeys peerPublickeys = null;
			PeerSignatures peerSignatures = null;
			P2SHAddress pAddress = null;
			try {
				peerPublickeys = new PeerPublickeys(publickey.getPublicKey());
				peerSignatures = new PeerSignatures(signature);
				pAddress = new P2SHAddress(txIn.getAddress().getCode());
				Peer peer = pAddress.getPeerList().get(peerSignatures.getSignatureSN() - 1);
				if(peerSignatures.getSignatureList().size() != peer.getAddressList().size()) {
					Log.Error("VerifySignature size verify failed please check your size");
					isValid = false;
				}
				else {
					// Verify Timestamp
					if((peer.getTimestamp() == 0) || (System.currentTimeMillis() > peer.getTimestamp())) {
						// Verify Signature
						for(int i=0; i<peer.getAddressList().size(); ++i) {
							if(!Util.verifySignature(peerPublickeys.getPublickeyList().get(i), peerSignatures.getSignatureList().get(i), Util.AddressTool.getAddressType(peer.getAddressList().get(i)), this, new byte[16]/*Util.getBlockHeaderHash(this)*/, peerSignatures.getSignatureSN())) {
								isValid = false;
								Log.Error("VerifySignature verify failed please check your Signature");
								break;
							}
						}
					}
					else {
						Log.Error("Current Tracnsaction's lock time haven't reached.");
						isValid = false;
					}
				}
			} catch (NoSuchFieldException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				isValid = false;
				Log.Error("During verifyPublickey error occur: " + e.getMessage());
			}
			
		}
		return isValid;
	}
	
}
