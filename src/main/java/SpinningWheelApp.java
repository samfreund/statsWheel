import javafx.animation.*;
import javafx.application.Application;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.canvas.*;
import javafx.scene.control.Button;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.text.*;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;
import javafx.util.Duration;

public class SpinningWheelApp extends Application {

    private static final int WHEEL_RADIUS = 200;
    private static final int SEGMENT_COUNT = 12;
    private static final String[] SEGMENT_LABELS = {
            "100", "200", "300", "400", "500", "600",
            "700", "800", "900", "Bankrupt", "Lose Turn", "1000"
    };

    private Group wheelGroup;
    private Rotate wheelRotation;

    @Override
    public void start(Stage primaryStage) {
        // Create wheel
        wheelGroup = new Group();
        drawWheel();

        // Set up rotation transform
        wheelRotation = new Rotate(0, 0, 0, 0, Rotate.Z_AXIS);
        wheelGroup.getTransforms().add(wheelRotation);

        // Center and scale wheel
        StackPane wheelContainer = new StackPane(wheelGroup);
        wheelContainer.setPrefSize(500, 500);
        wheelGroup.setTranslateX(250);
        wheelGroup.setTranslateY(250);

        // Spin button
        Button spinButton = new Button("Spin!");
        spinButton.setOnAction(e -> spinWheel());

        VBox root = new VBox(10, wheelContainer, spinButton);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(20));

        Scene scene = new Scene(root, 600, 600);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Wheel of Fortune Spinner");
        primaryStage.show();
    }

    private void drawWheel() {
        double anglePerSegment = 360.0 / SEGMENT_COUNT;

        for (int i = 0; i < SEGMENT_COUNT; i++) {
            Arc arc = new Arc(0, 0, WHEEL_RADIUS, WHEEL_RADIUS, i * anglePerSegment, anglePerSegment);
            arc.setType(ArcType.ROUND);
            arc.setFill(i % 2 == 0 ? Color.LIGHTBLUE : Color.LIGHTGREEN);
            arc.setStroke(Color.BLACK);

            Text label = new Text(SEGMENT_LABELS[i]);
            label.setFont(Font.font("Arial", FontWeight.BOLD, 14));
            label.setFill(Color.BLACK);

            double midAngle = (i + 0.5) * anglePerSegment;
            double radians = Math.toRadians(midAngle);
            double labelX = Math.cos(radians) * WHEEL_RADIUS * 0.6;
            double labelY = Math.sin(radians) * WHEEL_RADIUS * 0.6;
            label.setX(labelX - 20);
            label.setY(labelY);

            wheelGroup.getChildren().addAll(arc, label);
        }
    }

    private void spinWheel() {
        double rotations = 5 + Math.random() * 3; // 5 to 8 full spins
        double finalAngle = rotations * 360;

        KeyValue kv = new KeyValue(wheelRotation.angleProperty(), wheelRotation.getAngle() + finalAngle, Interpolator.EASE_OUT);
        KeyFrame kf = new KeyFrame(Duration.seconds(5), kv);
        Timeline timeline = new Timeline(kf);
        timeline.play();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
