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
package com.eqchains.rpc;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

import com.eqchains.avro.IO;
import com.eqchains.serialization.EQCInheritable;
import com.eqchains.serialization.EQCTypable;
import com.eqchains.serialization.EQCType;

/**
 * @author Xun Wang
 * @date Jun 25, 2019
 * @email 10509759@qq.com
 */
public abstract class AvroIO implements EQCTypable, EQCInheritable {
	
	public AvroIO() {}
	
	public AvroIO(ByteArrayInputStream is) throws Exception {
		parseBody(is);
	}
	
	public AvroIO(IO io) throws Exception {
		byte[] bytes = io.getObject().array();
		EQCType.assertNotNull(bytes);
		ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		parseBody(is);
		EQCType.assertNoRedundantData(is);
	}
	
	public IO getIO() throws Exception {
		return new IO(ByteBuffer.wrap(getBytes()));
	}

	public byte[] getBytes() throws Exception {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		os.write(getBodyBytes());
		return os.toByteArray();
	}
	
	/* (non-Javadoc)
	 * @see com.eqchains.serialization.EQCTypable#getBin()
	 */
	@Override
	public byte[] getBin() throws Exception {
		return EQCType.bytesToBIN(getBytes());
	}
	
}
