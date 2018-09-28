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

import com.eqzip.eqcoin.util.Log;
import com.eqzip.eqcoin.util.Util;

/**
 * @author Xun Wang
 * @date 9-12-2018
 * @email 10509759@qq.com
 */
public class EQCHeader {
	/*
	 * previous block hash |  target  | transactions hash | timestamp |  nonce  
   			16 bytes	      4bytes        16 bytes	      8 bytes	 8 bytes   
	 */
	private byte[]	preHash;
	private byte[]	target;
	private byte[]	txHash;
	private long	timestamp;
	private long	nonce;
	
	
	
	/**
	 * @param header
	 */
	public EQCHeader(byte[] header) {
		super();
		preHash = new byte[16];
		target = new byte[4];
		txHash = new byte[16];
		ByteArrayInputStream is = new ByteArrayInputStream(header);
		try {
			is.read(preHash);
			is.read(target);
			is.read(txHash);
			byte[] bytes = new byte[8];
			is.read(bytes);
			timestamp = Util.bytesToLong(bytes);
			is.read(bytes);
			nonce = Util.bytesToLong(bytes);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
	}

	/**
	 * 
	 */
	public EQCHeader() {
		super();
	}

	public byte[] getBytes() {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			os.write(preHash);
			os.write(target);
			os.write(txHash);
			os.write(Util.longToBytes(timestamp));
			os.write(Util.longToBytes(nonce));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return os.toByteArray();
	}
	
	/**
	 * @return the preHash
	 */
	public byte[] getPreHash() {
		return preHash;
	}
	/**
	 * @param preHash the preHash to set
	 */
	public void setPreHash(byte[] preHash) {
		this.preHash = preHash;
	}
	/**
	 * @return the target
	 */
	public byte[] getTarget() {
		return target;
	}
	/**
	 * @param target the target to set
	 */
	public void setTarget(byte[] target) {
		this.target = target;
	}
	/**
	 * @return the txHash
	 */
	public byte[] getTxHash() {
		return txHash;
	}
	/**
	 * @param txHash the txHash to set
	 */
	public void setTxHash(byte[] txHash) {
		this.txHash = txHash;
	}
	/**
	 * @return the timestamp
	 */
	public long getTimestamp() {
		return timestamp;
	}
	/**
	 * @param timestamp the timestamp to set
	 */
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	/**
	 * @return the nonce
	 */
	public long getNonce() {
		return nonce;
	}
	/**
	 * @param nonce the nonce to set
	 */
	public void setNonce(long nonce) {
		this.nonce = nonce;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString() 
	 */
	@Override
	public String toString() {
		return 
				"{\n" +
					"\"EQCHeader\":" + 
					"{\n" +
						"\"preHash\":" + "\"" + Util.getHexString(preHash) + "\"" + ",\n" +
						"\"target\":" + "\"" + Util.getHexString(Util.targetBytesToBigInteger(target).toByteArray()) + "\"" + ",\n" +
						"\"targetBytes\":" + "\"" + Integer.toHexString(Util.bytesToInt(target)) + "\"" + ",\n" +
						"\"txHash\":" + "\"" + Util.getHexString(txHash) + "\"" + ",\n" +
						"\"timestamp\":" + "\"" + Util.getGMTTime(timestamp) + "\"" + ",\n" +
						"\"nonce\":" + "\"" + Long.toHexString(nonce) + "\"" + "\n" +
					"}\n" +
				"}";
	}
	
	
}
