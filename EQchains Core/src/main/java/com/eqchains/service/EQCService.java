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
package com.eqchains.service;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import com.eqchains.service.state.EQCServiceState;
import com.eqchains.service.state.SleepState;
import com.eqchains.service.state.EQCServiceState.State;
import com.eqchains.util.Log;
import com.eqchains.util.Util;

/**
 * @author Xun Wang
 * @date Jun 29, 2019
 * @email 10509759@qq.com
 */
public abstract class EQCService implements Runnable {
	protected PriorityBlockingQueue<EQCServiceState> pendingMessage;
	protected Thread worker;
	protected AtomicBoolean isRunning;
	protected AtomicBoolean isPausing;
	protected AtomicBoolean isSleeping;
	protected AtomicBoolean isWaiting;
	protected AtomicBoolean isStopped = new AtomicBoolean(true);
	protected AtomicReference<State> state;
	protected final String name = this.getClass().getSimpleName() + " ";
	
	public synchronized void start() {
		synchronized (isStopped) {
			if(!isStopped.get()) {
				try {
					Log.info(name + "waiting for previous thread stop");
					isStopped.wait();
					Log.info(name + "previous thread stopped");
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					Log.info(name + e.getMessage());
				}
			}
		}
		boolean isNeedStart = false;
		if (worker == null) {
			isNeedStart = true;
			pendingMessage = new PriorityBlockingQueue<>(Util.HUNDREDPULS);
			isRunning = new AtomicBoolean(false);
			isPausing = new AtomicBoolean(false);
			isSleeping = new AtomicBoolean(false);
			isWaiting = new AtomicBoolean(false);
			isStopped = new AtomicBoolean(true);
			state = new AtomicReference<>();
		} else {
			if (!worker.isAlive()) {
				isNeedStart = true;
			}
		}
		if (isNeedStart) {
			worker = new Thread(this);
			worker.setPriority(Thread.NORM_PRIORITY);
			worker.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
				public void uncaughtException(Thread t, Throwable e) {
					Log.Error(name + "Uncaught Exception occur: " + e.getMessage());
					isStopped.set(true);
					Log.info(name + "beginning stop " + name);
					stop();
					Log.info(name + "beginning start " + name);
					start();
				}
			});
			isRunning.set(true);
			isStopped.set(false);
			state.set(State.RUNNING);
			worker.start();
		}
	}

	public synchronized boolean isRunning() {
		if(worker == null) {
			return false;
		}
		return worker.isAlive();
	}

	public synchronized void stop() {
		Log.info(name + "begining stop");
		isRunning.set(false);
		worker = null;
		pendingMessage.clear();
		if(isSleeping.get()) {
			resumeSleeping();
		}
		if (isPausing.get()) {
			resumePause();
		}
		offerState(new EQCServiceState(State.STOP));
	}

	public void pause() {
		synchronized (isPausing) {
			Log.info(name + "begining pause");
			isPausing.set(true);
		}
	}

	public void onPause(String ...phase) {
		synchronized (isPausing) {
			if (isPausing.get()) {
				try {
					if(phase != null) {
						Log.info(name + " paused at " + phase[0]);
					}
					Log.info(name + "is pausing now");
					isPausing.wait();
					isPausing.set(false);
					Log.info(name + "end of pause");
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					Log.Error(e.getMessage());
				}
			}
		}
	}

	public void resumePause() {
		synchronized (isPausing) {
			Log.info(name + "resume pause");
			isPausing.notify();
		}
	}

	public void waiting() {
		synchronized (isWaiting) {
			Log.info(name + "begin waiting");
			isWaiting.set(true);
			offerState(new EQCServiceState(State.WAIT));
		}
	}

	public void resumeWaiting() {
		synchronized (isWaiting) {
			Log.info(name + "resumeWaiting");
			isWaiting.notify();
		}
	}

	public void sleeping(long sleepTime) {
		synchronized (isSleeping) {
			isSleeping.set(true);
			offerState(new SleepState(sleepTime));
		}
	}

	public void resumeSleeping() {
		synchronized (isSleeping) {
			Log.info(name + "resumeSleeping");
			isSleeping.notify();
		}
	}

	@Override
	public void run() {
		Log.info(name + "worker thread is running now...");
		EQCServiceState state = null;
		while (isRunning.get()) {
			try {
				Log.info(name + "Waiting for new message...");
				state = pendingMessage.take();
				Log.info(name + "take new message: " + state);
				this.state.set(State.TAKE);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.Error(name + e.getMessage());
				this.state.set(State.ERROR);
				continue;
			}
			switch (state.getState()) {
			case SLEEP:
//				Log.info(name + "Begin onSleep");
				onSleep(state);
//				Log.info(name + "End onSleep");
				break;
			case WAIT:
				this.state.set(State.WAIT);
//				Log.info(name + "Begin onWaiting");
				onWaiting(state);
//				Log.info(name + "End onWaiting");
				break;
			case STOP:
//				Log.info(name + "Begin onStop");
				onStop(state);
//				Log.info(name + "End onStop");
				break;
			default:
//				Log.info(name + "Begin onDefault");
				onDefault(state);
//				Log.info(name + "End onDefault");
				break;
			}
		}
		Log.info(name + "worker thread stopped...");
		synchronized (isStopped) {
			isStopped.set(true);
			isStopped.notify();
		}
	}

	private void onSleep(EQCServiceState state) {
		synchronized (isSleeping) {
			this.state.set(State.SLEEP);
			SleepState sleepState = (SleepState) state;
			Log.info(name + "onSleep time: " + sleepState.getSleepTime());
			if (isSleeping.get()) {
				try {
					if (sleepState.getSleepTime() == 0) {
						isSleeping.wait();
					} else {
						isSleeping.wait(sleepState.getSleepTime());
					}
					isSleeping.set(false);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					Log.Error(e.getMessage());
				}
			}

			if (isRunning.get()) {
				onSleep(sleepState);
			}
		}
	}

	protected void onSleep(SleepState state) {
		
	}

	protected void onWaiting(EQCServiceState state) {
		synchronized (isWaiting) {
			if (isWaiting.get()) {
				Log.info(name + "onWaiting");
				try {
					isWaiting.wait();
					isWaiting.set(false);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					Log.Error(name + e.getMessage());
				}
			}
		}
	}

	protected void onStop(EQCServiceState state) {
		Log.info(name + "Received stop message need stop now");
	}

	protected void onDefault(EQCServiceState state) {

	}

	public void offerState(EQCServiceState state) {
//		if (isRunning.get()) {
			pendingMessage.offer(state);
//		}
	}

	/**
	 * @return the state
	 */
	public State getState() {
		return state.get();
	}

}
