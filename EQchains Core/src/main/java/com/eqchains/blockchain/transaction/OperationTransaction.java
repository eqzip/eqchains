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
import java.util.Arrays;
import java.util.Vector;

import org.rocksdb.RocksDBException;

import com.eqchains.blockchain.account.Account;
import com.eqchains.blockchain.account.Passport;
import com.eqchains.blockchain.account.AssetAccount;
import com.eqchains.blockchain.account.Passport.AddressShape;
import com.eqchains.blockchain.accountsmerkletree.AccountsMerkleTree;
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

	public OperationTransaction() {
		super(TransactionType.OPERATION);
	}

	public OperationTransaction(byte[] bytes, Passport.AddressShape addressShape)
			throws NoSuchFieldException, IOException, UnsupportedOperationException, IllegalStateException {
		super(TransactionType.OPERATION);
		EQCType.assertNotNull(bytes);
		ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		parseHeader(is, addressShape);
		parseBody(is, addressShape);
		EQCType.assertNoRedundantData(is);
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
	public boolean verify(AccountsMerkleTree accountsMerkleTree) throws NoSuchFieldException, IllegalStateException, RocksDBException, IOException, ClassNotFoundException, SQLException {
		// TODO Auto-generated method stub
		return super.verify(accountsMerkleTree);
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
		// TODO Auto-generated method stub
		return super.getBin(addressShape);
	}

	

	/* (non-Javadoc)
	 * @see com.eqchains.blockchain.transaction.TransferTransaction#isValid(com.eqchains.blockchain.accountsmerkletree.AccountsMerkleTree, com.eqchains.blockchain.account.Passport.AddressShape)
	 */
	@Override
	public boolean isValid(AccountsMerkleTree accountsMerkleTree, AddressShape addressShape)
			throws NoSuchFieldException, IllegalStateException, RocksDBException, IOException, Exception {
		if(!operation.isMeetPreconditions(this, accountsMerkleTree)) {
			Log.Error("Operation " + operation + " doesn't meet preconditions.");
			return false;
		}
		if(!super.isValid(accountsMerkleTree, addressShape)) {
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
	public boolean isSanity(AddressShape addressShape) {
		if(!isBasicSanity(addressShape)) {
			return false;
		}
		
		if (transactionType != TransactionType.OPERATION) {
			return false;
		}
		
		if(operation == null) {
			return false;
		}
		
		if(!operation.isSanity(addressShape)) {
			return false;
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
	
	public void update(AccountsMerkleTree accountsMerkleTree) throws Exception {
		super.update(accountsMerkleTree);
		if(!operation.execute(this, accountsMerkleTree)) {
			throw new IllegalStateException("During execute operation error occur: " + operation);
		}
	}
	
	public byte[] getBodyBytes(AddressShape addressShape) {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			// Serialization Operation
			os.write(operation.getBytes(addressShape));
			// Serialization Super body
			os.write(super.getBodyBytes(addressShape));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return os.toByteArray();
	}
	
	public void parseBody(ByteArrayInputStream is, AddressShape addressShape) throws NoSuchFieldException, IOException {
		byte[] data = null;
		// Parse Operation
		operation = Operation.parseOperation(is, addressShape);
		// Parse Super body
		super.parseBody(is, addressShape);
	}
	
}
