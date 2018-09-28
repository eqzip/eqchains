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

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.bouncycastle.jcajce.provider.symmetric.ARC4.Base;

import com.eqzip.eqcoin.util.Base58;
import com.eqzip.eqcoin.util.CRC8ITU;
import com.eqzip.eqcoin.util.Log;

/**
 * @author Xun Wang
 * @date Sep 24, 2018
 * @email 10509759@qq.com
 */
public class AddressTool {
	public final static byte V1 = 0;
	public final static int V1_PUBLICKEY_LEN = 33;
	public final static byte V2 = 1;
	public final static int V2_PUBLICKEY_LEN = 65;
	public final static byte V3 = 2;
	
	
	private AddressTool() {}
	
	/**
	 * @param bytes compressed pubkey's EQCCHA hash. Each input is extended 100 times using MultipleExtend
	 * @return EQC address
	 */
	public static String generateAddress(byte[] bytes, byte version) {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		// Calculate (version | trim(HASH))'s CRC8ITU
		os.write(version);
		try {
			os.write(trim(bytes));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		byte crc = CRC8ITU.update(os.toByteArray());
		// Generate address Base58(version) + Base58((trim(HASH) + (version | trim(HASH))'s CRC8ITU))
		os = new ByteArrayOutputStream();
		try {
			os.write(trim(bytes));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		os.write(crc);
		return Base58.encode(new byte[] {version}) + Base58.encode(os.toByteArray());
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
	
}
