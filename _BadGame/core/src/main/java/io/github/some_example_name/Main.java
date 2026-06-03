package io.github.some_example_name;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class Main extends ApplicationAdapter {
    public static int WIDTH = 640;
    public static int HEIGHT = 480;

    private static int UI_WIDTH = 854;  // 640 is 75% of 854
    private static int UI_HEIGHT = 640; // 480 is 75% of 640
    
    // --- SCENE2D UI VARIABLES ---
    private Stage stage;
    private Pixmap pixmap;
    private Texture texture;
    
    private static Sand[][] grid; 

    private float timer = 0f;
    private float timer2 = 0f;
    private final float tickRate = 0.016f; 

    @Override
    public void create() {
        grid = new Sand[WIDTH][HEIGHT];
        pixmap = new Pixmap(WIDTH, HEIGHT, Pixmap.Format.RGBA8888);
        texture = new Texture(pixmap);

        // 1. Create a Stage with a ScreenViewport
        stage = new Stage(new FitViewport(WIDTH, HEIGHT));
        
        // CRITICAL: Tell LibGDX to send all mouse/keyboard input to the Stage!
        Gdx.input.setInputProcessor(stage);

        // 2. Create a Layout Table and make it fill the whole screen
        Table rootTable = new Table();
        rootTable.setFillParent(true);
        stage.addActor(rootTable);

        // 3. Wrap our Sand Texture in a Scene2D Image Widget
        Image sandGameWidget = new Image(texture);
        
        // 4. Add an Input Listener directly to the Widget!
        sandGameWidget.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                // Assuming BlockPresets.Line(40) is implemented elsewhere in your project
                // BlockPresets.Line(40);
                
                // Example fallback if BlockPresets is missing:
                newBlock((int) x, (int) y, 10, java.awt.Color.RED);
                return true; 
            }

            @Override
            public void touchDragged(InputEvent event, float x, float y, int pointer) {
                newBlock((int) x, (int) y, 5, java.awt.Color.RED);
            }
        });

        // 5. Add the game widget to the center of our UI table
        rootTable.add(sandGameWidget).size(WIDTH, HEIGHT).center();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void render() {
        ScreenUtils.clear(0.1f, 0.1f, 0.1f, 1);

        // --- FULLSCREEN TOGGLE ---
        if (Gdx.input.isKeyJustPressed(Input.Keys.F11)) {
            if (Gdx.graphics.isFullscreen()) {
                Gdx.graphics.setWindowedMode(WIDTH, HEIGHT);
            } else {
                Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
            }
        }
        List<Integer> blob = getFullBridgeBlob();
        if (blob != null) {
            for (int index : blob) {
                int x = index % WIDTH;
                int y = index / WIDTH;
                if (grid[x][y] != null) {
                    grid[x][y] = null;
                }
            }
            System.out.println("Bridge Found! Total particles: " + blob.size());
        } else {
            System.out.println("No complete left-to-right bridge found.");
        }

        // --- SAND PHYSICS ---
        timer += Gdx.graphics.getDeltaTime();
        timer2 += Gdx.graphics.getDeltaTime();
        if (timer >= tickRate) {
            int simulationSpeed = 5; 
            int i2 = 1;
            for (int i = 0; i < simulationSpeed; i++) {
                i2 *= -1;
                updateSand(i2, timer2 >= 0.2f);
            }
            timer -= tickRate;
            if (timer2 >= 0.2f) timer -= 0.2f;
        }

        // --- DRAW TO PIXMAP ---
        pixmap.setColor(Color.BLACK);
        pixmap.fill(); 
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                if (grid[x][y] != null) {
                    int argb = grid[x][y].color.getRGB();
                    pixmap.drawPixel(x, HEIGHT - y - 1, (argb << 8) | argb >>> 24);
                }
            }
        }

        texture.draw(pixmap, 0, 0);

        // --- RENDER UI STAGE ---
        stage.act(Gdx.graphics.getDeltaTime());
        stage.draw();
    }

    public static void newBlock(int X, int Y, int size, java.awt.Color color) {
        for (int x = -size; x <= size; x++) {
            for (int y = -size; y <= size; y++) {
                int drawX = X + x;
                int drawY = Y + y;

                if (drawX >= 0 && drawX < WIDTH && drawY >= 0 && drawY < HEIGHT) {
                    Sand a = new Sand();
                    a.slowFall = true;
                    a.color = color;
                    grid[drawX][drawY] = a;
                }
            }
        }
    }

private void updateSand(int dir, boolean onesecond) {
    // Pass 1: Instantly ground any sand grains that are currently at the absolute bottom
    for (int x = 0; x < WIDTH; x++) {
        if (grid[x][0] != null) {
            grid[x][0].slowFall = false;
        }
    }

    // Pass 2: Run the grid physics loop
    for (int y = 0; y < HEIGHT - 1; y++) {
        for (int xx = 0; xx < WIDTH; xx++) {
            int x = xx;
            if (dir == 1) {
               x = WIDTH - xx - 1; 
            }
            
            // We are analyzing the particle sitting at grid[x][y + 1]
            if (grid[x][y + 1] != null) { 
                
                if (grid[x][y] == null) {
                    // --- FALL STRAIGHT DOWN ---
                    grid[x][y] = grid[x][y + 1];       
                    grid[x][y + 1] = null;
                    
                    // Turn off slowFall if it hits the ground OR lands on another sand particle
                    if (y == 0 || (y > 0 && grid[x][y - 1] != null)) {
                        grid[x][y].slowFall = false;
                    }
                } 
                else {
                    // --- BLOCKED DIRECTLY BELOW ---
                    // There is sand directly below it, so turn off slowFall immediately
                    grid[x][y + 1].slowFall = false;
                    
                    // Fallback to diagonal sliding logic using your original rules
                    if (!grid[x][y].slowFall || onesecond) {
                        boolean canGoLeft = x > 0 && grid[x - 1][y] == null;
                        boolean canGoRight = x < WIDTH - 1 && grid[x + 1][y] == null;
                        
                        if (canGoLeft) {
                            grid[x - 1][y] = grid[x][y + 1];
                            grid[x][y + 1] = null; 
                            
                            // Turn off slowFall if the diagonal slide lands it on the floor or sand
                            if (y == 0 || (y > 0 && grid[x - 1][y - 1] != null)) {
                                grid[x - 1][y].slowFall = false;
                            }
                        } else if (canGoRight) {
                            grid[x + 1][y] = grid[x][y + 1];
                            grid[x][y + 1] = null;
                            
                            // Turn off slowFall if the diagonal slide lands it on the floor or sand
                            if (y == 0 || (y > 0 && grid[x + 1][y - 1] != null)) {
                                grid[x + 1][y].slowFall = false;
                            }
                        }
                    }
                }
            }
        }
    }
}

    // --- ALGORITHM IMPLEMENTATION ---
    public static List<Integer> getFullBridgeBlob() {
        int totalCells = WIDTH * HEIGHT;
        boolean[] visited = new boolean[totalCells];
        int[] parentMap = new int[totalCells];
        
        Arrays.fill(parentMap, -1);

        // Step 1: Find ANY path from left to right
        List<Integer> starters = new ArrayList<>();
        for (int y = 0; y < HEIGHT; y++) {
            if (grid[0][y] != null) {
                starters.add(y * WIDTH); // Index math for x = 0
            }
        }

        int finalBridgeIndex = -1;
        java.awt.Color targetColor = null;

        searchLoop:
        for (int startIndex : starters) {
            if (visited[startIndex]) continue;

            int startY = startIndex / WIDTH;
            targetColor = grid[0][startY].color;
            
            Queue<Integer> queue = new LinkedList<>();
            queue.add(startIndex);
            visited[startIndex] = true;

            while (!queue.isEmpty()) {
                int currIndex = queue.poll();
                
                int cx = currIndex % WIDTH;
                int cy = currIndex / WIDTH;

                if (cx == WIDTH - 1) {
                    finalBridgeIndex = currIndex;
                    break searchLoop;
                }

                for (int ox = -1; ox <= 1; ox++) {
                    for (int oy = -1; oy <= 1; oy++) {
                        if (ox == 0 && oy == 0) continue;

                        int nx = cx + ox;
                        int ny = cy + oy;

                        if (nx >= 0 && nx < WIDTH && ny >= 0 && ny < HEIGHT) {
                            int ni = nx + ny * WIDTH;
                            Sand neighbor = grid[nx][ny];

                            if (neighbor != null && !visited[ni] && neighbor.color.equals(targetColor)) {
                                visited[ni] = true;
                                parentMap[ni] = currIndex;
                                queue.add(ni);
                            }
                        }
                    }
                }
            }
        }

        if (finalBridgeIndex == -1) return null;

        // Step 2: Expand the path into a full "Blob"
        List<Integer> fullBlob = new ArrayList<>();
        Queue<Integer> expansionQueue = new LinkedList<>();
        boolean[] blobVisited = new boolean[totalCells];

        int trace = finalBridgeIndex;
        while (trace != -1) {
            expansionQueue.add(trace);
            blobVisited[trace] = true;
            trace = parentMap[trace];
        }

        while (!expansionQueue.isEmpty()) {
            int currIndex = expansionQueue.poll();
            fullBlob.add(currIndex);

            int cx = currIndex % WIDTH;
            int cy = currIndex / WIDTH;

            for (int ox = -1; ox <= 1; ox++) {
                for (int oy = -1; oy <= 1; oy++) {
                    if (ox == 0 && oy == 0) continue;

                    int nx = cx + ox;
                    int ny = cy + oy;

                    if (nx >= 0 && nx < WIDTH && ny >= 0 && ny < HEIGHT) {
                        int ni = nx + ny * WIDTH;
                        Sand neighbor = grid[nx][ny];

                        if (neighbor != null && !blobVisited[ni] && neighbor.color.equals(targetColor)) {
                            blobVisited[ni] = true;
                            expansionQueue.add(ni);
                        }
                    }
                }
            }
        }

        return fullBlob;
    }

    @Override
    public void dispose() {
        stage.dispose();
        texture.dispose();
        pixmap.dispose();
    }
}