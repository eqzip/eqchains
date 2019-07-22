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
	protected AtomicReference<State> state;
	protected final String name = this.getClass().getSimpleName() + " ";

	public synchronized void start() {
		boolean isNeedStart = false;
		if (worker == null) {
			isNeedStart = true;
			pendingMessage = new PriorityBlockingQueue<>(Util.HUNDREDPULS);
			isRunning = new AtomicBoolean(false);
			isPausing = new AtomicBoolean(false);
			isSleeping = new AtomicBoolean(false);
			isWaiting = new AtomicBoolean(false);
			state = new AtomicReference<>();
		} else {
			if (!worker.isAlive()) {
				isNeedStart = true;
			}
		}
		if (isNeedStart) {
			worker = new Thread(this);
			worker.setPriority(Thread.NORM_PRIORITY);
			isRunning.set(true);
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
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		Log.info(name + " begining stop");
	}
	
	public void pause() {
		Log.info(name + " begining pause");
		isPausing.set(true);
	}

	public void onPause() {
		if (isPausing.get()) {
			synchronized (isPausing) {
				try {
					Log.info(name + " is pausing now");
					isPausing.wait();
					isPausing.set(false);
					Log.info(name + " end of pause");
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
			Log.info(name + " resume pause");
			isPausing.notify();
		}
	}

	public void waiting() {
		isWaiting.set(true);
		EQCServiceState state = new EQCServiceState();
		state.setState(State.WAIT);
		state.setTime(System.currentTimeMillis());
		offerState(state);
	}

	public void onWaiting() {
		if (isWaiting.get()) {
			synchronized (isWaiting) {
				try {
					isWaiting.wait();
					isWaiting.set(false);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					Log.Error(e.getMessage());
				}
			}
		}
	}

	public void resumeWaiting() {
		synchronized (isWaiting) {
			isWaiting.notify();
		}
	}

	public synchronized void sleeping() {
		isSleeping.set(true);
		EQCServiceState state = new EQCServiceState();
		state.setState(State.SLEEP);
		state.setTime(System.currentTimeMillis());
		offerState(state);
	}

	public void onSleeping(long time) {
		if (isSleeping.get()) {
			synchronized (isSleeping) {
				try {
					if (time == 0) {
						isSleeping.wait();
					} else {
						isSleeping.wait(time);
					}
					isSleeping.set(false);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					Log.Error(e.getMessage());
				}
			}
		}
	}

	public void resumeSleeping() {
		synchronized (isSleeping) {
			isSleeping.notify();
		}
	}

	@Override
	public void run() {
		Log.info(name + "is running now...");
		EQCServiceState state = null;
		while (isRunning.get()) {
			try {
//				Log.info(name + "Waiting for new message...");
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
				this.state.set(State.SLEEP);
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
		Log.info(name + " stopped");
	}

	private void onSleep(EQCServiceState state) {
		SleepState sleepState = (SleepState) state;
		Log.info(name + "onSleep time: " + sleepState.getSleepTime());
		isSleeping.set(true);
		onSleeping(sleepState.getSleepTime());
		if(isRunning.get()) {
			onSleep(sleepState);
		}
	}
	
	protected void onSleep(SleepState state) {
		
	}

	protected void onWaiting(EQCServiceState state) {
		Log.info(name + "onWaiting");
		onWaiting();
	}

	protected void onStop(EQCServiceState state) {

	}

	protected void onDefault(EQCServiceState state) {

	}

	public void offerState(EQCServiceState state) {
		if (isRunning.get()) {
			pendingMessage.offer(state);
		}
	}

	/**
	 * @return the state
	 */
	public State getState() {
		return state.get();
	}

}
