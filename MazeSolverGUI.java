import java.awt.*;
import java.io.*;
import java.util.*;  // For ArrayList, Queue, etc.
import javax.swing.*;

/**
 * MazeSolverGUI - A GUI application that creates random mazes or loads mazes from files
 * and finds the shortest path using BFS and A* algorithms.
 * 
 * In this version, the default maze assignment letters (for barrier, open, start, exit, and path)
 * are stored in instance variables that may be set by the user (for example via a maze file).
 * All functions (maze creation, loading, and drawing) have been modified to use these instance variables.
 */
public class MazeSolverGUI extends JFrame {
    // Instance variables for cell types with default values.
    private char barrierChar = 'B';
    private char openChar = 'O';
    private char startChar = 'S';
    private char exitChar = 'X';
    private char pathChar = '+';

    // Colors for visualization.
    private static final Color BARRIER_COLOR = Color.BLACK;
    private static final Color OPEN_COLOR = Color.WHITE;
    private static final Color EXIT_COLOR = Color.GREEN;
    private static final Color START_COLOR = Color.RED;
    private static final Color PATH_COLOR = Color.YELLOW;

    // UI Components.
    private JPanel mazePanel;
    private JPanel controlPanel;
    private JButton loadFileButton;
    private JButton generateMazeButton;
    private JButton findPathBFSButton;
    private JButton findPathAStarButton;
    private JButton changeExitButton;
    private JTextField rowsField;
    private JTextField colsField;
    private JCheckBox guaranteePathCheckbox;
    private JLabel statusLabel;

    // Maze data.
    private char[][] maze;
    private int rows;
    private int cols;
    private int startRow;
    private int startCol;
    private int exitRow;
    private int exitCol;
    // To avoid repeating the same exit positions.
    private final Set<String> previousExitPositions = new HashSet<>();

    // Random number generator.
    private final Random generator = new Random();

    /**
     * Constructor - initializes the GUI components and sets up the layout.
     */
    public MazeSolverGUI() {
        setTitle("Dynamic Maze Solver");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Initialize components.
        initComponents();

        // Set up layout.
        add(mazePanel, BorderLayout.CENTER);
        add(controlPanel, BorderLayout.EAST);

        // Set initial size and make visible.
        setSize(900, 700);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    /**
     * Initializes all GUI components.
     */
    private void initComponents() {
        // Maze panel (will display the maze).
        mazePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawMaze(g);
            }
        };
        mazePanel.setBackground(Color.LIGHT_GRAY);

        // Control panel.
        controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
        controlPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        controlPanel.setPreferredSize(new Dimension(250, getHeight()));

        // Controls for random maze generation.
        JPanel randomMazePanel = new JPanel(new GridLayout(0, 2, 5, 5));
        randomMazePanel.setBorder(BorderFactory.createTitledBorder("Random Maze Generation"));

        randomMazePanel.add(new JLabel("Rows:"));
        rowsField = new JTextField("15");
        randomMazePanel.add(rowsField);

        randomMazePanel.add(new JLabel("Columns:"));
        colsField = new JTextField("20");
        randomMazePanel.add(colsField);

        randomMazePanel.add(new JLabel("Guarantee Path:"));
        guaranteePathCheckbox = new JCheckBox();
        guaranteePathCheckbox.setSelected(true);
        randomMazePanel.add(guaranteePathCheckbox);

        generateMazeButton = new JButton("Generate Random Maze");
        generateMazeButton.addActionListener(e -> generateRandomMaze());

        // Controls for loading from file.
        JPanel filePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filePanel.setBorder(BorderFactory.createTitledBorder("Load from File"));

        loadFileButton = new JButton("Load Maze from File");
        loadFileButton.addActionListener(e -> loadMazeFromFile());
        filePanel.add(loadFileButton);

        // Controls for finding path.
        JPanel pathPanel = new JPanel(new GridLayout(0, 1, 5, 5));
        pathPanel.setBorder(BorderFactory.createTitledBorder("Path Finding"));

        findPathBFSButton = new JButton("Find Path (BFS)");
        findPathBFSButton.addActionListener(e -> findPathBFS());
        findPathBFSButton.setEnabled(false);
        pathPanel.add(findPathBFSButton);

        findPathAStarButton = new JButton("Find Path (A*)");
        findPathAStarButton.addActionListener(e -> findPathAStar());
        findPathAStarButton.setEnabled(false);
        pathPanel.add(findPathAStarButton);

        changeExitButton = new JButton("Change Exit Location");
        changeExitButton.addActionListener(e -> changeExitLocation());
        changeExitButton.setEnabled(false);
        pathPanel.add(changeExitButton);

        // Legend panel.
        JPanel legendPanel = new JPanel(new GridLayout(0, 2, 5, 5));
        legendPanel.setBorder(BorderFactory.createTitledBorder("Legend"));

        // Legend items reflect the current instance variable values.
        addLegendItem(legendPanel, BARRIER_COLOR, "Barrier (" + barrierChar + ")");
        addLegendItem(legendPanel, OPEN_COLOR, "Open (" + openChar + ")");
        addLegendItem(legendPanel, START_COLOR, "Start (" + startChar + ")");
        addLegendItem(legendPanel, EXIT_COLOR, "Exit (" + exitChar + ")");
        addLegendItem(legendPanel, PATH_COLOR, "Path (" + pathChar + ")");

        // Status label.
        statusLabel = new JLabel("Welcome to Dynamic Maze Solver");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        // Add all panels to the control panel.
        controlPanel.add(randomMazePanel);
        controlPanel.add(Box.createVerticalStrut(5));
        controlPanel.add(generateMazeButton);
        controlPanel.add(Box.createVerticalStrut(15));
        controlPanel.add(filePanel);
        controlPanel.add(Box.createVerticalStrut(15));
        controlPanel.add(pathPanel);
        controlPanel.add(Box.createVerticalStrut(15));
        controlPanel.add(legendPanel);
        controlPanel.add(Box.createVerticalStrut(15));
        controlPanel.add(statusLabel);
        controlPanel.add(Box.createVerticalGlue());
    }

    /**
     * Adds a legend item to the specified panel.
     *
     * @param panel The legend panel.
     * @param color The color to display.
     * @param label The label text.
     */
    private void addLegendItem(JPanel panel, Color color, String label) {
        JPanel colorSquare = new JPanel();
        colorSquare.setBackground(color);
        colorSquare.setPreferredSize(new Dimension(20, 20));
        colorSquare.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        panel.add(colorSquare);
        panel.add(new JLabel(label));
    }

    /**
     * Draws the maze on the graphics context.
     *
     * @param g Graphics context to draw on.
     */
    private void drawMaze(Graphics g) {
        if (maze == null) return;

        int width = mazePanel.getWidth();
        int height = mazePanel.getHeight();
        int cellWidth = Math.max(1, width / cols);
        int cellHeight = Math.max(1, height / rows);

        // Draw each cell.
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                char cell = maze[row][col];
                // Use if/else blocks since instance variables are not compile–time constants.
                if (cell == barrierChar) {
                    g.setColor(BARRIER_COLOR);
                } else if (cell == openChar) {
                    g.setColor(OPEN_COLOR);
                } else if (cell == exitChar) {
                    g.setColor(EXIT_COLOR);
                } else if (cell == startChar) {
                    g.setColor(START_COLOR);
                } else if (cell == pathChar) {
                    g.setColor(PATH_COLOR);
                } else {
                    g.setColor(Color.GRAY);
                }

                // Fill the cell.
                g.fillRect(col * cellWidth, row * cellHeight, cellWidth, cellHeight);

                // Draw cell border.
                g.setColor(Color.GRAY);
                g.drawRect(col * cellWidth, row * cellHeight, cellWidth, cellHeight);

                // Draw the cell's character.
                g.setColor(Color.BLACK);
                Font font = g.getFont();
                Font drawFont = font;
                if (cellWidth < 15 || cellHeight < 15) {
                    drawFont = new Font(font.getName(), font.getStyle(), 8);
                }
                g.setFont(drawFont);
                g.drawString(String.valueOf(cell),
                             col * cellWidth + cellWidth / 2 - 4,
                             row * cellHeight + cellHeight / 2 + 4);
                g.setFont(font); // Restore original font.
            }
        }
    }

    /**
     * Action handler for the "Generate Random Maze" button.
     */
    private void generateRandomMaze() {
        try {
            // Parse dimensions from text fields.
            rows = Integer.parseInt(rowsField.getText().trim());
            cols = Integer.parseInt(colsField.getText().trim());

            // Validate dimensions.
            if (rows < 5 || cols < 5) {
                JOptionPane.showMessageDialog(this,
                        "Maze dimensions must be at least 5x5",
                        "Invalid Dimensions",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Create the maze.
            createRandomMaze(guaranteePathCheckbox.isSelected());

            // Clear previous exit positions and add current exit position.
            previousExitPositions.clear();
            previousExitPositions.add(exitRow + "," + exitCol);

            statusLabel.setText("Random maze generated successfully");
            findPathBFSButton.setEnabled(true);
            findPathAStarButton.setEnabled(true);
            changeExitButton.setEnabled(true);
            mazePanel.repaint();

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "Please enter valid numbers for rows and columns",
                    "Invalid Input",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Creates a random maze with the specified dimensions.
     *
     * The maze is filled with barriers and bordered.
     * An exit is placed on one of the borders (not in a corner), then
     * either a guaranteed connected maze (using Prim's algorithm) is generated,
     * or random open spaces are carved.
     * Finally, a start position is chosen among the open cells.
     * Afterward, the exit location is adjusted until a path exists from the start.
     *
     * @param guaranteePath Whether to guarantee a valid path exists.
     */
    private void createRandomMaze(boolean guaranteePath) {
        // Initialize maze with barriers using barrierChar.
        maze = new char[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                maze[i][j] = barrierChar;
            }
        }

        // Create borders.
        for (int i = 0; i < rows; i++) {
            maze[i][0] = barrierChar;
            maze[i][cols - 1] = barrierChar;
        }
        for (int j = 0; j < cols; j++) {
            maze[0][j] = barrierChar;
            maze[rows - 1][j] = barrierChar;
        }

        // Place exit (not in corners) using exitChar.
        placeRandomExit();

        if (guaranteePath) {
            int attempts = 0;
            int maxAttempts = 10;
            boolean pathExists = false;
            while (!pathExists && attempts < maxAttempts) {
                generateMazeWithPrims();
                pathExists = checkIfPathExists();
                attempts++;
            }
            if (!pathExists) {
                statusLabel.setText("Warning: Could not generate a maze with a valid path after " + maxAttempts + " attempts.");
            }
        } else {
            generateRandomOpenSpaces();
        }

        // Place the start position using startChar.
        placeRandomStart();

        // Ensure that a path exists between start and exit.
        ensureExitReachable();
    }

    /**
     * Places the exit randomly on the border (not in corners).
     */
    private void placeRandomExit() {
        int side = generator.nextInt(4);
        switch (side) {
            case 0:
                // Top border.
                exitRow = 0;
                exitCol = 1 + generator.nextInt(cols - 2);
                break;
            case 1:
                // Right border.
                exitRow = 1 + generator.nextInt(rows - 2);
                exitCol = cols - 1;
                break;
            case 2:
                // Bottom border.
                exitRow = rows - 1;
                exitCol = 1 + generator.nextInt(cols - 2);
                break;
            case 3:
                // Left border.
                exitRow = 1 + generator.nextInt(rows - 2);
                exitCol = 0;
                break;
        }
        maze[exitRow][exitCol] = exitChar;
    }

    /**
     * Changes the exit location to a new random border position.
     */
    private void changeExitLocation() {
        if (maze == null) return;

        // Remove the current exit.
        maze[exitRow][exitCol] = barrierChar;

        // Clear any existing path.
        clearPath();

        int maxAttempts = 20;
        int attempts = 0;
        boolean exitPlaced = false;

        while (!exitPlaced && attempts < maxAttempts) {
            int side = generator.nextInt(4);
            int newExitRow = 0;
            int newExitCol = 0;

            switch (side) {
                case 0:
                    newExitRow = 0;
                    newExitCol = 1 + generator.nextInt(cols - 2);
                    break;
                case 1:
                    newExitRow = 1 + generator.nextInt(rows - 2);
                    newExitCol = cols - 1;
                    break;
                case 2:
                    newExitRow = rows - 1;
                    newExitCol = 1 + generator.nextInt(cols - 2);
                    break;
                case 3:
                    newExitRow = 1 + generator.nextInt(rows - 2);
                    newExitCol = 0;
                    break;
            }

            String position = newExitRow + "," + newExitCol;
            if (previousExitPositions.size() > Math.min(rows, cols)) {
                previousExitPositions.clear();
            }

            if (!previousExitPositions.contains(position)) {
                exitRow = newExitRow;
                exitCol = newExitCol;
                maze[exitRow][exitCol] = exitChar;
                previousExitPositions.add(position);
                exitPlaced = true;
            }
            attempts++;
        }

        if (!exitPlaced) {
            int side = generator.nextInt(4);
            switch (side) {
                case 0:
                    exitRow = 0;
                    exitCol = 1 + generator.nextInt(cols - 2);
                    break;
                case 1:
                    exitRow = 1 + generator.nextInt(rows - 2);
                    exitCol = cols - 1;
                    break;
                case 2:
                    exitRow = rows - 1;
                    exitCol = 1 + generator.nextInt(cols - 2);
                    break;
                case 3:
                    exitRow = 1 + generator.nextInt(rows - 2);
                    exitCol = 0;
                    break;
            }
            maze[exitRow][exitCol] = exitChar;
        }

        statusLabel.setText("Exit location changed");
        mazePanel.repaint();
    }

    /**
     * Places the start position randomly in an open cell.
     */
    private void placeRandomStart() {
        ArrayList<int[]> openSpaces = new ArrayList<>();
        for (int i = 1; i < rows - 1; i++) {
            for (int j = 1; j < cols - 1; j++) {
                if (maze[i][j] == openChar) {
                    openSpaces.add(new int[]{i, j});
                }
            }
        }

        if (openSpaces.isEmpty()) {
            int i = 1 + generator.nextInt(rows - 2);
            int j = 1 + generator.nextInt(cols - 2);
            maze[i][j] = openChar;
            openSpaces.add(new int[]{i, j});
        }

        int[] startPos = openSpaces.get(generator.nextInt(openSpaces.size()));
        startRow = startPos[0];
        startCol = startPos[1];
        maze[startRow][startCol] = startChar;
    }

    /**
     * Generates random open spaces within the maze.
     */
    private void generateRandomOpenSpaces() {
        for (int i = 1; i < rows - 1; i++) {
            for (int j = 1; j < cols - 1; j++) {
                if (generator.nextDouble() < 0.4) {
                    maze[i][j] = openChar;
                }
            }
        }
    }

    /**
     * Generates a maze with a guaranteed connected path using Prim's algorithm.
     *
     * The maze is assumed to be filled with barriers (except borders and the exit).
     * This method picks a random interior starting cell (using odd indices),
     * marks it as open, then iteratively carves passages using a list of walls.
     */
    private void generateMazeWithPrims() {
        ArrayList<Cell> wallList = new ArrayList<>();
        int startX = randomOddCoordinate(cols);
        int startY = randomOddCoordinate(rows);
        maze[startY][startX] = openChar;
        addWalls(startY, startX, wallList);

        while (!wallList.isEmpty()) {
            int randomIndex = generator.nextInt(wallList.size());
            Cell wall = wallList.get(randomIndex);
            int wy = wall.row;
            int wx = wall.col;

            // Check horizontal neighbors.
            if (wx - 1 >= 0 && wx + 1 < cols) {
                if (maze[wy][wx - 1] == openChar && maze[wy][wx + 1] == barrierChar) {
                    maze[wy][wx] = openChar;
                    maze[wy][wx + 1] = openChar;
                    addWalls(wy, wx + 1, wallList);
                } else if (maze[wy][wx + 1] == openChar && maze[wy][wx - 1] == barrierChar) {
                    maze[wy][wx] = openChar;
                    maze[wy][wx - 1] = openChar;
                    addWalls(wy, wx - 1, wallList);
                }
            }

            // Check vertical neighbors.
            if (wy - 1 >= 0 && wy + 1 < rows) {
                if (maze[wy - 1][wx] == openChar && maze[wy + 1][wx] == barrierChar) {
                    maze[wy][wx] = openChar;
                    maze[wy + 1][wx] = openChar;
                    addWalls(wy + 1, wx, wallList);
                } else if (maze[wy + 1][wx] == openChar && maze[wy - 1][wx] == barrierChar) {
                    maze[wy][wx] = openChar;
                    maze[wy - 1][wx] = openChar;
                    addWalls(wy - 1, wx, wallList);
                }
            }
            wallList.remove(randomIndex);
        }
    }

    /**
     * Returns a random odd number between 1 and max-2 (inclusive).
     *
     * @param max The dimension size (rows or columns).
     * @return An odd integer between 1 and max-2.
     */
    private int randomOddCoordinate(int max) {
        int available = (max - 2) / 2;
        return (generator.nextInt(available) * 2) + 1;
    }

    /**
     * Adds the neighboring walls (cells) of the given cell to the wall list.
     *
     * @param y The cell's row.
     * @param x The cell's column.
     * @param wallList The list to which to add wall cells.
     */
    private void addWalls(int y, int x, ArrayList<Cell> wallList) {
        // Up.
        if (y - 1 > 0 && maze[y - 1][x] == barrierChar) {
            wallList.add(new Cell(y - 1, x));
        }
        // Down.
        if (y + 1 < rows - 1 && maze[y + 1][x] == barrierChar) {
            wallList.add(new Cell(y + 1, x));
        }
        // Left.
        if (x - 1 > 0 && maze[y][x - 1] == barrierChar) {
            wallList.add(new Cell(y, x - 1));
        }
        // Right.
        if (x + 1 < cols - 1 && maze[y][x + 1] == barrierChar) {
            wallList.add(new Cell(y, x + 1));
        }
    }

    /**
     * Helper class to represent a cell in the maze.
     */
    private class Cell {
        int row, col;
        public Cell(int row, int col) {
            this.row = row;
            this.col = col;
        }
    }

    /**
     * Checks whether a path exists from start to exit using a simple BFS.
     *
     * @return true if a valid path exists; false otherwise.
     */
    private boolean checkIfPathExists() {
        if (maze == null) return false;
        boolean[][] visited = new boolean[rows][cols];
        Queue<int[]> queue = new LinkedList<>();
        queue.add(new int[]{startRow, startCol});
        visited[startRow][startCol] = true;
        int[][] directions = { {-1,0}, {0,1}, {1,0}, {0,-1} };

        while (!queue.isEmpty()) {
            int[] cell = queue.poll();
            int r = cell[0], c = cell[1];
            if (r == exitRow && c == exitCol) {
                return true;
            }
            for (int[] d : directions) {
                int nr = r + d[0], nc = c + d[1];
                if (nr >= 0 && nr < rows && nc >= 0 && nc < cols &&
                    !visited[nr][nc] && (maze[nr][nc] == openChar || maze[nr][nc] == exitChar || maze[nr][nc] == startChar)) {
                    visited[nr][nc] = true;
                    queue.add(new int[]{nr, nc});
                }
            }
        }
        return false;
    }

    /**
     * Ensures that a path exists from start to exit.
     * If not, repeatedly swaps the exit location (via changeExitLocation) until a path is found or the maximum attempts are reached.
     */
    private void ensureExitReachable() {
        int attempts = 0;
        int maxAttempts = 100;
        while (!checkIfPathExists() && attempts < maxAttempts) {
            changeExitLocation();
            attempts++;
        }
        if (attempts >= maxAttempts) {
            statusLabel.setText("Warning: Could not ensure exit reachability after " + maxAttempts + " attempts.");
        }
    }

    /**
     * Action handler for the "Load Maze from File" button.
     */
    private void loadMazeFromFile() {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                loadMaze(selectedFile);

                // Clear previous exit positions and record current exit.
                previousExitPositions.clear();
                previousExitPositions.add(exitRow + "," + exitCol);

                statusLabel.setText("Maze loaded from " + selectedFile.getName());
                findPathBFSButton.setEnabled(true);
                findPathAStarButton.setEnabled(true);
                changeExitButton.setEnabled(true);
                mazePanel.repaint();
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this,
                        "Error loading file: " + ex.getMessage(),
                        "File Error",
                        JOptionPane.ERROR_MESSAGE);
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this,
                        ex.getMessage(),
                        "Invalid Maze File",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Loads a maze from the specified file.
     *
     * The file must have six header lines:
     *  - Number of rows
     *  - Number of columns
     *  - Wall character
     *  - Open cell character
     *  - Start character
     *  - Exit character
     *
     * Followed by maze layout rows.
     *
     * @param file File to load the maze from.
     * @throws IOException If there's an error reading the file.
     * @throws IllegalArgumentException If the file format is invalid.
     */
    private void loadMaze(File file) throws IOException, IllegalArgumentException {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            // Read number of rows.
            String line = reader.readLine();
            if (line == null) {
                throw new IllegalArgumentException("Missing number of rows");
            }
            rows = Integer.parseInt(line.trim());

            // Read number of columns.
            line = reader.readLine();
            if (line == null) {
                throw new IllegalArgumentException("Missing number of columns");
            }
            cols = Integer.parseInt(line.trim());

            // Read the wall character.
            line = reader.readLine();
            if (line == null || line.trim().isEmpty()) {
                throw new IllegalArgumentException("Missing wall character");
            }
            char fileBarrierChar = line.trim().charAt(0);

            // Read the open cell character.
            line = reader.readLine();
            if (line == null || line.trim().isEmpty()) {
                throw new IllegalArgumentException("Missing open cell character");
            }
            char fileOpenChar = line.trim().charAt(0);

            // Read the start character.
            line = reader.readLine();
            if (line == null || line.trim().isEmpty()) {
                throw new IllegalArgumentException("Missing start character");
            }
            char fileStartChar = line.trim().charAt(0);

            // Read the exit character.
            line = reader.readLine();
            if (line == null || line.trim().isEmpty()) {
                throw new IllegalArgumentException("Missing exit character");
            }
            char fileExitChar = line.trim().charAt(0);

            // Update instance variables with the file-specified characters.
            barrierChar = fileBarrierChar;
            openChar = fileOpenChar;
            startChar = fileStartChar;
            exitChar = fileExitChar;

            // Initialize the maze grid.
            maze = new char[rows][cols];
            boolean startFound = false;
            boolean exitFound = false;

            // Read maze layout rows.
            for (int i = 0; i < rows; i++) {
                line = reader.readLine();
                if (line == null) {
                    throw new IllegalArgumentException("Maze data incomplete; expected " + rows + " rows of layout");
                }
                if (line.length() < cols) {
                    throw new IllegalArgumentException("Line " + (i + 7) + " does not contain enough characters (expected " + cols + ")");
                }
                for (int j = 0; j < cols; j++) {
                    char c = line.charAt(j);
                    if (c == barrierChar) {
                        maze[i][j] = barrierChar;
                    } else if (c == openChar) {
                        maze[i][j] = openChar;
                    } else if (c == startChar) {
                        if (startFound) {
                            throw new IllegalArgumentException("Multiple start positions found in the maze");
                        }
                        startFound = true;
                        startRow = i;
                        startCol = j;
                        maze[i][j] = startChar;
                    } else if (c == exitChar) {
                        if (exitFound) {
                            throw new IllegalArgumentException("Multiple exit positions found in the maze");
                        }
                        exitFound = true;
                        exitRow = i;
                        exitCol = j;
                        maze[i][j] = exitChar;
                    } else {
                        // Default unrecognized characters to open.
                        maze[i][j] = openChar;
                    }
                }
            }

            if (!startFound) {
                throw new IllegalArgumentException("Start position not found in maze layout");
            }
            if (!exitFound) {
                throw new IllegalArgumentException("Exit position not found in maze layout");
            }

            clearPath();
        }
    }

    /**
     * Clears any existing path in the maze by replacing path cells with open cells.
     */
    private void clearPath() {
        if (maze == null) return;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (maze[i][j] == pathChar) {
                    maze[i][j] = openChar;
                }
            }
        }
    }

    /**
     * Finds the shortest path using the Breadth-First Search (BFS) algorithm.
     * Colors the path to the exit (using pathChar) without changing the exit location.
     *
     * @return true if a path is found; false otherwise.
     */
    private boolean findPathBFS() {
        clearPath();
        if (maze == null) {
            statusLabel.setText("No maze loaded");
            return false;
        }

        Queue<int[]> queue = new LinkedList<>();
        queue.add(new int[]{startRow, startCol});
        boolean[][] visited = new boolean[rows][cols];
        int[][][] parent = new int[rows][cols][2];
        visited[startRow][startCol] = true;

        // Initialize parent pointers.
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                parent[i][j][0] = -1;
                parent[i][j][1] = -1;
            }
        }

        int[][] directions = { { -1, 0 }, { 0, 1 }, { 1, 0 }, { 0, -1 } };
        boolean foundExit = false;

        while (!queue.isEmpty() && !foundExit) {
            int[] current = queue.poll();
            int row = current[0];
            int col = current[1];
            if (maze[row][col] == exitChar) {
                foundExit = true;
                break;
            }
            for (int[] dir : directions) {
                int newRow = row + dir[0];
                int newCol = col + dir[1];
                if (newRow >= 0 && newRow < rows && newCol >= 0 && newCol < cols) {
                    if ((maze[newRow][newCol] == openChar || maze[newRow][newCol] == exitChar || maze[newRow][newCol] == startChar) 
                        && !visited[newRow][newCol]) {
                        queue.add(new int[]{newRow, newCol});
                        visited[newRow][newCol] = true;
                        parent[newRow][newCol][0] = row;
                        parent[newRow][newCol][1] = col;
                    }
                }
            }
        }

        if (!foundExit) {
            statusLabel.setText("BFS: No path found. Mouse is trapped!");
            return false;
        }
        reconstructPath(parent);
        statusLabel.setText("BFS: Path found! Mouse can escape the maze.");
        mazePanel.repaint();
        return true;
    }

    /**
     * Reconstructs the path from the parent array and updates the maze with pathChar.
     *
     * @param parent 3D array containing parent pointers for each cell.
     */
    private void reconstructPath(int[][][] parent) {
        int row = exitRow;
        int col = exitCol;
        while (true) {
            int parentRow = parent[row][col][0];
            int parentCol = parent[row][col][1];
            if (parentRow == -1 || parentCol == -1 || (parentRow == startRow && parentCol == startCol)) {
                break;
            }
            maze[parentRow][parentCol] = pathChar;
            row = parentRow;
            col = parentCol;
        }
    }

    /**
     * Finds the shortest path using the A* algorithm.
     * Colors the path with pathChar without changing the exit location.
     *
     * @return true if a path is found; false otherwise.
     */
    private boolean findPathAStar() {
        clearPath();
        if (maze == null) {
            statusLabel.setText("No maze loaded");
            return false;
        }
        PriorityQueue<Node> openSet = new PriorityQueue<>();
        boolean[][] closedSet = new boolean[rows][cols];
        Node startNode = new Node(startRow, startCol, 0, 
                manhattanDistance(startRow, startCol, exitRow, exitCol), null);
        openSet.add(startNode);
        int[][] directions = { { -1, 0 }, { 0, 1 }, { 1, 0 }, { 0, -1 } };

        while (!openSet.isEmpty()) {
            Node current = openSet.poll();
            if (maze[current.row][current.col] == exitChar) {
                Node pathNode = current;
                while (pathNode.parent != null) {
                    int row = pathNode.parent.row;
                    int col = pathNode.parent.col;
                    if (!(row == startRow && col == startCol)) {
                        maze[row][col] = pathChar;
                    }
                    pathNode = pathNode.parent;
                }
                statusLabel.setText("A*: Path found! Mouse can escape the maze.");
                mazePanel.repaint();
                return true;
            }
            closedSet[current.row][current.col] = true;
            for (int[] dir : directions) {
                int newRow = current.row + dir[0];
                int newCol = current.col + dir[1];
                if (newRow < 0 || newRow >= rows || newCol < 0 || newCol >= cols)
                    continue;
                if (maze[newRow][newCol] == barrierChar || closedSet[newRow][newCol])
                    continue;
                int gCost = current.gCost + 1;
                Node neighbor = new Node(newRow, newCol, gCost,
                        manhattanDistance(newRow, newCol, exitRow, exitCol),
                        current);
                boolean skipNode = false;
                for (Node openNode : openSet) {
                    if (openNode.row == newRow && openNode.col == newCol) {
                        if (gCost >= openNode.gCost) {
                            skipNode = true;
                            break;
                        }
                    }
                }
                if (!skipNode) {
                    openSet.add(neighbor);
                }
            }
        }
        statusLabel.setText("A*: No path found. Mouse is trapped!");
        return false;
    }

    /**
     * Returns the Manhattan distance between two points.
     */
    private int manhattanDistance(int row1, int col1, int row2, int col2) {
        return Math.abs(row1 - row2) + Math.abs(col1 - col2);
    }

    /**
     * Node class for the A* algorithm.
     */
    private class Node implements Comparable<Node> {
        int row, col, gCost, hCost, fCost;
        Node parent;

        Node(int row, int col, int gCost, int hCost, Node parent) {
            this.row = row;
            this.col = col;
            this.gCost = gCost;
            this.hCost = hCost;
            this.fCost = gCost + hCost;
            this.parent = parent;
        }

        @Override
        public int compareTo(Node other) {
            int result = Integer.compare(this.fCost, other.fCost);
            if (result == 0) {
                result = Integer.compare(this.hCost, other.hCost);
            }
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null || getClass() != obj.getClass())
                return false;
            Node node = (Node) obj;
            return row == node.row && col == node.col;
        }

        @Override
        public int hashCode() {
            return Objects.hash(row, col);
        }
    }

    /**
     * Main method to start the application.
     *
     * @param args Command line arguments (not used).
     */
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> new MazeSolverGUI());
    }
}
