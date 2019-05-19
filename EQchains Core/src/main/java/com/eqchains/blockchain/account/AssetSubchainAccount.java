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
import java.security.acl.Owner;

import com.eqchains.blockchain.AccountsMerkleTree;
import com.eqchains.blockchain.account.Account.AccountType;
import com.eqchains.blockchain.account.Account.Asset;
import com.eqchains.blockchain.account.Account.Key;
import com.eqchains.blockchain.account.SmartContractAccount.LanguageType;
import com.eqchains.blockchain.account.SmartContractAccount.SmartContractType;
import com.eqchains.blockchain.transaction.Address.AddressShape;
import com.eqchains.serialization.EQCTypable;
import com.eqchains.serialization.EQCType;
import com.eqchains.serialization.EQCType.ARRAY;
import com.eqchains.util.ID;
import com.eqchains.util.Log;
import com.eqchains.util.Util;

/**
 * @author Xun Wang
 * @date May 13, 2019
 * @email 10509759@qq.com
 */
public class AssetSubchainAccount extends SubchainAccount {
	/**
	 * Body field include version and assetSubchainHeader
	 */
	private ID version;
	private AssetSubchainHeader assetSubchainHeader;
	/*
	 * VERIFICATION_COUNT equal to the number of member variables of the class to be verified.
	 */
	protected final static byte HEADER_VERIFICATION_COUNT = 0; // Super Header + Sub Header verify count
	protected final static byte BODY_VERIFICATION_COUNT = 3; // Super Body + Sub Body verify count
	public final static byte MAX_VERSION = 0;
	
	public AssetSubchainAccount() {
		super(SubchainType.ASSET);
	}
	
	public AssetSubchainAccount(byte[] bytes) throws NoSuchFieldException, IOException {
		super(SubchainType.ASSET);
		if(!isValid(bytes)) {
			throw Util.DATA_FORMAT_EXCEPTION;
		}
		ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		parseHeader(is);
		parseBody(is);
	}
	
	/* (non-Javadoc)
	 * @see com.eqchains.blockchain.account.SmartContractAccount#isValid(byte[])
	 */
	public static boolean isValid(byte[] bytes) throws NoSuchFieldException, IOException {
		ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		return isHeaderValid(is) && isBodyValid(is) && EQCType.isInputStreamEnd(is);
	}

	/* (non-Javadoc)
	 * @see com.eqchains.blockchain.account.SmartContractAccount#parseHeader(java.io.ByteArrayInputStream)
	 */
	public void parseHeader(ByteArrayInputStream is) throws NoSuchFieldException, IOException {
		// TODO Auto-generated method stub
		super.parseHeader(is);
	}

	/* (non-Javadoc)
	 * @see com.eqchains.blockchain.account.SmartContractAccount#parseBody(java.io.ByteArrayInputStream)
	 */
	@Override
	public void parseBody(ByteArrayInputStream is) throws NoSuchFieldException, IOException {
		// TODO Auto-generated method stub
		super.parseBody(is);
		// Parse Version
		byte[] data = null;
		if ((data = EQCType.parseEQCBits(is)) != null) {
			version = EQCType.eqcBitsToID(data);
		}
		assetSubchainHeader = new AssetSubchainHeader();
		assetSubchainHeader.parse(is.readAllBytes());
	}

	/* (non-Javadoc)
	 * @see com.eqchains.blockchain.account.SmartContractAccount#isHeaderValid(java.io.ByteArrayInputStream)
	 */
	public static boolean isHeaderValid(ByteArrayInputStream is) throws NoSuchFieldException, IOException {
		// TODO Auto-generated method stub
		return SubchainAccount.isHeaderValid(is);
	}

	/* (non-Javadoc)
	 * @see com.eqchains.blockchain.account.SmartContractAccount#isBodyValid(java.io.ByteArrayInputStream)
	 */
	public static boolean isBodyValid(ByteArrayInputStream is) throws NoSuchFieldException, IOException {
		byte[] data = null;
		byte validCount = 0;
		
		// Parse Super Body
		if(SubchainAccount.isBodyValid(is)) {
			++validCount;
		}
		
		// Parse Sub Body
		// Parse version
		if ((data = EQCType.parseEQCBits(is)) != null) {
			++validCount;
		}
		// Parse AssetSubchainHeader
		if(AssetSubchainHeader.isValid(is)) {
			++validCount;
		}
		
		
		return (validCount == BODY_VERIFICATION_COUNT);
	}
	
	public static class AssetSubchainHeader implements EQCTypable {
		private ID version;
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
		
		/*
		 * VERIFICATION_COUNT equal to the number of member variables of the class to be verified.
		 */
		protected final static byte VERIFICATION_COUNT = 11;
		
		public AssetSubchainHeader() {}
		
		public void parse(byte[] bytes) throws NoSuchFieldException, IOException {
			parseAssetSubchainHeader(bytes);
		}
		
		public AssetSubchainHeader(byte[] bytes) throws NoSuchFieldException, IOException {
			parseAssetSubchainHeader(bytes);
		}
		
		private void parseAssetSubchainHeader(byte[] bytes) throws NoSuchFieldException, IOException {
			if(!isValid(bytes)) {
				throw Util.DATA_FORMAT_EXCEPTION;
			}
			ByteArrayInputStream is = new ByteArrayInputStream(bytes);
			parseBody(is);
		}
		
		public boolean isValid(byte[] bytes) throws NoSuchFieldException, IOException {
			ByteArrayInputStream is = new ByteArrayInputStream(bytes);
			return isBodyValid(is) && EQCType.isInputStreamEnd(is);
		}

		@Override
		public byte[] getBytes(AddressShape addressShape) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public byte[] getBytes() {
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			try {
				os.write(version.getEQCBits());
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
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.Error(e.getMessage());
			}
			return os.toByteArray();
		}

		@Override
		public byte[] getBin(AddressShape addressShape) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public byte[] getBin() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public boolean isSanity(AddressShape... addressShape) {
			// TODO Auto-generated method stub
			return false;
		}
		
		/**
		 * @return the version
		 */
		public ID getVersion() {
			return version;
		}

		/**
		 * @param version the version to set
		 */
		public void setVersion(ID version) {
			this.version = version;
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

		public static boolean isHeaderValid(ByteArrayInputStream is) throws NoSuchFieldException, IOException {
			// TODO Auto-generated method stub
			return false;
		}

		public static boolean isValid(ByteArrayInputStream is) throws NoSuchFieldException, IOException {
			return isBodyValid(is) && EQCType.isInputStreamEnd(is);
		}
		
		public static boolean isBodyValid(ByteArrayInputStream is) throws NoSuchFieldException, IOException {
			byte[] data = null;
			byte validCount = 0;

			// Parse Version
			if (((data = EQCType.parseEQCBits(is)) != null) && !EQCType.isNULL(data)) {
				++validCount;
			}

			// Parse subchainName
			data = null;
			if (((data = EQCType.parseBIN(is)) != null) && !EQCType.isNULL(data)) {
				++validCount;
			}

			// Parse symbol
			data = null;
			if (((data = EQCType.parseBIN(is)) != null) && !EQCType.isNULL(data)) {
				++validCount;
			}

			// Parse decimals
			data = null;
			if (((data = EQCType.parseBIN(is)) != null) && !EQCType.isNULL(data)) {
				++validCount;
			}

			// Parse maxSupply
			data = null;
			if (((data = EQCType.parseEQCBits(is)) != null) && !EQCType.isNULL(data)) {
				++validCount;
			}

			// Parse totalSupply
			data = null;
			if (((data = EQCType.parseEQCBits(is)) != null) && !EQCType.isNULL(data)) {
				++validCount;
			}

			// Parse ifCanChangeTotalSupply
			data = null;
			if (((data = EQCType.parseEQCBits(is)) != null)) {
				++validCount;
			}

			// Parse ifCanBurn
			data = null;
			if (((data = EQCType.parseEQCBits(is)) != null)) {
				++validCount;
			}

			// Parse totalAccountNumbers
			data = null;
			if (((data = EQCType.parseEQCBits(is)) != null) && !EQCType.isNULL(data)) {
				++validCount;
			}

			// Parse totalTransactionNumbers
			data = null;
			if (((data = EQCType.parseEQCBits(is)) != null) && !EQCType.isNULL(data)) {
				++validCount;
			}

			// Parse leasePeriod
			data = null;
			if (((data = EQCType.parseEQCBits(is)) != null)) {
				++validCount;
			}
			
			return (validCount == BODY_VERIFICATION_COUNT);
		}

		public void parseHeader(ByteArrayInputStream is) throws NoSuchFieldException, IOException {
			// TODO Auto-generated method stub
			
		}

		public void parseBody(ByteArrayInputStream is) throws NoSuchFieldException, IOException {
			// Parse Version
			byte[] data = null;
			if (((data = EQCType.parseEQCBits(is)) != null)) {
				version = new ID(data);
			}

			// Parse SubchainID
			data = null;
			if (((data = EQCType.parseEQCBits(is)) != null) && !EQCType.isNULL(data)) {
				subchainID = new ID(data);
			}
			
			// Parse subchainName
			data = null;
			if (((data = EQCType.parseBIN(is)) != null) && !EQCType.isNULL(data)) {
				subchainName = EQCType.bytesToASCIISting(data);
			}

			// Parse symbol
			data = null;
			if (((data = EQCType.parseBIN(is)) != null) && !EQCType.isNULL(data)) {
				symbol = EQCType.bytesToASCIISting(data);
			}

			// Parse decimals
			data = null;
			if (((data = EQCType.parseBIN(is)) != null) && !EQCType.isNULL(data)) {
				decimals = EQCType.bytesToASCIISting(data);
			}

			// Parse maxSupply
			data = null;
			if (((data = EQCType.parseEQCBits(is)) != null) && !EQCType.isNULL(data)) {
				maxSupply = EQCType.eqcBitsToID(data);
			}

			// Parse totalSupply
			data = null;
			if (((data = EQCType.parseEQCBits(is)) != null) && !EQCType.isNULL(data)) {
				maxSupply = EQCType.eqcBitsToID(data);
			}

			// Parse ifCanChangeTotalSupply
			data = null;
			if (((data = EQCType.parseEQCBits(is)) != null)) {
				ifCanChangeTotalSupply = EQCType.eqcBitsToBoolean(data);
			}

			// Parse ifCanBurn
			data = null;
			if (((data = EQCType.parseEQCBits(is)) != null)) {
				ifCanBurn = EQCType.eqcBitsToBoolean(data);
			}

			// Parse totalAccountNumbers
			data = null;
			if (((data = EQCType.parseEQCBits(is)) != null) && !EQCType.isNULL(data)) {
				totalAccountNumbers = EQCType.eqcBitsToID(data);
			}

			// Parse totalTransactionNumbers
			data = null;
			if (((data = EQCType.parseEQCBits(is)) != null) && !EQCType.isNULL(data)) {
				totalTransactionNumbers = EQCType.eqcBitsToID(data);
			}

		}

	}
	
}
