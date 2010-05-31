package lingyang;

public interface Handler {
	public void onRecive(Session session, Object messagge);

	public void onClose(Session session);

	public void onConnect(Session session);

	public void onSend(Session session);

	public void onIdle(Session session);

	public void onCreate(Session session);

	public void onErr(Session session);
}
