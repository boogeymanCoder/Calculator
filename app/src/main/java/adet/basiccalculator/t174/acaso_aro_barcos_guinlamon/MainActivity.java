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
        String prevEx = expression;
        expression += "/100";
        equals(view);

        expression = prevEx;
        expressionTextView.setText(expression);
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