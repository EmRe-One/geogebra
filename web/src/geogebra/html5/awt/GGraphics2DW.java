package geogebra.html5.awt;

import geogebra.common.awt.GAffineTransform;
import geogebra.common.awt.GAttributedCharacterIterator;
import geogebra.common.awt.GBasicStroke;
import geogebra.common.awt.GBufferedImage;
import geogebra.common.awt.GBufferedImageOp;
import geogebra.common.awt.GColor;
import geogebra.common.awt.GComposite;
import geogebra.common.awt.GDimension;
import geogebra.common.awt.GFontRenderContext;
import geogebra.common.awt.GGlyphVector;
import geogebra.common.awt.GGraphicsConfiguration;
import geogebra.common.awt.GImage;
import geogebra.common.awt.GImageObserver;
import geogebra.common.awt.GKey;
import geogebra.common.awt.GPaint;
import geogebra.common.awt.GRenderableImage;
import geogebra.common.awt.GRenderedImage;
import geogebra.common.awt.GRenderingHints;
import geogebra.common.factories.AwtFactory;
import geogebra.common.main.App;
import geogebra.common.util.StringUtil;
import geogebra.html5.gawt.BufferedImage;
import geogebra.html5.openjdk.awt.geom.PathIterator;
import geogebra.html5.openjdk.awt.geom.Polygon;
import geogebra.html5.openjdk.awt.geom.Shape;
import geogebra.html5.util.ImageLoadCallback;
import geogebra.html5.util.ImageWrapper;

import java.util.Map;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.CanvasPattern;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.canvas.dom.client.Context2d.Repetition;
import com.google.gwt.canvas.dom.client.ImageData;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayNumber;
import com.google.gwt.user.client.Element;

public class GGraphics2DW extends geogebra.common.awt.GGraphics2D {
	
	protected final Canvas canvas;
	private final MyContext2d context;
	protected geogebra.common.awt.GShape clipShape = null;

	private GFontW currentFont = new GFontW("normal");
	private GColor color = new GColorW(255,255,255,255), bgColor;
	private GAffineTransform savedTransform;
	private float [] dash_array = null;

	GPaint currentPaint = new GColorW(255,255,255,255);
	private JsArrayNumber jsarrn;
	
	/**
	 * the pixel ratio of the canvas.
	 */
	public int devicePixelRatio = 1;

	/**
	 * @param canvas
	 */
	public GGraphics2DW(Canvas canvas) {
		this.canvas = canvas;
		setDirection();
		
		this.context = (MyContext2d) canvas.getContext2d();
		savedTransform = new GAffineTransformW();
		preventContextMenu (canvas.getElement());
		//TODO put this back in
		//devicePixelRatio = checkPixelRatio(canvas.getElement());
		
	}
	
	private native int checkPixelRatio(Element canvas) /*-{
	    var devicePixelRatio = window.devicePixelRatio || 1,
	    	context = canvas.getContext("2d"),
	    	backingStorePixelRatio = context.webkitBackingStorePixelRatio
	        	|| context.mozBackingStorePixelRatio
	        	|| context.msBackingStorePixelRatio
	        	|| context.oBackingStorePixelRatio
	        	|| context.backingStorePixelRatio
	        	|| 1;
	
	    pixelRatio = devicePixelRatio / backingStorePixelRatio;
	    return pixelRatio;
   	}-*/;

	/**
	 * If we allow right-to left direction
	 * * checkboxes have their labels to the right
	 * * labels are drawn to the right, hence the check to fit in screen will probably fail
	 * * labels are malformed, eg )A=(1,2
	 */
	private void setDirection() {
		this.canvas.getElement().setDir("ltr");
    }

	public GGraphics2DW(Canvas canvas, boolean resetColor) {
		this(canvas);
		if(resetColor){
			updateCanvasColor();
		}
	}
	
	private native void preventContextMenu (Element canvas) /*-{
		canvas.addEventListener("contextmenu",function(e) {
			e.preventDefault();
			e.stopPropagation();
			return false;
		});
	}-*/;


	@Override
	public void draw3DRect(int x, int y, int width, int height, boolean raised) {
		App.debug("draw3DRect: implementation needed"); 

	}

	
	@Override
	public void fill3DRect(int x, int y, int width, int height, boolean raised) {
		App.debug("fill3DRect: implementation needed"); 
	}

	//tmp
	/**<p>
	 * Draws a shape.
	 * </p>
	 * 
	 * @param shape
	 */
	public void draw(Shape shape) {
		if (shape == null) {
			App.error("Error in EuclidianView.draw");
			return;
		}
		doDrawShape(shape, true);
		context.stroke();	
	}

	protected void doDrawShape(Shape shape, boolean enableDashEmulation) {
		context.beginPath();
		PathIterator it = shape.getPathIterator(null);
		double[] coords = new double[6];
		
		// see #1718
		//boolean enableDashEmulation = true;//nativeDashUsed || App.isFullAppGui(); 
		
		while (!it.isDone()) {
			int cu = it.currentSegment(coords);
			switch (cu) {
			case PathIterator.SEG_MOVETO:
				context.moveTo(coords[0], coords[1]);
				if (enableDashEmulation) setLastCoords(coords[0], coords[1]);
				break;
			case PathIterator.SEG_LINETO:
				if (dash_array == null || !enableDashEmulation) {
					context.lineTo(coords[0], coords[1]);
				} else {
					if (nativeDashUsed) {
						context.lineTo(coords[0], coords[1]);
					} else {
						drawDashedLine(pathLastX,pathLastY,coords[0], coords[1],jsarrn, context);
					}
				}
				setLastCoords(coords[0], coords[1]);
				break;
			case PathIterator.SEG_CUBICTO: 
				context.bezierCurveTo(coords[0], coords[1], coords[2], coords[3], coords[4], coords[5]);
				if (enableDashEmulation) setLastCoords(coords[4], coords[5]);
				break;
			case PathIterator.SEG_QUADTO:			
				context.quadraticCurveTo(coords[0], coords[1], coords[2], coords[3]);
				if (enableDashEmulation) setLastCoords(coords[2], coords[3]);
				break;
			case PathIterator.SEG_CLOSE:
				context.closePath();
			default:
				break;
			}
			it.next();
		}
		//this.closePath();
	}

	private double pathLastX;
	private double pathLastY;
	
	private void setLastCoords(double x, double y) {
		pathLastX = x;
		pathLastY = y;
	}


	@Override
	public boolean drawImage(GImage img, GAffineTransform xform, GImageObserver obs) {
		App.debug("drawImage: implementation needed for beauty"); // TODO Auto-generated
		return false;
	}

	//
	@Override
	public void drawImage(geogebra.common.awt.GBufferedImage img, GBufferedImageOp op, int x,
			int y) {
		BufferedImage bi = GBufferedImageW.getGawtImage(img);
		if(bi==null)
			return;
		try{
			context.drawImage(bi.getImageElement(), x, y);
		} catch (Exception e){
			App.error("error in context.drawImage method");
		}
	}

	
	@Override
	public void drawRenderedImage(GRenderedImage img, GAffineTransform xform) {
		App.debug("drawRenderedImage: implementation needed"); // TODO Auto-generated

	}

	
	@Override
	public void drawRenderableImage(GRenderableImage img, GAffineTransform xform) {
		App.debug("drawRenderableImage: implementation needed"); // TODO Auto-generated

	}

	
	@Override
	public void drawString(String str, int x, int y) {
		context.fillText(str, x, y);
	}

	
	@Override
	public void drawString(String str, float x, float y) {
		context.fillText(str, x, y);
	}

	
	@Override
	public void drawString(GAttributedCharacterIterator iterator, int x, int y) {
		App.debug("drawString: implementation needed"); // TODO Auto-generated

	}

	
	@Override
	public void drawString(GAttributedCharacterIterator iterator, float x,
			float y) {
		App.debug("drawString: implementation needed 2"); // TODO Auto-generated

	}

	
	@Override
	public void drawGlyphVector(GGlyphVector g, float x, float y) {
		App.debug("drawGlyphVector: implementation needed"); // TODO Auto-generated

	}


	/**
	 * @param shape
	 */
	public void fill(Shape shape) {
		if (shape == null) {
			App.printStacktrace("Error in EuclidianView.fill");
			return;
		}
		doDrawShape(shape, false);

		/*
		App.debug((shape instanceof GeneralPath)+"");
		App.debug((shape instanceof GeneralPathClipped)+"");
		App.debug((shape.getClass().toString())+"");
		*/
		
		// default winding rule changed for ggb50 (for Polygons) #3983
		if (shape instanceof geogebra.html5.openjdk.awt.geom.GeneralPath) {
			geogebra.html5.openjdk.awt.geom.GeneralPath gp = (geogebra.html5.openjdk.awt.geom.GeneralPath)shape;
			int rule = gp.getWindingRule();
			if (rule == geogebra.html5.openjdk.awt.geom.GeneralPath.WIND_EVEN_ODD) {
				context.fill("evenodd");		
			} else {
				// context.fill("") differs between browsers
				context.fill();
			}
		} else {
			context.fill();
		}
	}


	@Override
	public GGraphicsConfiguration getDeviceConfiguration() {
		App.debug("getDeviceConfiguration: implementation needed"); // TODO Auto-generated
		return null;
	}

	
	@Override
	public void setComposite(GComposite comp) {
		context.setGlobalAlpha(((GAlphaCompositeW)comp).getAlpha());
		
//		if (comp != null) {
//			float alpha= ((AlphaComposite) comp).getAlpha();
//			if (alpha >= 0f && alpha < 1f) {
//				context.setGlobalAlpha(alpha);
//			}
//			context.setGlobalAlpha(0.5d);
//			context.restore();
//		}
	}

	@Override
	public void setPaint(final GPaint paint) {
		if (paint instanceof GColor) {
			setColor((GColor)paint);
		} else if (paint instanceof GGradientPaintW) {
			context.setFillStyle(((GGradientPaintW)paint).getGradient(context));
			currentPaint = new GGradientPaintW((GGradientPaintW)paint);
			color = null;
		} else if (paint instanceof GTexturePaintW) {
			try {//bug in Firefox
				//https://groups.google.com/forum/#!msg/craftyjs/3qRwn_cW1gs/DdPTaCD81ikJ
				//NS_ERROR_NOT_AVAILABLE: Component is not available
				//https://bugzilla.mozilla.org/show_bug.cgi?id=574330
				if (((GTexturePaintW)paint).getImg().getPropertyBoolean("complete")) {
					currentPaint = new GTexturePaintW((GTexturePaintW)paint);
					CanvasPattern ptr = context.createPattern(((GTexturePaintW)paint).getImg(), Repetition.REPEAT);
					context.setFillStyle(ptr);
					color = null;
				} else {
					ImageWrapper.nativeon(((GTexturePaintW)paint).getImg(),
						"load",
						new ImageLoadCallback() {
							public void onLoad() {
								currentPaint = new GTexturePaintW((GTexturePaintW)paint);
								CanvasPattern ptr = context.createPattern(((GTexturePaintW)paint).getImg(), Repetition.REPEAT);
								context.setFillStyle(ptr);
								color = null;
							}
						}
					);
				}
			} catch (Throwable e) {
				App.error(e.getMessage());
			}
		} else {
			App.error("unknown paint type");
		}
	}

	@Override
	public void setStroke(GBasicStroke stroke) {
		if (stroke != null) {
			context.setLineWidth(((GBasicStrokeW)stroke).getLineWidth());
			context.setLineCap(((GBasicStrokeW)stroke).getEndCapString());
			context.setLineJoin(((GBasicStrokeW)stroke).getLineJoinString());

			float [] dasharr = ((GBasicStrokeW)stroke).getDashArray();
			if (dasharr != null) {
				jsarrn = JavaScriptObject.createArray().cast();
				jsarrn.setLength(dasharr.length);
				for (int i = 0; i < dasharr.length; i++)
					jsarrn.set(i, dasharr[i]);
				setStrokeDash( context, jsarrn );
			} else {
				setStrokeDash( context, null );
			}
			dash_array = dasharr;
		}
	}
	
	private boolean nativeDashUsed = false;

	public native void setStrokeDash(Context2d ctx, JsArrayNumber dasharray) /*-{
		if (dasharray === undefined || dasharray === null) {
			dasharray = [];
		}
		
		if (typeof ctx.setLineDash === 'function') {
			ctx.setLineDash(dasharray);
			this.@geogebra.html5.awt.GGraphics2DW::nativeDashUsed = true;
		} else if (typeof ctx.mozDash !== 'undefined') {
			ctx.mozDash = dasharray;
			this.@geogebra.html5.awt.GGraphics2DW::nativeDashUsed = true;			
		} else if (typeof ctx.webkitLineDash !== 'undefined') {
			ctx.webkitLineDash = dasharray;
			this.@geogebra.html5.awt.GGraphics2DW::nativeDashUsed = true;
		}
		
	}-*/;


	@Override
	public void setRenderingHint(GKey hintKey, Object hintValue) {
		//

	}

	
	@Override
	public Object getRenderingHint(GKey hintKey) {
		//
		return null;
	}

	
	@Override
	public void setRenderingHints(Map<?, ?> hints) {
		//

	}

	
	@Override
	public void addRenderingHints(Map<?, ?> hints) {
		//

	}

	
	@Override
	public GRenderingHints getRenderingHints() {
		//
		return null;
	}

	
	@Override
	public void translate(int x, int y) {
		context.translate(x, y);
		savedTransform.translate(x, y);
	}

	
	@Override
	public void translate(double tx, double ty) {
		context.translate(tx, ty);
		savedTransform.translate(tx, ty);

	}

	
	@Override
	public void rotate(double theta) {
		context.rotate(theta);
		savedTransform.concatenate(
				new GAffineTransformW(
						Math.cos(theta), Math.sin(theta), -Math.sin(theta), Math.cos(theta), 0, 0));

	}

	
	@Override
	public void rotate(double theta, double x, double y) {
		context.translate(x, y);
		context.rotate(theta);
		context.translate(-x, -y);
		savedTransform.concatenate(
				new GAffineTransformW(
						Math.cos(theta), Math.sin(theta), -Math.sin(theta), Math.cos(theta), x, y));
	}

	
	@Override
	public void scale(double sx, double sy) {
		context.scale(sx, sy);
		savedTransform.scale(sx, sy);
	}

	
	@Override
	public void shear(double shx, double shy) {
		transform(new GAffineTransformW(
			1, shy, shx, 1, 0, 0
		));
	}


	@Override
	public void transform(GAffineTransform Tx) {
		context.transform(Tx.getScaleX(), Tx.getShearY(),
				Tx.getShearX(), Tx.getScaleY(),
				((GAffineTransformW)Tx).getTranslateX(),
				((GAffineTransformW)Tx).getTranslateY());
		savedTransform.concatenate(Tx);
	}

	
	@Override
	public void setTransform(GAffineTransform Tx) {
		context.setTransform(Tx.getScaleX(), Tx.getShearY(),
		Tx.getShearX(), Tx.getScaleY(),
		((GAffineTransformW)Tx).getTranslateX(),
		((GAffineTransformW)Tx).getTranslateY());
		savedTransform = Tx;

	}

	
	@Override
	public GAffineTransform getTransform() {
		GAffineTransform ret = new GAffineTransformW();
		ret.setTransform(savedTransform);
		return ret;
	}
	

	@Override
	public GPaint getPaint() {
		return currentPaint;
		/* The other possible solution would be:

		// this could be an array as well, according to the documentation, so more difficult
		FillStrokeStyle fs = context.getFillStyle();
		Paint ret;
		if (fs.getType() == FillStrokeStyle.TYPE_CSSCOLOR) {
			// it is difficult to make a color out of csscolor
			ret = new Color((CssColor)fs);
		} else if (fs.getType() == FillStrokeStyle.TYPE_GRADIENT) {
			
		} else if (fs.getType() == FillStrokeStyle.TYPE_PATTERN) {
			
		}
		*/
	}

	
	@Override
	public GComposite getComposite() {
		return new GAlphaCompositeW(3, (float) context.getGlobalAlpha());
	
//		context.save();
//		//just to not return null;
//		return new AlphaComposite(0, 0) {
//		};
	}


	@Override
	public void setBackground(GColor color) {
		// This method only affects Graphics2D.clearRect (if there will be present)
		// and getBackground calls - currently Drawable.drawLabel
		this.bgColor = new GColorW((GColorW)color);
	}


	@Override
	public GColor getBackground() {
		return bgColor;
	}


	@Override
	public GBasicStroke getStroke() {

		return new GBasicStrokeW(
			(float) context.getLineWidth(), 
			GBasicStrokeW.getCap(context.getLineCap()),
			GBasicStrokeW.getJoin(context.getLineJoin()),
			0,
			dash_array,
			0
		);
	}

	public void clip(Shape shape2) {

		if (shape2 == null) {
			// for simple clip, no null is allowed
			clipShape = null;
			App.error("Error in Graphics2D.setClip");
			return;
		}
		clipShape = new GenericShape(shape2);
		doDrawShape(shape2, false);
		context.save();
		context.clip();
	}

	
	@Override
	public GFontRenderContext getFontRenderContext() {
		return new GFontRenderContextW(context);
	}

	
	@Override
	public GColor getColor() {
		return color;
	}

	
	@Override
	public GFontW getFont() {
		return currentFont;
	}
	
	public void setCoordinateSpaceSize(int width, int height) {
		canvas.setCoordinateSpaceWidth(width * devicePixelRatio);
		canvas.setCoordinateSpaceHeight(height * devicePixelRatio);
		scale(devicePixelRatio, devicePixelRatio);
		setWidth(width);
		setHeight(height);
		this.updateCanvasColor();
	}

	

	public int getOffsetWidth() {
		return canvas.getOffsetWidth();
	}

	public int getOffsetHeight() {
	 return canvas.getOffsetHeight();
	}

	public int getCoordinateSpaceWidth() {
	 return canvas.getCoordinateSpaceWidth();
	}

	public int getCoordinateSpaceHeight() {
		return canvas.getCoordinateSpaceHeight();
	}

	public int getAbsoluteTop() {
		return canvas.getAbsoluteTop();
	}

	public int getAbsoluteLeft() {
		return canvas.getAbsoluteLeft(); 
	}

	
	@Override
	public void setFont(geogebra.common.awt.GFont font) {
		if(font instanceof GFontW){
			currentFont=(GFontW)font;
			//TODO: pass other parameters here as well
			try {
				context.setFont(currentFont.getFullFontString());
			}
			catch(Throwable t) {
				App.error("problem setting font: "+currentFont.getFullFontString());
			}
		}

	}

	
	@Override
	public void setColor(GColor fillColor) {
		//checking for the same color here speeds up axis drawing by 25%
		if(fillColor != null && fillColor.equals(color)){
			return;
		}		
		this.color = fillColor;
		updateCanvasColor();
		this.currentPaint = new GColorW((GColorW)fillColor);
	}

	private void updateCanvasColor() {
		if(color == null){
			return;
		}
		String colorStr = "rgba("+color.getRed()+","+color.getGreen()+","+color.getBlue()+","+(color.getAlpha()/255d)+")";
		context.setStrokeStyle(colorStr);
		context.setFillStyle(colorStr);
		
	}


	@Override
	public void clip(geogebra.common.awt.GShape shape) {
		if (shape == null) {
			App.error("Error in Graphics2D.clip");
			return;
		}
		clipShape = shape;
		Shape shape2 = GenericShape.getGawtShape(shape);
		if (shape2 == null) {
			App.error("Error in Graphics2D.clip");
			return;
		}
		doDrawShape(shape2, false);
		context.save();
		context.clip();
	}

	public void drawGraphics(GGraphics2DW gother, int x, int y,
			GBufferedImageOp op) {

		if (gother==null)
			return;

		context.drawImage(gother.getCanvas().getCanvasElement(), x, y);
	}

	@Override
	public void fillRect(int x, int y, int w, int h) {
		context.fillRect(x, y, w, h);
	}

	@Override
	public void clearRect(int x, int y, int w, int h) {
		context.save();
		context.setTransform(1,0,0,1,0,0);
		context.clearRect(x, y, w, h);
		context.restore();
	}

	@Override
	public void drawLine(int x1, int y1, int x2, int y2) {

		/* TODO: there is some differences between the result of
		 * geogebra.awt.Graphics.drawLine(...) function.
		 * Here is an attempt to make longer the vertical and horizontal lines:
		 
		int x_1 = Math.min(x1,x2);
		int y_1 = Math.min(y1,y2);
		int x_2 = Math.max(x1,x2);
		int y_2 = Math.max(y1,y2);
		
		if(x1==x2){
			y_1--;
			y_2++;
		} else if(y1==y2){
			x_1--;
			x_2++;
		}
		 	
		context.beginPath();
		context.moveTo(x_1, y_1);
		context.lineTo(x_2, y_2);
		context.closePath();
		context.stroke();
*/
		context.beginPath();
		context.moveTo(x1, y1);
		context.lineTo(x2, y2);
		context.closePath();
		context.stroke();
		
	}


	@Override
	public void setClip(geogebra.common.awt.GShape shape) {
		clipShape = shape;
		if (shape == null) {
			// this may be an intentional call to restore the context
			context.restore();
			return;
		}
		Shape shape2 = GenericShape.getGawtShape(shape);
		if (shape2 == null) {
			App.error("Error in Graphics2D.setClip");
			return;
		}
		doDrawShape(shape2, false);
		if (clipShape != null) {
			// we should call this only if no clip was set or just after another clip to overwrite
			// in this case we don't want to double-clip something so let's restore the context
			context.restore();
		}
		context.save();
		context.clip();
	}


	@Override
	public void draw(geogebra.common.awt.GShape s) {
		draw(GenericShape.getGawtShape(s));
	}


	@Override
	public void fill(geogebra.common.awt.GShape s) {
		fill(GenericShape.getGawtShape(s));
	}

	@Override
	public geogebra.common.awt.GShape getClip() {
		return clipShape;
	}

	@Override
	public void drawRect(int x, int y, int width, int height) {
	 context.rect(x, y, width, height);
	 context.stroke();
	 
	}

	@Override
	public void setClip(int x, int y, int width, int height) {

		float [] dash_array_save = dash_array;
		dash_array = null;
		geogebra.common.awt.GShape sh = AwtFactory.prototype.newRectangle(x, y, width, height);
		setClip(sh);
		dash_array = dash_array_save;

		/* 
		 * alternative: makes clipping bad, see #3212
		 *
		//context.save();
		context.beginPath();
		context.moveTo(x, y);
		context.lineTo(x + width, y);
		context.lineTo(x + width, y + height);
		context.lineTo(x , y + height);
		context.lineTo(x , y);
		//context.closePath();
		context.clip();
		*/
	}

	public void setWidth(int w) {
		canvas.setWidth(w +"px");
	}


	public void setHeight(int h) {
		canvas.setHeight(h +"px");
	}


	public void setPreferredSize(GDimension preferredSize) {
		setWidth(preferredSize.getWidth());
		setHeight(preferredSize.getHeight());

		// do not use getOffsetWidth here,
		// as it is prepared by the browser and not yet ready...
		// if preferredSize can be negative, have a check for it instead
		setCoordinateSpaceSize(
				((preferredSize.getWidth() >= 0) ? preferredSize.getWidth() : 0) * devicePixelRatio ,
 				((preferredSize.getHeight() >= 0) ? preferredSize.getHeight() : 0) * devicePixelRatio);
	}

	public Canvas getCanvas() {
		return this.canvas;
	}


	@Override
	public void drawRoundRect(int x, int y, int width, int height,
			int arcWidth, int arcHeight) {
		roundRect(x,y,width,height,arcHeight);
		context.stroke();
		
		
	}
	
	/**
	 * Using arc, because arc to has buggy implementation in some browsers 
	 * @param x
	 * @param y
	 * @param w
	 * @param h
	 * @param r
	 */
	private void roundRect(int x,int y,int w,int h,int r){
		context.beginPath();
		int ey = y+h;
		int ex = x+w;
		float r2d = (float)Math.PI/180;
		context.moveTo(x+r,y);
		context.lineTo(ex-r,y);
		context.arc(ex-r,y+r,r,r2d*270,r2d*360,false);
		context.lineTo(ex,ey-r);
		context.arc(ex-r,ey-r,r,r2d*0,r2d*90,false);
		context.lineTo(x+r,ey);
		context.arc(x+r,ey-r,r,r2d*90,r2d*180,false);
		context.lineTo(x,y+r);
		context.arc(x+r,y+r,r,r2d*180,r2d*270,false);
		
		context.closePath();
	}

	@Override
	public void fillRoundRect(int x, int y, int width, int height,
			int arcWidth, int arcHeight) {
		roundRect(x,y,width,height,arcHeight);
		context.fill("evenodd");
		
	}


	public void fillPolygon(Polygon p) {
	 fill(p);
	}
	
	private native void drawDashedLine(double fromX, double fromY, double toX, double toY, JsArrayNumber pattern,Context2d ctx) /*-{
		// Our growth rate for our line can be one of the following:
		// (+,+), (+,-), (-,+), (-,-)
		// Because of this, our algorithm needs to understand if the x-coord and
		// y-coord should be getting smaller or larger and properly cap the values
		// based on (x,y).
		
		// make sure we don't get an infinite loop drawing eg
		//y = -7.85046229341888E-17x
		var EPSILON = 0.00000001;
		
		var lt = function (a, b) { return a <= b + EPSILON; };
		var gt = function (a, b) { return a >= b - EPSILON; };
		var capmin = function (a, b) { return $wnd.Math.min(a, b); };
		var capmax = function (a, b) { return $wnd.Math.max(a, b); };
		
		var checkX = { thereYet: gt, cap: capmin };
		var checkY = { thereYet: gt, cap: capmin };
		
		if (fromY - toY > 0) {
			checkY.thereYet = lt;
			checkY.cap = capmax;
		}
		if (fromX - toX > 0) {
			checkX.thereYet = lt;
			checkX.cap = capmax;
		}
		
		//ctx.moveTo(fromX, fromY);
		var offsetX = fromX;
		var offsetY = fromY;
		var idx = 0, dash = true;
		while (!(checkX.thereYet(offsetX, toX) && checkY.thereYet(offsetY, toY))) {
			var ang = $wnd.Math.atan2(toY - fromY, toX - fromX);
			var len = pattern[idx];
		
			offsetX = checkX.cap(toX, offsetX + ($wnd.Math.cos(ang) * len));
			offsetY = checkY.cap(toY, offsetY + ($wnd.Math.sin(ang) * len));
		
			if (dash) ctx.lineTo(offsetX, offsetY);
			else ctx.moveTo(offsetX, offsetY);
		
			idx = (idx + 1) % pattern.length;
			dash = !dash;
		}
		
		
	}-*/;
	
	public ImageData getImageData(int x, int y, int width, int height) {
		return context.getImageData(x, y, width, height);
	}
	
	/**
	 * @param data Imagedata to put on the canvas
	 */
	public void putImageData(ImageData data, double x, double y) {
		context.putImageData(data, x, y);
	}


	@Override
	public void drawImage(GImage img, int x, int y) {
		App.debug("drawImage: implementation needed");
	}


	@Override
	public void drawImage(GBufferedImage img, int x, int y) {
		drawImage(img, null, x, y);
	}


	@Override
	public void setAntialiasing() {
		// not needed
	}


	@Override
	public void setTransparent() {
		setComposite(GAlphaCompositeW.Src);		
	}


	public void fillWith(GColor color) {
		this.setColor(color);
		this.fillRect(0, 0, canvas.getCoordinateSpaceWidth(), canvas.getCoordinateSpaceHeight());
	}
	private boolean lastDebugOk = false;
	private boolean lastDebugException = false;
	public void debug() {
		// TODO Auto-generated method stub
		String physical = context.getFillStyle().toString().toUpperCase();
		String logical = "null";
		if(color != null){
			logical = color.getAlpha()< 255 ?"RGBA("+color.getRed()+", "+color.getGreen()+", "+color.getBlue()+", 0."+(int)(1000000 * color.getAlpha()/255d)+")": "#" + StringUtil.toHexString(color).toUpperCase();
		}
		if(color == null && physical.contains("OBJ")){
			System.out.println(hashCode()+": not colors");
			lastDebugOk = false;
			lastDebugException = false;
		}
		else if(!logical.equals(physical)){
			if(!lastDebugException){
				App.printStacktrace(hashCode()+": "+logical.replace(".0", "") + " / " + physical.replace(".0", "")) ;
			}
			lastDebugOk = false;
			lastDebugException = true;
		}else if(!lastDebugOk){
			System.out.println(hashCode()+": ok");
			lastDebugOk = true;
			lastDebugException = false;
		}
	}

	public int getScale() {
	    return devicePixelRatio;
    }
}
