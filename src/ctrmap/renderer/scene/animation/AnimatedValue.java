
package ctrmap.renderer.scene.animation;

/**
 *
 */
public class AnimatedValue {

	public boolean exists = false;
	public float value = 0f;

	public AnimatedValue() {
		this(0, false);
	}

	public AnimatedValue(float v) {
		exists = true;
		value = v;
	}

	public AnimatedValue(float v, boolean exists) {
		this.exists = exists;
		value = v;
	}
	
	public AnimatedValue set(float newValue) {
		value = newValue;
		exists = true;
		return this;
	}

	public void setIfNotExists(float newValue) {
		if (!exists) {
			set(newValue);
		}
	}

	@Override
	public String toString() {
		return exists ? String.valueOf(value) : "____";
	}

}
