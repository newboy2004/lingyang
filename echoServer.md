package demo;

import java.io.IOException;
import java.nio.ByteBuffer;

import lingyang.Handler;
import lingyang.NoSession;
import lingyang.Service;
import lingyang.Session;
import lingyang.Factory.ConfigureFactory;
import lingyang.Factory.ServiceFactory;
import lingyang.configure.Configure;
import lingyang.err.ChannelException;
import lingyang.err.InitException;
import lingyang.err.SelectorException;

class EchoHandler implements Handler{

> @Override
> public void onClose(Session session) {
> > // TODO Auto-generated method stub


> }

> @Override
> public void onConnect(Session session) {
> > // TODO Auto-generated method stub


> }

> @Override
> public void onCreate(Session session) {
> > // TODO Auto-generated method stub


> }

> @Override
> public void onErr(Session session) {
> > // TODO Auto-generated method stub


> }

> @Override
> public void onIdle(Session session) {
> > // TODO Auto-generated method stub


> }

> @Override
> public void onRecive(Session session, Object message) {
> > session.write(((ByteBuffer)message).duplicate());

> }

> @Override
> public void onWriteble(Session session) {

> }

> @Override
> public void onNoSession(NoSession noSessionEvent) {

> }

}
public class EchoServer {
> public static void main(String[.md](.md) args) {
> > try {
> > > Service service=ServiceFactory.getSevice();
> > > Configure configure=ConfigureFactory.getConfigure();
> > > configure.setHandler(new EchoHandler());
> > > service.setConfigure(configure);
> > > service.listenAt(88);

> > } catch (ChannelException e) {
> > > e.printStackTrace();

> > } catch (IOException e) {
> > > // TODO Auto-generated catch block
> > > e.printStackTrace();

> > } catch (SelectorException e) {
> > > // TODO Auto-generated catch block
> > > e.printStackTrace();

> > } catch (InitException e) {
> > > // TODO Auto-generated catch block
> > > e.printStackTrace();

> > }

> }
}