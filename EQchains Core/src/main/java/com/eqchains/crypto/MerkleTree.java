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
package com.eqchains.crypto;

import java.util.Iterator;
import java.util.Vector;

import com.eqchains.util.Util;

/**
 * @author Xun Wang
 * @date Nov 12, 2018
 * @email 10509759@qq.com
 */
public class MerkleTree {
	private byte[] root;
	private Vector<byte[]> nodeList;

	public MerkleTree(Vector<byte[]> bytes) {
		nodeList = bytes;
		root = null;
	}

	public void generateRoot() {
		if(nodeList.size() == 0) {
			return;
		}
		Vector<byte[]> nodes = nodeList;
		while ((nodes = getNextNodeList(nodes)).size() > 1) {
		}
		root = nodes.get(0);
	}

	public Vector<byte[]> getNextNodeList(Vector<byte[]> nodes) {
		Vector<byte[]> nextNodeList = new Vector<byte[]>();
		byte[] left = null, right = null, bytes = null;
		Iterator<byte[]> iterator = nodes.iterator();
		while (iterator.hasNext()) {
			// Left node
			left = iterator.next();
			iterator.remove();
			// Right node
			if (iterator.hasNext()) {
				right = iterator.next();
				iterator.remove();
				// Left node and right node's EQCCHA's hash
				bytes = new byte[left.length + right.length];
				System.arraycopy(left, 0, bytes, 0, left.length);
				System.arraycopy(right, 0, bytes, left.length, right.length);
			}
			else {
				bytes = left;
			}
			nextNodeList.add(Util.EQCCHA_MULTIPLE(bytes, Util.HUNDREDPULS, false));
		}
		// Clear original nodes to save memory
		nodes = null;
		return nextNodeList;
	}

	public byte[] getRoot() {
		return root;
	}

}
