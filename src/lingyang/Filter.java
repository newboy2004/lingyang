package lingyang;

public interface Filter {
	public Object in(Session session, Object data);

	public Object out(Session session, Object data);

	public void onEvent(Session session, Event event);
	
	public void onNoSession(NoSession noSessionEvent);

	public Filter next();

	public Filter before();

	public void setNext(Filter filter);

	public void setBefore(Filter filter);
}
