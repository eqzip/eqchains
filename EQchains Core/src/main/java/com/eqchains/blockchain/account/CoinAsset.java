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
package com.eqchains.blockchain.account;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import com.eqchains.util.ID;
import com.eqchains.util.Util;

/**
 * @author Xun Wang
 * @date Jun 6, 2019
 * @email 10509759@qq.com
 */
public class CoinAsset extends Asset {

	public CoinAsset() {
		super(AssetType.COIN);
	}

	public CoinAsset(ByteArrayInputStream is) throws NoSuchFieldException, IOException {
		super(is);
	}
	@Override
	public boolean isSanity() {
		if(assetType == null || version == null || assetID == null || balance == null || nonce == null) {
			return false;
		}
		if(!version.isSanity() || !assetID.isSanity() || !nonce.isSanity()) {
			return false;
		}
		if(assetType != AssetType.COIN) {
			return false;
		}
		if(assetID.equals(Asset.EQCOIN)) {
			if(balance.compareTo(new ID(Util.MIN_EQC)) < 0) {
				return false;
			}
		}
		else {
			if(!balance.isSanity()) {
				return false;
			}
		}
		return true;
	}
	@Override
	public String toInnerJson() {
		return 
				"\"CoinAsset\":" + 
				"\n{\n" +
					"\"AssetID\":" + "\"" + assetID + "\"" + ",\n" +
					"\"Balance\":" + "\"" + balance + "\"" + ",\n" +
					"\"Nonce\":" + "\"" + nonce + "\"" + "\n" +
				"}";
	}
}
