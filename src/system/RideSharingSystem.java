package system;

import java.io.*;

import rider.*;
import driver.*;
import ride.*;


public class RideSharingSystem implements IRideSharingSystem {

        private IBST<Integer, IRider> ridersById;
        private IBST<String, IBST<Integer, IRider>> ridersByName;
        private IBST<String, IBST<Integer, IRider>> ridersByHomeCity;
       
        private IBST<Integer, IDriver> driversById;
        private IBST<String, IDriver> driversByPlate;
        private IBST<VehicleType, IBST<Integer, IDriver>> driversByVehicleType;
       
        private IBST<Integer, IRide> ridesById;
        private IBST<String, IBST<Integer, IRide>> ridesByPickupLocation;
        private IBST<IDateTime, IBST<Integer, IRide>> ridesByPickupTime;          

    public RideSharingSystem() {
        ridersById = new BST<Integer, IRider>();
        ridersByName = new BST<String, IBST<Integer, IRider>>(); 
        ridersByHomeCity = new BST<String, IBST<Integer, IRider>>();
       
        driversById = new BST<Integer, IDriver>();
        driversByPlate = new BST<String, IDriver>();
        driversByVehicleType = new BST<VehicleType, IBST<Integer, IDriver>>();
       
        ridesById = new BST<Integer, IRide>();
        ridesByPickupLocation = new BST<String, IBST<Integer, IRide>>();
        ridesByPickupTime = new BST<IDateTime, IBST<Integer, IRide>>();
        
    }

    public int loadRidersFromCSV(String ridersFilePath) throws IllegalArgumentException {
        BufferedReader bufferedReader = null;
        String lineRead = "";
        int numberLoaded = 0;

        try {
            FileReader fileReader = new FileReader(ridersFilePath);
            bufferedReader = new BufferedReader(fileReader);
            while (true) {
                try {
                    if ((lineRead = bufferedReader.readLine()) == null)          // readLine() returns null when it reaches the end of the file.
                        return numberLoaded; 
                    String[] row = lineRead.split(",");
                    int id = Integer.parseInt(row[0]);
                    String name = row[1];
                    String email = row[2];
                    String phoneNumber = row[3];
                    String homeCity = row[4];
                    IRider rider = new Rider(id, name, email, phoneNumber, homeCity);
                    addRider(rider);
                    numberLoaded++;

                } catch (Exception e) {                // Skip lines that could not be parsed or added.
                    continue;
                }

            }
        
        } catch (IOException e) {
            return -1;

        } finally {
            
            try {
                bufferedReader.close();

            } catch (Exception e) {
                return -1;
            }
            
        }
    }

    public int loadDriversFromCSV(String driversFilePath) throws IllegalArgumentException {
        BufferedReader bufferedReader = null;
        String lineRead = "";
        int numberLoaded = 0;

        try {
            FileReader fileReader = new FileReader(driversFilePath);
            bufferedReader = new BufferedReader(fileReader);
            while(true) {
                try {
                    if ((lineRead = bufferedReader.readLine()) == null)
                        return numberLoaded;
                    String[] row = lineRead.split(",");
                    int id = Integer.parseInt(row[0]);
                    String name = row[1];
                    String phoneNumber = row[2];
                    String vehiclePlate = row[3];
                    VehicleType vehicleType = VehicleType.valueOf(row[4]);
                    IDriver driver = new Driver(id, name, phoneNumber, vehiclePlate, vehicleType);
                    addDriver(driver);
                    numberLoaded++;

                } catch (Exception e) {
                    continue;
                }
            }   

        } catch (IOException e) {
            System.out.println(e.getMessage());
            return -1;

        } finally {
            
            try {
                bufferedReader.close();

            } catch (Exception e) {
                System.out.println(e.getMessage());
                return -1;
            }
            
        }
    }

    public int loadRidesFromCSV(String ridesFilePath) {
        BufferedReader bufferedReader = null;
        String lineRead = "";
        int numberLoaded = 0;

        try {
            FileReader fileReader = new FileReader(ridesFilePath);
            bufferedReader = new BufferedReader(fileReader);
            while(true) {
                try {
                    if ((lineRead = bufferedReader.readLine()) == null)
                        return numberLoaded;
                    String[] row = lineRead.split(",");
                    String rideType = row[0];
                    String pickupLocation = row[2];
                    String pickupTimeString = row[2];
                    int[] pickupTimeArray = dateTimeRetrieve(pickupTimeString);
                    IDateTime pickupTime = new DateTime(pickupTimeArray[0], pickupTimeArray[1], pickupTimeArray[2], pickupTimeArray[3], pickupTimeArray[4]);
                    String dropoffTimeString = row[3];
                    int[] dropoffTimeArray = dateTimeRetrieve(dropoffTimeString);
                    IDateTime dropoffTime = new DateTime(dropoffTimeArray[0], dropoffTimeArray[1], dropoffTimeArray[2], dropoffTimeArray[3], dropoffTimeArray[4]);
                    ITimeSlot timeSlot = new TimeSlot(pickupTime, dropoffTime);
                    String dropoffLocation = row[4];
                    int driverId = Integer.parseInt(row[5]);
                    if (rideType.equals("PRIVATE")) {
                        int riderId = Integer.parseInt(row[6]);
                        schedulePrivateRide(pickupLocation, dropoffLocation, timeSlot, riderId, driverId);

                    } else {
                        String[] riders = row[6].split(";");           // Split the 7th column which contains the riders' ids, each separated by ";", then store them in an array.
                        int[] riderIds = new int[riders.length];

                        for (int i = 0; i < riderIds.length; i++)
                            riderIds[i] = Integer.parseInt(riders[i]);

                        scheduleSharedRide(pickupLocation, dropoffLocation, timeSlot, riderIds, driverId);

                    }
                    numberLoaded++;

                } catch (Exception e) {
                    continue;
                }
            }

        } catch (IOException e) {
            System.out.println(e.getMessage());
            return -1;

        } finally {
            try {
                bufferedReader.close();

            } catch (Exception e) {
                System.out.println(e.getMessage());
                return -1;
            }
        }
    }

    // ---------------------------------------------------------------- riders

    public boolean addRider(IRider rider) {
        if (!ridersById.insert(rider.getId(), rider))                               // Check uniqueness and adding at the same time.
            return false;

            if (ridersByName.findKey(rider.getName())) {
                ridersByName.retrieve().insert(rider.getId(), rider);               // Current now points to the inner tree (rider's name).
            } else {
                ridersByName.insert(rider.getName(), new BST<Integer, IRider>());
                ridersByName.retrieve().insert(rider.getId(), rider);              // Current now points to new inner tree.
            }

            if (ridersByHomeCity.findKey(rider.getHomeCity())) {
                ridersByHomeCity.retrieve().insert(rider.getId(), rider);
            } else {
                ridersByHomeCity.insert(rider.getHomeCity(), new BST<Integer, IRider>());
                ridersByHomeCity.retrieve().insert(rider.getId(), rider);
            }
            return true;
    }

    public IRider searchRiderById(int riderId) {
        if (ridersById.findKey(riderId))
            return ridersById.retrieve();                       // If found, current will point to it, making retrieve valid.
        return null;
    }

    public LinkedList<IRider> searchRidersByName(String fullName) {
        if (!ridersByName.findKey(fullName)) 
            return new LinkedList<IRider>();                        // Return an empty list if not found.
        return ridersByName.retrieve().inOrder();                   // Current pooints to the inner tree of "fullName";
    }

    public LinkedList<IRider> searchRidersByHomeCity(String homeCity) {
        if (!ridersByHomeCity.findKey(homeCity))
            return new LinkedList<IRider>();
        return ridersByHomeCity.retrieve().inOrder();
    }

    public LinkedList<IRider> getAllRiders() {
        return ridersById.inOrder();
    }

    public boolean removeRider(int riderId) {
        IRider rider = searchRiderById(riderId);
        if (rider == null)
            return false;

        // Remove rider
        ridersById.removeKey(riderId);

        ridersByName.findKey(rider.getName());               // ALWAYS returns true.    
        ridersByName.retrieve().removeKey(riderId);          // Access the inner BST and remove the rider from it.
        if (ridersByName.retrieve().empty()) 
            ridersByName.removeKey(rider.getName());

        ridersByHomeCity.findKey(rider.getHomeCity());
        ridersByHomeCity.retrieve().removeKey(riderId);
        if (ridersByHomeCity.retrieve().empty())            // Current of outer BST still points to the same inner BST.
            ridersByHomeCity.removeKey(rider.getHomeCity());

        // Cascade delete 
        // TODO: Implement cancelRide first.
    }

    // --------------------------------------------------------------- drivers

    public boolean addDriver(IDriver driver) {
        if (driversById.findKey(driver.getId()) || driversByPlate.findKey(driver.getVehiclePlate()))     // Check uniqueness first.
            return false;

            driversById.insert(driver.getId(), driver);

            driversByPlate.insert(driver.getVehiclePlate(), driver);

            if (driversByVehicleType.findKey(driver.getVehicleType())) 
                driversByVehicleType.retrieve().insert(driver.getId(), driver);
            else {
                driversByVehicleType.insert(driver.getVehicleType(), new BST<Integer, IDriver>());
                driversByVehicleType.retrieve().insert(driver.getId(), driver);
            }
            return true;
    }

    public IDriver searchDriverById(int driverId) {
        if (driversById.findKey(driverId))
            return driversById.retrieve();
        return null;
    }

    public LinkedList<IDriver> searchDriversByVehicleType(VehicleType vehicleType) {
        if (driversByVehicleType.findKey(vehicleType))
            return driversByVehicleType.retrieve().inOrder();
        return new LinkedList<IDriver>();
    }

    public LinkedList<IDriver> getAllDrivers() {
        return driversById.inOrder();
    }

    public boolean removeDriver(int driverId) {
        if (!driversById.findKey(driverId))
            return false;
        IDriver driver = driversById.retrieve();

        // Remove driver
        driversById.removeKey(driverId);

        driversByPlate.removeKey(driver.getVehiclePlate());

        driversByVehicleType.findKey(driver.getVehicleType());
        driversByVehicleType.retrieve().removeKey(driverId);                        // Remove the driver from the inner BST.
        if (driversByVehicleType.retrieve().empty())                                // Current still points to the same inner BST.
            driversByVehicleType.removeKey(driver.getVehicleType());

        // Cascade delete
        LinkedList<IRide> driverSchedule = driver.getSchedule().inOrder();          // Get all rides associated with the driver
        Node<IRide> pointer = driverSchedule.getHead();                             // and cancel each one.
        while (pointer != null) {
            IRide ride = pointer.getData();
            cancelRide(ride.getRideId());                                           // cancelRide will make sure the ride is deleted
            pointer = pointer.getNext();                                            // from every index and every person's schedule.
        }   

        return true;
    }

    // ----------------------------------------------------------------- rides

    public int schedulePrivateRide(String pickupLocation, String dropoffLocation, ITimeSlot timeSlot, int riderId, int driverId){
        IDriver driver = searchDriverById(driverId);
        IRider rider = searchRiderById(riderId);
        // Check if rider and driver exist & that there is no conflict.
        if (driver == null || rider == null || hasConflict(timeSlot, driver) || hasConflict(timeSlot, rider)) {
            return -1;
        }
        int rideId = generateUniqueRideId();
        IRide privateRide = new PrivateRide(rideId, pickupLocation, dropoffLocation, driver, timeSlot, rider);
        // Add ride to driver and rider schedules.
        driver.getSchedule().insert(timeSlot, privateRide);
        rider.getSchedule().insert(timeSlot, privateRide);
        // Add ride to indexes.
        ridesById.insert(rideId, privateRide);
        if (ridesByPickupLocation.findKey(pickupLocation))
            ridesByPickupLocation.retrieve().insert(rideId, privateRide);
        else {
            ridesByPickupLocation.insert(pickupLocation, new BST<Integer, IRide>());
            ridesByPickupLocation.retrieve().insert(rideId, privateRide);
        }

        if (ridesByPickupTime.findKey(timeSlot.getStart()))
            ridesByPickupTime.retrieve().insert(rideId, privateRide);
        else {
            ridesByPickupTime.insert(timeSlot.getStart(), new BST<Integer, IRide>());
            ridesByPickupTime.retrieve().insert(rideId, privateRide);
        }
        return rideId;
    }

    public int scheduleSharedRide(String pickupLocation, String dropoffLocation, ITimeSlot timeSlot, int[] riderIds, int driverId) {
        IDriver driver = searchDriverById(driverId);
        if (driver == null || riderIds.length < 2 || hasConflict(timeSlot, driver))
            return -1;

        // Create the SharedRide object and fill it with participants
        int rideId = generateUniqueRideId();
        IRide sharedRide = new SharedRide(rideId, pickupLocation, dropoffLocation, timeSlot, driver);

        for (int i = 0; i < riderIds.length; i++) {       
            int riderId = riderIds[i];
            IRider rider = searchRiderById(riderId);
            if (rider == null)
                return -1;

            if (hasConflict(timeSlot, rider) || !((SharedRide)sharedRide).addParticipant(rider))    // If there are duplicate riders, or given rider has conflict, return -1;
                return -1;
                
        }

        // At this point, the ride is 100% valid and shall be added to the system. 
        // Any failed checks earlier will not change the state of the system,
        // the system's state will change only beyond this point.

        // Add the ride to each associated person's schedule
        driver.getSchedule().insert(timeSlot, sharedRide);

        LinkedList<IRider> particpants = ((SharedRide)sharedRide).getParticipants();
        Node<IRider> pointer = particpants.getHead();
       
        while (pointer != null) {
            IRider rider = pointer.getData();
            rider.getSchedule().insert(timeSlot, sharedRide);
            pointer = pointer.getNext();
        }

        // Add the ride to each index
        ridesById.insert(rideId, sharedRide);

        if (ridesByPickupLocation.findKey(pickupLocation))
            ridesByPickupLocation.retrieve().insert(rideId, sharedRide);
        else {
            ridesByPickupLocation.insert(pickupLocation, new BST<Integer, IRide>());
            ridesByPickupLocation.retrieve().insert(rideId, sharedRide);
        }

        if (ridesByPickupTime.findKey(timeSlot.getStart()))
            ridesByPickupTime.retrieve().insert(rideId, sharedRide);
        else {
            ridesByPickupTime.insert(timeSlot.getStart(), new BST<Integer, IRide>());
            ridesByPickupTime.retrieve().insert(rideId, sharedRide);
        }
        
        return rideId;
    }

    public boolean cancelRide(int rideId) {
        if (!ridesById.findKey(rideId))                                     // Ride does not exist.
            return false;
        IRide ride = ridesById.retrieve();

        // Delete ride
        ridesById.removeKey(rideId);

        ridesByPickupLocation.findKey(ride.getPickupLocation());
        ridesByPickupLocation.retrieve().removeKey(rideId);
        if (ridesByPickupLocation.retrieve().empty())
            ridesByPickupLocation.removeKey(ride.getPickupLocation());

        ridesByPickupTime.findKey(ride.getTimeSlot().getStart());
        ridesByPickupTime.retrieve().removeKey(rideId);
        if (ridesByPickupTime.retrieve().empty()) 
            ridesByPickupTime.removeKey(ride.getTimeSlot().getStart());

        // Cascade Delete, remove the ride from the schedules of each person associated with it
        ride.getDriver().getSchedule().removeKey(ride.getTimeSlot());

        if (ride instanceof PrivateRide) {
            ((PrivateRide)ride).getRider().getSchedule().removeKey(ride.getTimeSlot());

        } else if (ride instanceof SharedRide) {
            LinkedList<IRider> particpants = ((SharedRide)ride).getParticipants();
            Node<IRider> pointer = particpants.getHead();
            
            while (pointer != null) {
                IRider rider = pointer.getData();
                rider.getSchedule().removeKey(ride.getTimeSlot());
                pointer = pointer.getNext();
            }
        }
        return true;
    }

    public LinkedList<IRide> searchRidesByPickupLocation(String pickupLocation) {
        if (ridesByPickupLocation.findKey(pickupLocation))
            return ridesByPickupLocation.retrieve().inOrder();
        return new LinkedList<IRide>();
    }

    public LinkedList<IRide> searchRidesByRiderName(String riderName) {
        if (!ridersByName.findKey(riderName))
            return new LinkedList<IRide>();
        IBST <Integer, IRide> resultTree = new BST<Integer, IRide>();

        LinkedList<IRider> riders = ridersByName.retrieve().inOrder();
        Node<IRider> riderPointer = riders.getHead();

        while (riderPointer != null) {                                               // Go through each rider
            IRider rider = riderPointer.getData();
            LinkedList<IRide> riderSchedule = rider.getSchedule().inOrder();
            Node<IRide> ridePointer = riderSchedule.getHead();

            while (ridePointer != null) {                                       // Go through each ride in schedule of a rider.
                IRide ride = ridePointer.getData();
                resultTree.insert(ride.getRideId(), ride);                      // Ensure no duplicate rides may be added.
                ridePointer = ridePointer.getNext();
            }
            riderPointer = riderPointer.getNext();
        }
        return resultTree.inOrder();
    }

    public LinkedList<IRide> getAllRidesAlphabetically() {
        return ridesById.inOrder();
    }

    public LinkedList<IRide> searchRidesByPickupTimeRange(IDateTime from, IDateTime to) {
        return ridesByPickupTime.rangeSearch(from, to)
    }

    // Takes string which contains all date time data ("MM/DD/YYYY HH:MM") in CSV file and puts each part into an integer array
    private int[] dateTimeRetrieve(String dateTime) {
        int[] proccessedDateTime = new int[5];
        // Splits string into ("MM", "DD", "YYYY HH:MM")
        String[] date = dateTime.split("/");
        // DateTime contructor takes (YYYY, MM, DD, HH, MM) so Month is index 1
        proccessedDateTime[1] = Integer.parseInt(date[0]);
        // Day is the third parameter so index 2
        proccessedDateTime[2] = Integer.parseInt(date[1]);
        // Splits the ("YYYY HH:MM") string into ("YYYY", "HH:MM")
        String[] yearTime = date[2].split(" ");
        // Year is the first parameter so index 0
        proccessedDateTime[0] = Integer.parseInt(yearTime[0]);
        // Splits the ("HH:MM") string into ("HH", "MM")
        String[] time = yearTime[1].split(":");
        // Hours
        proccessedDateTime[3] = Integer.parseInt(time[0]);
        // Minutes
        proccessedDateTime[4] = Integer.parseInt(time[1]);
        // Returns array containing (YYYY, MM, DD, HH, MM) as integers to easily make a DateTime object
        return proccessedDateTime;
    }

    // Checks if a given Person's schedule has a conflict with a given ride's time slot.
    // Conflict is detected if a person already has a scheduled ride at time slot "timeSlot".
    private boolean hasConflict(ITimeSlot timeSlot, IPerson person) {
        return person.getSchedule().findKey(timeSlot);    // If a ride at given time slot exists, returns true. Otherwise, false.
    }

    // Method that generates a unique ride id for every ride scheduled. No risk of duplicate ids since there is 1 generation per insertion.
    private int generateUniqueRideId() {
        if (this.ridesById.size() == 0)
            return 1;

        LinkedList<IRide> rideList = this.ridesById.inOrder();
        
        return rideList.getTail().getData().getRideId()+1;            // Max id + 1;

    }

}
