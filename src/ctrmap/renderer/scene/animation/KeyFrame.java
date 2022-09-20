
package ctrmap.renderer.scene.animation;

/**
 *
 */
public class KeyFrame {

	public float frame = 0;
	public float value = 0;
	public float inSlope = 0;
	public float outSlope = 0;
	public InterpolationMethod interpolation;

	public KeyFrame(KeyFrame source) {
		this(source.frame, source.value, source.inSlope, source.outSlope, source.interpolation);
	}

	public KeyFrame(float frame, float value) {
		this(frame, value, 0, 0, InterpolationMethod.LINEAR);
	}

	public KeyFrame(float frame, float value, float slope) {
		this(frame, value, slope, slope, InterpolationMethod.HERMITE);
	}

	public KeyFrame(float frame, float value, float inSlope, float outSlope) {
		this(frame, value, inSlope, outSlope, InterpolationMethod.HERMITE);
	}

	public KeyFrame(float frame, float value, float inSlope, float outSlope, InterpolationMethod interpolation) {
		this.frame = frame;
		this.value = value;
		this.inSlope = inSlope;
		this.outSlope = outSlope;
		this.interpolation = interpolation;
	}

	public KeyFrame() {
	}
	
	@Override
	public String toString() {
		switch (interpolation) {
			case STEP:
			case LINEAR:
				return frame + ": " + value;
			case HERMITE:
				return frame + ": " + inSlope + " -> " + value + " -> " + outSlope;
		}
		return "INVALID INTERPOLATION";
	}

	public enum InterpolationMethod {
		STEP, 
		LINEAR, 
		HERMITE
	}

}
