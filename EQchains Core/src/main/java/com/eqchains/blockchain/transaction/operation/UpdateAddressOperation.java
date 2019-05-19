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
package com.eqchains.blockchain.transaction.operation;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.eqchains.blockchain.AccountsMerkleTree;
import com.eqchains.blockchain.account.Account;
import com.eqchains.blockchain.transaction.Address;
import com.eqchains.blockchain.transaction.OperationTransaction;
import com.eqchains.blockchain.transaction.Address.AddressShape;
import com.eqchains.serialization.EQCType;
import com.eqchains.util.ID;
import com.eqchains.util.Log;
import com.eqchains.util.Util;
import com.eqchains.util.Util.AddressTool;
import com.eqchains.util.Util.AddressTool.AddressType;

/**
 * @author Xun Wang
 * @date Mar 27, 2019
 * @email 10509759@qq.com
 */
public class UpdateAddressOperation extends Operation {
	private Address address;
	
	public UpdateAddressOperation() {
		super(OP.ADDRESS);
	}

	public UpdateAddressOperation(byte[] bytes, AddressShape addressShape) {
		super(OP.ADDRESS);
		ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		byte[] data = null;

		try {
			// Parse OP
			if ((data = EQCType.parseEQCBits(is)) != null && !EQCType.isNULL(data)) {
				op = OP.get(EQCType.eqcBitsToInt(data));
			}
			// Parse Address
			data = null;
			if ((data = EQCType.parseBIN(is)) != null && !EQCType.isNULL(data)) {
				if (addressShape == AddressShape.READABLE) {
					address = new Address(EQCType.bytesToASCIISting(data));
				} else if (addressShape == AddressShape.ID || addressShape == AddressShape.AI) {
					address = new Address(AddressTool.AIToAddress(data));
				}
			}
		} catch (NoSuchFieldException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
	}

	public static boolean isValid(byte[] bytes, AddressShape addressShape) {
		ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		byte[] data = null;
		OP op = OP.INVALID;
		byte validCount = 0;

		try {
			// Parse OP
			if ((data = EQCType.parseEQCBits(is)) != null && !EQCType.isNULL(data)) {
				op = OP.get(EQCType.eqcBitsToInt(data));
				if (op == OP.ADDRESS) {
					++validCount;
				}
			}
			// Parse Address
			data = null;
			if ((data = EQCType.parseBIN(is)) != null && !EQCType.isNULL(data)) {
				++validCount;
			}
		} catch (NoSuchFieldException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return (validCount == VERIFICATION_COUNT) && EQCType.isInputStreamEnd(is);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.eqzip.eqcoin.blockchain.OperationTransaction.Operation#getBytes()
	 */
	@Override
	public byte[] getBytes() {
		return getBytes(AddressShape.AI);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.eqzip.eqcoin.blockchain.OperationTransaction.Operation#getBin()
	 */
	@Override
	public byte[] getBin() {
		return getBin(AddressShape.AI);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.eqzip.eqcoin.blockchain.OperationTransaction.Operation#getBytes(com.eqzip
	 * .eqcoin.blockchain.Address.AddressShape)
	 */
	@Override
	public byte[] getBytes(AddressShape addressShape) {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			// Serialization OP
			os.write(EQCType.longToEQCBits(op.ordinal()));
			// Serialization Address
			if(addressShape == AddressShape.ID || addressShape == AddressShape.AI) {
				os.write(address.getAddressShapeBin(AddressShape.AI));
			}
			else {
				os.write(address.getAddressShapeBin(addressShape));
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
	 * com.eqzip.eqcoin.blockchain.OperationTransaction.Operation#getBin(com.eqzip.
	 * eqcoin.blockchain.Address.AddressShape)
	 */
	@Override
	public byte[] getBin(AddressShape addressShape) {
		return EQCType.bytesToBIN(getBytes(addressShape));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.eqzip.eqcoin.blockchain.OperationTransaction.Operation#isValid()
	 */
	@Override
	public boolean isValid() {
		return (op == OP.ADDRESS) && address.isGood();
	}

	/* (non-Javadoc)
	 * @see com.eqzip.eqcoin.blockchain.OperationTransaction.Operation#execute()
	 */
	@Override
	public boolean execute(Object ...objects) {
		AccountsMerkleTree accountsMerkleTree = (AccountsMerkleTree) objects[0];
		ID id = (ID) objects[1];
		boolean isSucc = true;
		Account account = accountsMerkleTree.getAccount(id);
		if(account.isPublickeyExists()) {
			account.getKey().setPublickey(null);
		}
		address.setID(account.getID());
		account.getKey().setAddress(address);
		account.getKey().setAddressCreateHeight(accountsMerkleTree.getHeight().getNextID());
		isSucc = accountsMerkleTree.saveAccount(account);
		return isSucc;
	}

	/* (non-Javadoc)
	 * @see com.eqzip.eqcoin.blockchain.transaction.operation.Operation#isMeetPreconditions()
	 */
	@Override
	public boolean isMeetPreconditions(Object ...objects) {
		AccountsMerkleTree accountsMerkleTree = (AccountsMerkleTree) objects[0];
		return !accountsMerkleTree.isAccountExists(address, true);
	}

	/**
	 * @return the readableAddress
	 */
	public Address getAddress() {
		return address;
	}

	/**
	 * @param readableAddress the readableAddress to set
	 */
	public void setAddress(Address readableAddress) {
		this.address = readableAddress;
	}

	/* (non-Javadoc)
	 * @see com.eqzip.eqcoin.blockchain.transaction.operation.Operation#isSanity(com.eqzip.eqcoin.blockchain.transaction.Address.AddressShape[])
	 */
	@Override
	public boolean isSanity(AddressShape... addressShape) {
		if(op != OP.ADDRESS || address == null) {
			return false;
		}
		if(!address.isSanity(addressShape)) {
			return false;
		}
		return true;
	}
	
	@Override
	public String toInnerJson() {
		return 
		"\"UpdateAddressOperation\":" + 
		"\n{" +
			address.toInnerJson() + "\n" +
		"}";
	}
	
}
