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
import java.nio.ByteBuffer;

import com.eqzip.eqcoin.keystore.AddressTool;
import com.eqzip.eqcoin.util.EQCType;
import com.eqzip.eqcoin.util.Log;
import com.eqzip.eqcoin.util.SerialNumber;
import com.eqzip.eqcoin.util.Util;

/**
 * The PublicKey contains the compressed public key corresponding to the
 * specific address. The public key can be verified with any transaction
 * corresponding to this address.
 * 
 * @author Xun Wang
 * @date Sep 28, 2018
 * @email 10509759@qq.com
 */
public class PublicKey {
	// Current publickey's relevant address' serial number
	private SerialNumber addressSN = null;
	// The No. snAddress relevant public key if the address is V1&V2 which is fixed
	// bytes if is V3 which is bin
	private byte[] publicKey = null;

	public PublicKey() {
		super();
	}

	public PublicKey(byte[] bytes) {
		ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		int type;
		byte[] data;
		int iLen = 0;

		// Parse address' serial number
		ByteBuffer buff = ByteBuffer.allocate(10);
		while ((((type = is.read()) != -1) && ((byte) type & EQCType.BITS) != 0)) {
			buff.put((byte) type);
		}
		buff.put((byte) type);
		buff.flip();
		addressSN = new SerialNumber(buff.array());

		// Parse publicKey
		byte addressVersion = Util.getAddressVersion(addressSN);
		if (addressVersion == AddressTool.V1) {
			data = new byte[AddressTool.V1_PUBLICKEY_LEN];
			try {
				is.read(data);
				publicKey = data;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if (addressVersion == AddressTool.V2) {
			data = new byte[AddressTool.V2_PUBLICKEY_LEN];
			try {
				is.read(data);
				publicKey = data;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if (addressVersion == AddressTool.V3) {
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
						publicKey = data;
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					Log.Error(e.getMessage());
				}

			}
		}
	}
	
	public static boolean isValid(byte[] bytes) {
		ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		int type;
		byte[] data;
		int iLen = 0;
		byte validCount = 0;

		// Parse address' serial number
		ByteBuffer buff = ByteBuffer.allocate(10);
		while ((((type = is.read()) != -1) && ((byte) type & EQCType.BITS) != 0)) {
			buff.put((byte) type);
		}
		buff.put((byte) type);
		buff.flip();
		SerialNumber addressSN = new SerialNumber(buff.array());
		if(type != -1) {
			++validCount;
		}

		// Parse publicKey
		byte addressVersion = Util.getAddressVersion(addressSN);
		if (addressVersion == AddressTool.V1) {
			data = new byte[AddressTool.V1_PUBLICKEY_LEN];
			try {
				iLen = is.read(data);
				if(iLen == data.length) {
					++validCount;
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.Error(e.getMessage());
			}
		} else if (addressVersion == AddressTool.V2) {
			data = new byte[AddressTool.V2_PUBLICKEY_LEN];
			try {
				iLen = is.read(data);
				if(iLen == data.length) {
					++validCount;
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if (addressVersion == AddressTool.V3) {
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
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					Log.Error(e.getMessage());
				}

			}
		}
		return validCount == 2;
	}

	public byte[] getBytes() {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			os.write(addressSN.getBits());
			byte addressVersion = Util.getAddressVersion(addressSN);
			if ((addressVersion == AddressTool.V1) || (addressVersion == AddressTool.V2)) {
				os.write(publicKey);
			} else if (addressVersion == AddressTool.V3) {
				if (publicKey != null) {
					os.write(EQCType.bytesToBin(publicKey));
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
	 * @return the addressSN
	 */
	public SerialNumber getAddressSN() {
		return addressSN;
	}

	/**
	 * @param addressSN the addressSN to set
	 */
	public void setAddressSN(SerialNumber addressSN) {
		this.addressSN = addressSN;
	}

	/**
	 * @return the publicKey
	 */
	public byte[] getPublicKey() {
		return publicKey;
	}

	/**
	 * @param publicKey the publicKey to set
	 */
	public void setPublicKey(byte[] publicKey) {
		this.publicKey = publicKey;
	}

}
