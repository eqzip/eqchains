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
//package com.eqchains.persistence.avro;
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
//package com.eqzip.eqcoin.persistence.avro;
////
////import java.io.File;
////import java.io.IOException;
////import java.util.Vector;
////
////import org.apache.avro.file.DataFileReader;
////import org.apache.avro.file.DataFileWriter;
////import org.apache.avro.io.DatumReader;
////import org.apache.avro.io.DatumWriter;
////import org.apache.avro.specific.SpecificDatumReader;
////import org.apache.avro.specific.SpecificDatumWriter;
////
////import com.eqzip.eqcoin.blockchain.Address;
////import com.eqzip.eqcoin.blockchain.EQCBlock;
////import com.eqzip.eqcoin.blockchain.EQCBlockChain;
////import com.eqzip.eqcoin.blockchain.EQCHeader;
////import com.eqzip.eqcoin.blockchain.PublicKey;
////import com.eqzip.eqcoin.blockchain.Transaction;
////import com.eqzip.eqcoin.util.Log;
////import com.eqzip.eqcoin.util.SerialNumber;
////import com.eqzip.eqcoin.util.Util;
////
/////**
//// * @author Xun Wang
//// * @date Oct 6, 2018
//// * @email 10509759@qq.com
//// */
////public class EQCBlockChainAvro implements EQCBlockChain {
////	
////	private static EQCBlockChainAvro instance;
////	
////	private EQCBlockChainAvro() {
////	}
////	
////	public static EQCBlockChainAvro getInstance() {
////		if(instance == null) {
////			synchronized (EQCBlockChainAvro.class) {
////				if(instance == null) {
////					instance = new EQCBlockChainAvro();
////				}
////			}
////		}
////		return instance;
////	}
////
////	@Override
////	public synchronized SerialNumber getAddressSerialNumber(Address address) {
////		// TODO Auto-generated method stub
////		return null;
////	}
////
////	@Override
////	public Address getAddress(SerialNumber serialNumber) {
////		// TODO Auto-generated method stub
////		return null;
////	}
////
////	@Override
////	public boolean isAddressExists(Address address) {
////		// TODO Auto-generated method stub
////		return false;
////	}
////
////	@Override
////	public SerialNumber getLastAddressSerialNumber() {
////		// TODO Auto-generated method stub
////		return null;
////	}
////
////	@Override
////	public long getAddressTotalNumbers() {
////		// TODO Auto-generated method stub
////		return 0;
////	}
////
////	@Override
////	public SerialNumber getPublicKeySerialNumber(Address address) {
////		// TODO Auto-generated method stub
////		return null;
////	}
////
////	@Override
////	public PublicKey getPublicKey(SerialNumber SerialNumber) {
////		// TODO Auto-generated method stub
////		return null;
////	}
////
////	@Override
////	public boolean isPublicKeyExists(PublicKey publicKey) {
////		// TODO Auto-generated method stub
////		return false;
////	}
////
////	@Override
////	public SerialNumber getLastPublicKeySerialNumber() {
////		// TODO Auto-generated method stub
////		return null;
////	}
////
////	@Override
////	public long getPublicKeyTotalNumbers() {
////		// TODO Auto-generated method stub
////		return 0;
////	}
////
////	@Override
////	public EQCBlock getEQCBlock(SerialNumber height, boolean isSegwit) {
////		DatumReader<EQCBlockAvro> eqcBlockDatumReader = new SpecificDatumReader<EQCBlockAvro>(EQCBlockAvro.class); 
////		DataFileReader<EQCBlockAvro> dataFileReader = null; 
////		
////		File file = new File(Util.AVRO_PATH + "\\" + height.longValue() + ".avro");
////		try {
////			dataFileReader = new DataFileReader<EQCBlockAvro>(file, eqcBlockDatumReader);
////		} catch (IOException e) {
////			Log.Error(e.getMessage());
////		}
////		EQCBlockAvro eqcBlockAvro = null;
////		if(dataFileReader.hasNext()) {
////			eqcBlockAvro = dataFileReader.next();
////		}
////
////		return new EQCBlock(eqcBlockAvro, isSegwit);
////	}
////
////	@Override
////	public boolean isEQCBlockExists(SerialNumber height) {
////		// TODO Auto-generated method stub
////		return false;
////	}
////
////	@Override
////	public boolean isEQCBlockExists(EQCBlock eqcBlock) {
////		// TODO Auto-generated method stub
////		return false;
////	}
////
////	@Override
////	public boolean saveEQCBlock(EQCBlock eqcBlock) {
////		boolean isSaveSuccessful = false;
////		EQCBlockAvro eqcBlockAvro = new EQCBlockAvro();
////		eqcBlockAvro.setEQCHeader(eqcBlock.getEqcHeader().getByteBuffer());
////		eqcBlockAvro.setTransactions(eqcBlock.getTransactions().getByteBuffer());
////		eqcBlockAvro.setSignatures(eqcBlock.getSignatures().getByteBuffer());
////		
////		File file = new File(Util.AVRO_PATH + "\\" + eqcBlock.getTransactions().getTransactionsHeader().getHeight().longValue() + ".avro");
////		DatumWriter<EQCBlockAvro> eqcBlockDatumWriter = new SpecificDatumWriter<EQCBlockAvro>(EQCBlockAvro.class);
////		DataFileWriter<EQCBlockAvro> dataFileWriter = new DataFileWriter<EQCBlockAvro>(eqcBlockDatumWriter);
////		try {
////			dataFileWriter.create(eqcBlockAvro.getSchema(), file);
////			dataFileWriter.append(eqcBlockAvro);
////			dataFileWriter.close();
////			isSaveSuccessful = true;
////		} catch (IOException e) {
////			Log.Error(e.getMessage());
////		}
////
////		return isSaveSuccessful;
////	}
////
////	@Override
////	public boolean deleteEQCBlock(SerialNumber height) {
////		// TODO Auto-generated method stub
////		return false;
////	}
////
////	@Override
////	public EQCHeader getEQCHeader(SerialNumber height) {
////		// TODO Auto-generated method stub
////		return null;
////	}
////
////	@Override
////	public byte[] getTransactionsHash(SerialNumber height) {
////		// TODO Auto-generated method stub
////		return null;
////	}
////
////	@Override
////	public long getBalance(Address address) {
////		// TODO Auto-generated method stub
////		return 0;
////	}
////
////	@Override
////	public Vector<Transaction> getTransactionList(Address address) {
////		// TODO Auto-generated method stub
////		return null;
////	}
////
////	@Override
////	public long getBalance(Address address, SerialNumber height) {
////		// TODO Auto-generated method stub
////		return 0;
////	}
////
////	@Override
////	public Vector<Transaction> getTransactionList(Address address, SerialNumber height) {
////		// TODO Auto-generated method stub
////		return null;
////	}
////
////}
