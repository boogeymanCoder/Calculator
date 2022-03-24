package adet.basiccalculator.t174.acaso_aro_barcos_guinlamon;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Arrays;


public class MainActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener{
    Switch lightDarkSwitch = null;
    TextView answerTextView = null;
    TextView expressionTextView = null;
    String expression = "";
    Boolean darkMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lightDarkSwitch = (Switch) findViewById(R.id.lightDarkSwitch);
        lightDarkSwitch.setOnCheckedChangeListener(this);
        lightDarkSwitch.setChecked(darkMode);

        answerTextView = (TextView) findViewById(R.id.answerTextView);
        expressionTextView = (TextView) findViewById(R.id.expressionTextView);
        System.out.println("onCreate");
    }

    // Method of recovering values after config change
    @Override
    protected void onRestoreInstanceState (Bundle savedInstanceState) {
        super.onRestoreInstanceState (savedInstanceState);

        expression = savedInstanceState.getString("expression");
        expressionTextView.setText(expression);
        answerTextView.setText(savedInstanceState.getString("answer"));
        System.out.println("onRestoreInstanceState " + expression);

        darkMode = savedInstanceState.getBoolean("darkMode");
        lightDarkSwitch.setChecked(darkMode);
    }

    // Save the variable before config change
    @Override
    protected void onSaveInstanceState (Bundle outState) {
        super.onSaveInstanceState (outState);

        outState.putBoolean("darkMode", darkMode);
        outState.putString("expression", expression);
        outState.putString("answer", answerTextView.getText().toString());
        System.out.println("onSaveInstanceState");
    }


    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            System.out.println("checked");
            darkMode = true;
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            lightDarkSwitch.setText("Dark");
        } else {
            darkMode = false;
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            System.out.println("unchecked");
            lightDarkSwitch.setText("Light");
        }
    }

    public void solve() {
        try {
            Double result = eval(expression);
            System.out.println("expression: " + expression);
            System.out.println("result: " + result);

            if(result % 1 == 0){
                try {
                    int intAnswer = Integer.parseInt(String.valueOf(Math.round(result)));
                    answerTextView.setText(String.valueOf(intAnswer));
                    return;
                } catch (Exception e) {
                    System.out.println("Error: Cannot parse answer to int");
                }
            }
            BigDecimal d = new BigDecimal(String.valueOf(result));
            answerTextView.setText(d.toString());
        } catch (Exception e) {
            System.out.println("Cannot solve");
        }
    }

    public void equals(View view) {
        try {
            Double result = eval(expression);
            System.out.println("expression: " + expression);
            System.out.println("result: " + result);

            if(result % 1 == 0){
                try {
                    int intAnswer = Integer.parseInt(String.valueOf(Math.round(result)));
                    expression = String.valueOf(intAnswer);
                    expressionTextView.setText(expression);
                    answerTextView.setText(String.valueOf(intAnswer));
                    return;
                } catch (Exception e) {
                    System.out.println("Error: Cannot parse answer to int");
                }
            }
                BigDecimal d = new BigDecimal(String.valueOf(result));
                expression = d.toString();
                expressionTextView.setText(expression);
                answerTextView.setText(d.toString());
        } catch (Exception e) {
            answerTextView.setText("Error");
        }
    }

    public void clear(View view) {
        expression = "";

        expressionTextView.setText(expression);
        answerTextView.setText("");
    }

    public void percent(View view) {
        if (expression.isEmpty()) return;
//        String prevEx = expression;
//        expression += "/100";
//        equals(view);
//
//        expression = prevEx;
//        expressionTextView.setText(expression);

        try{
//            Step 1: get last number entered
            String lastNumber = getLastNumber(expression);

//            Step 2: Remove last number from expression
            int lastNumberLength = lastNumber.length();
            int expressionLength = expression.length();
            int end = expressionLength - lastNumberLength;
            String strippedExpression = expression.substring(0, end);

//            Step 3: Get biggest solvable value by stripping expression from 0 - last character
            Double bsv = null;
            try {
                bsv = biggestSolvableValue(strippedExpression);
            } catch(Exception e) {
                System.out.println("Inner percent error: " + e);

                expression = String.valueOf(eval(expression + "/100"));
                solve();
                expression = answerTextView.getText().toString();
                expressionTextView.setText(expression);
                return;
            }

//            Step 4: (bsv / 100) * lastNumberValue
            Double lastNumberValue = Double.parseDouble(lastNumber);
            Double percentileValue = (bsv / 100) * lastNumberValue;

//            Step 5: replace last number with percentile value
            expression = strippedExpression + toWhole(percentileValue);
            solve();
            expressionTextView.setText(expression);

            System.out.println("last number: " + lastNumber);
            System.out.println("stripped expression: " + strippedExpression);
            System.out.println("bsv: " + bsv);

        } catch(Exception e) {
            System.out.println("Percent error: " + e);
            return;
        }
    }

//  Gets biggest solvable value
    public Double biggestSolvableValue(String  exp) throws Exception{
        Double lastSolved = null;

        for(int i = 1; i < exp.length(); i++) {
            String strip = exp.substring(0, i);
            System.out.println("strip: " + strip);
            try {
                lastSolved = eval(strip);
            } catch (Exception e) {
                System.out.println("Cannot solve: " + strip);
            }
        }

        if (lastSolved == null) throw new Exception("Cannot get biggest solvable value");

        return lastSolved;
    }

//  Gets last number entered to the expression
    public String getLastNumber(String exp) throws Exception{
        Double lastNumber = null;
        int operatorIndex = 0;
        for(int i = exp.length() - 1; i >= 0; i--) {
            if(exp.charAt(i) == '.') {
                continue;
            }
            try{
                lastNumber = Double.parseDouble(exp.substring(i));
            } catch (Exception e) {
                System.out.println("Encountered none number value");
            }
        }
        if(lastNumber == null) throw new Exception("no last number");
//  Convert to int if whole
        return toWhole(lastNumber);
    }

//    Returns true if character is operator
    public boolean isOperator(char ch) {
        char[] operators = {'+', '-', '*', '/'};

        for (char operator: operators) {
            if (ch == operator) return true;
        }

        return false;
    }

//    Converts double to whole number string if possible
    public String toWhole(Double val) {
        if(val % 1 == 0){
            try {
                int intLastNumber = Integer.parseInt(String.valueOf(Math.round(val)));
                return String.valueOf(intLastNumber);
            } catch (Exception e) {
                System.out.println("Error: Cannot parse value to int, value:" + val);
            }
        }
        return String.valueOf(val);
    }

    public void erase(View view) {
        if(expression.equals("Error")) {
            expression = "";
            answerTextView.setText("");
        }

        if (expression.length() > 1) {
            expression = expression.substring(0, expression.length() - 1);

            if(!answerTextView.getText().toString().equals("")) {
                solve();
            }
        } else {
            expression = "";
            answerTextView.setText("");
        }

        expressionTextView.setText(expression);
    }

    public void onButtonClicked(View view) {
        String value = ((Button) view).getText().toString();

        try {
            eval(expression + value + "1");

            expressionTextView.setText(expression += value);
            solve();
        } catch (Exception e) {
            System.out.println("Cannot enter value, cause: " + e.toString());
        }
    }

    public static double eval(final String str) {
        return new Object() {
            int pos = -1, ch;

            void nextChar() {
                ch = (++pos < str.length()) ? str.charAt(pos) : -1;
            }

            boolean eat(int charToEat) {
                while (ch == ' ') nextChar();
                if (ch == charToEat) {
                    nextChar();
                    return true;
                }
                return false;
            }

            double parse() {
                nextChar();
                double x = parseExpression();
                if (pos < str.length()) throw new RuntimeException("Unexpected: " + (char)ch);
                return x;
            }

            // Grammar:
            // expression = term | expression `+` term | expression `-` term
            // term = factor | term `*` factor | term `/` factor
            // factor = `+` factor | `-` factor | `(` expression `)`
            //        | number | functionName factor | factor `^` factor

            double parseExpression() {
                double x = parseTerm();
                for (;;) {
                    if      (eat('+')) x += parseTerm(); // addition
                    else if (eat('-')) x -= parseTerm(); // subtraction
                    else return x;
                }
            }

            double parseTerm() {
                double x = parseFactor();
                for (;;) {
                    if      (eat('*')) x *= parseFactor(); // multiplication
                    else if (eat('/')) x /= parseFactor(); // division
                    else return x;
                }
            }

            double parseFactor() {
                if (eat('+')) return parseFactor(); // unary plus
                if (eat('-')) return -parseFactor(); // unary minus

                double x;
                int startPos = this.pos;
                if (eat('(')) { // parentheses
                    x = parseExpression();
                    eat(')');
                } else if ((ch >= '0' && ch <= '9') || ch == '.') { // numbers
                    while ((ch >= '0' && ch <= '9') || ch == '.') nextChar();
                    x = Double.parseDouble(str.substring(startPos, this.pos));
                } else if (ch >= 'a' && ch <= 'z') { // functions
                    while (ch >= 'a' && ch <= 'z') nextChar();
                    String func = str.substring(startPos, this.pos);
                    x = parseFactor();
                    if (func.equals("sqrt")) x = Math.sqrt(x);
                    else if (func.equals("sin")) x = Math.sin(Math.toRadians(x));
                    else if (func.equals("cos")) x = Math.cos(Math.toRadians(x));
                    else if (func.equals("tan")) x = Math.tan(Math.toRadians(x));
                    else throw new RuntimeException("Unknown function: " + func);
                } else {
                    throw new RuntimeException("Unexpected: " + (char)ch);
                }

                if (eat('^')) x = Math.pow(x, parseFactor()); // exponentiation

                return x;
            }
        }.parse();
    }
}