///**
// * EQZIPWallet - EQchains Foundation's EQZIPWallet
// * @copyright 2018-present EQchains Foundation All rights reserved...
// * Copyright of all works released by EQchains Foundation or jointly released by
// * EQchains Foundation with cooperative partners are owned by EQchains Foundation
// * and entitled to protection available from copyright law by country as well as
// * international conventions.
// * Attribution — You must give appropriate credit, provide a link to the license.
// * Non Commercial — You may not use the material for commercial purposes.
// * No Derivatives — If you remix, transform, or build upon the material, you may
// * not distribute the modified material.
// * For any use of above stated content of copyright beyond the scope of fair use
// * or without prior written permission, EQchains Foundation reserves all rights to
// * take any legal action and pursue any right or remedy available under applicable
// * law.
// * https://www.eqchains.com
// * 
// * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
// * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
// * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
// * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
// * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
// * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
// * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
// * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
// * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
// * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
// * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
// */
//package com.eqchains.service;
/////**
//// * EQZIPWallet - EQchains Foundation's EQZIPWallet
//// * @copyright 2018-2019 EQCOIN Foundation Inc.  All rights reserved...
//// * Copyright of all works released by EQCOIN Foundation or jointly released by EQCOIN Foundation 
//// * with cooperative partners are owned by EQCOIN Foundation and entitled to protection
//// * available from copyright law by country as well as international conventions.
//// * Attribution — You must give appropriate credit, provide a link to the license.
//// * Non Commercial — You may not use the material for commercial purposes.
//// * No Derivatives — If you remix, transform, or build upon the material, you may
//// * not distribute the modified material.
//// * For any use of above stated content of copyright beyond the scope of fair use
//// * or without prior written permission, EQCOIN Foundation reserves all rights to take any legal
//// * action and pursue any right or remedy available under applicable law.
//// * https://www.EQCOIN Foundation.com
//// * 
//// * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
//// * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
//// * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
//// * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
//// * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
//// * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
//// * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
//// * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
//// * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
//// * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
//// * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
//// */
/////**
// * EQZIPWallet - EQchains Foundation's EQZIPWallet
// * @copyright 2018-present EQchains Foundation All rights reserved...
// * Copyright of all works released by EQchains Foundation or jointly released by
// * EQchains Foundation with cooperative partners are owned by EQchains Foundation
// * and entitled to protection available from copyright law by country as well as
// * international conventions.
// * Attribution — You must give appropriate credit, provide a link to the license.
// * Non Commercial — You may not use the material for commercial purposes.
// * No Derivatives — If you remix, transform, or build upon the material, you may
// * not distribute the modified material.
// * For any use of above stated content of copyright beyond the scope of fair use
// * or without prior written permission, EQchains Foundation reserves all rights to
// * take any legal action and pursue any right or remedy available under applicable
// * law.
// * https://www.eqchains.com
// * 
// * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
// * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
// * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
// * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
// * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
// * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
// * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
// * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
// * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
// * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
// * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
// */
//package com.eqzip.eqcoin.service;
////
////import com.eqzip.eqcoin.blockchain.AccountsMerkleTree2;
////import com.eqzip.eqcoin.keystore.Keystore;
////
/////**
//// * @author Xun Wang
//// * @date Feb 24, 2019
//// * @email 10509759@qq.com
//// */
////public class AccountsMerkleTreeService {
////	private AccountsMerkleTree2 accountsMerkleTree;
////	private static AccountsMerkleTreeService instance;
////
////	public static synchronized AccountsMerkleTreeService getInstance() {
////		if (instance == null) {
////			synchronized (Keystore.class) {
////				if (instance == null) {
////					instance = new AccountsMerkleTreeService();
////				}
////			}
////		}
////		return instance;
////	}
////	
////	private AccountsMerkleTreeService() {
////		accountsMerkleTree = new AccountsMerkleTree2();
////	}
////	
////	/**
////	 * @return the accountsMerkleTree
////	 */
////	public synchronized AccountsMerkleTree2 getAccountsMerkleTree() {
////		return accountsMerkleTree;
////	}
////	
////}
