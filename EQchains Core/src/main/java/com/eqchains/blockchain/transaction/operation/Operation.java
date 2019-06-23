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

import org.rocksdb.RocksDBException;

import com.eqchains.blockchain.account.Passport;
import com.eqchains.blockchain.account.Passport.AddressShape;
import com.eqchains.blockchain.accountsmerkletree.AccountsMerkleTree;
import com.eqchains.blockchain.transaction.OperationTransaction;
import com.eqchains.blockchain.transaction.operation.Operation.OP;
import com.eqchains.serialization.EQCAddressShapeInheritable;
import com.eqchains.serialization.EQCAddressShapeTypable;
import com.eqchains.serialization.EQCInheritable;
import com.eqchains.serialization.EQCTypable;
import com.eqchains.serialization.EQCType;
import com.eqchains.util.ID;
import com.eqchains.util.Log;

/**
 * @author Xun Wang
 * @date Mar 27, 2019
 * @email 10509759@qq.com
 */
public abstract class Operation implements EQCAddressShapeTypable, EQCAddressShapeInheritable {

	public enum OP {
		ADDRESS, TXFEERATE, EMAIL, RENEW, INVALID;
		public static OP get(int ordinal) {
			OP op = null;
			switch (ordinal) {
			case 0:
				op = OP.ADDRESS;
				break;
			case 1:
				op = OP.TXFEERATE;
				break;
			case 2:
				op = OP.EMAIL;
				break;
			case 3:
				op = OP.RENEW;
				break;
			default:
				op = OP.INVALID;
				break;
			}
			return op;
		}
	}

	public final static int MAX_OP = OP.EMAIL.ordinal();
	protected OP op;
//	protected ID version;
//	protected Object parameter;

	public Operation(OP op) {
		this.op = op;
	}
	
	public boolean execute(Object ...objects) throws Exception {
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

	public boolean isMeetPreconditions(Object ...objects) throws Exception {
		return false;
	}

	public static OP parseOP(ByteArrayInputStream is) throws NoSuchFieldException, IllegalStateException, IOException {
		is.mark(0);
		OP op = OP.INVALID;
		int opCode = OP.INVALID.ordinal();
		opCode = EQCType.eqcBitsToInt(EQCType.parseEQCBits(is));
		op = OP.get(opCode);
		is.reset();
		return op;
	}

	public static Operation parseOperation(ByteArrayInputStream is, AddressShape addressShape) throws NoSuchFieldException, IllegalArgumentException, IOException {
		Operation operation = null;
		OP op = parseOP(is);

		if (op == OP.ADDRESS) {
			operation = new UpdateAddressOperation(is, addressShape);
		} else if (op == OP.TXFEERATE) {
			operation = new UpdateTxFeeRateOperation();
		} else if (op == OP.EMAIL) {

		} else if (op == OP.RENEW) {

		}
		return operation;
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

	@Override
	public byte[] getBytes(AddressShape addressShape) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public byte[] getBin(AddressShape addressShape) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isSanity(AddressShape addressShape) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isValid(AccountsMerkleTree accountsMerkleTree, AddressShape addressShape) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void parseBody(ByteArrayInputStream is, AddressShape addressShape)
			throws NoSuchFieldException, IOException, IllegalArgumentException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public byte[] getHeaderBytes(AddressShape addressShape) {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			// Serialization OP
			os.write(EQCType.longToEQCBits(op.ordinal()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return os.toByteArray();
	}

	@Override
	public byte[] getBodyBytes(AddressShape addressShape) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void parseHeader(ByteArrayInputStream is, AddressShape addressShape)
			throws NoSuchFieldException, IOException, IllegalArgumentException {
		// Parse OP
		op = OP.get(EQCType.eqcBitsToInt(EQCType.parseEQCBits(is)));
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((op == null) ? 0 : op.hashCode());
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
		Operation other = (Operation) obj;
		if (op != other.op)
			return false;
		return true;
	}
	
}
