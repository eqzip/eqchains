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
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import com.eqzip.eqcoin.util.EQCType;
import com.eqzip.eqcoin.util.Log;
import com.eqzip.eqcoin.util.SerialNumber;
import com.eqzip.eqcoin.util.Util;

/**
 * @author Xun Wang
 * @date Sep 27, 2018
 * @email 10509759@qq.com
 */
public class Address {
	private SerialNumber sn = null;
	private String address = null;
//	private final String email = null; // not support in mvp status
	private byte[] code = null;

	/**
	 * @param sn
	 * @param address
	 * @param data
	 */
	public Address(SerialNumber sn, String address, byte[] data) {
		super();
		this.sn = sn;
		this.address = address;
		this.code = data;
	}
	
	public Address() {
		
	}
	
	public Address(byte[] bytes) {
		ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		int type;
		byte[] data;
		int iLen = 0;

		// Parse SN
		ByteBuffer buff = ByteBuffer.allocate(10);
		while ((((type = is.read()) != -1) && ((byte) type & EQCType.BITS) != 0)) {
			buff.put((byte) type);
		}
		buff.put((byte) type);
		buff.flip();
		sn = new SerialNumber(buff.array());

		// Parse address
		type = is.read();
		if (EQCType.parseEQCType(type) == EQCType.FIXEDDATA) {
			data = new byte[EQCType.parseFixedDataLen(type)];
			try {
				iLen = is.read(data);
				if (iLen == data.length) {
					address = new String(data, StandardCharsets.US_ASCII);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		// Parse code
		type = is.read();
		if (type == -1) {
			code = null;
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
					code = data;
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

		// Parse SN
		ByteBuffer buff = ByteBuffer.allocate(10);
		while ((((type = is.read()) != -1) && (type & EQCType.BITS) != 0)) {
			buff.put((byte) type);
		}
		if (type != -1) {
			buff.put((byte) type);
			++validCount;
		}

		// Parse address
		type = is.read();
		if (EQCType.parseEQCType(type) == EQCType.FIXEDDATA) {
			data = new byte[EQCType.parseFixedDataLen(type)];
			try {
				iLen = is.read(data);
				if (iLen == data.length) {
					++validCount;
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		// Parse data
		type = is.read();
		if (type == -1) {
			++validCount;
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
			os.write(sn.getBits());
			os.write(EQCType.stringToFixedData(address));
			if (code != null) {
				os.write(EQCType.bytesToBin(code));
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return os.toByteArray();
	}

	/**
	 * @return the sn
	 */
	public SerialNumber getSn() {
		return sn;
	}

	/**
	 * @param sn the sn to set
	 */
	public void setSn(SerialNumber sn) {
		this.sn = sn;
	}

	/**
	 * @return the address
	 */
	public String getAddress() {
		return address;
	}

	/**
	 * @param address the address to set
	 */
	public void setAddress(String address) {
		this.address = address;
	}

	/**
	 * @return the data
	 */
	public byte[] getData() {
		return code;
	}

	/**
	 * @param data the data to set
	 */
	public void setData(byte[] data) {
		this.code = data;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((address == null) ? 0 : address.hashCode());
		result = prime * result + Arrays.hashCode(code);
		result = prime * result + ((sn == null) ? 0 : sn.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
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
		Address other = (Address) obj;
		if (address == null) {
			if (other.address != null)
				return false;
		} else if (!address.equals(other.address))
			return false;
		if (!Arrays.equals(code, other.code))
			return false;
		if (sn == null) {
			if (other.sn != null)
				return false;
		} else if (!sn.equals(other.sn))
			return false;
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return 
			"{\n" +
				"\"Address\":" + 
				"{\n" +
					"\"sn\":" + "\"" + sn.toString() + "\"" + ",\n" +
					"\"address\":" + "\"" + address + "\"" + ",\n" +
					"\"code\":" + "\"" + Util.getHexString(code) + "\"" + "\n" +
				"}\n" +
			"}";
	}

}
