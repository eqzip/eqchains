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
package com.eqchains.util;

import java.math.BigInteger;

/**
 * A part of the source code come from
 * https://github.com/bitcoin/bitcoin/blob/master/src/base58.cpp
 * https://github.com/bitcoin-labs/bitcoinj-minimal/blob/master/core/Base58.java
 * Thanks a billion for the contribution.
 * 
 * @author Xun Wang
 * @date 9-19-2018
 * @email 10509759@qq.com
 */
public class Base58 {
	/** All alphanumeric characters except for "0", "I", "O", and "l" */
	private static final String ALPHABET = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz";
	private static final BigInteger BASE58 = BigInteger.valueOf(58);
	private static final int ONE_CODE = (int)'1';
	private static final int NINE_CODE = (int)'9';
	private static final int A_CODE =  (int)'A';
	private static final int Z_CODE =  (int)'Z';
	private static final int a_CODE =  (int)'a';
	private static final int z_CODE =  (int)'z';

	public static String encode(final byte[] bytes) {
		BigInteger foo = new BigInteger(1, bytes);
		StringBuilder sb = new StringBuilder();
		while (foo.compareTo(BASE58) >= 0) {
			BigInteger remainder = foo.mod(BASE58);
			sb.insert(0, ALPHABET.charAt(remainder.intValue()));
			foo = foo.subtract(remainder).divide(BASE58);
		}
		sb.insert(0, ALPHABET.charAt(foo.intValue()));
		// Due to BigInteger ignore the leading zeroes of Hash
		// So here just insert the leading zeroes of Hash into the Number 
		// When length equal to 1 which means it is the type of Address which
		// without leading zeroes doesn't need special handle. But when length bigger
		// than 1 which means it is the Hash of Publickey many be exists leading zeroes
		// need special handle.
		if (bytes.length > 1) {
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
		byte[] decode = null, bytes = null;
		decode = bytes = decodeToBigInteger(input).toByteArray();
		// We may have got one more byte than we wanted, if the high bit of the
		// next-to-last byte was not zero. This
		// is because BigIntegers are represented with twos-compliment notation, thus if
		// the high bit of the last
		// byte happens to be 1 another 8 zero bits will be added to ensure the number
		// parses as positive. Detect
		// that case here and chop it off.
		boolean stripSignByte = bytes.length > 1 && bytes[0] == 0 && bytes[1] < 0;
//		Log.info("stripSignByte: " + stripSignByte);
		// Count the leading zeroes, if any.
		int leadingZeros = 0;
		for (int i = 0; (i < input.length() && input.charAt(i) == ALPHABET.charAt(0)); ++i) {
			leadingZeros++;
		}
		// When length equal to 1 which means it is the type of Address which
		// without leading zeroes doesn't need special handle. But when length bigger
		// than 1 which means it is the Hash of Publickey many be exists leading zeroes
		// need special handle.
		if((stripSignByte || leadingZeros > 0) && bytes.length > 1) {
			decode = new byte[bytes.length - (stripSignByte ? 1 : 0) + leadingZeros];
			System.arraycopy(bytes, stripSignByte ? 1 : 0, decode, leadingZeros, decode.length - leadingZeros);
		}
		return decode;
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

	public static boolean isBase58Char(char candidate) {
		int value = (int)(candidate&0xFFFF);
		if((value >= ONE_CODE && value <= NINE_CODE) || (value >= A_CODE && value <= Z_CODE) || (value >= a_CODE && value <= z_CODE)) {
			return true;
		}
		return false;
	}
	
}
