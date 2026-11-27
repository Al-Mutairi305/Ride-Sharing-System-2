package ride;
import driver.IDriver;
import rider.IRider;
import system.ITimeSlot;
import system.LinkedList;
import system.Node;
import system.IBST;
import system.BST;

public class SharedRide extends Ride implements ISharedRide {

    private IBST<Integer, IRider> participants;

    public SharedRide(int rideID, String pickupLocation, String dropoffLocation, ITimeSlot timeSlot, IDriver driver) {
        super(rideID, pickupLocation, dropoffLocation, timeSlot, driver);
        participants = new BST<Integer, IRider>();
    }

    public boolean hasRider(int riderId) {
        return participants.findKey(riderId);
    }

    public LinkedList<IRider> getParticipants() {
        return participants.inOrder();
    }

    public boolean addParticipant(IRider rider) {
        return participants.insert(rider.getId(), rider);
    }

    public boolean removeParticipantById(int riderId) {
        return participants.removeKey(riderId);
    }

    public boolean isEmpty() {
        return participants.empty();
    }

    public String toString() {
        String sharedRide = "Ride Type: Shared" + "\nRide ID: " + rideID + "\nDriver: " + driver.getName()
                + "\nPickup Location: " + pickupLocation + "\nDropoff Location: " + dropoffLocation;
        sharedRide += "\nPickup Time: " + timeSlot.getStart() + "\nDropoff Time: " + timeSlot.getEnd();

        sharedRide += "\nRider(s): ";
        if (participants.size() == 0) {
            sharedRide += "None";
            return sharedRide;
        }

        LinkedList<IRider> list = getParticipants();
        Node<IRider> current = list.getHead();

        while (current.getNext() != null) {
            sharedRide += current.getData().getName() + ", ";
            current = current.getNext();
        }

        sharedRide += current.getData().getName();
        return sharedRide;
    }

}
