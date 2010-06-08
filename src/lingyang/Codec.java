package lingyang;

import java.nio.ByteBuffer;

public abstract class Codec extends FilterAdapter{

	@Override
	public Object OnRecive(Session session, Object data) {
		return decode(session,(ByteBuffer)data);
	}

	@Override
	public Object OnSend(Session session, Object data) {
		return encode(session,data);
	}

	@Override
	public boolean onClose(Session session) {
		return true;
	}

	@Override
	public boolean onConnect(Session session) {
		return true;
	}

	@Override
	public boolean onCreate(Session session) {
		return false;
	}

	@Override
	public boolean onErr(Session session) {
		return true;
	}

	@Override
	public boolean onIdle(Session session) {
		return true;
	}

	public abstract Object decode(Session session, ByteBuffer rawData);
	
	public abstract ByteBuffer encode(Session session, Object message);
}
