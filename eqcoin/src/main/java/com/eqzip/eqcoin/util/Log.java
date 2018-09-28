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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import com.eqzip.eqcoin.util.Util.Os;

/**
 * @author Xun Wang
 * @date 9-11-2018
 * @email 10509759@qq.com
 */
public final class Log {

	private static Logger log;
	private static FileHandler fileHandler;
	private static ConsoleHandler consoleHandler;
	private static boolean DEBUG = true;

	private Log() {}

	private static void instance() {
		if (log == null) {
			synchronized (Log.class) {
				if (log == null) {
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
				}
			}
		}
	}

	public static void info(String info) {
		if (DEBUG) {
			instance();
			log.info(info);
			// flush buffer immediately otherwise the log data in the buffer maybe missing
			fileHandler.flush();
		}
	}
	
	public static void Error(String error) {
		info("[ERROR] " + error);
	}
	
	public static void Warn(String warn) {
		info("[WARN] " + warn);
	}
	
	public static String getStringDate(long timestamp) {
		   Date currentTime = new Date(timestamp);
		   SimpleDateFormat formatter = new SimpleDateFormat("[yyyy-MM-dd HH:mm:ss:sss]");
		   String dateString = formatter.format(currentTime);
		   return dateString;
		}
	
	public static class EQCFormatter extends Formatter {
		@Override
		public String format(LogRecord record) {
			return getStringDate(record.getMillis()) + "[" + Thread.currentThread().getStackTrace()[9].getClassName() + "."
					+ Thread.currentThread().getStackTrace()[9].getMethodName() + " "
					+ Thread.currentThread().getStackTrace()[9].getLineNumber() + "] " + record.getMessage() + "\r\n";
		}
	}

}
