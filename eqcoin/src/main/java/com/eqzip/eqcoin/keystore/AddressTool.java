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
package com.eqzip.eqcoin.keystore;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.bouncycastle.jcajce.provider.symmetric.ARC4.Base;

import com.eqzip.eqcoin.keystore.AddressTool.AddressType;
import com.eqzip.eqcoin.util.Base58;
import com.eqzip.eqcoin.util.Log;

/**
 * @author Xun Wang
 * @date Sep 24, 2018
 * @email 10509759@qq.com
 */
public class AddressTool {
	public final static int T1_PUBLICKEY_LEN = 33;
	public final static int T2_PUBLICKEY_LEN = 67;
	
	public enum AddressType{
		T1, T2, T3
	}
	
	private AddressTool() {}
	
	/**
	 * @param bytes compressed pubkey's EQCCHA hash. Each input is extended 100 times using MultipleExtend
	 * @param type EQC Address’ type
	 * @return EQC address
	 */
	public static String generateAddress(byte[] bytes, AddressType type) {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		// Calculate (type | trim(HASH))'s CRC8ITU
		os.write(type.ordinal());
		try {
			os.write(trim(bytes));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		byte crc = CRC8ITU.update(os.toByteArray());
		// Generate address Base58(type) + Base58((trim(HASH) + (type | trim(HASH))'s CRC8ITU))
		os = new ByteArrayOutputStream();
		try {
			os.write(trim(bytes));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		os.write(crc);
		return Base58.encode(new byte[] {(byte) type.ordinal()}) + Base58.encode(os.toByteArray());
	}
	
	public static boolean verifyAddress(String address) {
		byte[] bytes = null;
		try {
			bytes = Base58.decode(address.substring(1));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		byte crc = bytes[bytes.length-1];
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			byte[] b = Base58.decode(address.substring(0, 1));
			os.write(Base58.decode(address.substring(0, 1)));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		os.write(bytes, 0, bytes.length-1);
		byte CRC = CRC8ITU.update(os.toByteArray());
		return crc == CRC;
	}
	
	public static byte[] trim(final byte[] bytes) {
		int i=0;
		for(; i<bytes.length; ++i) {
			if(bytes[i] != 0) {
				break;
			}
		}
		int j=bytes.length-1;
		for(; j>0; --j) {
			if(bytes[j] != 0) {
				break;
			}
		}
		byte[] trim = new byte[j-i+1];
		System.arraycopy(bytes, i, trim, 0, trim.length);
		return trim;
	}
	
	public static AddressType getAddressType(String address) {
		byte type = 0;
		AddressType addressType = AddressType.T1;
		try {
			type = Base58.decodeToBigInteger(address.substring(0, 1)).byteValue();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		if(type == 0) {
			addressType = AddressType.T1;
		}
		else if(type == 1) {
			addressType = AddressType.T2;
		}
		else if(type == 2) {
			addressType = AddressType.T3;
		}
		return addressType;
	}
	
}
