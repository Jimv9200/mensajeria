package domain;

import java.io.Serializable;

public class User implements Serializable {
    private String username;
    private double monto;

    public User(String username, double monto) {
        this.username = username;
        this.monto = monto;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public double getMonto() {
        return monto;
    }

    public void setMonto(double monto) {
        this.monto = monto;
    }

    @Override
    public String toString() {
        return "User{" +
                "username='" + username + '\'' +
                ", monto=" + monto +
                '}';
    }
}
