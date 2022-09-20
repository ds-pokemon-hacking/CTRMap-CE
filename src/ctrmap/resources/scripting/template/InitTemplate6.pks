import core.Runtime;
import field.map.ZoneInitStage;

public class %CLASSNAME% {
	public static int g_mode = -1;

	public static void main(){
		switch (g_mode){
			case -1:
				OnLoadInvalid();
				break;
			case ZoneInitStage.MODEL_PRELOAD:
				OnModelPreload();
				break;
			case ZoneInitStage.ACTOR_PRELOAD:
				OnActorPreload();
				break;
			case ZoneInitStage.ZONE_LOAD_OK:
				OnZoneLoadFinished();
				break;
			default:
				Runtime.CommandNOP();
				break;
		}
	}

	static void OnLoadInvalid(){
		
	}

	static void OnModelPreload(){
		
	}

	static void OnActorPreload(){
		
	}

	static void OnZoneLoadFinished(){
		
	}
}