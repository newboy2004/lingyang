# Introduction #

java7的AIO是真正的异步IO模型，在window系统底层用重叠IO实现，在linux则用epoll实现.AIO相对NIO还有一个优点就是使用起来更简单，更顺手。新版lingyang基于AIO重新封装，代码结构更清晰，也更高效。


# 下面是新版lingyang一个简单的调用范例 #

package com.google.code.lingyang;

import java.nio.ByteBuffer;

public class Main {
> public static void main(String[.md](.md) args) {
> > MyS mys = new MyS();
> > mys.listen(7777);

> }
}

class MyS extends TcpServer {

> @Override
> public void onClose(Channel channel) {
> > System.out.println("closed");

> }

> @Override
> public void onConnect(Channel channel) {
> > // TODO Auto-generated method stub
> > System.out.println("connect");

> }

> @Override
> public void onIdle(Channel channel) {
> > // TODO Auto-generated method stub
> > System.out.println("Idle->" + channel.toString());

> }

> @Override
> public void onReceive(Channel channel, ByteBuffer byteBuf) {
> > byteBuf.flip();
> > for (int i = 0; i < byteBuf.limit(); ++i) {
> > > System.out.print((char) byteBuf.get());

> > }
> > byteBuf.flip();
> > channel.write(byteBuf);

> }

> @Override
> public void onThrowable(Channel channel, Throwable e) {
> > // TODO Auto-generated method stub
> > System.out.println("Exception->" + e.getMessage());

> }

> @Override
> public void onThrowable(Throwable e) {
> > System.out.println("Throwable->" + e.getMessage());

> }

> @Override
> public void onWrited(Channel channel, Long writed) {
> > // TODO Auto-generated method stub

> }

}