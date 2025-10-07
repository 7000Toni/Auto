
public class HorizontalMRPaneScrollBar extends HorizontalScrollBar {

	public HorizontalMRPaneScrollBar(ScrollBarOwner sbo, int dataSize, double minPos, double maxPos, double sbWidth, double sbHeight, double yPos) {
		super(sbo, dataSize, minPos, maxPos, sbWidth, sbHeight, yPos);
	}

	@Override
	protected void moveOwnerLeft(boolean fast) {
		int speed = 2;
		if (fast) {
			speed *= 5;
		}
		setPosition(speed, true);
	}
	
	@Override
	protected void moveOwnerRight(boolean fast) {
		int speed = 2;
		if (fast) {
			speed *= 5;
		}
		setPosition(speed, true);
	}
}
