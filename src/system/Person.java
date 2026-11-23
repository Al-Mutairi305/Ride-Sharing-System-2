package system;

import ride.IRide;

public abstract class Person implements IPerson {

    protected String name;
    protected String phoneNumber;
    protected final int id;
    protected IBST<ITimeSlot, IRide> schedule;

    public Person(int id, String name, String phoneNumber) throws IllegalArgumentException {
        this.id = id;
        this.name = name;
        setPhoneNumber(phoneNumber);
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) throws IllegalArgumentException {
        if (phoneNumber.length() != 10)
            throw new IllegalArgumentException("Invalid phone number, must be 10 digits.");

        for (int i = 0; i < phoneNumber.length(); i++) {
            if (Character.isDigit(phoneNumber.charAt(i)))
                continue;
            else
                throw new IllegalArgumentException("Invalid phone number, must be 10 digits.");
        }

        this.phoneNumber = phoneNumber;
    }

    public IBST<ITimeSlot, IRide> getSchedule() {
        return schedule;
    }

    
    public String toString() {
        return "ID: " + id + "\nName: " + name + "\nPhone Number: " + phoneNumber;
    }
    

}
