package com.google.code.lingyang.Impl;

import java.net.InetSocketAddress;
import java.net.StandardSocketOption;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.channels.spi.AsynchronousChannelProvider;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.google.code.lingyang.Profile.ListenModel;

public class ThreadStrategy {
	public final static int currentThread = 0;
	public final static int poolThread = 1;
	private int strategy = poolThread;
	private ThreadPoolExecutor threadPoolExecutor;
	private boolean shutdown = false;

	private ThreadStrategy(int strategy) {
		this.strategy = strategy;
		init();
	}

	private void init() {
		switch (strategy) {
		case currentThread:
			break;
		case poolThread:
			initThreadPool();
			break;
		default:
			initThreadPool();
			break;
		}
	}

	private void initThreadPool() {
		threadPoolExecutor = new ThreadPoolExecutor(100, 1024, 20,
				TimeUnit.MINUTES, new LinkedBlockingQueue<Runnable>());
	}

	public static ThreadStrategy getInstance(int strategy) {
		return new ThreadStrategy(strategy);
	}

	public void fireEvent(Event evt) {
		switch (strategy) {
		case currentThread:
			_fireEventOnCurrent(evt);
			break;
		case poolThread:
			_fireEventOnPool(evt);
			break;
		default:
			_fireEventOnPool(evt);
			break;
		}
	}

	public void fireListenEvent(Event evt) {
		switch (strategy) {
		case currentThread:
			_listenOnSingleThread(evt);
			break;
		case poolThread:
			_listenOnPool(evt);
			break;
		default:
			_listenOnPool(evt);
			break;
		}
	}

	public void _listenOnSingleThread(final Event evt) {
		final AsynchronousServerSocketChannel asynchronousServerSocketChannel = evt
				.getAsynServerSocketSocketChannel();
		Thread thread = new Thread(new ListenWorker(
				asynchronousServerSocketChannel, evt.getTcpServerCore(),
				ListenModel.Furtue));
		thread.setDaemon(true);
		thread.start();
	}

	private void _listenOnPool(final Event evt) {
		final AsynchronousServerSocketChannel asynchronousServerSocketChannel = evt
				.getAsynServerSocketSocketChannel();
		threadPoolExecutor.submit(new ListenWorker(
				asynchronousServerSocketChannel, evt.getTcpServerCore(),
				(ListenModel) evt.getAttachment()));

	}

	class ListenWorker implements Runnable {
		private AsynchronousServerSocketChannel asynchronousServerSocketChannel = null;
		private TcpServerCore core = null;
		private boolean furtue = true;

		public ListenWorker(
				AsynchronousServerSocketChannel asynchronousServerSocketChannel,
				TcpServerCore core, ListenModel listenModel) {
			this.asynchronousServerSocketChannel = asynchronousServerSocketChannel;
			this.core = core;
			switch (listenModel) {
			case Furtue:
				this.furtue = true;
				break;
			case CALLBACK:
				this.furtue = false;
				break;
			}
		}

		public void runWithFurtue() {
			while (!shutdown) {
				Future<AsynchronousSocketChannel> f = asynchronousServerSocketChannel
						.accept();
				AsynchronousSocketChannel aschanel = null;
				try {
					aschanel = f.get(1, TimeUnit.MINUTES);
					Channel0 channel = new Channel0(core, aschanel);
					core._onConnect(channel);
				} catch (TimeoutException ex) {
					continue;
				} catch (InterruptedException e) {
					core._onThrowable(e);
				} catch (ExecutionException e) {
					core._onThrowable(e);
				}
			}
		}

		public void runWithCallback() {
			try {
				asynchronousServerSocketChannel
						.accept(core,
								new CompletionHandler<AsynchronousSocketChannel, TcpServerCore>() {
									@Override
									public void completed(
											AsynchronousSocketChannel result,
											TcpServerCore attachment) {
										Channel0 channel = new Channel0(
												attachment, result);
										if (!shutdown) {
											attachment._onAccepted(channel);
										}
										attachment._onConnect(channel);
									}

									@Override
									public void failed(Throwable exc,
											TcpServerCore attachment) {
										attachment._onThrowable(exc);
									}
								});
			} catch (Exception e) {
				core._onThrowable(e);
			}
		}

		@Override
		public void run() {
			if (furtue) {
				runWithFurtue();
			} else {
				runWithCallback();
			}
		}

	}

	private void _fireEventOnPool(final Event evt) {
		evt.getChannel().pushEvent(evt);
		threadPoolExecutor.execute(new Runnable() {
			@Override
			public void run() {
				while (true) {
					Event e = evt.getChannel().poolEvent();
					if (e == null)
						break;
					_fireEventOnCurrent(e);
				}
			}
		});
	}

	private void _fireEventOnCurrent(Event evt) {
		switch (evt.getEvtType()) {
		case CLOSE:
			evt.getTcpServerCore().getContainer().onClose(evt.getChannel());
			break;
		case CONNECTED:
			evt.getTcpServerCore().getContainer().onConnect(evt.getChannel());
			break;
		case EXCEPTION:
			evt.getTcpServerCore()
					.getContainer()
					.onThrowable(evt.getChannel(),
							(Exception) evt.getAttachment());
			break;
		case RECIEVE:
			evt.getTcpServerCore()
					.getContainer()
					.onReceive(evt.getChannel(),
							(ByteBuffer) evt.getAttachment());
			break;
		case WRITED:
			evt.getTcpServerCore().getContainer()
					.onWrited(evt.getChannel(), (Long) evt.getAttachment());
			break;
		case CONNECT:
			_connect(evt.getTcpServerCore(),
					(InetSocketAddress) evt.getAttachment());
		case IDLE:
			evt.getTcpServerCore().getContainer().onIdle(evt.getChannel());
		}
	}

	private void _connect(TcpServerCore tcpServerCore, InetSocketAddress address) {
		try {
			AsynchronousSocketChannel aschannel = AsynchronousChannelProvider
					.provider().openAsynchronousSocketChannel(null);
			aschannel.setOption(StandardSocketOption.SO_REUSEADDR,
					Boolean.TRUE);
			aschannel.connect(address,
					new Object[] { tcpServerCore, aschannel },
					new CompletionHandler<Void, Object[]>() {
						@Override
						public void completed(Void result, Object[] attachment) {
							Channel0 channel = new Channel0(
									(TcpServerCore) attachment[0],
									(AsynchronousSocketChannel) attachment[1]);
							((TcpServerCore) attachment[0])._onConnect(channel);
						}

						@Override
						public void failed(Throwable exc, Object[] attachment) {
							((TcpServerCore) attachment[0])._onThrowable(exc);
						}
					});

		} catch (Exception e) {
			tcpServerCore._onThrowable(e);
		}
	}

	public void shutdown() {
		shutdown = true;
		threadPoolExecutor.shutdown();
	}
}
