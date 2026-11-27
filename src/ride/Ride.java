package ride;
import driver.IDriver;
import system.ITimeSlot;

public abstract class Ride implements IRide {

    protected int rideID;
    protected String pickupLocation;
    protected String dropoffLocation;
    protected IDriver driver;
    protected final ITimeSlot timeSlot;

    public Ride(int rideID, String pickupLocation, String dropoffLocation, ITimeSlot timeSlot, IDriver driver) {
        this.rideID = rideID;
        this.pickupLocation = pickupLocation;
        this.dropoffLocation = dropoffLocation;
        this.driver = driver;
        this.timeSlot = timeSlot;
    }

        // Returns the unique internal ride ID.
    public int getRideId() {
        return rideID;
    }

    // Returns the pickup location of the ride.
    public String getPickupLocation() {
        return pickupLocation;
    }

    // Sets the pickup location of the ride.
    public void setPickupLocation(String pickupLocation) {
        this.pickupLocation = pickupLocation;
    }

    // Returns the time slot [pickup date/time, drop-off date/time).
    public ITimeSlot getTimeSlot() {
        return timeSlot;
    }

    // Returns the drop-off location of the ride.
    public String getDropoffLocation() {
        return dropoffLocation;
    }

    // Sets the drop-off location of the ride.
    public void setDropoffLocation(String dropoffLocation) {
        this.dropoffLocation = dropoffLocation;
    }

    // Returns the driver assigned to this ride.
    public IDriver getDriver() {
        return driver;
    }

    // Sets the driver assigned to this ride.
    public void setDriver(IDriver driver) {
        this.driver = driver;
    }

    // Checks whether a rider participates in this ride.
    public abstract boolean hasRider(int riderId);

    // Returns a formatted string describing the ride.
    @Override
    public String toString() {
        return String.format("Ride ID: %s, Driver: %s, Pickup Location: %s, Dropoff Location: %s, Pickup Time: %s, Dropoff Time: %s"
                , rideID, driver.getName(), pickupLocation, dropoffLocation, timeSlot.getStart(), timeSlot.getEnd());
    }

    // Compares rides by ride ID (negative, zero, or positive).
    @Override
    public int compareTo(IRide other) {
        if (rideID < other.getRideId()) {
            return -1;
        } else if (rideID > other.getRideId()) {
            return 1;
        } else {
            return 0;
        }
    }
}