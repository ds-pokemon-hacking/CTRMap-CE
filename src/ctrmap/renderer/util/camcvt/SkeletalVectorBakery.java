package ctrmap.renderer.util.camcvt;

import ctrmap.renderer.scene.animation.skeletal.SkeletalAnimation;
import ctrmap.renderer.scene.model.Joint;
import ctrmap.renderer.scene.model.Skeleton;
import xstandard.math.vec.Matrix4;
import xstandard.math.vec.Vec3f;
import java.util.Iterator;

public class SkeletalVectorBakery implements Iterable<Vec3f> {
	private final SkeletalMatrixBakery matBakery;
	
	private final SkeletalVectorType vecType;
	
	private final Vec3f vec_tmp = new Vec3f();
	
	public SkeletalVectorBakery(SkeletalAnimation anm, Skeleton skl, Joint jnt, SkeletalVectorType vec) {
		matBakery = new SkeletalMatrixBakery(anm, skl, jnt);
		vecType = vec;
	}
	
	public float getCurrentFrame() {
		return matBakery.getCurrentFrame();
	}

	@Override
	public Iterator<Vec3f> iterator() {
		return new Iterator<Vec3f>() {
			
			private final Iterator<Matrix4> matIterator = matBakery.iterator();
			
			@Override
			public boolean hasNext() {
				return matIterator.hasNext();
			}

			@Override
			public Vec3f next() {
				Matrix4 mat = matIterator.next();
				
				switch (vecType) {
					case ROTATION:
						mat.getRotationTo(vec_tmp);
						break;
					case SCALE:
						mat.getScale(vec_tmp);
						break;
					case TRANSLATION:
						mat.getTranslation(vec_tmp);
						break;
				}
				
				return vec_tmp;
			}
		};
	}
}
