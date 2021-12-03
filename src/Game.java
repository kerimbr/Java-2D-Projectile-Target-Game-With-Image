import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;

public class Game extends JPanel implements KeyListener, ActionListener {

    private int width;
    private int height;

    // Ölçüler
    int terrainHeight;

    int moundHeight;
    int moundWidth;

    int ballPositionX;
    int ballPositionY;
    int ballRadius;

    int enemyPositionX;
    int enemyPositionY;
    int enemySize;

    double angleRadian;
    int timeOfFlight;


    int force;
    int velocityX;
    int velocityY;

    double deltaTime;
    double gravity = 9.8;


    //Mantıksal İfadeler
    boolean isMotion;
    boolean isShowEnemy;


    // Nesneler
    Ellipse2D ball;
    Rectangle2D enemy;
    Point2D targetPoint;

    //Zamanlayıcılar
    private Timer ballTimer;

    //Images
    Image img;



    public Game(int width, int height) {
        this.width = width;
        this.height = height;

        addKeyListener(this);
        setFocusable(true);

        ballTimer = new Timer(1000 / 60, this);

        try {
            img = ImageIO.read(new File("sky.png"));
        }catch (IOException e) {
            e.printStackTrace();
        }

        initGameSettings();
    }

    private void initGameSettings() {

        isMotion = false;
        isShowEnemy = true;


        terrainHeight = (int) (height - height * 0.15);
        moundHeight = (int) (height / 10);
        moundWidth = (int) (width / 10);

        ballRadius = 50;
        ballPositionX = (int) (width * 0.05);
        ballPositionY = terrainHeight - (moundHeight + ballRadius / 2);


        enemySize = 70;
        enemyPositionX = ThreadLocalRandom.current().nextInt((int) (width * 0.4), (int) (width * 0.9));
        enemyPositionY = ThreadLocalRandom.current().nextInt(0, ballPositionY);



        targetPoint = new Point2D.Float(enemyPositionX, enemyPositionY);

        double heightDif = 0;


        heightDif = ballPositionY - enemyPositionY;

        angleRadian = Math.atan(heightDif/(enemyPositionX - ballPositionX));

        timeOfFlight = 10;
        force = (int) ( ( (enemyPositionX - ballPositionX) / timeOfFlight));
        deltaTime = 0.1;
    }


    private void doDrawing(Graphics g) {

        Graphics2D g2d = (Graphics2D) g;

        RenderingHints rh = new RenderingHints(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        rh.put(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHints(rh);


        g2d.drawImage(img, 0, 0, null);


        drawDashboard(g2d);

        drawMap(g2d);

        if (!isMotion) drawArrow(g2d);

        drawBall(g2d);

        if (isShowEnemy) drawEnemy(g2d);

        g2d.dispose();
    }

    private void drawEnemy(Graphics2D g2d) {
        enemy = new Rectangle2D.Float(enemyPositionX, enemyPositionY, enemySize, enemySize);
        g2d.setColor(Color.RED);
        g2d.fill(enemy);
    }

    private void drawBall(Graphics2D g2d) {
        Color ballColor = new Color(33, 71, 205);
        g2d.setColor(ballColor);
        ball = getEllipseFromCenter(ballPositionX, ballPositionY, ballRadius, ballRadius);
        g2d.fill(ball);
    }

    private void drawMap(Graphics2D g2d) {

        Rectangle2D terrain = new Rectangle2D.Float(0, terrainHeight, width, terrainHeight);
        Color terrainColor = new Color(52, 140, 49);
        g2d.setColor(terrainColor);
        g2d.fill(terrain);


        Rectangle2D mound = new Rectangle2D.Float(0, terrainHeight - moundHeight, moundWidth, moundHeight);
        Color moundColor = new Color(40, 19, 2);
        g2d.setColor(moundColor);
        g2d.fill(mound);
    }

    private void drawDashboard(Graphics2D g2d) {

        g2d.drawString("Press 'R' to Restart", 50, 50);
        g2d.drawString("Press 'Space' to Start", 50, 70);




        g2d.setFont(new Font("TimesRoman",Font.BOLD,40));

        g2d.setColor(new Color(23, 22, 22, 169));
        g2d.drawString((int)Math.toDegrees(angleRadian) + "°",width/2,height/3-50);

        g2d.drawString((int)force + " m/s",width/2 - 30,height/3);



    }

    private void drawArrow(Graphics2D g2d) {

        float[] dash = {4f, 0f, 2f};
        BasicStroke basicStroke = new BasicStroke(1, BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_ROUND, 1.0f, dash, 2f);

        g2d.setStroke(basicStroke);
        g2d.setColor(Color.DARK_GRAY);

        int arrowX = ballPositionX;
        int arrowY = ballPositionY;

        Point2D x1y1 = new Point2D.Float(arrowX,arrowY);

        arrowX = (int) (arrowX + 80 * Math.cos(angleRadian));
        arrowY = (int) (arrowY - 80 * Math.sin(angleRadian));

        Point2D x2y2 = new Point2D.Float(arrowX,arrowY);
        Line2D arrow = new Line2D.Float(x1y1,x2y2);

        g2d.draw(arrow);

    }

    private Ellipse2D getEllipseFromCenter(double x, double y, double width, double height) {
        double newX = x - width / 2.0;
        double newY = y - height / 2.0;
        return new Ellipse2D.Double(newX, newY, width, height);
    }


    private void restartGame() {
        initGameSettings();
        repaint();
    }


    // Override Methods


    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        doDrawing(g);
    }


    @Override
    public void actionPerformed(ActionEvent e) {


        deltaTime += 0.1;
        ballPositionX = (int) (ballPositionX + velocityX);
        ballPositionY = (int) (ballPositionY - velocityY + gravity * deltaTime);


        if (ballPositionY >= terrainHeight - ballRadius / 3) {
            ballTimer.stop();

            if (isShowEnemy) {
                int dialogResult = JOptionPane.showConfirmDialog(
                        null,
                        "Vuramadın :(",
                        "Tekrar Dene",
                        JOptionPane.YES_NO_OPTION
                );

                if (dialogResult == JOptionPane.YES_OPTION) {
                    restartGame();
                }
            }
        }

        if (isShowEnemy) {
            if (enemy.intersects(ball.getBounds2D())) {
                isShowEnemy = false;
                repaint();
                int dialogResult = JOptionPane.showConfirmDialog(
                        null,
                        "Tebrikler Hedefi Vurdun *-* \n Tekrar Denemek İster misin ?",
                        "Tebrikler",
                        JOptionPane.YES_NO_OPTION
                );
                if (dialogResult == JOptionPane.OK_OPTION) {
                    ballTimer.stop();
                    restartGame();
                }
            }
        }


        repaint();
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {

        if (e.getKeyCode() == KeyEvent.VK_R) {
            ballTimer.stop();
            restartGame();
        }

        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            isMotion = true;
            velocityX = (int) (force * Math.cos(angleRadian));
            velocityY = (int) (force * Math.sin(angleRadian));

            ballTimer.start();
            repaint();
        }

    }

    @Override
    public void keyReleased(KeyEvent e) {
    }


}
