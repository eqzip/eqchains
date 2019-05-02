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
package com.eqchains.blockchain;

import static org.junit.jupiter.api.Assumptions.assumingThat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.Signature;
import java.util.Vector;

import javax.xml.crypto.Data;

import com.eqchains.blockchain.transaction.Address.AddressShape;
import com.eqchains.serialization.EQCTypable;
import com.eqchains.serialization.EQCType;
import com.eqchains.serialization.EQCType.ARRAY;
import com.eqchains.util.Log;
import com.eqchains.util.Util;

/**
 * @author Xun Wang
 * @date Sep 28, 2018
 * @email 10509759@qq.com
 */
public class Signatures implements EQCTypable {
	private Vector<byte[]> signatureList;
	private long signatureListSize;
	/*
	 * VERIFICATION_COUNT equal to the number of member variables of the class to be
	 * verified.
	 */
	private final static byte VERIFICATION_COUNT = 0;

	public Signatures() {
		super();
		signatureList = new Vector<byte[]>();
	}

	public Signatures(byte[] bytes) throws NoSuchFieldException, IOException {
		parseSignatures(bytes);
	}
	
	public Signatures(ByteBuffer byteBuffer) throws NoSuchFieldException, IOException {
		parseSignatures(byteBuffer.array());
	}
	
	private void parseSignatures(byte[] bytes) throws NoSuchFieldException, IOException{
		ARRAY array = EQCType.parseARRAY(bytes);
		if(array != null) {
			signatureListSize = array.length;
			signatureList = array.elements;
		}
	}

	public static boolean isValid(ByteBuffer byteBuffer) throws NoSuchFieldException, IOException {
		return isValid(byteBuffer.array());
	}
	
	public static boolean isValid(byte[] bytes) throws NoSuchFieldException, IOException {
		int validCount = 0;
		ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		ARRAY array = EQCType.parseARRAY(is);
		if((array == null) || !array.isNULL()) {
			++validCount;
		}
		return (validCount > VERIFICATION_COUNT) && EQCType.isInputStreamEnd(is);
	}

	/**
	 * If the signature is V1 or V2 just directly add the raw data if it is V3 then
	 * add bin. The sequence of signatures is the same with transactions.
	 * 
	 * @param bytes The signature's bytes
	 */
	public void addSignature(byte[] bytes) {
		if(bytes == null) {
			return;
		}
		signatureList.add(bytes);
	}

	@Override
	@Deprecated
	public byte[] getBytes() {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			if (signatureList == null || (signatureList.size() == 0)) {
				os.write(EQCType.bytesToBIN(null));
			} else {
				for (byte[] signature : signatureList) {
					os.write(EQCType.bytesToBIN(signature));
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return os.toByteArray();
	}

	/**
	 * Get the Signature's ARRAY
	 * @see com.eqchains.serialization.EQCTypable#getBin()
	 */
	@Override
	public byte[] getBin() {
		// TODO Auto-generated method stub
		return EQCType.bytesArrayToARRAY(signatureList);
	}
	
	public ByteBuffer getByteBuffer() {
		return ByteBuffer.wrap(getBin());
	}

	private String getSignatureListString() {
		String tx = null;
		if (signatureList != null && signatureList.size() > 0) {
			tx = "\n[\n";
			if (signatureList.size() > 1) {
				for (int i = 0; i < signatureList.size() - 1; ++i) {
					tx += getSignatureJson(signatureList.get(i)) + ",\n";
				}
			}
			tx += getSignatureJson(signatureList.get(signatureList.size() - 1));
			tx += "\n]";
		} else {
			tx = "[]";
		}
		return tx;
	}
	
	private String getSignatureJson(byte[] signature) {
		return "{\n\"Signature\":\"" + Util.dumpBytes(signature, 16) + "\"\n}";
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
		
				"\"SignatureList\":" + 
				"\n{\n" +
					"\"Size\":\"" + signatureListSize + "\",\n" +
					"\"List\":" + 
					getSignatureListString() + "\n" +
				"}";
		
	}
	
	public int getSize() {
		return getBin().length;
	}
	
	public Vector<byte[]> getSignatureList(){
		return signatureList;
	}

	@Override
	public boolean isSanity(AddressShape... addressShape) {
		if(addressShape.length != 0) {
			return false;
		}
		if(signatureList.size() != signatureListSize) {
			return false;
		}
		for(byte[] bytes : signatureList) {
			if(bytes == null) {
				return false;
			}
		}
		return false;
	}
	
	public byte[] getSignaturesMerkelTreeRoot() {
		if(signatureList.size() == 0) {
			return null;
		}
		Vector<byte[]> signatures = new Vector<>();
		for(byte[] signature : signatureList) {
			signatures.add(signature);
		}
		return Util.getMerkleTreeRoot(signatures);
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
	
}
