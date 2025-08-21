package fr.frinn.custommachinery.client.screen.creation.gui;

import fr.frinn.custommachinery.client.screen.creation.gui.GuiEditorWidget.Change;
import fr.frinn.custommachinery.client.screen.creation.gui.GuiEditorWidget.GroupWidgetChange;
import fr.frinn.custommachinery.client.screen.creation.gui.GuiEditorWidget.SingleWidgetChange;
import fr.frinn.custommachinery.client.screen.creation.gui.GuiEditorWidget.WidgetEditorWidget;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

public class WidgetCompactor {

    public static void compact(List<WidgetEditorWidget<?>> widgets, Consumer<WidgetEditorWidget<?>> change, int tolerance, int padding) {
        // 1. Sort by row (y), then by column (x)
        widgets.sort(Comparator.<WidgetEditorWidget<?>>comparingInt(WidgetEditorWidget::getY).thenComparingInt(WidgetEditorWidget::getX));

        // 2. Find origin (object closest to 0,0)
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        for(WidgetEditorWidget<?> widget : widgets) {
            minX = Math.min(minX, widget.getX());
            minY = Math.min(minY, widget.getY());
        }

        // 3. Detect unique X (columns) and Y (rows) with tolerance
        List<Integer> columns = clusterValues(extract(widgets, true), tolerance);
        List<Integer> rows = clusterValues(extract(widgets, false), tolerance);

        int numCols = columns.size();
        int numRows = rows.size();

        // 4. Map objects to grid cells
        WidgetEditorWidget<?>[][] grid = new WidgetEditorWidget[numRows][numCols];
        for(WidgetEditorWidget<?> widget : widgets) {
            int row = nearestIndex(rows, widget.getY());
            int col = nearestIndex(columns, widget.getX());
            grid[row][col] = widget;
        }

        // 5. Compute max width per column & max height per row
        int[] colWidths = new int[numCols];
        int[] rowHeights = new int[numRows];

        for(int r = 0; r < numRows; r++) {
            for(int c = 0; c < numCols; c++) {
                WidgetEditorWidget<?> widget = grid[r][c];
                if(widget != null) {
                    colWidths[c] = Math.max(colWidths[c], widget.getWidth());
                    rowHeights[r] = Math.max(rowHeights[r], widget.getHeight());
                }
            }
        }

        // 6. Compute prefix sums for positions
        int[] xOffsets = new int[numCols];
        int[] yOffsets = new int[numRows];

        for(int c = 1; c < numCols; c++)
            xOffsets[c] = xOffsets[c - 1] + colWidths[c - 1] + padding;

        for(int r = 1; r < numRows; r++)
            yOffsets[r] = yOffsets[r - 1] + rowHeights[r - 1] + padding;

        // 7. Assign new positions
        for(int r = 0; r < numRows; r++) {
            for(int c = 0; c < numCols; c++) {
                WidgetEditorWidget<?> widget = grid[r][c];
                if(widget != null) {
                    change.accept(widget);
                    widget.setPosition(minX + xOffsets[c], minY + yOffsets[r]);
                }
            }
        }
    }

    private static List<Integer> extract(List<WidgetEditorWidget<?>> widgets, boolean isX) {
        List<Integer> values = new ArrayList<>();
        for(WidgetEditorWidget<?> widget : widgets) values.add(isX ? widget.getX() : widget.getY());
        Collections.sort(values);
        return values;
    }

    private static List<Integer> clusterValues(List<Integer> values, int tolerance) {
        List<Integer> clustered = new ArrayList<>();
        for (int v : values) {
            if (clustered.isEmpty() || Math.abs(clustered.getLast() - v) > tolerance) {
                clustered.add(v);
            }
        }
        return clustered;
    }

    private static int nearestIndex(List<Integer> values, int val) {
        int bestIdx = 0;
        int bestDist = Integer.MAX_VALUE;
        for (int i = 0; i < values.size(); i++) {
            int dist = Math.abs(values.get(i) - val);
            if (dist < bestDist) {
                bestDist = dist;
                bestIdx = i;
            }
        }
        return bestIdx;
    }
}
