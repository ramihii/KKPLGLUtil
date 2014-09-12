package okkapel.kkplglutil.rendering;

import static org.lwjgl.opengl.GL11.*;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.lwjgl.opengl.GL15;

import okkapel.kkplglutil.util.Texture;

public class GLHandler {
	private static List<GLRenderObj> rendObjs;
//	private static List<GLRenderObjPointer> rendObjPtrs;
	
	private static HashMap<String, Integer> groupNames = null;
	private static List<GLRenderGroup> groups = null;
	
	private static GLRenderObj buffer = null;
	
	public static void init() {
		rendObjs = new ArrayList<GLRenderObj>();
//		rendObjPtrs = new ArrayList<GLRenderObjPointer>();
		
		groupNames = new HashMap<String, Integer>();
		groups = new ArrayList<GLRenderGroup>();
	}
	
	public static void deinit() {
		for(int i=0;i<rendObjs.size();i++) {
			if(rendObjs.get(i) != null) {
				rendObjs.get(i).deleteVbo();
			}
		}
	}
	
	public static void renderGroup(String name) {
		Integer value = groupNames.get(name);
		renderGroup(value == null ? -1 : value.intValue());
	}
	
	public static void renderGroup(int id) {
		if(id < 0 || id >= groups.size()) {
			System.err.println("GLRenderGroup not found. Ignoring render call...");
			return;
		}
		groups.get(id).renderGroup();
	}
	
	public static GLRenderObjPointer createVBO(FloatBuffer data, int gl_usage, Texture tex, int vertCount) {
		buffer = new GLRenderObj(GLRenderMethod.VERTEX_BUFFER_OBJECT, data, gl_usage);
		int arrid = -1;
		for(int i=0;i<rendObjs.size();i++) {
			if(rendObjs.get(i) == null) {
				arrid = i;
				rendObjs.set(i, buffer);
			}
		}
		if(arrid == -1) {
			if(rendObjs.add(buffer)) {
				return new GLRenderObjPointer(0, tex, rendObjs.size()-1, vertCount);
			}
			return null;
		} else {
			return new GLRenderObjPointer(0, tex, arrid, vertCount);
		}
	}
	

	
	public static void renderRendPtr(GLRenderObjPointer ptr) {
		renderRendObj(ptr.getOffset(), ptr.getTexture(), rendObjs.get(ptr.getArrIndex()), ptr.getVertCount());
	}
	
	private static void renderRendObj(int offset, Texture tex, GLRenderObj robj, int vcount) {
		glEnableClientState(GL_VERTEX_ARRAY);
		glEnableClientState(GL_COLOR_ARRAY);
		
		if(tex != null) {
			glEnableClientState(GL_TEXTURE_COORD_ARRAY);
			tex.bind();
		}
		
		if(robj.getRenderMethod() == GLRenderMethod.VERTEX_BUFFER_OBJECT) {
			// VBO
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, robj.getVBOId());
			
			glTexCoordPointer(2, GL_FLOAT, RenderBufferGenerator.DEFAULT_GL_STRIDE, 0);
			glVertexPointer(3, GL_FLOAT, 8*4, 5*4);
			glColorPointer(3, GL_FLOAT, 8*4, 2*4);
			
			glDrawArrays(GL_TRIANGLES, offset, vcount);
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
			
		} else {
			// VA
			glInterleavedArrays(GL_T2F_C3F_V3F, RenderBufferGenerator.DEFAULT_GL_STRIDE, robj.getData());
			glDrawArrays(GL_TRIANGLES, offset, vcount);
		}
		
		glDisableClientState(GL_VERTEX_ARRAY);
		glDisableClientState(GL_COLOR_ARRAY);
		
		if(tex != null) {
			glBindTexture(GL_TEXTURE_2D, 0);
			glDisableClientState(GL_TEXTURE_COORD_ARRAY);
		}
	}
	
}