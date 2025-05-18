import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class SpinningWheelApp extends Application {

    // Constants for the roulette wheel
    private static final int WHEEL_RADIUS = 200;
    private static final int WHEEL_CENTER_RADIUS = 50;
    private static final int TOTAL_NUMBERS = 37; // 0-36
    private static final int[] ROULETTE_NUMBERS = {
            0, 32, 15, 19, 4, 21, 2, 25, 17, 34, 6, 27, 13, 36, 11, 30, 8, 23, 10, 5, 24, 16, 33, 1, 20, 14, 31, 9, 22, 18, 29, 7, 28, 12, 35, 3, 26
    };
    
    // Colors for roulette numbers
    private static final Map<Integer, Color> NUMBER_COLORS = new HashMap<>();
    static {
        NUMBER_COLORS.put(0, Color.GREEN);
        for (int i = 1; i <= 36; i++) {
            if (i == 1 || i == 3 || i == 5 || i == 7 || i == 9 || i == 12 || i == 14 || 
                i == 16 || i == 18 || i == 19 || i == 21 || i == 23 || i == 25 || i == 27 || 
                i == 30 || i == 32 || i == 34 || i == 36) {
                NUMBER_COLORS.put(i, Color.RED);
            } else {
                NUMBER_COLORS.put(i, Color.BLACK);
            }
        }
    }

    // UI components
    private Group wheelGroup;
    private Rotate wheelRotation;
    private Timeline spinTimeline;
    private Label resultLabel;
    private Label balanceLabel;
    private Map<String, TextField> betFields = new HashMap<>();
    private double balance = 1000.0;
    private int lastResult = -1;
    
    // For tracking bets
    private Map<String, Double> currentBets = new HashMap<>();

    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();
        
        // Create wheel view
        wheelGroup = new Group();
        drawRouletteWheel();
        
        Group wheelContainer = new Group();
        wheelContainer.getChildren().add(wheelGroup);
        
        // Add a pointer at the top
        Line pointer = new Line(0, -WHEEL_RADIUS - 20, 0, -WHEEL_RADIUS);
        pointer.setStrokeWidth(3);
        pointer.setStroke(Color.GOLD);
        
        Group wheelWithPointer = new Group();
        wheelWithPointer.getChildren().addAll(wheelContainer, pointer);
        wheelWithPointer.setTranslateX(WHEEL_RADIUS + 50);
        wheelWithPointer.setTranslateY(WHEEL_RADIUS + 50);
        
        VBox wheelBox = new VBox(20);
        wheelBox.setAlignment(Pos.CENTER);
        wheelBox.getChildren().add(wheelWithPointer);
        
        // Result display
        resultLabel = new Label("Spin the wheel to start");
        resultLabel.setFont(Font.font(18));
        resultLabel.setStyle("-fx-font-weight: bold;");
        
        // Balance display
        balanceLabel = new Label("Balance: $" + balance);
        balanceLabel.setFont(Font.font(16));
        
        wheelBox.getChildren().addAll(resultLabel, balanceLabel);
        root.setCenter(wheelBox);
        
        // Create betting area
        VBox bettingArea = createBettingArea();
        root.setRight(bettingArea);
        
        // Set scene
        Scene scene = new Scene(root, 900, 700);
        primaryStage.setTitle("Roulette Wheel");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    private void drawRouletteWheel() {
        // Background circle
        Circle background = new Circle(0, 0, WHEEL_RADIUS);
        background.setFill(Color.DARKBLUE);
        background.setStroke(Color.GOLD);
        background.setStrokeWidth(2);
        wheelGroup.getChildren().add(background);
        
        // Wheel segments
        double segmentAngle = 360.0 / TOTAL_NUMBERS;
        for (int i = 0; i < TOTAL_NUMBERS; i++) {
            int number = ROULETTE_NUMBERS[i];
            double startAngle = i * segmentAngle;
            
            // Create segment
            Arc segment = new Arc(0, 0, WHEEL_RADIUS, WHEEL_RADIUS, startAngle, segmentAngle);
            segment.setType(ArcType.ROUND);
            segment.setFill(NUMBER_COLORS.get(number));
            segment.setStroke(Color.GOLD);
            segment.setStrokeWidth(1);
            
            // Number text
            Text text = new Text(String.valueOf(number));
            text.setFill(Color.WHITE);
            text.setFont(Font.font(14));
            
            // Position the text
            double textAngle = startAngle + segmentAngle / 2;
            double textRadius = WHEEL_RADIUS * 0.75;
            double textX = -Math.sin(Math.toRadians(textAngle)) * textRadius;
            double textY = Math.cos(Math.toRadians(textAngle)) * textRadius;
            
            text.setX(textX - text.getBoundsInLocal().getWidth() / 2);
            text.setY(textY + text.getBoundsInLocal().getHeight() / 4);
            
            // Rotate text to be tangential to the wheel
            Rotate textRotate = new Rotate(90 - textAngle);
            text.getTransforms().add(textRotate);
            
            wheelGroup.getChildren().addAll(segment, text);
        }
        
        // Center circle
        Circle center = new Circle(0, 0, WHEEL_CENTER_RADIUS);
        center.setFill(Color.DARKGRAY);
        center.setStroke(Color.GOLD);
        center.setStrokeWidth(2);
        wheelGroup.getChildren().add(center);
        
        // Setup rotation
        wheelRotation = new Rotate(0, 0, 0);
        wheelGroup.getTransforms().add(wheelRotation);
    }
    
    private VBox createBettingArea() {
        VBox bettingArea = new VBox(15);
        bettingArea.setPadding(new Insets(20));
        bettingArea.setAlignment(Pos.TOP_CENTER);
        
        Label bettingTitle = new Label("Place Your Bets");
        bettingTitle.setFont(Font.font(18));
        bettingTitle.setStyle("-fx-font-weight: bold;");
        
        // Bet types
        GridPane betGrid = new GridPane();
        betGrid.setHgap(10);
        betGrid.setVgap(10);
        
        int row = 0;
        
        // Straight bets (single numbers)
        addBetRow(betGrid, row++, "Straight (0-36)", "straightBet", "Number:");
        
        // Color bets
        addBetRow(betGrid, row++, "Red", "redBet", "Amount:");
        addBetRow(betGrid, row++, "Black", "blackBet", "Amount:");
        
        // Odd/Even bets
        addBetRow(betGrid, row++, "Odd", "oddBet", "Amount:");
        addBetRow(betGrid, row++, "Even", "evenBet", "Amount:");
        
        // Dozen bets
        addBetRow(betGrid, row++, "1st Dozen (1-12)", "firstDozenBet", "Amount:");
        addBetRow(betGrid, row++, "2nd Dozen (13-24)", "secondDozenBet", "Amount:");
        addBetRow(betGrid, row++, "3rd Dozen (25-36)", "thirdDozenBet", "Amount:");
        
        // High/Low bets
        addBetRow(betGrid, row++, "Low (1-18)", "lowBet", "Amount:");
        addBetRow(betGrid, row++, "High (19-36)", "highBet", "Amount:");
        
        // Spin button
        Button spinButton = new Button("SPIN");
        spinButton.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-background-color: #c91b1b; -fx-text-fill: white;");
        spinButton.setPrefWidth(150);
        spinButton.setOnAction(e -> spinWheel());
        
        // Clear bets button
        Button clearButton = new Button("Clear Bets");
        clearButton.setStyle("-fx-font-size: 14px;");
        clearButton.setPrefWidth(150);
        clearButton.setOnAction(e -> clearBets());
        
        HBox buttonBox = new HBox(15, spinButton, clearButton);
        buttonBox.setAlignment(Pos.CENTER);
        
        bettingArea.getChildren().addAll(bettingTitle, betGrid, buttonBox);
        return bettingArea;
    }
    
    private void addBetRow(GridPane grid, int row, String betName, String betKey, String fieldLabel) {
        Label nameLabel = new Label(betName);
        Label amountLabel = new Label(fieldLabel);
        TextField amountField = new TextField("0");
        amountField.setPrefWidth(80);
        
        grid.add(nameLabel, 0, row);
        grid.add(amountLabel, 1, row);
        grid.add(amountField, 2, row);
        
        betFields.put(betKey, amountField);
    }
    
    private void spinWheel() {
        // Collect bets
        collectBets();
        
        // Calculate total bet amount
        double totalBet = currentBets.values().stream().mapToDouble(Double::doubleValue).sum();
        
        // Check if player has enough balance
        if (totalBet > balance) {
            resultLabel.setText("Insufficient funds!");
            resultLabel.setTextFill(Color.RED);
            return;
        }
        
        // Deduct bet amount from balance
        balance -= totalBet;
        updateBalanceDisplay();
        
        // Generate a random spin
        Random random = new Random();
        int spinDuration = 5000 + random.nextInt(2000); // 5-7 seconds
        int numRotations = 10 + random.nextInt(5);
        
        // Calculate total rotation
        double targetAngle = 360 * numRotations + random.nextInt(360);
        
        // Animation for spinning the wheel
        if (spinTimeline != null && spinTimeline.getStatus() == Animation.Status.RUNNING) {
            spinTimeline.stop();
        }
        
        SimpleDoubleProperty angle = new SimpleDoubleProperty(0);
        angle.addListener((obs, oldVal, newVal) -> {
            wheelRotation.setAngle(newVal.doubleValue());
        });
        
        spinTimeline = new Timeline(
            new KeyFrame(Duration.ZERO, new KeyValue(angle, wheelRotation.getAngle())),
            new KeyFrame(Duration.millis(spinDuration), new KeyValue(angle, targetAngle))
        );
        
        spinTimeline.setOnFinished(e -> handleSpinResult(targetAngle));
        spinTimeline.play();
        
        // Update UI state
        resultLabel.setText("Spinning...");
        resultLabel.setTextFill(Color.BLACK);
    }
    
    private void handleSpinResult(double finalAngle) {
        // Calculate which number the wheel landed on
        double segmentAngle = 360.0 / TOTAL_NUMBERS;
        int segmentIndex = (int) (Math.floor(finalAngle % 360 / segmentAngle)) % TOTAL_NUMBERS;
        segmentIndex = (TOTAL_NUMBERS - segmentIndex) % TOTAL_NUMBERS; // Reverse direction
        
        lastResult = ROULETTE_NUMBERS[segmentIndex];
        
        // Display result
        Color resultColor = NUMBER_COLORS.get(lastResult);
        String colorName = (resultColor == Color.RED) ? "RED" : (resultColor == Color.BLACK) ? "BLACK" : "GREEN";
        
        resultLabel.setText("Result: " + lastResult + " " + colorName);
        resultLabel.setTextFill(resultColor);
        
        // Process winnings
        processWinnings();
    }
    
    private void collectBets() {
        currentBets.clear();
        
        // Process each bet type
        for (Map.Entry<String, TextField> entry : betFields.entrySet()) {
            String betKey = entry.getKey();
            TextField field = entry.getValue();
            
            try {
                double amount = Double.parseDouble(field.getText());
                if (amount > 0) {
                    currentBets.put(betKey, amount);
                }
            } catch (NumberFormatException e) {
                // Invalid input, ignore
                field.setText("0");
            }
        }
    }
    
    private void processWinnings() {
        double winnings = 0;
        
        // Check each bet type for wins
        for (Map.Entry<String, Double> bet : currentBets.entrySet()) {
            String betType = bet.getKey();
            double betAmount = bet.getValue();
            
            switch (betType) {
                case "straightBet":
                    try {
                        int betNumber = Integer.parseInt(betFields.get(betType).getText());
                        if (betNumber == lastResult) {
                            winnings += betAmount * 36; // 35:1 payout plus original bet
                        }
                    } catch (NumberFormatException e) {
                        // Invalid number, no win
                    }
                    break;
                    
                case "redBet":
                    if (NUMBER_COLORS.get(lastResult) == Color.RED) {
                        winnings += betAmount * 2; // 1:1 payout
                    }
                    break;
                    
                case "blackBet":
                    if (NUMBER_COLORS.get(lastResult) == Color.BLACK) {
                        winnings += betAmount * 2; // 1:1 payout
                    }
                    break;
                    
                case "oddBet":
                    if (lastResult > 0 && lastResult % 2 == 1) {
                        winnings += betAmount * 2; // 1:1 payout
                    }
                    break;
                    
                case "evenBet":
                    if (lastResult > 0 && lastResult % 2 == 0) {
                        winnings += betAmount * 2; // 1:1 payout
                    }
                    break;
                    
                case "firstDozenBet":
                    if (lastResult >= 1 && lastResult <= 12) {
                        winnings += betAmount * 3; // 2:1 payout
                    }
                    break;
                    
                case "secondDozenBet":
                    if (lastResult >= 13 && lastResult <= 24) {
                        winnings += betAmount * 3; // 2:1 payout
                    }
                    break;
                    
                case "thirdDozenBet":
                    if (lastResult >= 25 && lastResult <= 36) {
                        winnings += betAmount * 3; // 2:1 payout
                    }
                    break;
                    
                case "lowBet":
                    if (lastResult >= 1 && lastResult <= 18) {
                        winnings += betAmount * 2; // 1:1 payout
                    }
                    break;
                    
                case "highBet":
                    if (lastResult >= 19 && lastResult <= 36) {
                        winnings += betAmount * 2; // 1:1 payout
                    }
                    break;
            }
        }
        
        // Update balance
        if (winnings > 0) {
            balance += winnings;
            resultLabel.setText(resultLabel.getText() + " - You won $" + winnings + "!");
        }
        
        updateBalanceDisplay();
    }
    
    private void clearBets() {
        for (TextField field : betFields.values()) {
            field.setText("0");
        }
        currentBets.clear();
    }
    
    private void updateBalanceDisplay() {
        balanceLabel.setText("Balance: $" + String.format("%.2f", balance));
    }

    public static void main(String[] args) {
        launch(args);
    }
}