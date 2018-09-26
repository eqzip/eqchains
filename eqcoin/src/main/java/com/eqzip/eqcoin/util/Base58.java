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
 * A part of the source code come from
 * https://github.com/bitcoin-labs/bitcoinj-minimal/blob/master/core/Base58.java
 * Thanks a billion for the contribution.
 * 
 * @author Xun Wang
 * @date 9-19-2018
 * @email 10509759@qq.com
 */
public class Base58 {

	private static final String ALPHABET = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz";
	private static final BigInteger BASE58 = BigInteger.valueOf(58);

	public static String encode(final byte[] bytes) {
		BigInteger foo = new BigInteger(1, bytes);
		StringBuilder sb = new StringBuilder();
		while (foo.compareTo(BASE58) >= 0) {
			BigInteger remainder = foo.mod(BASE58);
			sb.insert(0, ALPHABET.charAt(remainder.intValue()));
			foo = foo.subtract(remainder).divide(BASE58);
		}
		sb.insert(0, ALPHABET.charAt(foo.intValue()));
		// Due to BigInteger ignore the leading zeroes Convert leading zeroes too.
		if (bytes.length > 1 && bytes[0] == 0 && bytes[1] < 0) {
			for (byte b : bytes) {
				if (b == 0) {
					sb.insert(0, ALPHABET.charAt(0));
				} else {
					break;
				}
			}
		}
		return sb.toString();
	}

	public static byte[] decode(String input) throws Exception {
		byte[] bytes = decodeToBigInteger(input).toByteArray();
		// We may have got one more byte than we wanted, if the high bit of the
		// next-to-last byte was not zero. This
		// is because BigIntegers are represented with twos-compliment notation, thus if
		// the high bit of the last
		// byte happens to be 1 another 8 zero bits will be added to ensure the number
		// parses as positive. Detect
		// that case here and chop it off.
		boolean stripSignByte = bytes.length > 1 && bytes[0] == 0 && bytes[1] < 0;
		// Count the leading zeros, if any.
		int leadingZeros = 0;
		for (int i = 0; (i < input.length() && input.charAt(i) == ALPHABET.charAt(0)); ++i) {
			leadingZeros++;
		}
		// Now cut/pad correctly. Java 6 has a convenience for this, but Android can't
		// use it.
		byte[] tmp = null;
		if (decodeToBigInteger(input).compareTo(BigInteger.ZERO) != 0) {
			tmp = new byte[bytes.length - (stripSignByte ? 1 : 0) + leadingZeros];
			System.arraycopy(bytes, stripSignByte ? 1 : 0, tmp, leadingZeros, tmp.length - leadingZeros);
		}
		else {
			tmp = new byte[] {0};
		}
		return tmp;
	}

	public static BigInteger decodeToBigInteger(String input) throws Exception {
		BigInteger bi = BigInteger.ZERO;
		// Work backwards through the string.
		for (int i = input.length() - 1; i >= 0; --i) {
			int alphaIndex = ALPHABET.indexOf(input.charAt(i));
			if (alphaIndex == -1) {
				throw new Exception("Illegal character " + input.charAt(i) + " at " + i);
			}
			bi = bi.add(BigInteger.valueOf(alphaIndex).multiply(BASE58.pow(input.length() - 1 - i)));
		}
		return bi;
	}

}
