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
public class Transaction {
	private TxIn txIn;
	private Vector<TxOut> txOutList;
	private byte[] signature;
	public final static int MAX_TXOUT = 10;
	
	public Transaction() {
		super();
	}
	
	public byte[] getBytes() {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			// When signature&verify should include the hash of the block header where the TxIn address is located at the beginning.
			// But here for save the space just doen't include it.
			os.write(Util.longToBits(txIn.value));
			for(TxOut txOut : txOutList) {
				os.write(Util.longToBits(txOut.getValue()));
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return os.toByteArray();
	}
	
	public byte[] getBin() {
		return EQCType.bytesToBin(getBytes());
	}
	
	public void addTxOut(TxOut txOut) {
		if(txOutList.size() >= MAX_TXOUT) {
			throw new UnsupportedOperationException("The number of TxOut cannot exceed 10.");
		}
		if(!isTxOutExists(txOut)) {
			txOutList.add(txOut);
		}
	}
	
	public int getTxOutNumber() {
		return txOutList.size();
	}
	
	public long getTxFee() {
		long totalTxOut = 0;
		for(TxOut txOut : txOutList) {
			totalTxOut += txOut.getValue();
		}
		return txIn.value - totalTxOut;
	}
	
	public boolean isTxOutExists(TxOut txOut) {
		return txOutList.contains(txOut);
	}

	/**
	 * @return the txIn
	 */
	public TxIn getTxIn() {
		return txIn;
	}

	/**
	 * @param txIn the txIn to set
	 */
	public void setTxIn(TxIn txIn) {
		this.txIn = txIn;
	}

	/**
	 * @return the txOutList
	 */
	public Vector<TxOut> getTxOutList() {
		return txOutList;
	}

	/**
	 * @param txOutList the txOutList to set
	 */
	public void setTxOutList(Vector<TxOut> txOutList) {
		this.txOutList = txOutList;
	}

	/**
	 * @return the signature
	 */
	public byte[] getSignature() {
		return signature;
	}

	/**
	 * @param signature the signature to set
	 */
	public void setSignature(byte[] signature) {
		this.signature = signature;
	}
=======
import com.eqzip.eqcoin.util.Log;
import com.eqzip.eqcoin.util.Util;

/**
 * @author Xun Wang
 * @date Sep 28, 2018
 * @email 10509759@qq.com
 */
public class Transaction {
	private TxIn txIn;
	private Vector<TxOut> vecTxOut;
	
	public Transaction() {
		super();
	}
	
	public byte[] getBytes() {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			os.write(Util.longToBits(txIn.value));
			for(TxOut txOut : vecTxOut) {
				os.write(Util.longToBits(txOut.value));
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return os.toByteArray();
	}
	
	public void addTxOut(TxOut txOut) {
		if(!isTxOutExists(txOut)) {
			vecTxOut.add(txOut);
		}
	}
	
	public long getTxFee() {
		long totalTxOut = 0;
		for(TxOut txOut : vecTxOut) {
			totalTxOut += txOut.value;
		}
		return txIn.value - totalTxOut;
	}
	
	public boolean isTxOutExists(TxOut txOut) {
		return vecTxOut.contains(txOut);
	}

	/**
	 * @return the txIn
	 */
	public TxIn getTxIn() {
		return txIn;
	}

	/**
	 * @param txIn the txIn to set
	 */
	public void setTxIn(TxIn txIn) {
		this.txIn = txIn;
	}

>>>>>>> branch 'master' of https://github.com/eqzip/eqcoin.git
	
}
