package lingyang.future;

public interface Future<T> {
	public void waiting() throws InterruptedException;
	public T get();
	
}
