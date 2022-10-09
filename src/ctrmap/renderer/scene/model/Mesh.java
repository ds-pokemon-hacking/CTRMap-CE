package ctrmap.renderer.scene.model;

import xstandard.math.vec.Vec3f;
import ctrmap.renderer.scene.texturing.Material;
import ctrmap.renderer.scene.Scene;
import ctrmap.renderer.scenegraph.NamedResource;
import ctrmap.renderer.scene.metadata.MetaData;
import ctrmap.renderer.scene.model.draw.vtxlist.AbstractVertexList;
import ctrmap.renderer.scene.model.draw.vtxlist.VertexArrayList;
import ctrmap.renderer.scene.model.draw.buffers.MeshBufferManager;
import ctrmap.renderer.scene.model.draw.vtxlist.VertexListUsage;
import xstandard.util.collections.IntList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class Mesh implements NamedResource, Iterable<Vertex> {

	public Model parentModel;
	
	public String name;
	public String materialName;
	public String visGroupName;
	
	public int renderLayer = 0;

	public AbstractVertexList vertices = new VertexArrayList();
	public IntList indices = new IntList();
	
	public final MeshBufferManager buffers;
	
	public PrimitiveType primitiveType = PrimitiveType.TRIS;
	public VertexListUsage bufferType = VertexListUsage.STATIC;
	public SkinningType skinningType = SkinningType.SMOOTH;
	
	public boolean useIBO = false;
	public boolean hasNormal = false;
	public boolean hasTangent = false;
	public boolean hasBoneIndices = false;
	public boolean hasBoneWeights = false;
	public boolean hasColor = false;
	public boolean[] hasUV = new boolean[3];
	public MetaData metaData = new MetaData();

	public Mesh() {
		buffers = new MeshBufferManager(this);
	}

	public Mesh(Mesh source) {
		this();

		setAttributes(source);

		vertices = new VertexArrayList(source.vertices);
		indices = new IntList(source.indices);
		useIBO = source.useIBO;
		metaData = source.metaData;
	}

	public void setAttributes(Mesh source) {
		name = source.name;

		skinningType = source.skinningType;
		primitiveType = source.primitiveType;
		hasNormal = source.hasNormal;
		hasBoneIndices = source.hasBoneIndices;
		hasBoneWeights = source.hasBoneWeights;
		hasColor = source.hasColor;
		hasUV = source.hasUV.clone();

		renderLayer = source.renderLayer;

		materialName = source.materialName;
		visGroupName = source.visGroupName;
	}

	public int getBoolAttribsHash() {
		int hash = 7;
		hash = 37 * hash + Objects.hashCode(hasColor);
		hash = 37 * hash + Objects.hashCode(hasNormal);
		hash = 37 * hash + Arrays.hashCode(hasUV);
		hash = 37 * hash + Objects.hashCode(hasBoneIndices);
		hash = 37 * hash + Objects.hashCode(hasBoneWeights);
		return hash;
	}
	
	public Iterable<Face> faces(){
		return new Iterable<Face>() {
			@Override
			public Iterator<Face> iterator() {
				return new Iterator<Face>() {
					
					private int max = getVertexCount();
					private int index = 0;
					private int stride = PrimitiveType.getPrimitiveTypeSeparationSize(primitiveType, Mesh.this);
					
					@Override
					public boolean hasNext() {
						return index < max;
					}

					@Override
					public Face next() {
						Face face = new Face(primitiveType, Mesh.this, index);
						index += stride;
						return face;
					}
				};
			}
		};
	}

	@Override
	public String getName() {
		return name;
	}

	public Vec3f calcMinVector() {
		Vec3f out = new Vec3f(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);
		for (Vertex vtx : vertices) {
			out.min(vtx.position);
		}
		return out;
	}

	public Vec3f calcMaxVector() {
		Vec3f out = new Vec3f(-Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE);
		for (Vertex vtx : vertices) {
			out.max(vtx.position);
		}
		return out;
	}

	public Material getMaterial(List<Material> materials) {
		return (Material) Scene.getNamedObject(materialName, materials);
	}

	public Material getMaterial(Model parent) {
		return getMaterial(parent.materials);
	}

	public void createBuffers() {
		buffers.updateAll();
	}
	
	public int getFaceCount() {
		return getVertexCount() / PrimitiveType.getPrimitiveTypeSeparationSize(primitiveType, this);
	}
	
	public int getVertexCount(){
		return useIBO ? indices.size() : vertices.size();
	}

	public boolean hasUV(int index) {
		return hasUV[index];
	}
	
	public int getActiveUVLayerCount() {
		int count = 0;
		for (int i = 0; i < hasUV.length; i++) {
			if (hasUV(i)) {
				count++;
			}
		}
		return count;
	}

	public void createAndInvalidateBuffers() {
		createBuffers();
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public Iterator<Vertex> iterator() {
		if (useIBO){
			return new Iterator<Vertex>() {
				
				private int idxIdx = 0;
				
				@Override
				public boolean hasNext() {
					return idxIdx < indices.size();
				}

				@Override
				public Vertex next() {
					return vertices.get(indices.get(idxIdx++));
				}
			};
		}
		else {
			return vertices.iterator();
		}
	}

	public static enum SkinningType {
		NONE,
		RIGID,
		SMOOTH
	}
}
