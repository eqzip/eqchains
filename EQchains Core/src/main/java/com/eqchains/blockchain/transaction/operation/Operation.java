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
import java.io.IOException;

import com.eqchains.blockchain.AccountsMerkleTree;
import com.eqchains.blockchain.transaction.Address;
import com.eqchains.blockchain.transaction.OperationTransaction;
import com.eqchains.blockchain.transaction.Address.AddressShape;
import com.eqchains.serialization.EQCTypable;
import com.eqchains.serialization.EQCType;
import com.eqchains.util.ID;
import com.eqchains.util.Log;

/**
 * @author Xun Wang
 * @date Mar 27, 2019
 * @email 10509759@qq.com
 */
public class Operation implements EQCTypable {

	public enum OP {
		ADDRESS, EMAIL, RENEW, INVALID;
		public static OP get(int ordinal) {
			OP op = null;
			switch (ordinal) {
			case 0:
				op = OP.ADDRESS;
				break;
			case 1:
				op = OP.EMAIL;
				break;
			case 2:
				op = OP.RENEW;
				break;
			case 3:
			default:
				op = OP.INVALID;
				break;
			}
			return op;
		}
	}

	public final static int MAX_OP = OP.EMAIL.ordinal();
	protected OP op;
	protected Object parameter;
	/*
	 * VERIFICATION_COUNT equal to the number of member variables of the class to be
	 * verified.
	 */
	protected final static byte VERIFICATION_COUNT = 2;

	public Operation(OP op) {
		this.op = op;
	}
	
	public boolean execute(Object ...objects) {
		return false;
	}
	
	/**
	 * @return the op
	 */
	public OP getOP() {
		return op;
	}

	/**
	 * @param op the op to set
	 */
	public void setOP(OP op) {
		this.op = op;
	}

	@Override
	public byte[] getBytes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public byte[] getBin() {
		// TODO Auto-generated method stub
		return null;
	}

	public byte[] getBytes(Address.AddressShape addressShape) {
		return null;
	}

	public byte[] getBin(Address.AddressShape addressShape) {
		return null;
	}

	public static boolean isValid(byte[] bytes) {
		return false;
	}

	public boolean isValid() {
		return false;
	}
	
	public boolean isMeetPreconditions(Object ...objects) {
		return false;
	}

	public static OP parseOP(byte[] bytes) {
		OP op = OP.INVALID;
		ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		byte[] data = null;
		int opCode = -1;
		try {
			if ((data = EQCType.parseEQCBits(is)) != null && !EQCType.isNULL(data)) {
				opCode = EQCType.eqcBitsToInt(data);
			}
			op = OP.get(opCode);
		} catch (NoSuchFieldException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return op;
	}

	public static Operation parseOperation(byte[] bytes, AddressShape addressShape) {
		Operation operation = null;
		OP op = parseOP(bytes);

		if (op == OP.ADDRESS) {
			operation = new UpdateAddressOperation(bytes, addressShape);
		} else if (op == OP.EMAIL) {

		} else if (op == OP.RENEW) {

		}
		return operation;
	}

	@Override
	public boolean isSanity(AddressShape... addressShape) {
		// TODO Auto-generated method stub
		return false;
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
		return null;
	}

}
