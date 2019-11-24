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
package com.eqchains.rpc;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Vector;

import com.eqchains.avro.O;
import com.eqchains.blockchain.accountsmerkletree.AccountsMerkleTree;
import com.eqchains.serialization.EQCType;
import com.eqchains.serialization.EQCType.ARRAY;
import com.eqchains.util.Util;

/**
 * @author Xun Wang
 * @date Jun 27, 2019
 * @email 10509759@qq.com
 */
public class TransactionIndexList<T> extends IO<T> {
	private Vector<TransactionIndex<T>> transactionIndexList;
	private long transactionIndexListSize;
	private long syncTime;
	
	public TransactionIndexList() {
		transactionIndexList = new Vector<>();
	}
	
	public TransactionIndexList(T type) throws Exception {
		transactionIndexList = new Vector<>();
		parse(type);
	}

	/* (non-Javadoc)
	 * @see com.eqchains.serialization.EQCTypable#isSanity()
	 */
	@Override
	public boolean isSanity() {
		if(transactionIndexList == null || syncTime < 0) {
			return false;
		}
		if(transactionIndexList.size() != transactionIndexListSize) {
			return false;
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see com.eqchains.serialization.EQCTypable#isValid(com.eqchains.blockchain.accountsmerkletree.AccountsMerkleTree)
	 */
	@Override
	public boolean isValid(AccountsMerkleTree accountsMerkleTree) throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see com.eqchains.serialization.EQCInheritable#parseHeader(java.io.ByteArrayInputStream)
	 */
	@Override
	public void parseHeader(ByteArrayInputStream is) throws Exception {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see com.eqchains.serialization.EQCInheritable#parseBody(java.io.ByteArrayInputStream)
	 */
	@Override
	public void parseBody(ByteArrayInputStream is) throws Exception {
		ARRAY array = EQCType.parseARRAY(is);
		transactionIndexListSize = array.size;
		ByteArrayInputStream is1 = new ByteArrayInputStream(array.elements);
		for(int i=0; i<array.size; ++i) {
			transactionIndexList.add(new TransactionIndex(is1));
		}
		syncTime = Util.bytesToLong(EQCType.parseEQCBits(is));
	}

	/* (non-Javadoc)
	 * @see com.eqchains.serialization.EQCInheritable#getHeaderBytes()
	 */
	@Override
	public byte[] getHeaderBytes() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see com.eqchains.serialization.EQCInheritable#getBodyBytes()
	 */
	@Override
	public byte[] getBodyBytes() throws Exception {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		Vector<byte[]> bytes = null;
		if (transactionIndexList.size() > 0) {
			bytes = new Vector<>();
			for (TransactionIndex transactionIndex : transactionIndexList) {
				bytes.add(transactionIndex.getBytes());
			}
		}
		os.write(EQCType.bytesArrayToARRAY(bytes));
		os.write(EQCType.longToEQCBits(syncTime));
		return os.toByteArray();
	}

	public void addTransactionIndex(TransactionIndex transactionIndex) {
		transactionIndexList.add(transactionIndex);
	}

	/**
	 * @return the transactionIndexList
	 */
	public Vector<TransactionIndex<T>> getTransactionIndexList() {
		return transactionIndexList;
	}

	/**
	 * @param transactionIndexList the transactionIndexList to set
	 */
	public void setTransactionIndexList(Vector<TransactionIndex<T>> transactionIndexList) {
		this.transactionIndexList = transactionIndexList;
	}

	/**
	 * @return the syncTime
	 */
	public long getSyncTime() {
		return syncTime;
	}

	/**
	 * @param syncTime the syncTime to set
	 */
	public void setSyncTime(long syncTime) {
		this.syncTime = syncTime;
	}
	
}
