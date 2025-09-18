import javafx.animation.AnimationTimer;

public class MarketReplay {
	private DataSet data;
	private int start;
	
	public MarketReplay(DataSet data, int start) {
		this.data = data;
		this.start = start;
	}
	
	public void run() {
		new AnimationTimer() {
			
			@Override
			public void handle(long now) {
				// TODO Auto-generated method stub
				
			}
		}.start();
	}
}
