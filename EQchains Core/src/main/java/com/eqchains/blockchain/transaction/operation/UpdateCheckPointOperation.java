/**
 * EQchains core - EQchains Federation's EQchains core library
 * @copyright 2018-present EQchains Federation All rights reserved...
 * Copyright of all works released by EQchains Federation or jointly released by
 * EQchains Federation with cooperative partners are owned by EQchains Federation
 * and entitled to protection available from copyright law by country as well as
 * international conventions.
 * Attribution — You must give appropriate credit, provide a link to the license.
 * Non Commercial — You may not use the material for commercial purposes.
 * No Derivatives — If you remix, transform, or build upon the material, you may
 * not distribute the modified material.
 * For any use of above stated content of copyright beyond the scope of fair use
 * or without prior written permission, EQchains Federation reserves all rights to
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
import java.util.Arrays;

import com.eqchains.blockchain.accountsmerkletree.PassportsMerkleTree;
import com.eqchains.blockchain.passport.Asset;
import com.eqchains.blockchain.passport.EQcoinSubchainPassport;
import com.eqchains.blockchain.passport.Lock.LockShape;
import com.eqchains.blockchain.transaction.OperationTransaction;
import com.eqchains.blockchain.transaction.operation.Operation.OP;
import com.eqchains.serialization.EQCType;
import com.eqchains.util.ID;
import com.eqchains.util.Log;
import com.eqchains.util.Util;

/**
 * @author Xun Wang
 * @date Aug 19, 2019
 * @email 10509759@qq.com
 */
public class UpdateCheckPointOperation extends Operation {
	private byte[] checkPointHash;
	private ID checkPointHeight;
	
	public UpdateCheckPointOperation(OP op) {
		super(OP.CHECKPOINT);
	}

	public UpdateCheckPointOperation(ByteArrayInputStream is, LockShape addressShape) throws NoSuchFieldException, IllegalArgumentException, IOException {
		super(OP.CHECKPOINT);
		parseHeader(is, addressShape);
		parseBody(is, addressShape);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.eqzip.eqcoin.blockchain.OperationTransaction.Operation#getBytes(com.eqzip
	 * .eqcoin.blockchain.Address.AddressShape)
	 */
	@Override
	public byte[] getBytes(LockShape addressShape) {
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
	 * com.eqzip.eqcoin.blockchain.OperationTransaction.Operation#getBin(com.eqzip.
	 * eqcoin.blockchain.Address.AddressShape)
	 */
	@Override
	public byte[] getBin(LockShape addressShape) {
		return EQCType.bytesToBIN(getBytes(addressShape));
	}

	/* (non-Javadoc)
	 * @see com.eqzip.eqcoin.blockchain.OperationTransaction.Operation#execute()
	 */
	@Override
	public boolean execute(Object ...objects) throws Exception {
		PassportsMerkleTree accountsMerkleTree = (PassportsMerkleTree) objects[1];
		EQcoinSubchainPassport account = (EQcoinSubchainPassport) accountsMerkleTree.getPassport(Asset.EQCOIN, true);
		account.setCheckPointHash(checkPointHash);
		account.setCheckPointHeight(checkPointHeight);
		accountsMerkleTree.savePassport(account);
		return true;
	}

	/* (non-Javadoc)
	 * @see com.eqzip.eqcoin.blockchain.transaction.operation.Operation#isMeetPreconditions()
	 */
	@Override
	public boolean isMeetPreconditions(Object ...objects) throws Exception {
		OperationTransaction operationTransaction = (OperationTransaction) objects[0];
		PassportsMerkleTree accountsMerkleTree = (PassportsMerkleTree) objects[1];
		boolean isMeetPreconditions = true;
		if(isSanity(null) && operationTransaction.getTxIn().getKey().getID().equals(ID.NINE)) {
			if(checkPointHeight.compareTo(Util.DB().getEQCBlockTailHeight()) > 0) {
				isMeetPreconditions = false;
			}
			else {
				if(!Arrays.equals(checkPointHash, Util.EQCCHA_MULTIPLE_DUAL(Util.DB().getEQCHeaderHash(checkPointHeight), Util.ONE, true, true))) {
					isMeetPreconditions = false;
				}
			}
		}
		else {
			isMeetPreconditions = false;
		}
		return isMeetPreconditions;
	}

	/* (non-Javadoc)
	 * @see com.eqzip.eqcoin.blockchain.transaction.operation.Operation#isSanity(com.eqzip.eqcoin.blockchain.transaction.Address.AddressShape[])
	 */
	@Override
	public boolean isSanity(LockShape addressShape) {
		if(op != OP.CHECKPOINT) {
			return false;
		}
		if(checkPointHash == null || checkPointHash.length != 32) {
			return false;
		}
		if(!checkPointHeight.isSanity()) {
			return false;
		}
		return true;
	}
	
	@Override
	public String toInnerJson() {
		return 
		"\"UpdateCheckPointOperation\":" + 
		"\n{" +
		"\"CheckPointHash\":" + "\"" + Util.dumpBytes(checkPointHash, 16) + "\","  + 
		"\"CheckPointHeight\":" + "\"" + checkPointHeight + "\""  + 
		"\n}\n";
	}

	/* (non-Javadoc)
	 * @see com.eqchains.blockchain.transaction.operation.Operation#parseBody(java.io.ByteArrayInputStream, com.eqchains.blockchain.transaction.Address.AddressShape)
	 */
	@Override
	public void parseBody(ByteArrayInputStream is, LockShape addressShape)
			throws NoSuchFieldException, IOException, IllegalArgumentException {
		checkPointHash = EQCType.parseBIN(is);
		checkPointHeight = EQCType.parseID(is);
	}

	/* (non-Javadoc)
	 * @see com.eqchains.blockchain.transaction.operation.Operation#getBodyBytes(com.eqchains.blockchain.transaction.Address.AddressShape)
	 */
	@Override
	public byte[] getBodyBytes(LockShape addressShape) {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			os.write(EQCType.bytesToBIN(checkPointHash));
			os.write(checkPointHeight.getEQCBits());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return os.toByteArray();
	}

	/**
	 * @return the checkPointHash
	 */
	public byte[] getCheckPointHash() {
		return checkPointHash;
	}

	/**
	 * @param checkPointHash the checkPointHash to set
	 */
	public void setCheckPointHash(byte[] checkPointHash) {
		this.checkPointHash = checkPointHash;
	}

	/**
	 * @return the checkPointHeight
	 */
	public ID getCheckPointHeight() {
		return checkPointHeight;
	}

	/**
	 * @param checkPointHeight the checkPointHeight to set
	 */
	public void setCheckPointHeight(ID checkPointHeight) {
		this.checkPointHeight = checkPointHeight;
	}
	
}
