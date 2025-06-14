package game_object;

import user_interface.GameScreen;

import static user_interface.GameWindow.SCREEN_HEIGHT;
import static user_interface.GameWindow.SCREEN_WIDTH;
import static util.Resource.getImage;
import static util.Resource.isJar;

import java.awt.AlphaComposite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import manager.SoundManager;
import misc.GameState;

public class Score {

	// value by which score is increasing
	private static final double SCORE_INC = 0.1;
	// length of score on screen, max 99999 but i dont think that anyone will play
	// that long so.....
	private static final int SCORE_LENGTH = 5;
	// width and height of single number on sprite
	private static final int NUMBER_WIDTH = 20;
	private static final int NUMBER_HEIGHT = 21;
	// here i calculate position of score on screen
	private static final int CURRENT_SCORE_X = SCREEN_WIDTH - (SCORE_LENGTH * NUMBER_WIDTH + SCREEN_WIDTH / 100);
	private static final int HI_SCORE_X = SCREEN_WIDTH - (SCORE_LENGTH * NUMBER_WIDTH + SCREEN_WIDTH / 100) * 2;
	private static final int HI_X = SCREEN_WIDTH
			- ((SCORE_LENGTH * NUMBER_WIDTH + SCREEN_WIDTH / 100) * 2 + NUMBER_WIDTH * 2 + SCREEN_WIDTH / 100);
	private static final int SCORE_Y = SCREEN_HEIGHT / 25;

	private GameScreen gameScreen;
	private String scoreFileName;
	private File scoreFile;
	private BufferedImage hi;
	private BufferedImage numbers;
	private SoundManager scoreUpSound;

	private double score;
	private int hiScore;

	public Score(GameScreen gameScreen) {
		this.gameScreen = gameScreen;
		score = 0;
		scoreFileName = "best-scores.txt";
		scoreFile = new File("resources/" + scoreFileName);
		readScore();
		hi = getImage("resources/hi.png");
		numbers = getImage("resources/numbers.png");
		scoreUpSound = new SoundManager("resources/scoreup.wav");
		scoreUpSound.startThread();
	}

	public void scoreUp() {
		score += SCORE_INC;
		// play sound every 100 points
		if ((int) score != 0 && score % 100 <= 0.1)
			scoreUpSound.play();
	}

	// getting single number from sprite
	private BufferedImage cropImage(BufferedImage image, int number) {
		return image.getSubimage(number * NUMBER_WIDTH, 0, NUMBER_WIDTH, NUMBER_HEIGHT);
	}

	// method to use StringBuilder
	private int[] scoreToArray(double scoreType) {
		int scoreArray[] = new int[SCORE_LENGTH];
		int tempScore = (int) scoreType;

		// Create a StringBuilder to build the score in reverse
		StringBuilder scoreBuilder = new StringBuilder();
		String scoreStr = Integer.toString(tempScore);
		scoreBuilder.append(scoreStr);

		// Pad with leading zeros if needed
		while (scoreBuilder.length() < SCORE_LENGTH) {
			scoreBuilder.insert(0, "0");
		}

		// If the score is longer than SCORE_LENGTH, truncate it
		if (scoreBuilder.length() > SCORE_LENGTH) {
			scoreBuilder = new StringBuilder(scoreBuilder.substring(scoreBuilder.length() - SCORE_LENGTH));
		}

		// Convert back to array of digits
		for (int i = 0; i < SCORE_LENGTH; i++) {
			scoreArray[i] = Character.getNumericValue(scoreBuilder.charAt(SCORE_LENGTH - i - 1));
		}

		return scoreArray;
	}

	// Method to write the current score to a file if it's higher than the high
	// score
	public void writeScore() {
		// Only write the score if it's higher than the current high score
		if (score > hiScore) {
			File file;

			// Determine file path depending on whether the app is running from a JAR
			if (isJar())
				// If running from a JAR, get the resource path and append the score file name
				file = new File(ClassLoader.getSystemClassLoader().getResource("").getPath() + scoreFileName);
			else
				// If not running from a JAR (e.g., in an IDE), use the predefined score file
				file = scoreFile;

			// Try-with-resources to automatically close the BufferedWriter
			try (BufferedWriter bw = new BufferedWriter(new FileWriter(file, true))) {

				// Write the score, date, and player name to the file in a structured format
				bw.write(String.format("result=%s,date=%s,player=%s\n",
						Integer.toString((int) score), // Convert score to integer string
						new SimpleDateFormat("yyyyMMdd_HHmmss") // Format current date/time
								.format(Calendar.getInstance().getTime()),
						"Dino" // Hardcoded player name
				));

				// Explicitly close the writer (not strictly needed because of
				// try-with-resources, but harmless)
				bw.close();
			} catch (IOException e) {
				// Print the error stack trace if there's an issue writing to the file
				e.printStackTrace();
			}
		}
	}

	private void readScore() {
		// another ClassLoader to know from where to read best scores
		if (scoreFile.exists()
				|| new File(ClassLoader.getSystemClassLoader().getResource("").getPath() + scoreFileName).exists()) {
			String line = "";
			File file;
			// again jar file check
			if (isJar())
				file = new File(ClassLoader.getSystemClassLoader().getResource("").getPath() + scoreFileName);
			else
				file = scoreFile;
			if (file.exists()) {
				try (BufferedReader br = new BufferedReader(new FileReader(file))) {
					while ((line = br.readLine()) != null) {
						Matcher m = Pattern.compile("result=(\\d+),date=([\\d_]+),player=(\\w+)").matcher(line);
						if (m.find()) {
							if (Integer.parseInt(m.group(1)) > hiScore)
								hiScore = Integer.parseInt(m.group(1));
							// System.out.printf("result = %s date = %s player = %s\n", m.group(1),
							// m.group(2), m.group(3));
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} else
			hiScore = (int) score;
	}

	public void scoreReset() {
		if (score > hiScore)
			hiScore = (int) score;
		score = 0;
	}

	public void draw(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;
		int scoreArray[] = scoreToArray(score);
		for (int i = 0; i < SCORE_LENGTH; i++) {
			// this if needed to make blinking animation when score increased by 100
			if ((!((int) score >= 12 && (int) score % 100 <= 12) || (int) score % 3 == 0)
					|| gameScreen.getGameState() == GameState.GAME_STATE_OVER)
				g2d.drawImage(cropImage(numbers, scoreArray[SCORE_LENGTH - i - 1]), CURRENT_SCORE_X + i * NUMBER_WIDTH, SCORE_Y,
						null);
		}
		if (hiScore > 0) {
			int hiScoreArray[] = scoreToArray(hiScore);
			g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
			for (int i = 0; i < SCORE_LENGTH; i++) {
				g2d.drawImage(cropImage(numbers, hiScoreArray[SCORE_LENGTH - i - 1]), HI_SCORE_X + i * NUMBER_WIDTH, SCORE_Y,
						null);
			}
			g2d.drawImage(hi, HI_X, SCORE_Y, null);
			g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
		}
	}

}
