package user_interface;

import static user_interface.GameWindow.SCREEN_HEIGHT;
import static user_interface.GameWindow.SCREEN_WIDTH;
import static util.Resource.getImage;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;

import game_object.Clouds;
import game_object.Dino;
import game_object.Land;
import game_object.Score;
import manager.ControlsManager;
import manager.EnemyManager;
import manager.SoundManager;
import misc.Controls;
import misc.DinoState;
import misc.GameState;
import game_object.Shield;
import game_object.Block;

import java.util.ArrayList;
import java.util.Iterator;

@SuppressWarnings(value = { "serial" })
public class GameScreen extends JPanel implements Runnable {

	private Thread thread;
	private Shield shield;

	private static final int STARTING_SPEED_X = -5;
	private static final double DIFFICULTY_INC = -0.0002;

	public static final double GRAVITY = 0.4;
	// public static final int GROUND_Y = 280;
	public static final int GROUND_Y = 380;

	public static final double SPEED_Y = -12;

	private final int FPS = 100;
	private final int NS_PER_FRAME = 1_000_000_000 / FPS;

	private double speedX = STARTING_SPEED_X;
	private GameState gameState = GameState.GAME_STATE_START;
	private int introCountdown = 1000;
	private boolean introJump = true;
	private boolean showHitboxes = false;
	private boolean collisions = true;

	private Controls controls;
	private Score score;
	private Dino dino;
	private Land land;
	private Clouds clouds;
	private EnemyManager eManager;
	private SoundManager gameOverSound;
	private ControlsManager cManager;;
	private int shakeDuration = 0;
	private int shakeIntensity = 20; // pixels to shake

	private static final int SHIELD_SPAWN_INTERVAL = 30000; // 30 seconds in milliseconds
	private long lastShieldSpawnTime;

	private BufferedImage shieldImg;
	private ArrayList<Block> shieldPowerUps;

	public GameScreen() {
		thread = new Thread(this);
		controls = new Controls(this);
		super.add(controls.pressUp);
		super.add(controls.releaseUp);
		super.add(controls.pressDown);
		super.add(controls.releaseDown);
		super.add(controls.pressDebug);
		// super.add(controls.releaseDebug);
		super.add(controls.pressPause);
		// super.add(controls.releaseP);
		cManager = new ControlsManager(controls, this);
		score = new Score(this);
		dino = new Dino(controls);
		land = new Land(this);
		clouds = new Clouds(this);
		eManager = new EnemyManager(this);
		gameOverSound = new SoundManager("resources/dead.wav");
		gameOverSound.startThread();

		// Initialize shield-related objects
		shield = new Shield(5); // Shield lasts for 5 seconds
		shieldPowerUps = new ArrayList<>();
		shieldImg = getImage("resources/shield.png");
		lastShieldSpawnTime = System.currentTimeMillis();
	}

	public void startThread() {
		thread.start();
	}

	public void startGame() {
		if (shield != null) {
			shield.reset(); // Reset the shield timer
		}
	}

	@Override
	public void run() {
		long prevFrameTime = System.nanoTime();
		int waitingTime = 0;
		while (true) {
			cManager.update();
			updateFrame();
			repaint();
			waitingTime = (int) ((NS_PER_FRAME - (System.nanoTime() - prevFrameTime)) / 1_000_000);
			if (waitingTime < 0)
				waitingTime = 1;
			SoundManager.WAITING_TIME = waitingTime;
			// little pause to not start new game if you are spamming your keys
			if (gameState == GameState.GAME_STATE_OVER)
				waitingTime = 1000;
			try {
				Thread.sleep(waitingTime);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			prevFrameTime = System.nanoTime();
		}
	}

	public double getSpeedX() {
		return speedX;
	}

	public GameState getGameState() {
		return gameState;
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);

		// Screen shake effect
		int offsetX = 0;
		int offsetY = 0;

		// Apply screen shake only when the game is in progress and shakeDuration is
		// positive
		if (gameState == GameState.GAME_STATE_IN_PROGRESS && shakeDuration > 0) {
			offsetX = (int) (Math.random() * shakeIntensity - shakeIntensity / 2);
			offsetY = (int) (Math.random() * shakeIntensity - shakeIntensity / 2);
			shakeDuration--; // Decrease shake duration after each frame
		}

		// Create a new Graphics2D object that supports transformations
		Graphics2D g2 = (Graphics2D) g.create();
		g2.translate(offsetX, offsetY); // Apply the shake offset

		// Draw background
		g2.setColor(new Color(246, 246, 246)); // Set background color
		g2.fillRect(0, 0, getWidth(), getHeight()); // Fill the whole window with the color

		// Switch between different game states and render the corresponding screen
		switch (gameState) {
			case GameState.GAME_STATE_START:
				startScreen(g2); // Render start screen
				break;
			case GameState.GAME_STATE_INTRO:
				introScreen(g2); // Render intro screen
				break;
			case GameState.GAME_STATE_IN_PROGRESS:
				inProgressScreen(g2); // Render in-progress screen
				break;
			case GameState.GAME_STATE_OVER:
				gameOverScreen(g2); // Render game over screen
				break;
			case GameState.GAME_STATE_PAUSED:
				pausedScreen(g2); // Render paused screen
				break;
			default:
				break;
		}

		// Dispose of the Graphics2D object to clean up
		g2.dispose();
	}

	public void triggerShake() {
		shakeDuration = 50; // Set how long the shake lasts (10 frames, for example)
	}

	// update all entities positions
	private void updateFrame() {
		switch (gameState) {
			case GAME_STATE_INTRO:
				dino.updatePosition();
				if (!introJump && dino.getDinoState() == DinoState.DINO_RUN)
					land.updatePosition();
				clouds.updatePosition();
				introCountdown += speedX;
				if (introCountdown <= 0)
					gameState = GameState.GAME_STATE_IN_PROGRESS;
				if (introJump) {
					dino.jump();
					dino.setDinoState(DinoState.DINO_JUMP);
					introJump = false;
				}
				break;
			case GAME_STATE_IN_PROGRESS:
				speedX += DIFFICULTY_INC;
				dino.updatePosition();
				land.updatePosition();
				clouds.updatePosition();
				eManager.updatePosition();
				shield.update();

				// Update shield power-ups
				for (Iterator<Block> iterator = shieldPowerUps.iterator(); iterator.hasNext();) {
					Block shieldPU = iterator.next();
					shieldPU.x += speedX;

					// Check collision with dino
					if (dino.getHitbox()
							.intersects(new Rectangle((int) shieldPU.x, shieldPU.y, shieldPU.width, shieldPU.height))) {
						shield.activate();
						iterator.remove();
					} else if (shieldPU.x + shieldPU.width < 0) {
						iterator.remove();
					}
				}

				// Spawn new shield power-up
				if (System.currentTimeMillis() - lastShieldSpawnTime >= SHIELD_SPAWN_INTERVAL) {
					spawnShieldPowerUp();
					lastShieldSpawnTime = System.currentTimeMillis();
				}

				if (eManager.isCollision(dino.getHitbox()) && !shield.isActive()) {
					gameState = GameState.GAME_STATE_OVER;
					dino.dinoGameOver();
					score.writeScore();
					gameOverSound.play();

					// Trigger screen shake when the Dino dies after a collision
					System.out.println("Collision detected, triggering screen shake.");
					triggerShake();
				}

				score.scoreUp();
				break;
			default:
				break;
		}
	}

	private void drawDebugMenu(Graphics g) {
		g.setColor(Color.RED);
		g.drawLine(0, GROUND_Y, getWidth(), GROUND_Y);
		// clouds.drawHitBox(g);
		dino.drawHitbox(g);
		eManager.drawHitbox(g);
		String speedInfo = "SPEED_X: " + String.valueOf(Math.round(speedX * 1000D) / 1000D);
		g.drawString(speedInfo, (int) (SCREEN_WIDTH / 100), (int) (SCREEN_HEIGHT / 25));
	}

	private void startScreen(Graphics g) {
		land.draw(g);
		dino.draw(g);
		BufferedImage introImage = getImage("resources/intro-text.png");
		Graphics2D g2d = (Graphics2D) g;
		g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, introCountdown / 1000f));
		g2d.drawImage(introImage, SCREEN_WIDTH / 2 - introImage.getWidth() / 2, SCREEN_HEIGHT / 2 - introImage.getHeight(),
				null);
		g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
	}

	private void introScreen(Graphics g) {
		clouds.draw(g);
		startScreen(g);
	}

	private void inProgressScreen(Graphics g) {
		clouds.draw(g);
		land.draw(g);
		eManager.draw(g);

		// Draw shield power-ups
		for (Block shieldPU : shieldPowerUps) {
			g.drawImage(shieldPU.img, (int) shieldPU.x, shieldPU.y, shieldPU.width, shieldPU.height, null);
		}

		// Draw shield effect if active
		if (shield.isActive()) {
			Graphics2D g2d = (Graphics2D) g;
			g2d.setColor(new Color(0, 150, 255, 80));
			Rectangle dinoBox = dino.getHitbox();
			g2d.fillOval(dinoBox.x - 5, dinoBox.y - 5, dinoBox.width + 10, dinoBox.height + 10);
		}

		dino.draw(g);
		score.draw(g);

		// Draw shield timer if active
		if (shield.isActive()) {
			g.setColor(new Color(0, 150, 255));
			g.setFont(new Font("Arial", Font.BOLD, 14));
			g.drawString("Shield: " + shield.getRemainingTime() + "s", SCREEN_WIDTH - 150, 40);
		}

		// Draw time until next shield
		long timeUntilNextShield = Math.max(0, SHIELD_SPAWN_INTERVAL - (System.currentTimeMillis() - lastShieldSpawnTime));
		g.setColor(new Color(60, 179, 113));
		g.setFont(new Font("Arial", Font.BOLD, 12));
		g.drawString("Next Shield: " + (timeUntilNextShield / 1000) + "s", SCREEN_WIDTH - 150, 60);

		if (showHitboxes)
			drawDebugMenu(g);
	}

	private void gameOverScreen(Graphics g) {
		inProgressScreen(g);
		BufferedImage gameOverImage = getImage("resources/game-over.png");
		BufferedImage replayImage = getImage("resources/replay.png");
		g.drawImage(gameOverImage, SCREEN_WIDTH / 2 - gameOverImage.getWidth() / 2,
				SCREEN_HEIGHT / 2 - gameOverImage.getHeight() * 2, null);
		g.drawImage(replayImage, SCREEN_WIDTH / 2 - replayImage.getWidth() / 2, SCREEN_HEIGHT / 2, null);
	}

	private void pausedScreen(Graphics g) {
		inProgressScreen(g);
		BufferedImage pausedImage = getImage("resources/paused.png");
		g.drawImage(pausedImage, SCREEN_WIDTH / 2 - pausedImage.getWidth() / 2, SCREEN_HEIGHT / 2 - pausedImage.getHeight(),
				null);
	}

	public void pressUpAction() {
		if (gameState == GameState.GAME_STATE_IN_PROGRESS) {
			dino.jump();
			dino.setDinoState(DinoState.DINO_JUMP);
		}
	}

	public void releaseUpAction() {
		if (gameState == GameState.GAME_STATE_START)
			gameState = GameState.GAME_STATE_INTRO;
		if (gameState == GameState.GAME_STATE_OVER) {
			speedX = STARTING_SPEED_X;
			score.scoreReset();
			eManager.clearEnemy();
			dino.resetDino();
			clouds.clearClouds();
			land.resetLand();
			gameState = GameState.GAME_STATE_IN_PROGRESS;

		}
	}

	public void pressDownAction() {
		if (dino.getDinoState() != DinoState.DINO_JUMP && gameState == GameState.GAME_STATE_IN_PROGRESS)
			dino.setDinoState(DinoState.DINO_DOWN_RUN);
	}

	public void releaseDownAction() {
		if (dino.getDinoState() != DinoState.DINO_JUMP && gameState == GameState.GAME_STATE_IN_PROGRESS)
			dino.setDinoState(DinoState.DINO_RUN);
	}

	public void pressDebugAction() {
		if (showHitboxes == false)
			showHitboxes = true;
		else
			showHitboxes = false;
		if (collisions == true)
			collisions = false;
		else
			collisions = true;
	}

	public void pressPauseAction() {
		if (gameState == GameState.GAME_STATE_IN_PROGRESS)
			gameState = GameState.GAME_STATE_PAUSED;
		else
			gameState = GameState.GAME_STATE_IN_PROGRESS;
	}

	private void spawnShieldPowerUp() {
		if (gameState == GameState.GAME_STATE_IN_PROGRESS) {
			int xPosition = SCREEN_WIDTH + 50;
			int shieldHeight = 40;
			int yPosition = GROUND_Y - shieldHeight;
			shieldPowerUps.add(new Block(xPosition, yPosition, 40, shieldHeight, shieldImg));
		}
	}

	public void resetGame() {
		shield.deactivate();
		shieldPowerUps.clear();
		lastShieldSpawnTime = System.currentTimeMillis();
	}

}
