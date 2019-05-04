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
package com.eqchains.blockchain;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import com.eqchains.blockchain.transaction.Address.AddressShape;
import com.eqchains.serialization.EQCTypable;
import com.eqchains.serialization.EQCType;
import com.eqchains.util.ID;
import com.eqchains.util.Log;
import com.eqchains.util.Util;

/**
 * @author Xun Wang
 * @date Sep 28, 2018
 * @email 10509759@qq.com
 */
public class TransactionsHeader implements EQCTypable {
	private BigInteger version;
	// Bin type 16 bytes use EQCCHA_MULTIPLE(final byte[] bytes, 1, true) generate
	// it.
	private byte[] signaturesHash = null;
	private final static int signaturesHashLen = 16;
	/*
	 * VERIFICATION_COUNT equal to the number of member variables of the class to be
	 * verified.
	 * 
	 * Due to signaturesHash is optional(When only have CoinBase transaction the signaturesHash is null).
	 * Which is BIN7 type
	 */
	private final static byte VERIFICATION_COUNT = 2;

	public TransactionsHeader() {
		super();
		version = BigInteger.ZERO;
	}

	public TransactionsHeader(byte[] bytes) throws NoSuchFieldException, IOException {
		ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		byte[] data = null;

		// Parse version
		if ((data = EQCType.parseEQCBits(is)) != null) {
			version = EQCType.eqcBitsToBigInteger(data);
		}
		
		// Parse signaturesHash
		data = null;
		if ((data = EQCType.parseBIN(is)) != null && !EQCType.isBINX(data)) {
			signaturesHash = data;
		}
	}

	public static boolean isValid(byte[] bytes) throws NoSuchFieldException, IOException {
		ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		byte[] data = null;
		byte validCount = 0;

		// Parse version
		if (((data = EQCType.parseEQCBits(is)) != null) && !EQCType.isNULL(data)) {
			++validCount;
		}

		// Parse signaturesHash(optional can be null if haven't any Transaction except CoinBase this field will be null)
		data = null;
		if (((data = EQCType.parseBIN(is)) != null)) {
			++validCount;
		}

		return (validCount == VERIFICATION_COUNT) && EQCType.isInputStreamEnd(is);
	}

	@Override
	public byte[] getBytes() {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			os.write(EQCType.bigIntegerToEQCBits(version));
			os.write(EQCType.bytesToBIN(signaturesHash));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return os.toByteArray();
	}

	/**
	 * @return the version
	 */
	public BigInteger getVersion() {
		return version;
	}

	/**
	 * @param version the version to set
	 */
	public void setVersion(BigInteger version) {
		this.version = version;
	}

	/**
	 * @return the signaturesHash
	 */
	public byte[] getSignaturesHash() {
		return signaturesHash;
	}

	/**
	 * @param signaturesHash the signaturesHash to set
	 */
	public void setSignaturesHash(byte[] signaturesHash) {
		this.signaturesHash = signaturesHash;
	}

	@Override
	public byte[] getBin() {
		return EQCType.bytesToBIN(getBytes());
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
				"\"TransactionsHeader\":" + 
				"{\n" +
					"\"version\":" + "\"" + Long.toHexString(version.longValue()) + "\"" + ",\n" +
					"\"signaturesHash\":" + "\"" + Util.getHexString(signaturesHash)  + "\"" + "\n" +
				"}";
	}
	
	public int getSize() {
		return getBytes().length;
	}

	@Override
	public boolean isSanity(AddressShape... addressShape) {
		// TODO Auto-generated method stub
		return false;
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
