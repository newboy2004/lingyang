package lingyang;

public abstract class FilterAdapter implements Filter {

	Filter next;
	Filter before;

	public Filter before() {

		return before;
	}

	public Object in(Session session, Object data) {
		return next.in(session, OnRecive(session, data));
	}

	public Filter next() {
		return next;
	}

	public void onEvent(Session session, Event event) {
		boolean ret = false;
		switch (event) {
		case create:
			ret = onCreate(session);
			break;
		case connect:
			ret = onConnect(session);
			break;
		case idle:
			ret = onIdle(session);
			break;
		case err:
			ret = onErr(session);
			break;
		case close:
			ret = onClose(session);
			break;
		}
		if (ret) {
			next.onEvent(session, event);
		}
	}

	public Object out(Session session, Object data) {
		return before.out(session, OnSend(session, data));
	}
	public void setNext(Filter filter){
		this.next=filter;
	}

	public void setBefore(Filter filter){
		this.before=filter;
	}
	public abstract Object OnRecive(Session session, Object data);

	public abstract Object OnSend(Session session, Object data);

	public abstract boolean onConnect(Session session);

	public abstract boolean onCreate(Session session);

	public abstract boolean onIdle(Session session);

	public abstract boolean onClose(Session session);

	public abstract boolean onErr(Session session);
}
