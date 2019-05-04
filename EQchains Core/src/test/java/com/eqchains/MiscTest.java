/**
 * EQchains core - EQchains Foundation's EQchains core library
 * @copyright 2018-present EQchains Foundation All rights reserved...
 * Copyright of all works released by EQchains Foundation or jointly released by
 * EQchains Foundation with cooperative partners are owned by EQchains Foundation
 * and entitled to protection available from copyright law by country as well as
 * international conventions.
 * Attribution — You must give appropriate credit, provide a link to the license.
 * Non Commercial — You may not use the material for commercial purposes.
 * No Derivatives — If you remix, transform, or build upon the material, you may
 * not distribute the modified material.
 * For any use of above stated content of copyright beyond the scope of fair use
 * or without prior written permission, EQchains Foundation reserves all rights to
 * take any legal action and pursue any right or remedy available under applicable
 * law.
 * https://www.eqchains.com
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
package com.eqchains;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.eqchains.blockchain.Account;
import com.eqchains.blockchain.transaction.Address;
import com.eqchains.keystore.Keystore;
import com.eqchains.persistence.h2.EQCBlockChainH2;
import com.eqchains.persistence.rocksdb.EQCBlockChainRocksDB;
import com.eqchains.util.Base58;
import com.eqchains.util.ID;
import com.eqchains.util.Log;
import com.eqchains.util.Util;
import com.eqchains.util.Util.AddressTool;
import com.eqchains.util.Util.AddressTool.AddressType;


/**
 * @author Xun Wang
 * @date Apr 25, 2019
 * @email 10509759@qq.com
 */
public class MiscTest {
	
	   @Test
	    void verifyAddressCRC32C() {
		   String readableAddress = Keystore.getInstance().getUserAccounts().get(0).getReadableAddress();
		   Log.info(readableAddress);
	        assertTrue(AddressTool.verifyAddressCRC32C(readableAddress));
	    }
	   
	   @Test
	   void base58AndCrc32c() {
		   byte[] bytes = Util.getSecureRandomBytes();
		   String address = Base58.encode(bytes);
		   Log.info(address);
		   try {
			byte[] bytes1 = Base58.decode(address);
			assertArrayEquals(bytes, bytes1);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	   }
	   
	   @Test
	   void generateAddress() {
		   byte[] publickey =  Util.AESDecrypt(Keystore.getInstance().getUserAccounts().get(1).getPublicKey(), "abc");
		   String address = AddressTool.generateAddress(publickey, AddressType.T1);
		   Log.info(address);
		   assertTrue(AddressTool.verifyAddressPublickey(address, publickey));
	   }
	   
	   @Test
	   void saveAccount() {
		   Account account = new Account();
		   Address address = new Address();
		   address.setReadableAddress(Keystore.getInstance().getUserAccounts().get(0).getReadableAddress());
		   address.setID(ID.ONE);
		   account.setAddress(address);
		   account.setAddressCreateHeight(ID.ZERO);
		   account.setBalance(Util.MIN_EQC);
		   account.setBalanceUpdateHeight(ID.ZERO);
		   EQCBlockChainRocksDB.getInstance().saveAccount(account);
		   Account account2 = EQCBlockChainRocksDB.getInstance().getAccount(ID.ONE);
		   assertEquals(account, account2);
	   }
	   
	   @Test
	   void verifyAI2Address() {
		   Address address = new Address();
		   address.setReadableAddress(Keystore.getInstance().getUserAccounts().get(0).getReadableAddress());
		   assertEquals(AddressTool.AIToAddress(address.getAddressAI()), address.getReadableAddress());
	   }
	   
	   @Test
	   void ID() {
		   ID id = ID.ZERO;
		   ID id2 = id.add(ID.ONE);
		   assertEquals(id, ID.ZERO);
		   assertEquals(id2, ID.ONE);
	   }
	   
	   @Test
	   void snapshot() {
		   Account account = EQCBlockChainH2.getInstance().getAccountSnapshot(ID.TWO.getNextID(), ID.ONE);
		   assertEquals(account.getBalanceUpdateHeight(), ID.ONE);
	   }
}
