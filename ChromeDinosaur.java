

    import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import javax.swing.*;

public class ChromeDinosaur extends JPanel implements ActionListener, KeyListener {
    int boardWidth = 750;
    int boardHeight = 250;

    private Dinosaur dinosaur;
    private ArrayList<Cactus> cactusArray;

    private boolean gameOver = false;
    private boolean gameStarted = false;
    private int score = 0;

    private Timer gameLoop;
    private Timer placeCactusTimer;

    public ChromeDinosaur() {
        setPreferredSize(new Dimension(boardWidth, boardHeight));
        setBackground(Color.lightGray);
        setFocusable(true);
        addKeyListener(this);

        // Initialize dinosaur
        dinosaur = new Dinosaur();

        // Initialize cactus array
        cactusArray = new ArrayList<>();

        // Game timer
        gameLoop = new Timer(1000 / 60, this); // 60 frames per second

        // Place cactus timer
        placeCactusTimer = new Timer(1500, e -> placeCactus());
    }

    private void placeCactus() {
        if (gameOver) return;

        double placeCactusChance = Math.random(); // 0 - 0.999999
        Cactus cactus = CactusFactory.createCactus(placeCactusChance, boardHeight);
        cactusArray.add(cactus);

        if (cactusArray.size() > 10) {
            cactusArray.remove(0); // remove the first cactus from ArrayList
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    private void draw(Graphics g) {
        // Draw dinosaur
        dinosaur.draw(g);

        // Draw cactus
        for (Cactus cactus : cactusArray) {
            cactus.draw(g);
        }

        // Draw score
        g.setColor(Color.black);
        g.setFont(new Font("Courier", Font.PLAIN, 32));
        g.drawString(String.valueOf(score), 10, 35);
    }

    private void move() {
        if (!gameStarted || gameOver) return;

        // Move dinosaur
        dinosaur.move();

        // Move cactus and check for collisions
        for (Cactus cactus : cactusArray) {
            cactus.move();

            if (dinosaur.collidesWith(cactus)) {
                gameOver = true;
                dinosaur.die();
                placeCactusTimer.stop();
                gameLoop.stop();
                showEndDialog("Continue Again ! ", false);
                return;
            }
        }

        // Update score
        score++;
        if (score >= 1001) {
            gameOver = true;
            placeCactusTimer.stop();
            gameLoop.stop();
            showEndDialog("***Winner***", true);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        move();
        repaint();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            if (!gameStarted) {
                gameStarted = true;
                gameLoop.start();
                placeCactusTimer.start();
            }

            if (dinosaur.isOnGround() && !gameOver) {
                dinosaur.jump();
            }

            if (gameOver) {
                resetGame();
                gameLoop.start();
                placeCactusTimer.start();
            }
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {}

    private void resetGame() {
        dinosaur.reset();
        cactusArray.clear();
        score = 0;
        gameOver = false;
        gameStarted = false;
    }

    private void showEndDialog(String message, boolean isWinner) {
        if (isWinner) {
            JLabel winnerLabel = new JLabel(message);
            winnerLabel.setForeground(Color.RED);
            winnerLabel.setFont(new Font("Courier", Font.BOLD, 32));

            JOptionPane.showMessageDialog(this, winnerLabel, "Game finished", JOptionPane.PLAIN_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, message, "Game Over", JOptionPane.PLAIN_MESSAGE);
        }
        resetGame();
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Chrome Dinosaur Game");
        ChromeDinosaur game = new ChromeDinosaur();
        frame.add(game);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

class Block {
    private int x;
    private int y;
    private int width;
    private int height;
    private Image img;

    public Block(int x, int y, int width, int height, Image img) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.img = img;
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public Image getImg() { return img; }
    public void setX(int x) { this.x = x; }
    public void setY(int y) { this.y = y; }
    public void setImg(Image img) { this.img = img; }

    public void draw(Graphics g) {
        g.drawImage(img, x, y, width, height, null);
    }
}

class Dinosaur extends Block {
    private static final int JUMP_VELOCITY = -17;
    private static final int GRAVITY = 1;

    private int velocityY = 0;
    private int groundY;

    private Image runImg;
    private Image jumpImg;
    private Image deadImg;

    public Dinosaur() {
        super(50, 156, 88, 94, new ImageIcon(Dinosaur.class.getResource("./img/dino-run.gif")).getImage());
        runImg = getImg();
        jumpImg = new ImageIcon(Dinosaur.class.getResource("./img/dino-jump.png")).getImage();
        deadImg = new ImageIcon(Dinosaur.class.getResource("./img/dino-dead.png")).getImage();
        groundY = getY();
    }

    public void move() {
        velocityY += GRAVITY;
        setY(getY() + velocityY);

        if (getY() > groundY) {
            setY(groundY);
            velocityY = 0;
            setImg(runImg);
        }
    }

    public void jump() {
        velocityY = JUMP_VELOCITY;
        setImg(jumpImg);
    }

    public boolean isOnGround() {
        return getY() == groundY;
    }

    public void die() {
        setImg(deadImg);
    }

    public void reset() {
        setY(groundY);
        setImg(runImg);
        velocityY = 0;
    }

    public boolean collidesWith(Block block) {
        return getX() < block.getX() + block.getWidth() &&
               getX() + getWidth() > block.getX() &&
               getY() < block.getY() + block.getHeight() &&
               getY() + getHeight() > block.getY();
    }
}

class Cactus extends Block {
    private static final int VELOCITY_X = -12;

    public Cactus(int x, int y, int width, int height, Image img) {
        super(x, y, width, height, img);
    }

    public void move() {
        setX(getX() + VELOCITY_X);
    }
}

class CactusFactory {
    public static Cactus createCactus(double chance, int boardHeight) {
        int cactusY = boardHeight - 70;
        if (chance > 0.90) {
            return new Cactus(700, cactusY, 102, 70, new ImageIcon(Cactus.class.getResource("./img/cactus3.png")).getImage());
        } else if (chance > 0.70) {
            return new Cactus(700, cactusY, 69, 70, new ImageIcon(Cactus.class.getResource("./img/cactus2.png")).getImage());
        } else {
            return new Cactus(700, cactusY, 34, 70, new ImageIcon(Cactus.class.getResource("./img/cactus1.png")).getImage());
        }
    }
}

