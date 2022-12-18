package ctrmap.renderer.scene.animation;

import ctrmap.renderer.backends.base.RenderSettings;
import xstandard.INamed;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;

public abstract class AbstractAnimationController implements INamed {

	public Queue<AbstractAnimation> animeList = new LinkedList<>();

	public AbstractAnimation anim;

	public float frame = 0f;

	public float speedMultiplier = 1f;

	private boolean paused = false;

	public boolean registered = false;

	public boolean loop = true;

	public Runnable callback;

	public AbstractAnimationController(AbstractAnimation anm) {
		if (anm != null) {
			queueAnime(anm);
		}
	}

	public AbstractAnimationController(AbstractAnimation anm, Runnable callback) {
		this(anm);
		this.callback = callback;
	}

	public AbstractAnimationController(Collection<AbstractAnimation> queue) {
		for (AbstractAnimation a : queue) {
			queueAnime(a);
		}
	}

	public final void queueAnime(AbstractAnimation anm) {
		animeList.add(anm);
	}

	public void register(RenderSettings settings) {
		if (!registered) {
			registered = true;
			if (settings.ANIMATION_OPTIMIZE) {
				for (AbstractAnimation a : animeList) {
					a.optimize();
				}
				if (anim != null) {
					anim.optimize();
				}
			}
		}
	}

	public void advanceFrame(float globalStep, RenderSettings settings) {
		if (!paused && registered) {
			frame += globalStep * speedMultiplier;

			if (speedMultiplier >= 0f) {
				if (anim == null || frame > anim.frameCount) {
					if (!animeList.isEmpty()) {
						if (anim != null) {
							frame %= anim.frameCount;
						}
						anim = animeList.remove();
					} else {
						if (callback != null) {
							pauseAnimation();
							doCallback = true;
						} else if (loop) {
							frame %= anim.frameCount;
						} else {
							pauseAnimation();
						}
					}
				}
			} else {
				if (frame < 0f) {
					if (loop && anim != null) {
						frame %= anim.frameCount;
						frame = anim.frameCount + frame;
					} else {
						pauseAnimation();
					}
				}
			}
		}
	}

	public void forceNextAnime() {
		frame = 0;
		if (!animeList.isEmpty()) {
			anim = animeList.remove();
		} else {
			anim = null;
		}
	}

	private boolean doCallback = false;

	public void callback() {
		if (doCallback && callback != null) {
			Runnable c = callback;
			callback = null; //prevent recursion
			c.run();
			doCallback = false;
		}
	}

	public void pauseOrUnpauseAnimation() {
		if (!anim.isLooped && frame > anim.frameCount) {
			frame = 0;
		}
		paused = !paused;
	}

	public void resumeAnimation() {
		paused = false;
	}

	public void restartAnimation() {
		if (speedMultiplier < 0f) {
			frame = anim == null ? 0 : anim.frameCount;
		} else {
			frame = 0;
		}
		paused = false;
	}

	public void stopAnimation() {
		paused = true;
		if (callback != null) {
			doCallback = true;
			callback();
		}
		else if (loop) {
			frame = 0;
		}
	}

	public void pauseAnimation() {
		paused = true;
	}

	public boolean paused() {
		return paused;
	}

	@Override
	public String getName() {
		if (anim == null) {
			return "(Idle animation controller)";
		}
		return anim.name;
	}

	@Override
	public void setName(String name) {
		anim.name = name;
	}
}
