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
package com.eqchains.rpc;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Vector;

import com.eqchains.avro.O;
import com.eqchains.blockchain.accountsmerkletree.AccountsMerkleTree;
import com.eqchains.serialization.EQCType;
import com.eqchains.serialization.EQCType.ARRAY;

/**
 * @author Xun Wang
 * @date Jun 28, 2019
 * @email 10509759@qq.com
 */
public class IPList extends AvroO {
	private Vector<String> ipList;
	private long ipListSize;
	
	public IPList() {
		ipList = new Vector<>();
	}
	
	public IPList(O io) throws Exception {
		ipList = new Vector<>();
		parse(io);
	}
	
	/* (non-Javadoc)
	 * @see com.eqchains.serialization.EQCTypable#isSanity()
	 */
	@Override
	public boolean isSanity() {
		if(ipList == null) {
			return false;
		}
		if(ipListSize != ipList.size()) {
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
		ipListSize = array.length;
		ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(array.elements);
		for(int i=0; i<ipListSize; ++i) {
			ipList.add(EQCType.bytesToASCIISting(EQCType.parseBIN(byteArrayInputStream)));
		}
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
		Vector<byte[]> ips = new Vector<>();
		for(String ip:ipList) {
			ips.add(EQCType.bytesToBIN(EQCType.stringToASCIIBytes(ip)));
		}
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		os.write(EQCType.bytesArrayToARRAY(ips));
		return os.toByteArray();
	}

	public void addIP(String ip) {
		if(!ipList.contains(ip)) {
			ipList.add(ip);
		}
	}

	/**
	 * @return the ipList
	 */
	public Vector<String> getIpList() {
		return ipList;
	}

	/**
	 * @param ipList the ipList to set
	 */
	public void setIpList(Vector<String> ipList) {
		this.ipList = ipList;
	}
	
	public boolean contains(String ip) {
		return ipList.contains(ip);
	}
	
	public boolean isEmpty() {
		return ipList.isEmpty();
	}
	
}
