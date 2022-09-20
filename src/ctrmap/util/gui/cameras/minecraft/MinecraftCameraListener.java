
package ctrmap.util.gui.cameras.minecraft;

public interface MinecraftCameraListener {
	public void onMotionEvent(MotionEvent e);
	
	public static enum MotionEvent {
		MOVE_BEGIN,
		MOVE_ACC_FIN,
		MOVE_END,
		
		RUN_BEGIN,
		RUN_END,
		
		JUMP_BEGIN,
		JUMP_PEAK,
		
		HIT_GROUND,
		
		CROUCH_BEGIN,
		CROUCH_DONE,
		CROUCH_END
	}
}
