package com.google.code.lingyang.Impl;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOption;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;

import com.google.code.lingyang.BufferCacher;
import com.google.code.lingyang.Channel;
import com.google.code.lingyang.Container;
import com.google.code.lingyang.Profile;

public class TcpServerCore {
	AsynchronousServerSocketChannel asynchronousServerSocketChannel;
	private BufferCacher bufferCacher = new BufferCacher0();
	private Collection<Channel> channels;
	private Container container;
	IdleStrategy idleStrategy;
	int port = 8080;
	Profile profile;
	ThreadStrategy threadStrategy;

	public TcpServerCore(Container container, Profile profile) {
		this.container = container;
		this.port = profile.getListenPort();
		this.profile = profile;
		threadStrategy = ThreadStrategy.getInstance(profile.getStrategy());
		channels = new CopyOnWriteArrayList<Channel>();
		idleStrategy = new IdleStrategy(this, profile.getIdlePeriod());
		idleStrategy.updateFlash(channels);
		idleStrategy.start();
	}

	private void _closeListener() {
		if (asynchronousServerSocketChannel == null)
			return;
		if (!asynchronousServerSocketChannel.isOpen()) {
			return;
		}
		try {
			asynchronousServerSocketChannel.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void _Connect(InetSocketAddress inetSocketAddress) {
		threadStrategy.fireEvent(new Event(this, null, EventType.CONNECT,
				inetSocketAddress));
	}

	private void _listen(
			AsynchronousServerSocketChannel asynchronousServerSocketChannel) {
		threadStrategy.fireListenEvent(new Event(this,
				asynchronousServerSocketChannel, EventType.LISTEN, profile
						.getListenModel()));
	}

	public void _onAccepted(Channel0 channel) {
		threadStrategy.fireListenEvent(new Event(this,
				asynchronousServerSocketChannel, EventType.LISTEN, profile
						.getListenModel()));

	}

	public void _onClose(Channel0 channel) {
		unRigisterChannel(channel);
		threadStrategy
				.fireEvent(new Event(this, channel, EventType.CLOSE, null));
	}

	public void _onConnect(Channel0 channel) {
		rigisterNewChannel(channel);
		setOptions(channel);
		threadStrategy.fireEvent(new Event(this, channel, EventType.CONNECTED,
				null));
		channel.read();
	}

	public void _onIdle(Channel0 channel) {
		threadStrategy
				.fireEvent(new Event(this, channel, EventType.IDLE, null));
	}

	public void _onReceive(Channel0 channel, ByteBuffer byteBuf) {
		channel.updateReadTime();
		threadStrategy.fireEvent(new Event(this, channel, EventType.RECIEVE,
				byteBuf));
	}

	public void _onThrowable(Channel0 channel, Throwable e) {
		threadStrategy.fireEvent(new Event(this, channel, EventType.EXCEPTION,
				e));
	}

	public void _onThrowable(Throwable e) {
		threadStrategy.fireEvent(new Event(this, null, EventType.EXCEPTION, e));
	}

	void _onWrited(Channel0 channel, Long result) {
		channel.updateWriteTime();
		threadStrategy.fireEvent(new Event(this, channel, EventType.WRITED,
				result));
	}

	public BufferCacher getBufferCacher() {
		return bufferCacher;
	}

	public Container getContainer() {
		return container;
	}

	public void listen() {
		try {
			asynchronousServerSocketChannel = AsynchronousServerSocketChannel
					.open();
			asynchronousServerSocketChannel.setOption(StandardSocketOption.SO_REUSEADDR,
					Boolean.TRUE);
			asynchronousServerSocketChannel.bind(new InetSocketAddress(port));
			_listen(asynchronousServerSocketChannel);
		} catch (IOException e) {
			_onThrowable(e);
		}
	}

	void rigisterNewChannel(Channel channel) {
		this.channels.add(channel);
	}

	void setOptions(Channel0 channel) {
		try {
			if (profile.isKeepalive()) {
				channel.setOption(StandardSocketOption.SO_KEEPALIVE,
						Boolean.TRUE);
			} else {
				channel.setOption(StandardSocketOption.SO_KEEPALIVE,
						Boolean.FALSE);
			}
			if (profile.isTcp_NoDelay()) {
				channel.setOption(StandardSocketOption.TCP_NODELAY,
						Boolean.TRUE);
			}else{
				channel.setOption(StandardSocketOption.TCP_NODELAY,
						Boolean.FALSE);
			}
			if (profile.isReuseAddr()) {
				channel.setOption(StandardSocketOption.SO_REUSEADDR,
						Boolean.TRUE);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void shutdown() {
		_closeListener();
		threadStrategy.shutdown();
	}

	void unRigisterChannel(Channel channel) {
		this.channels.remove(channel);
	}

}
