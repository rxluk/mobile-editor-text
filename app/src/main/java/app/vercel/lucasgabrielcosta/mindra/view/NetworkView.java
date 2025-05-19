package app.vercel.lucasgabrielcosta.mindra.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import app.vercel.lucasgabrielcosta.mindra.R;
import app.vercel.lucasgabrielcosta.mindra.model.Note;

public class NetworkView extends View {
    private static final String TAG = "NetworkView";

    public interface OnNodeSelectedListener {
        void onNodeSelected(Note note);
    }

    private static final float NODE_RADIUS = 60f;
    private static final float NODE_SPACING = 250f;
    private static final int TEXT_SIZE = 28;
    private static final int MAX_TITLE_LENGTH = 10;

    private List<Note> notes = new ArrayList<>();
    private Map<String, List<Integer>> connectionsMap = new HashMap<>();
    private List<NodePosition> nodePositions = new ArrayList<>();
    private OnNodeSelectedListener nodeSelectedListener;

    private float translateX;
    private float translateY;
    private float scaleFactor = 1.0f;
    private int selectedNodeIndex = -1;
    private boolean positionsInitialized = false;

    private ScaleGestureDetector scaleGestureDetector;
    private float lastTouchX;
    private float lastTouchY;
    private boolean isDragging = false;

    private long lastClickTime = 0;
    private static final long DOUBLE_CLICK_TIME_DELTA = 300; // milissegundos

    private Paint nodePaint;
    private Paint selectedNodePaint;
    private Paint textPaint;
    private Paint linePaint;
    private Paint arrowPaint;

    private static class NodePosition {
        float x, y;
        Note note;

        NodePosition(float x, float y, Note note) {
            this.x = x;
            this.y = y;
            this.note = note;
        }
    }

    public NetworkView(Context context) {
        super(context);
        init();
    }

    public NetworkView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public NetworkView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        // Inicializa o detector de escala
        scaleGestureDetector = new ScaleGestureDetector(getContext(), new ScaleListener());

        // Configura os objetos Paint
        nodePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        nodePaint.setColor(getResources().getColor(R.color.primary));
        nodePaint.setStyle(Paint.Style.FILL);

        selectedNodePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        selectedNodePaint.setColor(getResources().getColor(R.color.primary_dark));
        selectedNodePaint.setStyle(Paint.Style.FILL);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(TEXT_SIZE);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTypeface(Typeface.DEFAULT_BOLD);

        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setColor(getResources().getColor(R.color.primary_light));
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(3f);

        arrowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        arrowPaint.setColor(getResources().getColor(R.color.primary_light));
        arrowPaint.setStyle(Paint.Style.FILL);
    }

    public void setOnNodeSelectedListener(OnNodeSelectedListener listener) {
        this.nodeSelectedListener = listener;
    }

    public void setNotes(List<Note> notes) {
        this.notes = notes;
        generateConnectionsMap();
        positionsInitialized = false;
        requestLayout();
        invalidate();
    }

    private void generateConnectionsMap() {
        connectionsMap.clear();

        // Mapeia todas as conexões possíveis
        for (int i = 0; i < notes.size(); i++) {
            List<String> connections = notes.get(i).getConnections();
            if (connections != null) {
                for (String connection : connections) {
                    // Adiciona a conexão ao mapa
                    if (!connectionsMap.containsKey(connection)) {
                        connectionsMap.put(connection, new ArrayList<>());
                    }
                    connectionsMap.get(connection).add(i);
                }
            }
        }
    }

    private void generateNodePositions() {
        nodePositions.clear();
        if (notes.isEmpty()) return;

        Log.d(TAG, "Gerando posições para " + notes.size() + " nós");

        Random random = new Random(System.currentTimeMillis());
        int width = getWidth();
        int height = getHeight();

        translateX = width / 2f;
        translateY = height / 2f;

        if (width <= 0) width = 1000;
        if (height <= 0) height = 1000;

        float centerX = 0;
        float centerY = 0;

        if (!notes.isEmpty()) {
            NodePosition firstNode = new NodePosition(centerX, centerY, notes.get(0));
            nodePositions.add(firstNode);
        }

        int nodeCount = notes.size();
        for (int i = 1; i < nodeCount; i++) {
            float angle = (float) (2 * Math.PI * i / nodeCount);
            float distance = NODE_SPACING * Math.min(nodeCount * 0.15f, 2.0f);

            angle += random.nextFloat() * 0.2f - 0.1f;
            distance += random.nextFloat() * (NODE_SPACING * 0.3f);

            float x = centerX + distance * (float) Math.cos(angle);
            float y = centerY + distance * (float) Math.sin(angle);

            nodePositions.add(new NodePosition(x, y, notes.get(i)));
        }

        positionsInitialized = true;
        Log.d(TAG, "Posições geradas: " + nodePositions.size());
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        Log.d(TAG, "onSizeChanged: w=" + w + ", h=" + h);

        translateX = w / 2f;
        translateY = h / 2f;

        if (!positionsInitialized || nodePositions.isEmpty()) {
            generateNodePositions();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (!positionsInitialized && getWidth() > 0 && getHeight() > 0) {
            generateNodePositions();
        }

        if (nodePositions.isEmpty()) {
            return;
        }

        canvas.save();
        canvas.translate(translateX, translateY);
        canvas.scale(scaleFactor, scaleFactor);

        drawConnections(canvas);

        drawNodes(canvas);

        canvas.restore();
    }

    private void drawConnections(Canvas canvas) {
        for (int i = 0; i < nodePositions.size(); i++) {
            NodePosition sourceNode = nodePositions.get(i);
            Note sourceNote = sourceNode.note;

            if (sourceNote.getConnections() == null || sourceNote.getConnections().isEmpty()) {
                continue;
            }

            for (String connection : sourceNote.getConnections()) {
                List<Integer> connectedNodeIndices = connectionsMap.get(connection);
                if (connectedNodeIndices == null) continue;

                for (Integer targetIndex : connectedNodeIndices) {
                    if (targetIndex != i && targetIndex < nodePositions.size()) {
                        NodePosition targetNode = nodePositions.get(targetIndex);
                        drawConnection(canvas, sourceNode, targetNode);
                    }
                }
            }
        }
    }

    private void drawConnection(Canvas canvas, NodePosition source, NodePosition target) {
        float sourceX = source.x;
        float sourceY = source.y;
        float targetX = target.x;
        float targetY = target.y;

        double angle = Math.atan2(targetY - sourceY, targetX - sourceX);

        float startX = (float) (sourceX + NODE_RADIUS * Math.cos(angle));
        float startY = (float) (sourceY + NODE_RADIUS * Math.sin(angle));
        float endX = (float) (targetX - NODE_RADIUS * Math.cos(angle));
        float endY = (float) (targetY - NODE_RADIUS * Math.sin(angle));

        canvas.drawLine(startX, startY, endX, endY, linePaint);

        drawArrow(canvas, endX, endY, angle);
    }

    private void drawArrow(Canvas canvas, float x, float y, double angle) {
        float arrowSize = 15f;

        Path path = new Path();
        path.moveTo(x, y);
        path.lineTo((float) (x - arrowSize * Math.cos(angle - Math.PI/6)),
                (float) (y - arrowSize * Math.sin(angle - Math.PI/6)));
        path.lineTo((float) (x - arrowSize * Math.cos(angle + Math.PI/6)),
                (float) (y - arrowSize * Math.sin(angle + Math.PI/6)));
        path.close();

        canvas.drawPath(path, arrowPaint);
    }

    private void drawNodes(Canvas canvas) {
        for (int i = 0; i < nodePositions.size(); i++) {
            NodePosition node = nodePositions.get(i);

            Paint currentPaint = (i == selectedNodeIndex) ? selectedNodePaint : nodePaint;

            canvas.drawCircle(node.x, node.y, NODE_RADIUS, currentPaint);

            String title = node.note.getTitle();
            if (title.length() > MAX_TITLE_LENGTH) {
                title = title.substring(0, MAX_TITLE_LENGTH) + "...";
            }

            float textHeight = textPaint.descent() - textPaint.ascent();
            float textOffset = textHeight / 2 - textPaint.descent();

            canvas.drawText(title, node.x, node.y + textOffset, textPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean scaleHandled = scaleGestureDetector.onTouchEvent(event);

        if (scaleGestureDetector.isInProgress()) {
            return true;
        }

        final int action = event.getAction();
        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                lastTouchX = event.getX();
                lastTouchY = event.getY();

                int nodeIndex = findNodeAtPosition(event.getX(), event.getY());

                if (nodeIndex >= 0) {
                    long clickTime = System.currentTimeMillis();
                    if (clickTime - lastClickTime < DOUBLE_CLICK_TIME_DELTA) {
                        if (nodeSelectedListener != null) {
                            nodeSelectedListener.onNodeSelected(nodePositions.get(nodeIndex).note);
                        }
                    } else {
                        selectedNodeIndex = nodeIndex;
                        invalidate();
                    }
                    lastClickTime = clickTime;
                    return true;
                } else {
                    isDragging = true;
                    return true;
                }

            case MotionEvent.ACTION_MOVE:
                if (isDragging) {
                    float dx = event.getX() - lastTouchX;
                    float dy = event.getY() - lastTouchY;

                    translateX += dx;
                    translateY += dy;

                    lastTouchX = event.getX();
                    lastTouchY = event.getY();

                    invalidate();
                    return true;
                }
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                isDragging = false;
                return true;
        }

        return super.onTouchEvent(event);
    }

    private int findNodeAtPosition(float x, float y) {
        float graphX = (x - translateX) / scaleFactor;
        float graphY = (y - translateY) / scaleFactor;

        for (int i = 0; i < nodePositions.size(); i++) {
            NodePosition node = nodePositions.get(i);
            float distance = (float) Math.sqrt(Math.pow(graphX - node.x, 2) + Math.pow(graphY - node.y, 2));
            if (distance <= NODE_RADIUS) {
                return i;
            }
        }

        return -1;
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            scaleFactor *= detector.getScaleFactor();

            scaleFactor = Math.max(0.1f, Math.min(scaleFactor, 5.0f));

            invalidate();
            return true;
        }
    }
}