package com.driver.services.impl;

import com.driver.model.*;
import com.driver.services.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.driver.repository.CustomerRepository;
import com.driver.repository.DriverRepository;
import com.driver.repository.TripBookingRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CustomerServiceImpl implements CustomerService {

	@Autowired
	CustomerRepository customerRepository2;

	@Autowired
	DriverRepository driverRepository2;

	@Autowired
	TripBookingRepository tripBookingRepository2;

	@Override
	public void register(Customer customer) {
		//Save the customer in database
		customerRepository2.save(customer);
	}

	@Override
	public void deleteCustomer(Integer customerId) {
		// Delete customer without using deleteById function
		Optional<Customer> optionalCustomer=customerRepository2.findById(customerId);
		if(!optionalCustomer.isPresent()) return;

		Customer curCustomer=optionalCustomer.get();
		customerRepository2.delete(curCustomer);
	}

	@Override
	public TripBooking bookTrip(int customerId, String fromLocation, String toLocation, int distanceInKm) throws Exception{
		//Book the driver with lowest driverId who is free (cab available variable is Boolean.TRUE). If no driver is available, throw "No cab available!" exception
		//Avoid using SQL query

		Optional<Customer> optionalCustomer=customerRepository2.findById(customerId);
		if(!optionalCustomer.isPresent()) return null;

		Customer curCustomer=optionalCustomer.get();

		Cab cab=null;
		Driver curDriver=null;
		for(Driver driver:driverRepository2.findAll()){
			if(driver.getCab().getAvailable()){
				cab=driver.getCab();
				curDriver=driver;
				break;
			}
		}
		if(cab==null) throw new Exception("No cab available!");
		TripBooking tripBooking= new TripBooking();
		tripBooking.setFromLocation(fromLocation);
		tripBooking.setToLocation(toLocation);
		tripBooking.setDistanceInKm(distanceInKm);
		tripBooking.setStatus(TripStatus.CONFIRMED);
		int bill=cab.getPerKmRate()*distanceInKm;
		tripBooking.setBill(bill);
		tripBooking.setCustomer(curCustomer);
		tripBooking.setDriver(curDriver);

		curCustomer.getTripBookingList().add(tripBooking);
		curDriver.getTripBookingList().add(tripBooking);

		TripBooking savedTripBooking=tripBookingRepository2.save(tripBooking);
		return savedTripBooking;

	}

	@Override
	public void cancelTrip(Integer tripId){
		//Cancel the trip having given trip Id and update TripBooking attributes accordingly
		Optional<TripBooking> optionalTripBooking=tripBookingRepository2.findById(tripId);
		if(!optionalTripBooking.isPresent()) return ;
		TripBooking curTrip=optionalTripBooking.get();
		curTrip.setStatus(TripStatus.CANCELED);
	}

	@Override
	public void completeTrip(Integer tripId){
		//Complete the trip having given trip Id and update TripBooking attributes accordingly
		Optional<TripBooking> optionalTripBooking=tripBookingRepository2.findById(tripId);
		if(!optionalTripBooking.isPresent()) return ;

		TripBooking curTrip=optionalTripBooking.get();
		curTrip.setStatus(TripStatus.COMPLETED);

	}
}
