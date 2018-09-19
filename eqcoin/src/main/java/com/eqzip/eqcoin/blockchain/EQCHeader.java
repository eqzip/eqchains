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

import com.eqzip.eqcoin.util.Util;

/**
 * @author Xun Wang
 * @date 9-12-2018
 * @email 10509759@qq.com
 */
public class EQCHeader {
	/*
	 * previous block hash |  target  | transactions hash | timestamp |  nonce  
   			64 bytes	      64bytes        64 bytes	      8 bytes	 8 bytes   
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
		preHash = new byte[64];
		target = new byte[64];
		txHash = new byte[64];
		System.arraycopy(header, 0, preHash, 0, 64);
		System.arraycopy(header, 64, target, 0, 64);
		System.arraycopy(header, 128, txHash, 0, 64);
		
		byte[] bytes = new byte[8];
		System.arraycopy(header, 192, bytes, 0, 8);
		timestamp = Util.bytesToLong(bytes);
		
		System.arraycopy(header, 200, bytes, 0, 8);
		nonce = Util.bytesToLong(bytes);
	}

	/**
	 * 
	 */
	public EQCHeader() {
		super();
	}

	public byte[] getBytes() {
		byte[] bytes = new byte[208];
		
		System.arraycopy(preHash, 0, bytes, 0, 64);
		System.arraycopy(target, 0, bytes, 64, 64);
		System.arraycopy(txHash, 0, bytes, 128, 64);
		System.arraycopy(Util.longToBytes(timestamp), 0, bytes, 192, 8);
		System.arraycopy(Util.longToBytes(nonce), 0, bytes, 200, 8);
		
		return bytes;
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
						"\"target\":" + "\"" + Util.getHexString(target) + "\"" + ",\n" +
						"\"txHash\":" + "\"" + Util.getHexString(txHash) + "\"" + ",\n" +
						"\"timestamp\":" + "\"" + Util.getGMTTime(timestamp) + "\"" + ",\n" +
						"\"nonce\":" + "\"" + Long.toHexString(nonce) + "\"" + "\n" +
					"}\n" +
				"}";
	}
	
	
}
