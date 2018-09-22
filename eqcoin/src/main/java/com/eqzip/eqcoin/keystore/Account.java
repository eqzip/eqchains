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
package com.eqzip.eqcoin.keystore;

import static org.junit.jupiter.api.Assumptions.assumingThat;

import java.awt.Window.Type;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Arrays;

import org.msgpack.annotation.Message;

import com.eqzip.eqcoin.util.EQCType;
import com.eqzip.eqcoin.util.Util;

/**
 * @author Xun Wang
 * @date 9-19-2018
 * @email 10509759@qq.com
 */

@Message
public class Account {

	private String userName;
	private byte[] pwdHash;
	private byte[] privateKey;
	private String address;
	private long balance;
	
	public Account() {}
	
	/**
	 * @param userName
	 * @param pwdHash
	 * @param privateKey
	 * @param address
	 * @param balance
	 */
	public Account(String userName, byte[] pwdHash, byte[] privateKey, String address, long balance) {
		super();
		this.userName = userName;
		this.pwdHash = pwdHash;
		this.privateKey = privateKey;
		this.address = address;
		this.balance = balance;
	}
	
	public Account(byte[] bytes) {
		super();
		ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		int type;
		type = is.read();
		byte[] data;
		int iLen = 0;

		// Parse userName
		if (EQCType.parseEQCType(type) == EQCType.STRING) {
			data = new byte[EQCType.parseStringLen(type)];
			try {
				iLen = is.read(data);
				if (iLen == data.length) {
					userName = new String(data);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		// Parse pwdHash
		type = is.read();
		if (type == EQCType.BIN8) {
			iLen = is.read();
			data = new byte[iLen];
			try {
				iLen = is.read(data);
				if (iLen == data.length) {
					pwdHash = data;
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		// Parse privateKey
		type = is.read();
		if (type == EQCType.BIN8) {
			iLen = is.read();
			data = new byte[iLen];
			try {
				iLen = is.read(data);
				if (iLen == data.length) {
					privateKey = data;
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		// Parse address
		type = is.read();
		if (EQCType.parseEQCType(type) == EQCType.STRING) {
			data = new byte[EQCType.parseStringLen(type)];
			try {
				iLen = is.read(data);
				if (iLen == data.length) {
					address = new String(data);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		// Parse balance
		data = new byte[8];
		try {
			iLen = is.read(data);
			if (iLen == data.length) {
				balance = Util.bytesToLong(data);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static boolean isValid(byte[] bytes) {
		ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		int type;
		type = is.read();
		byte[] data;
		byte validCount = 0;
		int iLen = 0;

		// Parse userName
		if (EQCType.parseEQCType(type) == EQCType.STRING) {
			data = new byte[EQCType.parseStringLen(type)];
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

		// Parse pwdHash
		type = is.read();
		if (type == EQCType.BIN8) {
			type = 0;
			iLen = is.read();
			data = new byte[iLen];
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

		// Parse privateKey
		type = is.read();
		if (type == EQCType.BIN8) {
			type = 0;
			iLen = is.read();
			data = new byte[iLen];
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
		
		// Parse address
		type = is.read();
		if (EQCType.parseEQCType(type) == EQCType.STRING) {
			data = new byte[EQCType.parseStringLen(type)];
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
		
		// Parse balance
		data = new byte[8];
		try {
			iLen = is.read(data);
			if (iLen == data.length) {
				++validCount;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return validCount == 5;
	}

	public byte[] getBytes() {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			// userName
			os.write(EQCType.stringToBits(userName));
			// pwdHash
			os.write(EQCType.bytesToBin(pwdHash));
			// privateKey
			os.write(EQCType.bytesToBin(privateKey));
			// address
			os.write(EQCType.stringToBits(address));
			// balance
			os.write(Util.longToBytes(balance));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return os.toByteArray();
	}
	
	/**
	 * @return the userName
	 */
	public String getUserName() {
		return userName;
	}

	/**
	 * @param userName the userName to set
	 */
	public void setUserName(String userName) {
		this.userName = userName;
	}

	/**
	 * @return the pwdHash
	 */
	public byte[] getPwdHash() {
		return pwdHash;
	}

	/**
	 * @param pwdHash the pwdHash to set
	 */
	public void setPwdHash(byte[] pwdHash) {
		this.pwdHash = pwdHash;
	}

	/**
	 * @return the privateKey
	 */
	public byte[] getPrivateKey() {
		return privateKey;
	}

	/**
	 * @param privateKey the privateKey to set
	 */
	public void setPrivateKey(byte[] privateKey) {
		this.privateKey = privateKey;
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
	 * @return the balance
	 */
	public long getBalance() {
		return balance;
	}

	/**
	 * @param balance the balance to set
	 */
	public void setBalance(long balance) {
		this.balance = balance;
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
		result = prime * result + Arrays.hashCode(privateKey);
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
		Account other = (Account) obj;
		if (address == null) {
			if (other.address != null)
				return false;
		} else if (!address.equals(other.address))
			return false;
		if (!Arrays.equals(privateKey, other.privateKey))
			return false;
		return true;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Account [userName=" + userName + ", pwdHash=" + Arrays.toString(pwdHash) + ", privateKey="
				+ Arrays.toString(privateKey) + ", address=" + address + ", balance=" + balance + "]";
	}

}
