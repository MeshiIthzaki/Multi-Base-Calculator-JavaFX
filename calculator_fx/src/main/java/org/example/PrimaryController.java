package org.example;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import java.util.Objects;
import java.util.Stack;

public class PrimaryController {

    @FXML // letters buttons
    private Button aBtn, bBtn, cBtn, dBtn, eBtn, fBtn;

    @FXML // numbers buttons
    private Button zeroBtn, oneBtn, twoBtn, threeBtn, fourBtn, fiveBtn, sixBtn, sevenBtn, eightBtn, nineBtn;

    @FXML // operator buttons
    private Button addBtn, subBtn, mulBtn, divBtn, equalBtn, clearBtn;

    @FXML
    private ComboBox<String> baseBox;

    @FXML
    private TextField displayTF;

    private int base = 0;

    @FXML
    private void chooseBase(ActionEvent event) {
        int prevBase = base;
        String chosen = baseBox.getSelectionModel().getSelectedItem();
        switch (chosen){
            case "BIN":
                base = 2;
                convertExpression(prevBase, base);
                setBinBtns();
                break;
            case "OCT":
                base = 8;
                convertExpression(prevBase, base);
                setOctBtns();
                break;
            case "DEC":
                base = 10;
                convertExpression(prevBase, base);
                setDecBtns();
                break;
            case "HEX":
                base = 16;
                convertExpression(prevBase, base);
                setDecBtns();
                enableLetters();
                break;
        }
    }

    @FXML
    private void convertExpression(int prevBase, int newBase) {
        if (prevBase == 0) {
            prevBase = newBase;
        }
        String expression = displayTF.getText();
        // Split the expression using regex to separate numbers and operators
        String[] parts = expression.split("(?<=[+\\-*/])|(?=[+\\-*/])");

        StringBuilder result = new StringBuilder();

        for (String part : parts) {
            if (!part.isEmpty()) { // Ignore empty parts
                try {
                    // Attempt to parse the part as a number
                    int num = Integer.parseInt(part, prevBase);
                    String convertedNum = Integer.toString(num, newBase);
                    result.append(convertedNum);
                } catch (NumberFormatException e) {
                    // If parsing as a number fails, treat it as an operator
                    result.append(part);
                }
            }
        }
        // Set the result to the display text field
        displayTF.setText(result.toString().toUpperCase());
    }

    private void setBinBtns(){
        twoBtn.setDisable(true);
        threeBtn.setDisable(true);
        fourBtn.setDisable(true);
        fiveBtn.setDisable(true);
        sixBtn.setDisable(true);
        sevenBtn.setDisable(true);
        eightBtn.setDisable(true);
        nineBtn.setDisable(true);
        disableLetters();
    }

    private void setOctBtns() {
        twoBtn.setDisable(false);
        threeBtn.setDisable(false);
        fourBtn.setDisable(false);
        fiveBtn.setDisable(false);
        sixBtn.setDisable(false);
        sevenBtn.setDisable(false);
        eightBtn.setDisable(true);
        nineBtn.setDisable(true);
        disableLetters();
    }

    private void setDecBtns() {
        twoBtn.setDisable(false);
        threeBtn.setDisable(false);
        fourBtn.setDisable(false);
        fiveBtn.setDisable(false);
        sixBtn.setDisable(false);
        sevenBtn.setDisable(false);
        eightBtn.setDisable(false);
        nineBtn.setDisable(false);
        disableLetters();
    }

    private void enableLetters(){
        aBtn.setDisable(false);
        bBtn.setDisable(false);
        cBtn.setDisable(false);
        dBtn.setDisable(false);
        eBtn.setDisable(false);
        fBtn.setDisable(false);
    }

    private void disableLetters(){
        aBtn.setDisable(true);
        bBtn.setDisable(true);
        cBtn.setDisable(true);
        dBtn.setDisable(true);
        eBtn.setDisable(true);
        fBtn.setDisable(true);
    }

    @FXML
    private void typeChar(ActionEvent event) {
        if (baseBox.getValue() == null)
            return;

        if (displayTF.getText().equals("Error: invalid expression: \"\"") ||
                displayTF.getText().equals("Error: trying to divide by 0 (evaluated: \"0\")") ||
                displayTF.getText().equals("ERROR: TRYING TO DIVIDE BY 0 (EVALUATED: \"0\")") ||
                displayTF.getText().equals("ERROR: INVALID EXPRESSION: \"\""))
            displayTF.clear();

        Button btn = (Button) event.getSource();
        displayTF.appendText(btn.getText());
    }

    @FXML
    private void pressClear(ActionEvent event) {
        displayTF.clear();
    }

    @FXML
    private void pressEqual(ActionEvent event) {
        String expression = displayTF.getText();
        if (base == 0) base = 10;
        String result = calculate(expression, base);
        displayTF.setText(result);
    }

    @FXML
    private void initialize() {
        baseBox.getItems().add("BIN");
        baseBox.getItems().add("OCT");
        baseBox.getItems().add("DEC");
        baseBox.getItems().add("HEX");
    }

    private String calculate(String expression, int base) {
        if (!isValidExpression(expression, base)) {
            return "Error: invalid expression: \"\"";
        }

        try {
            String result = evaluateExpression(expression, base);
            if (Objects.equals(result, "Error: trying to divide by 0 (evaluated: \"0\")")) {
                return "Error: trying to divide by 0 (evaluated: \"0\")";
            }
            return result.toUpperCase();
        } catch (ArithmeticException e) {
            System.out.println(e.getMessage());
        }
        return "Error: invalid expression: \"\"";
    }

    private String evaluateExpression(String expression, int base) {
        // Use a more complex regex to split on operators while keeping them as separate tokens.
        String[] tokens = expression.split("(?<=[+\\-*/])|(?=[+\\-*/])");
        Stack<Double> values = new Stack<>();
        Stack<String> ops = new Stack<>();
        for (String token : tokens) {
            token = token.trim();
            if (token.isEmpty()) continue;
            if (token.matches("[0-9A-F]+")) {
                values.push(Double.parseDouble(Integer.toString(Integer.parseInt(token, base))));
            } else if (token.matches("[+\\-*/]")) {
                while (!ops.isEmpty() && hasPrecedence(token, ops.peek())) {
                    values.push(applyOp(ops.pop(), values.pop(), values.pop()));
                }
                ops.push(token);
            }
        }

        while (!ops.isEmpty()) {
            values.push(applyOp(ops.pop(), values.pop(), values.pop()));
        }

        if (Objects.equals(displayTF.getText(), "Error: trying to divide by 0 (evaluated: \"0\")")) {
            return "Error: trying to divide by 0 (evaluated: \"0\")";
        }
        // Converting the result back to the requested base as a string
        return Integer.toString(values.pop().intValue(), base);
    }

    private boolean hasPrecedence(String op1, String op2) {
        return (!op2.equals("+") && !op2.equals("-")) || (!op1.equals("*") && !op1.equals("/"));
    }

    @FXML
    private double applyOp(String op, double b, double a) {
        switch (op) {
            case "+":
                return a + b;
            case "-":
                return a - b;
            case "*":
                return a * b;
            case "/":
            case "\\":
                if (b == 0) {
                    // Print the error message for division by zero and exit.
                    displayTF.setText("Error: trying to divide by 0 (evaluated: \"0\")");
                    return -1;
                }
                return a / b;
            default:
                // Exit silently for any invalid operation
                System.exit(1);
                return 0; // This is required for compilation, but will never be reached due to the exit above.
        }
    }

    private boolean isValidExpression(String expression, int base) {

        // Check for empty expression
        if (expression.isEmpty()) {
            return false;
        }
        // Check for consecutive digits separated by spaces
        if (expression.matches(".*\\d\\s+\\d.*")) {
            return false;
        }

        // Remove all spaces from the expression
        expression = expression.replaceAll("\\s+", "");

        // Check if the expression starts or ends with an operator
        if (expression.startsWith("+") || expression.startsWith("*") || expression.startsWith("/") || expression.startsWith("-")) {
            return false;
        }
        if (expression.endsWith("+") || expression.endsWith("-") || expression.endsWith("*") || expression.endsWith("/")) {
            return false;
        }
        // Check for consecutive operators (e.g., ++, --, **, //, etc.)
        if (expression.matches(".*[+\\-*/]{2,}.*")) {
            return false;
        }

        // Define valid character sets based on the base
        String validChars;
        switch (base) {
            case 2:
                validChars = "01";
                break;
            case 8:
                validChars = "01234567";
                break;
            case 10:
                validChars = "0123456789";
                break;
            case 16:
                validChars = "0123456789ABCDEF";
                break;
            default:
                // If the base is not 2, 8, 10, or 16, then it's not supported
                return false;
        }

        // Build the regex pattern using the valid character set
        String pattern = "[" + validChars + "+\\-*/]+";

        // Check for a valid pattern
        return expression.matches(pattern);
    }
}
