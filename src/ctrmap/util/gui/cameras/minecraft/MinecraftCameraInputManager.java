package ctrmap.util.gui.cameras.minecraft;

import ctrmap.util.gui.cameras.FPSCameraInputManager;
import ctrmap.renderer.scene.Camera;
import ctrmap.renderer.scene.Scene;
import ctrmap.renderer.scenegraph.SceneAnimationCallback;
import xstandard.util.ArraysEx;
import java.awt.AWTException;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.swing.event.MouseInputListener;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;

public class MinecraftCameraInputManager extends FPSCameraInputManager implements MouseWheelListener, MouseMotionListener, MouseInputListener, KeyListener {

	private List<MinecraftCameraListener> listeners = new ArrayList<>();

	private MinecraftHeightProvider height;
	
	private float GROUND_OFFSET = 50f;

	public MinecraftCameraInputManager() {
		super();
		speed = 10f;
		try {
			robot = new Robot();
		} catch (AWTException ex) {
			Logger.getLogger(MinecraftCameraInputManager.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
	
	public void setGroundOffset(float val){
		GROUND_OFFSET = val;
	}

	public void addMinecraftCameraListener(MinecraftCameraListener l) {
		ArraysEx.addIfNotNullOrContains(listeners, l);
	}

	public void setHeightProvider(MinecraftHeightProvider h) {
		height = h;
	}

	private void fireMotionEvent(MinecraftCameraListener.MotionEvent e) {
		for (MinecraftCameraListener l : listeners) {
			l.onMotionEvent(e);
		}
	}

	private BufferedImage cursorImg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);

	private final Cursor blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(cursorImg, new Point(0, 0), "InvisibleCursor");

	public boolean isPaused = true;

	@Override
	public void deactivate() {
		isPaused = true;
		super.deactivate();
	}

	@Override
	protected SceneAnimationCallback createAnimationCallback() {
		return new SceneAnimationCallback() {

			private float velocity = 0f;

			private float gravity = 0f;

			private float groundY = 0f;

			private float crouchMul = 1f;


			public static final float G = 9.81f;

			private float airTime = 0f;

			private float FOVINC = 0f;

			private float lastVelocity = velocity;
			private float lastGravity = gravity;
			private float lastY = cam.translation.y;
			private float lastVelocityMaxMul = 1f;
			private float lastCrouchMul = crouchMul;

			private float lastFOVINC = FOVINC;

			private float sourceVelocityMaxMul = 1f;
			
			private float lastValidLRSign = 0f;
			private float lastValidUDSign = 0f;

			@Override
			public void run(float frameAdvance) {
				float rawSpeed = speed * frameAdvance;

				if (isCameraEnabled && allowMotion && !isPaused) {
					if (height != null) {
						groundY = height.getHeight(MinecraftCameraInputManager.this);
					} else {
						groundY = 0f;
					}

					float lastBaseFOV = cam.FOV - lastFOVINC;

					float translateX;
					float translateY;
					float translateZ;

					if (keycodes.contains(KeyEvent.VK_ESCAPE)) {
						isPaused = true;
						if (parent != null) {
							parent.setCursor(null);
						}
						return;
					}

					//reset values
					translateX = 0f;
					translateY = 0f;
					translateZ = 0f;
					float upDownSignum = keycodes.contains(KeyEvent.VK_W) ? 1f : keycodes.contains(KeyEvent.VK_S) ? -1f : 0f;
					float leftRightSignum = keycodes.contains(KeyEvent.VK_A) ? 1f : keycodes.contains(KeyEvent.VK_D) ? -1f : 0f;
					
					if (upDownSignum != 0){
						lastValidUDSign = upDownSignum;
						if (leftRightSignum == 0){
							lastValidLRSign = 0;
						}
					}
					if (leftRightSignum != 0){
						lastValidLRSign = leftRightSignum;
						if (upDownSignum == 0){
							lastValidUDSign = 0;
						}
					}

					boolean isMoving = (upDownSignum != 0f || leftRightSignum != 0f);
					
					float targetVelocityMaxMul = keycodes.contains(KeyEvent.VK_CONTROL) && isMoving ? 1.789f : 1f;
					//Increase the velocity multiplier when running
					if (lastVelocityMaxMul != targetVelocityMaxMul) {
						sourceVelocityMaxMul = lastVelocityMaxMul;
					}

					if (isMoving) {
						//Accelerate
						if (velocity < targetVelocityMaxMul) {
							velocity += 0.2f;
							if (velocity > targetVelocityMaxMul) {
								velocity = targetVelocityMaxMul;
							}
						}
						//Deaccelerate
						if (velocity > targetVelocityMaxMul) {
							velocity -= 0.1f;
							if (velocity < targetVelocityMaxMul) {
								velocity = targetVelocityMaxMul;
							}
						}
					} else {
						//Brake
						if (velocity > 0f) {
							velocity -= 0.1f;
						}
						if (velocity < 0f) {
							velocity = 0f;
						}
					}
					if (velocity == targetVelocityMaxMul){
						sourceVelocityMaxMul = targetVelocityMaxMul;
					}
					if (sourceVelocityMaxMul < targetVelocityMaxMul) {
						FOVINC = Math.max(0, (velocity - sourceVelocityMaxMul) / (targetVelocityMaxMul - sourceVelocityMaxMul) * 10f);
					}
					else if (targetVelocityMaxMul < sourceVelocityMaxMul) {
						FOVINC = Math.max(0, 10f - (velocity - sourceVelocityMaxMul) / (targetVelocityMaxMul - sourceVelocityMaxMul) * 10f);
					}
					else if (targetVelocityMaxMul == sourceVelocityMaxMul) {
						//division by zero - keep FOVINC
					}
					
					float xzSpeed = rawSpeed * velocity * 0.7f;

					cam.FOV = lastBaseFOV + FOVINC;
					lastFOVINC = FOVINC;

					//Crouch
					if (keycodes.contains(KeyEvent.VK_SHIFT)) {
						if (crouchMul > 0.5f) {
							crouchMul -= frameAdvance * 0.08f;
						}
						if (crouchMul < 0.5f) {
							crouchMul = 0.5f;
						}
					} else {
						if (crouchMul < 1f) {
							crouchMul += frameAdvance * 0.09f; //get up faster
						}
						if (crouchMul > 1f) {
							crouchMul = 1f;
						}
					}

					float groundOffset = crouchMul * GROUND_OFFSET;

					boolean isMidAir = cam.translation.y > groundY + groundOffset;

					//Increase falling speed
					//Yea, I know this isn't how G is supposed to be used :D
					if (gravity < G && isMidAir) {
						gravity += frameAdvance * 1.3f;
						if (gravity > G) {
							gravity = G;
						}
					}

					//Jump -> set gravity to negative
					if (keycodes.contains(KeyEvent.VK_SPACE) && cam.translation.y == groundY + groundOffset) {
						gravity = -G;
						fireMotionEvent(MinecraftCameraListener.MotionEvent.JUMP_BEGIN);
					}

					//Apply gravity
					translateY = -gravity * speed / G / 4f;

					//Accumulate air time
					if (!isMidAir || gravity != G) {
						airTime = 0f;
					} else {
						airTime += frameAdvance;
					}

					//Reduce motion speed in mid-air
					xzSpeed /= Math.max(1f, Math.min(G, airTime / 30f * G)); //1 second of air time = 30 frames - max air time 1s till stall

					//Actually move
					if (cam.mode == Camera.Mode.PERSPECTIVE) {
						//Check left over from FPSCameraInputManager
						if (lastValidUDSign != 0f) {
							translateX -= lastValidUDSign * Math.sin(Math.toRadians(cam.rotation.y)) * Math.min(1f, Math.tan(Math.toRadians(90 - Math.abs(cam.rotation.x)))) * xzSpeed;
							translateZ -= lastValidUDSign * Math.cos(Math.toRadians(cam.rotation.y)) * Math.min(1f, Math.tan(Math.toRadians(90 - Math.abs(cam.rotation.x)))) * xzSpeed;
						}
						if (lastValidLRSign != 0f) {
							translateX += lastValidLRSign * Math.sin(Math.toRadians(cam.rotation.y - 90f)) * xzSpeed;
							translateZ += lastValidLRSign * Math.cos(Math.toRadians(cam.rotation.y - 90f)) * xzSpeed;
						}
					}

					//commit
					cam.translation.x += translateX;
					cam.translation.y += translateY;
					cam.translation.z += translateZ;

					cam.translation.y = Math.max(groundY + groundOffset, cam.translation.y);

					//Fire motion events
					if (cam.translation.y == groundY + groundOffset && cam.translation.y != lastY) {
						fireMotionEvent(MinecraftCameraListener.MotionEvent.HIT_GROUND);
					}
					if (lastGravity < 0 && gravity >= 0) {
						fireMotionEvent(MinecraftCameraListener.MotionEvent.JUMP_PEAK);
					}
					if (targetVelocityMaxMul > lastVelocityMaxMul) {
						fireMotionEvent(MinecraftCameraListener.MotionEvent.RUN_BEGIN);
					} else if (targetVelocityMaxMul < lastVelocityMaxMul) {
						fireMotionEvent(MinecraftCameraListener.MotionEvent.RUN_END);
					}
					if (velocity == 0f && lastVelocity != 0f) {
						fireMotionEvent(MinecraftCameraListener.MotionEvent.MOVE_END);
					} else if (velocity != 0f && lastVelocity == 0f) {
						fireMotionEvent(MinecraftCameraListener.MotionEvent.MOVE_BEGIN);
					}
					if (velocity == targetVelocityMaxMul && lastVelocity != targetVelocityMaxMul) {
						fireMotionEvent(MinecraftCameraListener.MotionEvent.MOVE_ACC_FIN);
					}
					if (lastCrouchMul == 1f && crouchMul != 1f) {
						fireMotionEvent(MinecraftCameraListener.MotionEvent.CROUCH_BEGIN);
					} else if (lastCrouchMul == 0.5f && crouchMul != 0.5f) {
						fireMotionEvent(MinecraftCameraListener.MotionEvent.CROUCH_END);
					} else if (crouchMul == 0.5f && lastCrouchMul != 0.5f) {
						fireMotionEvent(MinecraftCameraListener.MotionEvent.CROUCH_DONE);
					}
					lastVelocityMaxMul = targetVelocityMaxMul;
					lastVelocity = velocity;
					lastGravity = gravity;
					lastY = cam.translation.y;
					lastCrouchMul = crouchMul;
				}
			}
		};
	}

	private Robot robot;

	private boolean initialClick = false;

	public boolean isInitialClick() {
		return initialClick;
	}

	@Override
	public void mousePressed(MouseEvent e) {
		initialClick = false;
		if (isCameraEnabled && allowMotion) {
			if (SwingUtilities.isLeftMouseButton(e)) {
				if (isPaused) {
					isPaused = false;
					if (parent != null) {
						parent.setCursor(blankCursor);
					}
					e.consume();
					initialClick = true;
				}
			}
		}
		super.mousePressed(e);
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		boolean needOverrideMouse = false;
		if (isCameraEnabled && allowMotion && !isPaused) {
			cam.rotation.y -= ((e.getX() - originMouseX) / 2f) % 360f;
			cam.rotation.x = Math.max(-90f, Math.min(90f, cam.rotation.x - (e.getY() - originMouseY) / 2f)) % 360f;

			for (Scene s : presentScenes) {
				s.stopCameraAnimations();
			}
			if (parent != null) {
				int overrideMouseX = e.getXOnScreen();
				int overrideMouseY = e.getYOnScreen();
				if (e.getX() > parent.getWidth() * 0.95f) {
					overrideMouseX = (int) (e.getXOnScreen() - parent.getWidth() * 0.9f);
					needOverrideMouse = true;
				} else if (e.getX() < parent.getWidth() * 0.05f) {
					overrideMouseX = (int) (e.getXOnScreen() + parent.getWidth() * 0.9f);
					needOverrideMouse = true;
				}
				if (e.getY() > parent.getHeight() * 0.95f) {
					overrideMouseY = (int) (e.getYOnScreen() - parent.getHeight() * 0.9f);
					needOverrideMouse = true;
				} else if (e.getY() < parent.getHeight() * 0.05f) {
					overrideMouseY = (int) (e.getYOnScreen() + parent.getHeight() * 0.9f);
					needOverrideMouse = true;
				}
				if (needOverrideMouse) {
					robot.mouseMove(overrideMouseX, overrideMouseY);
					Point los = parent.getLocationOnScreen();
					originMouseX = overrideMouseX - los.x;
					originMouseY = overrideMouseY - los.y;
				}
			}
		}

		if (!needOverrideMouse) {
			setOrigins(e);
		}
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		if (isCameraEnabled && allowMotion) {
			if (!e.isControlDown()) {
				speed = Math.max(speed - e.getWheelRotation(), 2f);
			}
		}
	}
	
	@Override
	public void mouseDragged(MouseEvent e) {
		mouseMoved(e);
	}
}
