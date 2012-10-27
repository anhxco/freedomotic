package it.freedomotic.util;

import it.freedomotic.app.Freedomotic;
import it.freedomotic.model.geometry.*;
import java.awt.Color;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;

/**
 *
 * @author Enrico
 */
public class TopologyUtils {

    private TopologyUtils() {
    }

    /**
     * Converts a Freedomotic Shape into an AWT Shape Remember that modifiers
     * like rotation and offset are not applyed
     *
     * @param input
     * @return
     */
    public static Shape convertToAWT(FreedomShape input) {
        if (input instanceof FreedomPolygon) {
            return convertToAWT((FreedomPolygon) input);
        } else {
            if (input instanceof FreedomEllipse) {
                return convertToAWT((FreedomEllipse) input);
            } else {
                throw new IllegalArgumentException("The kind of shape in input is unknown");
            }
        }
    }

    public static Shape convertToAWT(FreedomShape input, double xScale, double yScale) {
        if (input instanceof FreedomPolygon) {
            Shape shape = convertToAWT((FreedomPolygon) input);
            AffineTransform transform = new AffineTransform();
            transform.scale(xScale, yScale);
            Shape transformed = transform.createTransformedShape(shape);
            return transformed;
        } else {
            if (input instanceof FreedomEllipse) {
                Shape shape = convertToAWT((FreedomEllipse) input);
                AffineTransform transform = new AffineTransform();
                transform.scale(xScale, yScale);
                Shape transformed = transform.createTransformedShape(shape);
                return transformed;
            } else {
                throw new IllegalArgumentException("The kind of shape in input is unknown");
            }
        }
    }

    public static Color convertColorToAWT(FreedomColor color) {
        Color awtColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
        return awtColor;
    }

    private static Point convertToAWT(FreedomPoint fPoint) {
        return new Point(fPoint.getX(), fPoint.getY());
    }

    private static Ellipse2D convertToAWT(FreedomEllipse fEllipse) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private static Polygon convertToAWT(FreedomPolygon input) {
        FreedomPolygon polygon = (FreedomPolygon) input;
        Polygon output = new Polygon();
        for (FreedomPoint point : polygon.getPoints()) {
            output.addPoint(point.getX(), point.getY());
        }
        return output;
    }

    public static FreedomPolygon translate(FreedomShape input, int xoffset, int yoffset) {
        if (input instanceof FreedomPolygon) {
            return (translate((FreedomPolygon) input, xoffset, yoffset));
        } else {
            throw new UnsupportedOperationException("Not yet implemented");
        }
    }

    private static FreedomPolygon translate(FreedomPolygon input, int xoffset, int yoffset) {
        FreedomPolygon output = new FreedomPolygon();
        for (FreedomPoint point : input.getPoints()) {
            output.append(point.getX() + xoffset, point.getY() + yoffset);
        }
        return output;
    }

    public static FreedomPolygon rotate(FreedomPolygon input, int degrees) {
        FreedomPoint pivot = input.getPoints().get(0);//getRectangleCenter(getBoundingBox(input));
        FreedomPolygon output = new FreedomPolygon();
        for (FreedomPoint point : input.getPoints()) {
            output.append(rotatePoint(point, pivot, degrees));
        }
        return output;
    }

    private static FreedomPolygon getBoundingBox(FreedomPolygon input) {
        int minx = Integer.MAX_VALUE,
                miny = Integer.MAX_VALUE,
                maxx = Integer.MIN_VALUE,
                maxy = Integer.MIN_VALUE;
        for (FreedomPoint p : input.getPoints()) {
            minx = Math.min(minx, p.getX());
            miny = Math.min(miny, p.getY());
            maxx = Math.max(maxx, p.getX());
            maxy = Math.max(maxy, p.getY());
        }
        FreedomPolygon poly = new FreedomPolygon();
        poly.append(minx, miny);
        poly.append(maxx, miny);
        poly.append(maxx, maxy);
        poly.append(minx, maxy);
        return poly;
    }

    private static FreedomPoint getRectangleCenter(FreedomPolygon input) {
        FreedomPoint min = input.getPoints().get(0);
        FreedomPoint max = input.getPoints().get(2);
        int x = ((max.getX() - min.getX()) / 2) + min.getX();
        int y = ((max.getY() - min.getY() / 2) + min.getY());
        return new FreedomPoint(x, y);
    }

    /**
     * taken from
     * http://stackoverflow.com/questions/10533403/how-to-rotate-a-polygon-around-a-point-with-java
     * all credits to respective authors
     *
     *
     */
    private static FreedomPoint rotatePoint(FreedomPoint pt, FreedomPoint pivot, double degrees) {
        double radians = Math.toRadians(degrees);
        double cosAngle = Math.cos(radians);
        double sinAngle = Math.sin(radians);

        int x = (int) Math.round(pivot.getX() + (double) ((pt.getX() - pivot.getX()) * cosAngle - (pt.getY() - pivot.getY()) * sinAngle));
        int y = (int) Math.round(pivot.getY() + (double) ((pt.getX() - pivot.getX()) * sinAngle + (pt.getY() - pivot.getY()) * cosAngle));
        return new FreedomPoint(x, y);
    }

    /**
     * Checks if some of the edge of source polygon is inside target polygon
     * area.
     * 
     * WARNING: some use case are not covered. 
     * For example to know if to polygons instersects we check only if one or 
     * more edges of the two polygon are inside the other polygon shape. 
     * This works for our uses cases (check if a door is inside a room) but if 
     * we have two polygons like this (a "plus sign" shape) it will return false 
     * even if they "intersects" each other (because no edges are contained)
     *
     * @param source
     * @param target
     * @return
     */
    public static boolean intersects(FreedomPolygon source, FreedomPolygon target) {
        for (FreedomPoint edge : source.getPoints()) {
            if (contains(target, edge)) {
               return true;
            }
        }
        for (FreedomPoint edge : target.getPoints()) {
            if (contains(source, edge)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if a point is inside the polygon shape.
     * @param fShape
     * @param fPoint
     * @return true if inside, false if on border or outside
     */
    public static boolean contains(FreedomShape fShape, FreedomPoint fPoint) {
        ArrayList<Float> lx = new ArrayList<Float>();
        ArrayList<Float> ly = new ArrayList<Float>();
        int verticesNum = 0;
        float px = fPoint.getX();
        float py = fPoint.getY();
        if (fShape instanceof FreedomPolygon) {
            FreedomPolygon poly = (FreedomPolygon) fShape;
            verticesNum = poly.getPoints().size();
            for (int i = 0; i < poly.getPoints().size(); i++) {
                lx.add((float) poly.getPoints().get(i).getX());
                ly.add((float) poly.getPoints().get(i).getY());
            }
        }

        //TODO: converting: change this code please
        float x[] = new float[lx.size()];
        for (int i = 0; i < lx.size(); i++) {
            Float f = lx.get(i);
            x[i] = (f != null ? f : Float.NaN); // Or whatever default you want.
        }
        //TODO: converting: change this code please
        float y[] = new float[ly.size()];
        for (int i = 0; i < ly.size(); i++) {
            Float f = ly.get(i);
            y[i] = (f != null ? f : Float.NaN); // Or whatever default you want.
        }

        //algorithm starts
        if (verticesNum < 3) {
            return false;
        }

        boolean oddNodes = false;
        float x2 = x[verticesNum - 1];
        float y2 = y[verticesNum - 1];
        float x1, y1;
        for (int i = 0; i < verticesNum; x2 = x1, y2 = y1, ++i) {
            x1 = x[i];
            y1 = y[i];
            if (((y1 < py) && (y2 >= py))
                    || (y1 >= py) && (y2 < py)) {
                if ((py - y1) / (y2 - y1)
                        * (x2 - x1) < (px - x1)) {
                    oddNodes = !oddNodes;
                }
            }
        }
        return oddNodes;
    }
}