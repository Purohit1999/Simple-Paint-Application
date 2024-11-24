import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;
import javax.imageio.ImageIO;
import javax.swing.*;
/*
 * MIT License
 * Copyright (c) 2024 Purohit1999
 */


class SimplePaintPanel extends JPanel {
    // Set to store the painted black pixels
    private final Set<Point> blackPixels = new HashSet<>();
    private final int brushSize;

    private int mouseButtonDown = 0; // Tracks which mouse button is pressed (left or right)

    // Constructor for the painting panel with default brush size
    public SimplePaintPanel() {
        this(5, new HashSet<>());
    }

    // Constructor for custom brush size and black pixel set
    public SimplePaintPanel(int brushSize, Set<Point> blackPixels) {
        this.brushSize = brushSize;
        this.blackPixels.addAll(blackPixels);

        // Set the preferred size of the panel
        this.setPreferredSize(new Dimension(300, 300));

        // Add mouse listener for painting functionality
        final SimplePaintPanel self = this;
        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent ev) {
                if (mouseButtonDown == 1) { // Left button: Add pixels
                    addPixels(getPixelsAround(ev.getPoint()));
                } else if (mouseButtonDown == 3) { // Right button: Remove pixels
                    removePixels(getPixelsAround(ev.getPoint()));
                }
            }

            @Override
            public void mousePressed(MouseEvent ev) {
                self.mouseButtonDown = ev.getButton();
            }
        };

        // Attach mouse listeners to the panel
        this.addMouseMotionListener(mouseAdapter);
        this.addMouseListener(mouseAdapter);
    }

    // Paint method to redraw the panel
    @Override
    public void paint(Graphics g) {
        int w = this.getWidth();
        int h = this.getHeight();

        // Set the background color to white
        g.setColor(Color.white);
        g.fillRect(0, 0, w, h);

        // Set the color to black and draw each black pixel
        g.setColor(Color.black);
        for (Point point : blackPixels) {
            g.drawRect(point.x, point.y, 1, 1);
        }
    }

    // Clears the panel by removing all black pixels
    public void clear() {
        this.blackPixels.clear();
        this.invalidate();
        this.repaint();
    }

    // Add a collection of black pixels
    public void addPixels(Collection<? extends Point> pixels) {
        this.blackPixels.addAll(pixels);
        this.invalidate();
        this.repaint();
    }

    // Remove a collection of black pixels
    public void removePixels(Collection<? extends Point> pixels) {
        this.blackPixels.removeAll(pixels);
        this.invalidate();
        this.repaint();
    }

    // Check if a pixel exists
    public boolean isPixel(Point pixel) {
        return this.blackPixels.contains(pixel);
    }

    // Get surrounding pixels based on the brush size
    private Collection<? extends Point> getPixelsAround(Point point) {
        Set<Point> points = new HashSet<>();
        for (int x = point.x - brushSize; x < point.x + brushSize; x++) {
            for (int y = point.y - brushSize; y < point.y + brushSize; y++) {
                points.add(new Point(x, y));
            }
        }
        return points;
    }
}

public class Main extends JFrame implements ActionListener {
    private final String ACTION_NEW = "New Image";
    private final String ACTION_LOAD = "Load Image";
    private final String ACTION_SAVE = "Save Image";

    private final SimplePaintPanel paintPanel = new SimplePaintPanel();

    // Main constructor to set up the JFrame and menu
    public Main() {
        super();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("Simple Paint");

        // Initialize menu and add it to the frame
        initMenu();
        this.getContentPane().add(paintPanel);
        pack();
        setVisible(true);
    }

    // Initialize the menu with options for new, load, and save
    private void initMenu() {
        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu("File");

        JMenuItem mnuNew = new JMenuItem(ACTION_NEW);
        JMenuItem mnuLoad = new JMenuItem(ACTION_LOAD);
        JMenuItem mnuSave = new JMenuItem(ACTION_SAVE);

        // Set action commands for menu items
        mnuNew.setActionCommand(ACTION_NEW);
        mnuLoad.setActionCommand(ACTION_LOAD);
        mnuSave.setActionCommand(ACTION_SAVE);

        // Add action listeners to menu items
        mnuNew.addActionListener(this);
        mnuLoad.addActionListener(this);
        mnuSave.addActionListener(this);

        // Add menu items to the menu and menu bar
        menu.add(mnuNew);
        menu.add(mnuLoad);
        menu.add(mnuSave);
        menuBar.add(menu);

        this.setJMenuBar(menuBar);
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        // Handle menu actions
        switch (ev.getActionCommand()) {
            case ACTION_NEW:
                paintPanel.clear(); // Clear the panel for a new image
                break;
            case ACTION_LOAD:
                doLoadImage(); // Load an image from file
                break;
            case ACTION_SAVE:
                doSaveImage(); // Save the painted image to file
                break;
        }
    }

    // Save the current painting as a PNG file
    private void doSaveImage() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int result = fileChooser.showSaveDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) return;

        File saveFile = fileChooser.getSelectedFile();
        if (!saveFile.getAbsolutePath().toLowerCase().endsWith(".png")) {
            saveFile = new File(saveFile.getAbsolutePath() + ".png");
        }

        // Create an image and write black pixels
        BufferedImage image = new BufferedImage(
                paintPanel.getSize().width, paintPanel.getSize().height, BufferedImage.TYPE_INT_RGB
        );

        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                image.setRGB(x, y, Color.white.getRGB());
                if (paintPanel.isPixel(new Point(x, y))) {
                    image.setRGB(x, y, Color.black.getRGB());
                }
            }
        }

        try {
            ImageIO.write(image, "png", saveFile);
        } catch (IOException e) {
            return;
        }
    }

    // Load an image from a file and display it on the panel
    private void doLoadImage() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int result = fileChooser.showOpenDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) return;

        File openFile = fileChooser.getSelectedFile();
        BufferedImage image;
        try (FileInputStream fis = new FileInputStream(openFile)) {
            image = ImageIO.read(fis);
        } catch (IOException e) {
            return;
        }

        if (image == null) return;
        paintPanel.clear();

        // Add black pixels from the loaded image
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                Color c = new Color(image.getRGB(x, y));
                if ((c.getBlue() < 128 || c.getRed() < 128 || c.getGreen() < 128) && c.getAlpha() == 255) {
                    paintPanel.addPixels(Collections.singleton(new Point(x, y)));
                }
            }
        }
    }

    // Main method to launch the application
    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::new);
    }
}

