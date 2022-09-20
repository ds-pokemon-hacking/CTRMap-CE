package ctrmap.missioncontrol_base;

import xstandard.audio.AudioClip;
import ctrmap.renderer.backends.base.RenderSettings;
import ctrmap.util.CMPrefs;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

public class AudioSettings {

	public static final AudioSettings defaultSettings = new AudioSettings();

	public static final float VOLUME_RANGE = 100f;

	public boolean ON = true;
	public float VOLUME = 100f;

	private List<AudioSettingsListener> listeners = new ArrayList<>();

	public AudioSettings() {
		loadDefaults();
	}

	public final void loadDefaults() {
		ON = Defaults.DEF_ON;
		VOLUME = Defaults.DEF_VOLUME;
		fireUpdate();
	}

	public void processClip(AudioClip c) {
		if (c != null) {
			c.setMasterVolume(VOLUME);
			if (!ON && c.isActive()) {
				c.stop();
			}
		}
	}

	public void addSettingsListener(AudioSettingsListener asl) {
		listeners.add(asl);
	}

	public void removeSettingsListener(AudioSettingsListener asl) {
		listeners.remove(asl);
	}

	public void fireUpdate() {
		for (AudioSettingsListener l : listeners) {
			l.onUpdate();
		}
	}

	public static interface AudioSettingsListener {

		public void onUpdate();
	}

	public static class Defaults {

		private static final Preferences prefs = CMPrefs.node(RenderSettings.Defaults.class.getName());

		private static String KEY_DEF_ON = "audioIsEnabled";
		private static String KEY_DEF_VOLUME = "audioVolume";

		public static boolean DEF_ON;
		public static float DEF_VOLUME;

		static {
			DEF_ON = prefs.getBoolean(KEY_DEF_ON, true);
			DEF_VOLUME = prefs.getFloat(KEY_DEF_VOLUME, 100f);
		}

		public static void save() {
			prefs.putBoolean(KEY_DEF_ON, DEF_ON);
			prefs.putFloat(KEY_DEF_VOLUME, DEF_VOLUME);
		}
	}
}
