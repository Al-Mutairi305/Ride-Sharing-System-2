package ride;
import driver.IDriver;
import rider.IRider;
import system.ITimeSlot;

public class PrivateRide extends Ride implements IPrivateRide{
    private IRider rider;

    public PrivateRide(int rideID, String pickupLocation, String dropoffLocation, IDriver driver, ITimeSlot timeSlot, IRider rider) {
        super(rideID, pickupLocation, dropoffLocation, driver, timeSlot);
        this.rider = rider;
    }

    public boolean hasRider(int riderId) {
        return (rider.getId() == riderId);
    }

    public IRider getRider() {
        return rider;
    }

    @Override
    public String toString() {
        return "Ride Type: Private" + "\nRide ID: " + rideID + "\nDriver: " + driver.getName()
                + "\nPickup Location: " + pickupLocation + "\nDropoff Location: " + dropoffLocation
        + "\nPickup Time: " + timeSlot.getStart() + "\nDropoff Time: " + timeSlot.getEnd()+ "\nRider: "+rider.getName();
    }
}
