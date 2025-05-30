import java.awt.Point;

public class SeatManager {
    private final Point[] seats;
    private final boolean[] occupied;

    public SeatManager(Point[] seats) {
        this.seats = seats;
        this.occupied = new boolean[seats.length];
    }

    public synchronized int assignSeat() {
        for (int i = 0; i < seats.length; i++) {
            if (!occupied[i]) {
                occupied[i] = true;
                return i;
            }
        }
        return -1;
    }

    public synchronized void releaseSeat(int index) {
        if (index >= 0 && index < occupied.length) {
            occupied[index] = false;
        }
    }

    public Point getSeatPosition(int index) {
        return seats[index];
    }
}