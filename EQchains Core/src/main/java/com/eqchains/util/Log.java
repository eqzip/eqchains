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
package com.eqchains.util;

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
//					System.out.close();
					log = Logger.getLogger(MethodHandles.lookup().lookupClass().getSimpleName());
					log.setLevel(Level.ALL);
					log.setUseParentHandlers(false);
					try {
						fileHandler = new FileHandler(Util.LOG_PATH, true);
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
//			System.out.println(info);
			log.info(info);
			// flush buffer immediately otherwise the log data in the buffer maybe missing
			if(fileHandler != null) {
				fileHandler.flush();
			}
		}
	}
	
	public static void Error(String error) {
		if (DEBUG) {
			instance();
//			System.out.println(error);
			log.info("[ERROR]" + error);
			// flush buffer immediately otherwise the log data in the buffer maybe missing
			if(fileHandler != null) {
				fileHandler.flush();
			}
		}
	}
	
	public static void Warn(String warn) {
		if (DEBUG) {
			instance();
//			System.out.println(warn);
			log.info("[WARN]" + warn);
			// flush buffer immediately otherwise the log data in the buffer maybe missing
			if(fileHandler != null) {
				fileHandler.flush();
			}
		}
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
					+ Thread.currentThread().getStackTrace()[9].getLineNumber() + "]" + record.getMessage() + "\r\n";
		}
	}

}
