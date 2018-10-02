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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Vector;

<<<<<<< HEAD
import com.eqzip.eqcoin.util.EQCType;
import com.eqzip.eqcoin.util.Log;
import com.eqzip.eqcoin.util.Util;

/**
 * @author Xun Wang
 * @date Sep 28, 2018
 * @email 10509759@qq.com
 */
public class Signatures {
	private Vector<byte[]> signatures;

	public Signatures() {
		super();
	}
	
	/**
	 * If the signature is V1 or V2 just directly add the raw data if it is V3 then add bin.
	 * The sequence of signatures is the same with transactions.
	 * @param bytes	The signature's bytes
	 */
	public void addSignature(byte[] bytes) {
		signatures.add(bytes);
	}
	
	public byte[] getBytes() {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			for(byte[] signature : signatures) {
				os.write(EQCType.bytesToBin(signature));
=======
import com.eqzip.eqcoin.util.Log;
import com.eqzip.eqcoin.util.Util;

/**
 * @author Xun Wang
 * @date Sep 28, 2018
 * @email 10509759@qq.com
 */
public class Signatures {
	private Vector<byte[]> signatures;

	public Signatures() {
		super();
	}
	
	/**
	 * If the signature is V1 or V2 just directly add the raw data if it is V3 then add bin.
	 * The sequence of signatures is the same with transactions.
	 * @param bytes	The signature's bytes
	 */
	public void addSignature(byte[] bytes) {
		signatures.add(bytes);
	}
	
	public byte[] getBytes() {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			for(byte[] signature : signatures) {
				os.write(signature);
>>>>>>> branch 'master' of https://github.com/eqzip/eqcoin.git
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return os.toByteArray();
	}
	
}
