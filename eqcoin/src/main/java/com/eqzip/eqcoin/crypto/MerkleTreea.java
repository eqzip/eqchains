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
package com.eqzip.eqcoin.crypto;

import java.util.Vector;

import com.eqzip.eqcoin.util.Util;

/**
 * @author Xun Wang
 * @date Jan 2, 2019
 * @email 10509759@qq.com
 */
public class MerkleTreea<T> {
	private byte[] root;
	private Vector<byte[]> nodeList;

	public MerkleTreea(Vector<T> nodes) {
		nodeList = new Vector<byte[]>();
		for(T bytes: nodes) {
//			nodeList.add(Util.EQCCHA_MULTIPLE(bytes.getClass().get, 1, true));
		}
		root = null;
	}
}
