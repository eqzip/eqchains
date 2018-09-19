/**
 * EQCoin core - EQZIP's EQCoin core library
 * @copyright 2018 EQZIP Inc.  All rights reserved...
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
package com.eqzip.eqcoin.util;


/**
 * @author Xun Wang
 * @date 9-17-2018
 * @email 10509759@qq.com
 */
/*
 * Copyright (C) 2010  Preston Lacey http://javaflacencoder.sourceforge.net/
 * All Rights Reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

/**
 * Class to calculate a CRC8 checksum.
 * 
 * @author Preston Lacey
 */
public class CRC8 {

	private static byte[] crcTable = new byte[256];

	static {
		for (int i = 0; i < crcTable.length; ++i) {
			crcTable[i] = ReverseInternal((byte)i);
		}
	}

	public static byte Calculate(byte[] data, byte polynom, byte initial, byte finalXor, boolean refIn, boolean refOut)
    {

        int crc = initial;
        
        for(int i=0; i<data.length; ++i) {
        	byte temp = (byte) (crc ^ (refIn ? crcTable[data[i]] : data[i]));
        	for(int j=0; j<8; ++j) {
        		if ((temp & 0x80) != 0) // старший бит = 1

                    temp = (byte) ((temp << 1) ^ polynom);

                else // старший бит = 0

                    temp = (byte) (temp << 1);
        	}
        	 crc = (byte)temp;
        }
        
//        foreach (byte b in data)
//
//        {
//
//            var temp = crc ^ (refIn ? Reversed[b] : b);
//
//            for (var j = 0; j < 8; j++)
//
//            {
//
//                if ((temp & 0x80) != 0) // старший бит = 1
//
//                    temp = (temp << 1) ^ polynom;
//
//                else // старший бит = 0
//
//                    temp = temp << 1;
//
//            }
//
//            crc = (byte)temp;
//
//        }

        if (refOut)

            crc = crcTable[crc];

        return (byte) (crc ^ finalXor);

    }


	
	private static byte ReverseInternal(byte b){
		int result = 0;
		for (int i = 0; i < 8; ++i) {
			result += ((b >> 8 - i - 1) & 1) << i;
		}
		return (byte) result;
	}

}