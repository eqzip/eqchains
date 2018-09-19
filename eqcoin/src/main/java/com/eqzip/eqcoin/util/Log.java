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

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.Date;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * @author Xun Wang
 * @date 9-11-2018
 * @email 10509759@qq.com
 */
public final class Log {
	
	private final static Logger log;
	private static FileHandler fileHandler;
	private static ConsoleHandler consoleHandler;
	
	static {
		log = Logger.getLogger(MethodHandles.lookup().lookupClass().getSimpleName());
		log.setLevel(Level.ALL);
		log.setUseParentHandlers(false);
		try {
			fileHandler = new FileHandler(Util.PATH + "/log.txt", true);
			fileHandler.setFormatter(new EQCFormatter());
			log.addHandler(fileHandler);
			consoleHandler = new ConsoleHandler();
			consoleHandler.setFormatter(new EQCFormatter());
			log.addHandler(consoleHandler);
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally {
			fileHandler.close();
			consoleHandler.close();
		}
	}
	
	private	Log() {}
	
	public static void info(String info) {
		log.info(info);
		// flush buffer immediately otherwise the log data in the buffer maybe missing
		fileHandler.flush();
	}
	
	public static class EQCFormatter extends Formatter {
	    @Override
	    public String format(LogRecord record) {
	    	return new Date(record.getMillis()) + " " + Thread.currentThread().getStackTrace()[9].getClassName() + "." +
	    			Thread.currentThread().getStackTrace()[9].getMethodName() + " line:" + Thread.currentThread().getStackTrace()[9].getLineNumber() + "\r\n" +
	    			record.getMessage()+"\r\n";
	    }
	}
	
	
}
