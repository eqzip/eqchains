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
import com.eqchains.blockchain.transaction.operation.Operation;
import com.eqchains.blockchain.transaction.operation.Operation.OP;
import com.eqchains.serialization.EQCTypable;
import com.eqchains.serialization.EQCType;
import com.eqchains.serialization.EQCType.ARRAY;
import com.eqchains.util.ID;
import com.eqchains.util.Log;
import com.eqchains.util.Util;
import com.eqchains.util.Util.AddressTool;

/**
 * @author Xun Wang
 * @date Mar 21, 2019
 * @email 10509759@qq.com
 */
/**
 * @author Xun Wang
 * @date Mar 25, 2019
 * @email 10509759@qq.com
 */
public class OperationTransaction extends TransferTransaction {
	private Operation operation;
	public final static int MIN_TXOUT = 0;

	private void init() {
	}

	public OperationTransaction() {
		super(TransactionType.OPERATION);
		init();
	}

	public OperationTransaction(byte[] bytes, Address.AddressShape addressShape)
			throws NoSuchFieldException, IOException, UnsupportedOperationException {
		super(TransactionType.OPERATION);
		init();
		ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		byte[] data = null;

		if (addressShape == Address.AddressShape.ID) {
			// Parse Solo
			if ((data = EQCType.parseEQCBits(is)) != null) {
				int solo = (int) EQCType.eqcBitsToLong(data);
				if (solo != SOLO) {
					throw new IllegalStateException("OperationTransaction Solo is invalid: " + solo);
				}
			}

			// Parse Transaction type
			if ((data = EQCType.parseEQCBits(is)) != null && !EQCType.isNULL(data)) {
				int transactionType = (int) EQCType.eqcBitsToLong(data);
				if (transactionType != TransactionType.OPERATION.ordinal()) {
					throw new IllegalStateException(
							"OperationTransaction TransactionType is invalid: " + transactionType);
				} else {
					this.transactionType = TransactionType.OPERATION;
				}
			}

			// Parse Operation
			data = null;
			if ((data = EQCType.parseBIN(is)) != null && !EQCType.isNULL(data)) {
					operation = Operation.parseOperation(data, addressShape);
			}

			// Parse nonce
			data = null;
			if ((data = EQCType.parseEQCBits(is)) != null && !EQCType.isNULL(data)) {
				nonce = new ID(data);
			}

			// Parse TxIn
			// Parse TxIn address
			data = null;
			if ((data = EQCType.parseEQCBits(is)) != null && !EQCType.isNULL(data)) {
				txIn.getAddress().setID(new ID(data));
			}
			// Parse TxIn value
			data = null;
			if ((data = EQCType.parseEQCBits(is)) != null && !EQCType.isNULL(data)) {
				txIn.setValue(EQCType.eqcBitsToLong(data));
			}

			// If the ByteArrayInputStream doesn't end which means have TxOut
			if (!EQCType.isInputStreamEnd(is)) {
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
			}
		} else if (addressShape == Address.AddressShape.READABLE || addressShape == Address.AddressShape.AI) {
			// Parse Solo
			if ((data = EQCType.parseEQCBits(is)) != null && !EQCType.isNULL(data)) {
				int solo = (int) EQCType.eqcBitsToLong(data);
				if (solo != SOLO) {
					throw new IllegalStateException("OperationTransaction Solo is invalid: " + solo);
				}
			}

			// Parse Transaction type
			if ((data = EQCType.parseEQCBits(is)) != null && !EQCType.isNULL(data)) {
				int transactionType = (int) EQCType.eqcBitsToLong(data);
				if (transactionType != TransactionType.OPERATION.ordinal()) {
					throw new IllegalStateException(
							"OperationTransaction TransactionType is invalid: " + transactionType);
				} else {
					this.transactionType = TransactionType.OPERATION;
				}
			}

			// Parse Operation
			data = null;
			if ((data = EQCType.parseBIN(is)) != null && !EQCType.isNULL(data)) {
					operation = Operation.parseOperation(data, addressShape);
			}

			// Parse nonce
			data = null;
			if ((data = EQCType.parseEQCBits(is)) != null && !EQCType.isNULL(data)) {
				nonce = new ID(data);
			}

			// Parse TxIn
			// Parse TxIn address
			data = null;
			if ((data = EQCType.parseBIN(is)) != null && !EQCType.isNULL(data)) {
				if (addressShape == Address.AddressShape.READABLE) {
					txIn.getAddress().setReadableAddress(EQCType.bytesToASCIISting(data));
				} else if (addressShape == Address.AddressShape.AI) {
					txIn.getAddress().setReadableAddress(Util.AddressTool.AIToAddress(data));
				}
			}
			// Parse TxIn value
			data = null;
			if ((data = EQCType.parseEQCBits(is)) != null && !EQCType.isNULL(data)) {
				txIn.setValue(EQCType.eqcBitsToLong(data));
			}

			// If the ByteArrayInputStream doesn't end which means have TxOut
			if (!EQCType.isInputStreamEnd(is)) {
				// Parse TxOut
				data = null;
				while ((data = EQCType.parseBIN(is)) != null && !EQCType.isNULL(data)) {
					TxOut txOut = new TxOut();
					// Parse TxOut address
					if (addressShape == Address.AddressShape.READABLE) {
						txOut.getAddress().setReadableAddress(EQCType.bytesToASCIISting(data));
					} else if (addressShape == Address.AddressShape.AI) {
						txOut.getAddress().setReadableAddress(Util.AddressTool.AIToAddress(data));
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
	}

	public static boolean isValid(byte[] bytes, AddressShape addressShape) throws NoSuchFieldException, IOException {
		ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		byte[] data;
		byte txOutValidCount = 0;
		boolean isSoloValid = false, isTransactionTypeValid = false, isOperationValid = false, isTxInValid = false,
				isTxOutValid = false, isNonceValid = false;

		if (addressShape == Address.AddressShape.ID) {
			// Parse Solo
			if ((data = EQCType.parseEQCBits(is)) != null) {
				int solo = (int) EQCType.eqcBitsToLong(data);
				if (solo == SOLO) {
					isSoloValid = true;
				}
			}

			// Parse Transaction type
			if ((data = EQCType.parseEQCBits(is)) != null && !EQCType.isNULL(data)) {
				int transactionType = (int) EQCType.eqcBitsToLong(data);
				if (transactionType == TransactionType.OPERATION.ordinal()) {
					isTransactionTypeValid = true;
				} 
			}

			// Parse Operation
			data = null;
			if ((data = EQCType.parseBIN(is)) != null && !EQCType.isNULL(data)) {
					isOperationValid = true;
			}

			// Parse nonce
			data = null;
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

			// If the ByteArrayInputStream doesn't end which means have TxOut
			if (!EQCType.isInputStreamEnd(is)) {
				// Parse TxOut
				data = null;
				while ((data = EQCType.parseEQCBits(is)) != null && !EQCType.isNULL(data)) {
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
			else {
				isTxOutValid = true;
			}
		} else if (addressShape == Address.AddressShape.READABLE || addressShape == Address.AddressShape.AI) {
			// Parse Solo
			if ((data = EQCType.parseEQCBits(is)) != null && !EQCType.isNULL(data)) {
				int solo = (int) EQCType.eqcBitsToLong(data);
				if (solo == SOLO) {
					isSoloValid = true;
				}
			}

			// Parse Transaction type
			if ((data = EQCType.parseEQCBits(is)) != null && !EQCType.isNULL(data)) {
				int transactionType = (int) EQCType.eqcBitsToLong(data);
				if (transactionType == TransactionType.OPERATION.ordinal()) {
					isTransactionTypeValid = true;
				} 
			}

			// Parse Operation
			data = null;
			if ((data = EQCType.parseBIN(is)) != null && !EQCType.isNULL(data)) {
					isOperationValid = true;
			}

			// Parse nonce
			data = null;
			if ((data = EQCType.parseEQCBits(is)) != null && !EQCType.isNULL(data)) {
				isNonceValid = true;
			}

			// Parse TxIn
			// Parse TxIn address
			data = null;
			if ((data = EQCType.parseBIN(is)) != null && !EQCType.isNULL(data)) {
				// Parse TxIn value
				data = null;
				if ((data = EQCType.parseEQCBits(is)) != null && !EQCType.isNULL(data)) {
					isTxInValid = true;
				}
			}
			
			// If the ByteArrayInputStream doesn't end which means have TxOut
			if (!EQCType.isInputStreamEnd(is)) {
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
			else {
				isTxOutValid = true;
			}
		}

		return isSoloValid && isOperationValid && isTxInValid && (txOutValidCount >= MIN_TXOUT)
				&& (txOutValidCount <= MAX_TXOUT) && isTxOutValid && isNonceValid && EQCType.isInputStreamEnd(is);
	}

	/**
	 * @return the operation
	 */
	public Operation getOperation() {
		return operation;
	}

	/**
	 * @param operation the operation to set
	 */
	public void setOperation(Operation operation) {
		this.operation = operation;
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
			os.write(EQCType.longToEQCBits(TransactionType.OPERATION.ordinal()));
			// Serialization Operation
			os.write(operation.getBin(addressShape));
			// Serialization nonce
			os.write(EQCType.bigIntegerToEQCBits(nonce));
			// Serialization TxIn
			if (txIn != null) {
				os.write(txIn.getBytes(addressShape));
			}
			if (txOutList.size() > 0) {
				// Serialization TxOut
				for (TxOut txOut : txOutList) {
					os.write(txOut.getBytes(addressShape));
				}
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
	 * @see com.eqzip.eqcoin.blockchain.Transaction#getMaxBillingSize()
	 */
	@Override
	public int getMaxBillingSize() {
		int size = 0;

		// TransferTransaction size
		size += super.getMaxBillingSize();

		// Operations size
		size += operation.getBin(AddressShape.AI).length;

		return size;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.eqzip.eqcoin.blockchain.Transaction#getBillingSize()
	 */
	@Override
	public int getBillingSize() {
		int size = 0;
		// TransferTransaction size
		size += super.getBillingSize();

		// Operations size
		size += operation.getBin(AddressShape.AI).length;
		return super.getBillingSize();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.eqzip.eqcoin.blockchain.Transaction#verify(com.eqzip.eqcoin.blockchain.
	 * AccountsMerkleTree)
	 */
	@Override
	public boolean verify(AccountsMerkleTree accountsMerkleTree) {
		// TODO Auto-generated method stub
		return super.verify(accountsMerkleTree);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.eqzip.eqcoin.blockchain.Transaction#getBytes()
	 */
	@Override
	public byte[] getBytes() {
		// TODO Auto-generated method stub
		return super.getBytes();
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
		// TODO Auto-generated method stub
		return super.getBin(addressShape);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.eqzip.eqcoin.blockchain.Transaction#getBin()
	 */
	@Override
	public byte[] getBin() {
		// TODO Auto-generated method stub
		return super.getBin();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.eqzip.eqcoin.blockchain.Transaction#isValid(com.eqzip.eqcoin.blockchain.
	 * AccountsMerkleTree)
	 */
	@Override
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
		if (txInAccount.isPublickeyExists()) {
			if (!txInAccount.getPublickey().equals(publickey)) {
				return false;
			}
		} else {
			// Verify Publickey
			if (AddressTool.verifyAddressPublickey(txIn.getAddress().getReadableAddress(), publickey.getPublicKey())) {
				return false;
			}
		}

		// Check balance
		if (txIn.getValue() + Util.MIN_EQC > txInAccount.getBalance()) {
			return false;
		}

		// Check if the number of TxOut is greater than or equal to 0 and less than or equal to 10
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.eqzip.eqcoin.blockchain.Transaction#isTxOutNumberValid()
	 */
	@Override
	public boolean isTxOutNumberValid() {
		return (txOutList.size() >= MIN_TXOUT) && (txOutList.size() <= MAX_TXOUT);
	}

	public void execute(AccountsMerkleTree accountsMerkleTree, ID id) {
			if(!operation.execute(accountsMerkleTree, id)) {
				throw new IllegalStateException("During execute operation error occur: " + operation);
			}
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((operation == null) ? 0 : operation.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		OperationTransaction other = (OperationTransaction) obj;
		if (operation == null) {
			if (other.operation != null)
				return false;
		} else if (!operation.equals(other.operation))
			return false;
		return true;
	}
	
	public String toInnerJson() {
		return
		"\"OperationTransaction\":" + "\n{\n" + txIn.toInnerJson() + ",\n"
				+ operation.toInnerJson() + ",\n"
				+ "\"TxOutList\":" + "\n{\n" + "\"Size\":" + "\"" + txOutList.size() + "\"" + ",\n"
				+ "\"List\":" + "\n" + getTxOutString() + "\n},\n"
				+ "\"Nonce\":" + "\"" + nonce + "\"" + ",\n"
				+ "\"Signature\":" + ((signature == null) ? null : "\"" + Util.getHexString(signature) + "\"") + ",\n" + "\"Publickey\":" 
				+ ((publickey.getPublicKey() == null) ? null : "\"" + Util.getHexString(publickey.getPublicKey()) + "\"")+ "\n" + "}";
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
		
		if (transactionType != TransactionType.OPERATION) {
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
	
	@Override
	public boolean isAllValueValid() {
		if ((txOutList.size() > 0 && (txIn.getValue() < Util.MIN_EQC)) || (txIn.getValue() >= Util.MAX_EQC)) {
			return false;
		}

		for (TxOut txOut : txOutList) {
			if ((txOut.getValue() < Util.MIN_EQC) || (txOut.getValue() >= Util.MAX_EQC)) {
				return false;
			}
		}

		return true;
	}
	
}
