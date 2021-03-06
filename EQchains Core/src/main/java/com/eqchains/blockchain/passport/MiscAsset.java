/**
 * EQcoin core - EQcoin Federation's EQcoin core library
 * @copyright 2018-present EQcoin Federation All rights reserved...
 * Copyright of all works released by EQcoin Federation or jointly released by
 * EQcoin Federation with cooperative partners are owned by EQcoin Federation
 * and entitled to protection available from copyright law by country as well as
 * international conventions.
 * Attribution — You must give appropriate credit, provide a link to the license.
 * Non Commercial — You may not use the material for commercial purposes.
 * No Derivatives — If you remix, transform, or build upon the material, you may
 * not distribute the modified material.
 * For any use of above stated content of copyright beyond the scope of fair use
 * or without prior written permission, EQcoin Federation reserves all rights to
 * take any legal action and pursue any right or remedy available under applicable
 * law.
 * https://www.eqcoin.org
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
package com.eqchains.blockchain.passport;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.eqchains.blockchain.passport.Asset.AssetType;
import com.eqchains.serialization.EQCType;
import com.eqchains.util.ID;
import com.eqchains.util.Log;
import com.eqchains.util.Util;

/**
 * @author Xun Wang
 * @date Jun 6, 2019
 * @email 10509759@qq.com
 */
public class MiscAsset extends Asset {

	public MiscAsset() {
		super(AssetType.MISC);
	}
	
	public MiscAsset(ByteArrayInputStream is) throws NoSuchFieldException, IOException {
		super(AssetType.MISC);
		parseHeader(is);
		parseBody(is);
	}

	@Override
	public boolean isSanity() {
		if(assetType == null || version == null || assetID == null || nonce == null) {
			return false;
		}
		if(balance != null) {
			return false;
		}
		if(!version.isSanity() || !assetID.isSanity() || !nonce.isSanity()) {
			return false;
		}
		if(assetType != AssetType.MISC) {
			return false;
		}
		return true;
	}
	@Override
	public String toInnerJson() {
		return 
				"\"MiscAsset\":" + 
				"\n{\n" +
					"\"AssetID\":" + "\"" + assetID + "\"" + ",\n" +
					"\"Nonce\":" + "\"" + nonce + "\"" + "\n" +
				"}";
	}

	/* (non-Javadoc)
	 * @see com.eqchains.blockchain.account.Asset#parseBody(java.io.ByteArrayInputStream)
	 */
	@Override
	public void parseBody(ByteArrayInputStream is) throws NoSuchFieldException, IOException {
		// Parse AssetID
		assetID = new ID(EQCType.parseEQCBits(is));
		
		// Parse Nonce
		nonce = new ID(EQCType.parseEQCBits(is));
	}

	/* (non-Javadoc)
	 * @see com.eqchains.blockchain.account.Asset#getBodyBytes()
	 */
	@Override
	public byte[] getBodyBytes() {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			os.write(assetID.getEQCBits());
			os.write(nonce.getEQCBits());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return os.toByteArray();
	}

}
