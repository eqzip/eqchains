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

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Vector;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.eqzip.eqcoin.util.Log;
import com.eqzip.eqcoin.util.Util;

/**
 * @author Xun Wang
 * @date Sep 24, 2018
 * @email 10509759@qq.com
 */
class AddressTest {
	private static Vector<String> vec = new Vector<String>();

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeAll
	static void setUpBeforeClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterAll
	static void tearDownAfterClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeEach
	void setUp() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterEach
	void tearDown() throws Exception {
	}

	/**
	 * Test method for
	 * {@link com.eqzip.eqcoin.keystore.Address#generateAddress(byte[], byte)}.
	 */
	@Test
	void testGenerateAddress() {
		for (int i = 0; i < 100; ++i) {
			vec.add(AddressTool.generateAddress(Util.EQCCHA_MULTIPLE(Util.getSecureRandomBytes(), Util.HUNDRED, true), AddressTool.V3));
			Log.info(vec.get(i));
		}
	}

	/**
	 * Test method for
	 * {@link com.eqzip.eqcoin.keystore.Address#verifyAddress(java.lang.String)}.
	 */
	@Test
	void testVerifyAddress() {
		for (int i = 0; i < 100; ++i) {
//			vec.add(AddressTool.generateAddress(Util.EQCCHA_MULTIPLE(Util.getSecureRandomBytes(), Util.HUNDRED, true), AddressTool.V2));
			Log.info(vec.get(i));
			if (AddressTool.verifyAddress(vec.get(i))) {
				Log.info("address " + i + " verify passed");
			}
			else {
				Log.info("address " + i + " verify failed");
			}
			assertTrue(AddressTool.verifyAddress(vec.get(i)));
		}
	}

	/**
	 * Test method for {@link com.eqzip.eqcoin.keystore.Address#trim(byte[])}.
	 */
	@Test
	void testTrim() {
		byte[] bytes = new byte[28];
		for(int i=10; i<20; ++i) {
			bytes[i] = 1;
		}
		byte[] bytes10 = new byte[10];
		for(int i=0; i<10; ++i) {
			bytes10[i] = 1;
		}
		assertTrue(Arrays.equals(AddressTool.trim(bytes), bytes10));
	}

}
