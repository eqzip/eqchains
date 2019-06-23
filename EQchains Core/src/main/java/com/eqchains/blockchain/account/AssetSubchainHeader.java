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
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.eqchains.blockchain.account.Passport.AddressShape;
import com.eqchains.blockchain.accountsmerkletree.AccountsMerkleTree;
import com.eqchains.serialization.EQCInheritable;
import com.eqchains.serialization.EQCTypable;
import com.eqchains.serialization.EQCType;
import com.eqchains.util.ID;
import com.eqchains.util.Log;
import com.eqchains.util.Util;

/**
 * @author Xun Wang
 * @date May 19, 2019
 * @email 10509759@qq.com
 */
public class AssetSubchainHeader implements EQCTypable, EQCInheritable {
	private ID founderID;
	private ID subchainID;
	private String subchainName;
	private String symbol;
	private String decimals;
	private ID maxSupply;
	private boolean ifCanChangeMaxSupply;
	private ID totalSupply;
	private boolean ifCanChangeTotalSupply;
	private boolean ifCanBurn;
	private ID totalAccountNumbers;
	private ID totalTransactionNumbers;
	// Need do more job to use full regression check the url's format if is valid
	// length less than 20
	private String url;
	private byte[] logo;
	
	public AssetSubchainHeader() {}
	
	public AssetSubchainHeader(byte[] bytes) throws NoSuchFieldException, IOException {
		EQCType.assertNotNull(bytes);
		ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		parseBody(is);
	}
	
	public AssetSubchainHeader(ByteArrayInputStream is) throws NoSuchFieldException, IOException {
		EQCType.assertNotNull(is);
		parseBody(is);
	}
	
	@Override
	public byte[] getBytes() {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			os.write(getBodyBytes());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return os.toByteArray();
	}

	@Override
	public byte[] getBin() {
		// TODO Auto-generated method stub
		return null;
	}
	
	/**
	 * @return the subchainName
	 */
	public String getSubchainName() {
		return subchainName;
	}

	/**
	 * @param subchainName the subchainName to set
	 */
	public void setSubchainName(String subchainName) {
		this.subchainName = subchainName;
	}

	/**
	 * @return the symbol
	 */
	public String getSymbol() {
		return symbol;
	}

	/**
	 * @param symbol the symbol to set
	 */
	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	/**
	 * @return the decimals
	 */
	public String getDecimals() {
		return decimals;
	}

	/**
	 * @param decimals the decimals to set
	 */
	public void setDecimals(String decimals) {
		this.decimals = decimals;
	}

	/**
	 * @return the maxSupply
	 */
	public ID getMaxSupply() {
		return maxSupply;
	}

	/**
	 * @param maxSupply the maxSupply to set
	 */
	public void setMaxSupply(ID maxSupply) {
		this.maxSupply = maxSupply;
	}

	/**
	 * @return the ifCanChangeMaxSupply
	 */
	public boolean isIfCanChangeMaxSupply() {
		return ifCanChangeMaxSupply;
	}

	/**
	 * @param ifCanChangeMaxSupply the ifCanChangeMaxSupply to set
	 */
	public void setIfCanChangeMaxSupply(boolean ifCanChangeMaxSupply) {
		this.ifCanChangeMaxSupply = ifCanChangeMaxSupply;
	}

	/**
	 * @return the totalSupply
	 */
	public ID getTotalSupply() {
		return totalSupply;
	}

	/**
	 * @param totalSupply the totalSupply to set
	 */
	public void setTotalSupply(ID totalSupply) {
		this.totalSupply = totalSupply;
	}

	/**
	 * @return the ifCanChangeTotalSupply
	 */
	public boolean isIfCanChangeTotalSupply() {
		return ifCanChangeTotalSupply;
	}

	/**
	 * @param ifCanChangeTotalSupply the ifCanChangeTotalSupply to set
	 */
	public void setIfCanChangeTotalSupply(boolean ifCanChangeTotalSupply) {
		this.ifCanChangeTotalSupply = ifCanChangeTotalSupply;
	}

	/**
	 * @return the ifCanBurn
	 */
	public boolean isIfCanBurn() {
		return ifCanBurn;
	}

	/**
	 * @param ifCanBurn the ifCanBurn to set
	 */
	public void setIfCanBurn(boolean ifCanBurn) {
		this.ifCanBurn = ifCanBurn;
	}

	/**
	 * @return the totalAccountNumbers
	 */
	public ID getTotalAccountNumbers() {
		return totalAccountNumbers;
	}

	/**
	 * @param totalAccountNumbers the totalAccountNumbers to set
	 */
	public void setTotalAccountNumbers(ID totalAccountNumbers) {
		this.totalAccountNumbers = totalAccountNumbers;
	}

	/**
	 * @return the totalTransactionNumbers
	 */
	public ID getTotalTransactionNumbers() {
		return totalTransactionNumbers;
	}

	/**
	 * @param totalTransactionNumbers the totalTransactionNumbers to set
	 */
	public void setTotalTransactionNumbers(ID totalTransactionNumbers) {
		this.totalTransactionNumbers = totalTransactionNumbers;
	}
	
	/**
	 * @return the subchainID
	 */
	public ID getSubchainID() {
		return subchainID;
	}

	/**
	 * @param subchainID the subchainID to set
	 */
	public void setSubchainID(ID subchainID) {
		this.subchainID = subchainID;
	}

	public void parseHeader(ByteArrayInputStream is) throws NoSuchFieldException, IOException {
		// TODO Auto-generated method stub
		
	}

	/**
	 * @return the founderID
	 */
	public ID getFounderID() {
		return founderID;
	}

	/**
	 * @param founderID the founderID to set
	 */
	public void setFounderID(ID founderID) {
		this.founderID = founderID;
	}

	public void parseBody(ByteArrayInputStream is) throws NoSuchFieldException, IOException {
		// parse FounderID
		founderID = new ID(EQCType.parseEQCBits(is));
		// Parse SubchainID
		subchainID = new ID(EQCType.parseEQCBits(is));
		// Parse subchainName
		subchainName = EQCType.bytesToASCIISting(EQCType.parseBIN(is));
		// Parse symbol
		symbol = EQCType.bytesToASCIISting(EQCType.parseBIN(is));
		// Parse decimals
		decimals = EQCType.bytesToASCIISting(EQCType.parseBIN(is));
		// Parse maxSupply
		maxSupply = EQCType.eqcBitsToID(EQCType.parseEQCBits(is));
		// Parse ifCanChangeMaxSupply
		ifCanChangeMaxSupply = EQCType.eqcBitsToBoolean(EQCType.parseEQCBits(is));
		// Parse totalSupply
		totalSupply = EQCType.eqcBitsToID(EQCType.parseEQCBits(is));
		// Parse ifCanChangeTotalSupply
		ifCanChangeTotalSupply = EQCType.eqcBitsToBoolean(EQCType.parseEQCBits(is));
		// Parse ifCanBurn
		ifCanBurn = EQCType.eqcBitsToBoolean(EQCType.parseEQCBits(is));
		// Parse totalAccountNumbers
		totalAccountNumbers = EQCType.eqcBitsToID(EQCType.parseEQCBits(is));
		// Parse totalTransactionNumbers
		totalTransactionNumbers = EQCType.eqcBitsToID(EQCType.parseEQCBits(is));
		// Parse url
		url = EQCType.bytesToASCIISting(EQCType.parseBIN(is));
		// Parse logo
		logo = EQCType.parseBIN(is);
	}

	@Override
	public byte[] getHeaderBytes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public byte[] getBodyBytes() {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			os.write(founderID.getEQCBits());
			os.write(subchainID.getEQCBits());
			os.write(EQCType.stringToBIN(subchainName));
			os.write(EQCType.stringToBIN(symbol));
			os.write(EQCType.stringToBIN(decimals));
			os.write(maxSupply.getEQCBits());
			os.write(EQCType.booleanToEQCBits(ifCanChangeMaxSupply));
			os.write(totalSupply.getEQCBits());
			os.write(EQCType.booleanToEQCBits(ifCanChangeTotalSupply));
			os.write(EQCType.booleanToEQCBits(ifCanBurn));
			os.write(totalAccountNumbers.getEQCBits());
			os.write(totalTransactionNumbers.getEQCBits());
			os.write(EQCType.stringToBIN(url));
			os.write(EQCType.bytesToBIN(logo));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return os.toByteArray();
	}

	@Override
	public boolean isSanity() {
		if(founderID == null || subchainID == null || subchainName == null || symbol == null || decimals == null || maxSupply == null ||
				totalSupply == null || totalAccountNumbers == null || totalTransactionNumbers == null) {
			return false;
		}
		if(!founderID.isSanity() || !subchainID.isSanity() || !(subchainName.length() > 0) || !(symbol.length() > 0) || !(decimals.length() > 0) ||
				!maxSupply.isSanity() || !totalSupply.isSanity() || !totalAccountNumbers.isSanity() || !totalTransactionNumbers.isSanity()) {
			return false;
		}
		return true;
	}

	@Override
	public boolean isValid(AccountsMerkleTree accountsMerkleTree) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "{\n" +
				toInnerJson() +
				"\n}";
	}
	
	public String toInnerJson() {
		return 
				"\"AssetSubchainHeader\":" + 
				"\n{\n" +
					"\"FounderID\":" + "\"" + founderID + "\"" + ",\n" +
					"\"SubchainID\":" + "\"" + subchainID + "\"" + ",\n" +
					"\"SubchainName\":" + "\"" + subchainName + "\"" + ",\n" +
					"\"Symbol\":" + "\"" + symbol + "\"" + ",\n" +
					"\"Decimals\":" + "\"" + decimals + "\"" + ",\n" +
					"\"MaxSupply\":" + "\"" + maxSupply + "\"" + ",\n" +
					"\"IfCanChangeMaxSupply\":" + "\"" + ifCanChangeMaxSupply + "\"" + ",\n" +
					"\"TotalSupply\":" + "\"" + totalSupply + "\"" + ",\n" +
					"\"IfCanChangeTotalSupply\":" + "\"" + ifCanChangeTotalSupply + "\"" + ",\n" +
					"\"IfCanBurn\":" + "\"" + ifCanBurn + "\"" + ",\n" +
					"\"TotalAccountNumbers\":" + "\"" + totalAccountNumbers + "\"" + ",\n" +
					"\"TotalTransactionNumbers\":" + "\"" + totalTransactionNumbers + "\"" + ",\n" +
					"\"URL\":" + "\"" + url + "\"" + ",\n" +
					"\"LOGO\":" + "\"" + Util.dumpBytes(logo, 16) + "\""  + "\n}\n";
	}

	/**
	 * @return the url
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * @param url the url to set
	 */
	public void setUrl(String url) {
		this.url = url;
	}

	/**
	 * @return the logo
	 */
	public byte[] getLogo() {
		return logo;
	}

	/**
	 * @param logo the logo to set
	 */
	public void setLogo(byte[] logo) {
		this.logo = logo;
	}
	
}
