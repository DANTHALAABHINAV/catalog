import java.io.*;
import java.math.BigInteger;
import java.util.*;
import java.util.regex.*;

public class PolynomialBigIntegerOnly {
    public static void main(String[] args) throws IOException {
        // Read JSON input
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader("input2.json"))) {
            String line;
            while ((line = br.readLine()) != null) sb.append(line.trim());
        }
        String json = sb.toString();

        // Extract n and k
        int n = extractInt(json, "\"n\"\\s*:\\s*(\\d+)");
        int k = extractInt(json, "\"k\"\\s*:\\s*(\\d+)");

        // Parse x and y points
        Pattern p = Pattern.compile("\"(\\d+)\"\\s*:\\s*\\{[^}]*?\"base\"\\s*:\\s*\"(\\d+)\"[^}]*?\"value\"\\s*:\\s*\"([\\da-zA-Z]+)\"");
        Matcher m = p.matcher(json);

        List<Integer> xList = new ArrayList<>();
        List<BigInteger> yList = new ArrayList<>();

        while (m.find()) {
            int x = Integer.parseInt(m.group(1));
            int base = Integer.parseInt(m.group(2));
            String val = m.group(3);
            BigInteger y = new BigInteger(val, base);
            xList.add(x);
            yList.add(y);
        }

        int size = k + 1;
        Fraction[][] A = new Fraction[size][size];
        Fraction[] B = new Fraction[size];

        for (int i = 0; i < size; i++) {
            int x = xList.get(i);
            BigInteger xi = BigInteger.valueOf(x);
            B[i] = new Fraction(yList.get(i));
            for (int j = 0; j < size; j++) {
                BigInteger power = xi.pow(k - j);
                A[i][j] = new Fraction(power);
            }
        }

        Fraction[] result = gaussianElimination(A, B);
        Fraction constant = result[size - 1]; // constant term is last
        System.out.println("Constant term: " + constant); // Output only as fraction
    }

    static int extractInt(String json, String pattern) {
        Matcher matcher = Pattern.compile(pattern).matcher(json);
        return matcher.find() ? Integer.parseInt(matcher.group(1)) : -1;
    }

    static Fraction[] gaussianElimination(Fraction[][] A, Fraction[] B) {
        int n = B.length;
        Fraction[][] mat = new Fraction[n][n + 1];

        // Build augmented matrix
        for (int i = 0; i < n; i++) {
            System.arraycopy(A[i], 0, mat[i], 0, n);
            mat[i][n] = B[i];
        }

        // Forward elimination
        for (int i = 0; i < n; i++) {
            // Pivot
            int maxRow = i;
            for (int k = i + 1; k < n; k++) {
                if (mat[k][i].abs().compareTo(mat[maxRow][i].abs()) > 0) {
                    maxRow = k;
                }
            }

            Fraction[] temp = mat[i];
            mat[i] = mat[maxRow];
            mat[maxRow] = temp;

            // Normalize and eliminate
            for (int k = i + 1; k < n; k++) {
                Fraction factor = mat[k][i].divide(mat[i][i]);
                for (int j = i; j <= n; j++) {
                    mat[k][j] = mat[k][j].subtract(factor.multiply(mat[i][j]));
                }
            }
        }

        // Back-substitution
        Fraction[] x = new Fraction[n];
        for (int i = n - 1; i >= 0; i--) {
            Fraction sum = mat[i][n];
            for (int j = i + 1; j < n; j++) {
                sum = sum.subtract(mat[i][j].multiply(x[j]));
            }
            x[i] = sum.divide(mat[i][i]);
        }

        return x;
    }

    // Fraction class using BigInteger
    static class Fraction {
        BigInteger num;
        BigInteger den;

        Fraction(BigInteger n, BigInteger d) {
            if (d.signum() == 0) throw new ArithmeticException("Divide by zero");
            if (d.signum() < 0) {
                n = n.negate();
                d = d.negate();
            }
            BigInteger g = n.gcd(d);
            this.num = n.divide(g);
            this.den = d.divide(g);
        }

        Fraction(BigInteger n) {
            this(n, BigInteger.ONE);
        }

        Fraction add(Fraction other) {
            return new Fraction(this.num.multiply(other.den).add(other.num.multiply(this.den)),
                                this.den.multiply(other.den));
        }

        Fraction subtract(Fraction other) {
            return new Fraction(this.num.multiply(other.den).subtract(other.num.multiply(this.den)),
                                this.den.multiply(other.den));
        }

        Fraction multiply(Fraction other) {
            return new Fraction(this.num.multiply(other.num), this.den.multiply(other.den));
        }

        Fraction divide(Fraction other) {
            return new Fraction(this.num.multiply(other.den), this.den.multiply(other.num));
        }

        Fraction abs() {
            return new Fraction(num.abs(), den);
        }

        int compareTo(Fraction other) {
            return this.num.multiply(other.den).compareTo(other.num.multiply(this.den));
        }

        public String toString() {
            return den.equals(BigInteger.ONE) ? num.toString() : num + "/" + den;
        }
    }
}
