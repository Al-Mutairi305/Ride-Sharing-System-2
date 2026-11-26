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

    public boolean loadRidersFromCSV(String ridersFilePath) throws IllegalArgumentException {
        BufferedReader bufferedReader = null;
        String lineRead = "";

        try {
            FileReader fileReader = new FileReader(ridersFilePath);
            bufferedReader = new BufferedReader(fileReader);

            while ((lineRead = bufferedReader.readLine()) != null) {
                String[] row = lineRead.split(",");
                int id = Integer.parseInt(row[0]);
                String name = row[1];
                String email = row[2];
                String phoneNumber = row[3];
                String homeCity = row[4];
                IRider rider = new Rider(id, name, phoneNumber, email, homeCity);
                addRider(rider);
            }

        } catch (IOException e) {
            System.out.println(e.getMessage());
            return false;

        } finally {
            
            try {
                bufferedReader.close();
            } catch (Exception e) {
                System.out.println(e.getMessage());
                return false;
            }
            
        }
        
        return true;
    
    }

    public boolean loadDriversFromCSV(String driversFilePath) throws IllegalArgumentException {
        BufferedReader bufferedReader = null;
        String lineRead = "";

        try {
            FileReader fileReader = new FileReader(driversFilePath);
            bufferedReader = new BufferedReader(fileReader);

            while ((lineRead = bufferedReader.readLine()) != null) {
                String[] row = lineRead.split(",");
                int id = Integer.parseInt(row[0]);
                String name = row[1];
                String phoneNumber = row[2];
                String vehiclePlate = row[3];
                VehicleType vehicleType = VehicleType.valueOf(row[4]);
                IDriver driver = new Driver(id, name, phoneNumber, vehiclePlate, vehicleType);
                addDriver(driver);
            }

        } catch (IOException e) {
            System.out.println(e.getMessage());
            return false;

        } finally {
            
            try {
                bufferedReader.close();
            } catch (Exception e) {
                System.out.println(e.getMessage());
                return false;
            }
            
        }
        
        return true;
    }

    public boolean loadRidesFromCSV(String ridesFilePath) {
        BufferedReader bufferedReader = null;
        String lineRead = "";

        try {
            FileReader fileReader = new FileReader(ridesFilePath);
            bufferedReader = new BufferedReader(fileReader);

            while ((lineRead = bufferedReader.readLine()) != null) {
                // Format of each line is "RIDETYPE, PICKUPLOCATION, PICKUPDATE, DROPOFFDATE, DROPOFFLOCATION, DRIVERID, RIDERID(s)"
                String[] row = lineRead.split(",");
                String rideType = row[0];
                String pickupLocation = row[1];
                String pickupTimeString = row[2];
                int[] pickupTimeArray = dateTimeRetrieve(pickupTimeString);
                IDateTime pickupTime = new DateTime(pickupTimeArray[0], pickupTimeArray[1], pickupTimeArray[2], pickupTimeArray[3], pickupTimeArray[4]);
                String dropoffTimeString = row[3];
                int[] dropoffTimeArray = dateTimeRetrieve(dropoffTimeString);
                IDateTime dropoffTime = new DateTime(dropoffTimeArray[0], dropoffTimeArray[1], dropoffTimeArray[2], dropoffTimeArray[3], dropoffTimeArray[4]);
                String dropoffLocation = row[4];
                int driverId = Integer.parseInt(row[5]);
                
                if (rideType.equals("PRIVATE")) {
                    int riderId = Integer.parseInt(row[6]);
                    schedulePrivateRide(pickupLocation, pickupTime, dropoffTime, dropoffLocation, riderId, driverId);
                    
            
                } else {
                    String[] riders = row[6].split(";");           // Split the 7th column which contains the riders' ids, each separated by ";", then store them in an array.
                    int[] riderIds = new int[riders.length];

                    for (int i = 0; i < riderIds.length; i++)
                        riderIds[i] = Integer.parseInt(riders[i]);

                    scheduleSharedRide(pickupLocation, pickupTime, dropoffTime, dropoffLocation, riderIds, driverId);

                }
                
            }

        } catch (IOException e) {
            System.out.println(e.getMessage());
            return false;

        } finally {
            
            try {
                bufferedReader.close();
            } catch (Exception e) {
                System.out.println(e.getMessage());
                return false;
            }
            
        }
        
        return true;
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

    public boolean schedulePrivateRide(String pickupLocation, IDateTime pickupTime, IDateTime dropoffTime, String dropoffLocation, int riderId, int driverId){
        // Checks if rider and driver exist.
        if (riderList.findById(riderId) != null && driverList.findById(driverId) != null) {
            int rideID = generateUniqueRideId();
            IDriver driver = driverList.findById(driverId);
            IRider rider = riderList.findById(riderId);
            IRide ride = new PrivateRide(rideID, pickupLocation, dropoffLocation, driver, pickupTime, dropoffTime, rider);
            // Checks that there is no conflict in schedule.
            if (!hasConflict(ride)) {
                // Adds ride to driver and rider's ride histories.
                ((Person) driver).addToRideHistory(ride);
                ((Person) rider).addToRideHistory(ride);
                rideList.addRide(ride);
                return true;
            }
        }
        return false;
    }

    public boolean scheduleSharedRide(String pickupLocation, IDateTime pickupTime, IDateTime dropoffTime, String dropoffLocation, int[] riderIds, int driverId) {
        IDriver driver = searchDriverById(driverId);
        if (driver == null)
            return false;
        int rideId = generateUniqueRideId();
        IRide sharedRide = new SharedRide(rideId, pickupLocation, dropoffLocation, driver, pickupTime, dropoffTime);

        for (int i = 0; i < riderIds.length; i++) {
                    
                    int riderId = riderIds[i];
                    IRider rider = searchRiderById(riderId);
                    if (rider == null)
                    return false;
                    ((SharedRide)sharedRide).addParticipant(rider);   // addParticipants ensure no duplicates will be added.

                
        }

        if (hasConflict(sharedRide))
            return false;

        ((Person)driver).addToRideHistory(sharedRide);                 // Add the ride to the system's list, driver's ride history, and each rider's ride history.
        
        LinkedList<IRider> sharedRiderList = ((SharedRide)sharedRide).getParticipants();
        Node<IRider> currentRiderNode = sharedRiderList.getHead();

        while (currentRiderNode != null) {        // Same iterations as previous for loop.
            IRider rider = currentRiderNode.getData();
            ((Person)rider).addToRideHistory(sharedRide);

            currentRiderNode = currentRiderNode.getNext();
        }
        
        rideList.addRide(sharedRide);
        return true;
    }

    public LinkedList<IRide> searchRidesByPickupLocation(String pickupLocation) {
        return rideList.findByPickupLocation(pickupLocation);
    }

    public LinkedList<IRide> searchRidesByRiderName(String riderName) {
        return rideList.findByRiderName(riderName);
    }

    public LinkedList<IRider> getSharedRideParticipants(String pickupLocation) {   // PickupLocation is not unique. Therefore, we will traverse every shared ride associated with that pick up location instead.
        IRiderList result = new RiderList();                                              
        LinkedList<IRide> rideList = this.rideList.findByPickupLocation(pickupLocation);
        Node<IRide> currentRideNode = rideList.getHead();

        while (currentRideNode != null) {
            IRide ride = currentRideNode.getData();
            
            if (ride instanceof SharedRide) {         // Traverse all shared rides from rideList.
                LinkedList<IRider> participants = ((SharedRide)ride).getParticipants();
                Node<IRider> currentParticipantNode = participants.getHead();

                while (currentParticipantNode != null) {              // Traverse each participant from each shared ride.
                    IRider rider = currentParticipantNode.getData();
                    result.add(rider);                                         // Using IRiderList's add method to prevent duplicate riders.

                    currentParticipantNode = currentParticipantNode.getNext();
                }
            }
            currentRideNode = currentRideNode.getNext();
        }
        return result.getAll();
    }

    public LinkedList<IRide> getAllRidesAlphabetically() {
        return rideList.getAllAlphabetically();
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

    private boolean hasConflict(IRide ride) {
        LinkedList<IRide> list = rideList.getAllAlphabetically();
        Node<IRide> current = list.getHead();

        while (current != null) {
            IRide currentRide = current.getData();

            if (currentRide instanceof PrivateRide) {         // Compare the private ride with the passed ride.
                int riderId = ((PrivateRide)currentRide).getRider().getId();
                
                if (currentRide.getDriver().getId() == ride.getDriver().getId()    // Check if both rides have common driver or rider.
                || ride.hasRider(riderId)) {

                    if (ride.getPickupTime().compareTo(currentRide.getDropoffTime()) <0 && currentRide.getPickupTime().compareTo(ride.getDropoffTime()) < 0 )  // The overlap condition (From requirements).
                        return true;
                }

            } else if (currentRide instanceof SharedRide) {         // Compare the shared ride with the passed ride.
                LinkedList<IRider> sharedList = ((SharedRide)currentRide).getParticipants();   // Get the shared ride's list of riders.
                Node<IRider> currentRiderNode = sharedList.getHead();

                while (currentRiderNode != null) {             // Traverse the list of riders and check if each rider is associated with passed ride or not.
                    int riderId = currentRiderNode.getData().getId();

                    if (currentRide.getDriver().getId() == ride.getDriver().getId()
                    || ride.hasRider(riderId))  {

                        if (ride.getPickupTime().compareTo(currentRide.getDropoffTime()) < 0 && currentRide.getPickupTime().compareTo(ride.getDropoffTime()) < 0 )
                        return true;
                    }

                    currentRiderNode = currentRiderNode.getNext();
                }
            }
            

            current = current.getNext();
        }

        return false;
         
         
    }

    // Method that generates a unique ride id for every ride scheduled. No risk of duplicate ids since there is 1 generation per insertion.
    private int generateUniqueRideId() {
        if (this.rideList.size() == 0)
            return 1;

        LinkedList<IRide> rideList = this.rideList.getAllAlphabetically();
        int maxId = rideList.getHead().getData().getRideId();
        Node<IRide> currentRideNode = rideList.getHead().getNext();

        while (currentRideNode != null) {          // Traverse the current ride list to find the maximum ID.
            IRide ride = currentRideNode.getData();

            if (maxId < ride.getRideId())
                maxId = ride.getRideId();

            currentRideNode = currentRideNode.getNext();
        }

        return maxId+1;

    }

}
