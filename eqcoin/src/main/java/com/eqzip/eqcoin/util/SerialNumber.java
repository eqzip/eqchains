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
package com.eqzip.eqcoin.util;

import java.math.BigInteger;

/**
 * @author Xun Wang
 * @date 9-17-2018
 * @email 10509759@qq.com
 */
public class SerialNumber {
	
	private BigInteger serialNumber;
	
	/**
	 * @param bits
	 */
	public SerialNumber(final byte[] bits) {
		serialNumber = Util.bitsToBigInteger(bits);
	}
	
	/**
	 * @param value
	 */
	public SerialNumber(final BigInteger value) {
		serialNumber = value;
	}
	
	/**
	 * The default constructor
	 */
	public SerialNumber() {}
	
	/**
	 * @param sn previous SerialNumber
	 * @return return true if current SN equal to previous SN + 1 otherwise return false
	 */
	public boolean isNextSN(final SerialNumber previousSN) {
		BigInteger tmp = new BigInteger(1, previousSN.serialNumber.toByteArray());
		return (serialNumber.compareTo(tmp.add(BigInteger.ONE)) == 0);
	}
	
	/**
	 * @param bits previous SerialNumber's varbits
	 * @return return true if current SN equal to previous SN + 1 otherwise return false
	 */
	public boolean isNextSN(final byte[] bits) {
		BigInteger tmp = Util.bitsToBigInteger(bits);
		return (serialNumber.compareTo(tmp.add(BigInteger.ONE)) == 0);
	}
	
	/**
	 * @return the next SerialNumber
	 */
	public SerialNumber getNextSN() {
		BigInteger tmp = new BigInteger(1, serialNumber.toByteArray());
		return new SerialNumber(tmp.add(BigInteger.ONE));
	}
	
	/**
	 * @return current serial number's varbits
	 */
	public byte[] getBits() {
		return Util.bigIntegerToBits(serialNumber);
	}
	
	/**
	 * @return the serialNumber
	 */
	public BigInteger getSerialNumber() {
		return serialNumber;
	}
	
	/**
	 * @param serialNumber the serialNumber to set
	 */
	public void setSerialNumber(final BigInteger serialNumber) {
		this.serialNumber = serialNumber;
	}
	
}
