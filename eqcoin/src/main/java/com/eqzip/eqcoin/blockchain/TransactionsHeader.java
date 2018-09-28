/**
 * EQCoin core - EQZIP's EQCoin core library
 * @copyright 2018 EQZIP Inc.  All rights reserved...
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
package com.eqzip.eqcoin.blockchain;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import com.eqzip.eqcoin.util.EQCType;
import com.eqzip.eqcoin.util.Log;
import com.eqzip.eqcoin.util.SerialNumber;
import com.eqzip.eqcoin.util.Util;

/**
 * @author Xun Wang
 * @date Sep 28, 2018
 * @email 10509759@qq.com
 */
public class TransactionsHeader {
	private byte version = 0;
	private BigInteger height;
	private byte[] signaturesHash = null;
	
	public TransactionsHeader() {
		super();
	}
	
	public TransactionsHeader(byte[] bytes) {
		ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		int type;
		byte[] data;
		int iLen = 0;

		// Parse version
		version = (byte) is.read();
		
		// Parse height
		ByteBuffer buff = ByteBuffer.allocate(10);
		while ((((type = is.read()) != -1) && ((byte) type & EQCType.BITS) != 0)) {
			buff.put((byte) type);
		}
		buff.put((byte) type);
		buff.flip();
		height = Util.bitsToBigInteger(buff.array());

		// Parse signaturesHash
		type = is.read();
		if (type == -1) {
			throw new IndexOutOfBoundsException("During parse signaturesHash error haven't relevant data"); 
		} else if (EQCType.isBin(type)) {
			try {
				// Get xxx raw data
				iLen = EQCType.getBinLen(type);
				data = new byte[iLen];
				is.read(data);
				// Get xxx raw data's value
				iLen = EQCType.getBinDataLen(type, data);
				// Read the content
				data = new byte[iLen];
				iLen = is.read(data);
				if (iLen == data.length) {
					signaturesHash = data;
				}
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

		}
	}
	
	public static boolean isValid(byte[] bytes) {
		ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		int type;
		byte[] data;
		byte validCount = 0;
		int iLen = 0;

		// Parse version
		byte version = (byte) is.read();
		if(version != -1) {
			++validCount;
		}
				
		// Parse height
		ByteBuffer buff = ByteBuffer.allocate(10);
		while ((((type = is.read()) != -1) && (type & EQCType.BITS) != 0)) {
			buff.put((byte) type);
		}
		if (type != -1) {
			buff.put((byte) type);
			++validCount;
		}

		// Parse signaturesHash
		type = is.read();
		if (type == -1) {
			throw new IndexOutOfBoundsException("During parse signaturesHash error haven't relevant data"); 
		} else if (EQCType.isBin(type)) {
			try {
				// Get xxx raw data
				iLen = EQCType.getBinLen(type);
				data = new byte[iLen];
				is.read(data);
				// Get xxx raw data's value
				iLen = EQCType.getBinDataLen(type, data);
				// Read the content
				data = new byte[iLen];
				iLen = is.read(data);
				if (iLen == data.length) {
					++validCount;
				}
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

		}

		return validCount == 3;

	}

	public byte[] getBytes() {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			os.write(version);
			os.write(Util.bigIntegerToBits(height));
			if (signaturesHash != null) {
				os.write(EQCType.bytesToBin(signaturesHash));
			}
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
	public byte getVersion() {
		return version;
	}
	/**
	 * @param version the version to set
	 */
	public void setVersion(byte version) {
		this.version = version;
	}
	/**
	 * @return the height
	 */
	public BigInteger getHeight() {
		return height;
	}
	/**
	 * @param height the height to set
	 */
	public void setHeight(BigInteger height) {
		this.height = height;
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
	
	
	
}
