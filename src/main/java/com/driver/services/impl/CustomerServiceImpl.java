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

		Customer customer=optionalCustomer.get();

		TripBooking tripBooking = new TripBooking();
		List<Driver> driverList=driverRepository2.findAll();
		driverList.sort((a,b)->(a.getDriverId()-b.getDriverId()));
		for (Driver driver:driverList) {
			if (driver.getCab().getAvailable()){
				driver.getCab().setAvailable(false);
				tripBooking.setFromLocation(fromLocation);
				tripBooking.setToLocation(toLocation);
				tripBooking.setDistanceInKm(distanceInKm);
				tripBooking.setStatus(TripStatus.CONFIRMED);
				tripBooking.setBill(distanceInKm*driver.getCab().getPerKmRate());

				tripBooking.setDriver(driver);
				tripBooking.setCustomer(customer);
//				TripBooking savedTripBooking = tripBookingRepository2.save(tripBooking);
				customer.getTripBookingList().add(tripBooking);
				driver.getTripBookingList().add(tripBooking);
				customerRepository2.save(customer);
				driverRepository2.save(driver);
				return tripBooking;
			}
		}
		throw new RuntimeException("No cab available!");

	}

	@Override
	public void cancelTrip(Integer tripId){
		//Cancel the trip having given trip Id and update TripBooking attributes accordingly
		Optional<TripBooking> optionalTripBooking=tripBookingRepository2.findById(tripId);
		if(!optionalTripBooking.isPresent()) return ;
		TripBooking curTrip=optionalTripBooking.get();
		curTrip.setStatus(TripStatus.CANCELED);
		curTrip.setBill(0);

		Driver driver=curTrip.getDriver();
		driver.getCab().setAvailable(true);

		driverRepository2.save(driver);
		tripBookingRepository2.save(curTrip);
	}

	@Override
	public void completeTrip(Integer tripId){
		//Complete the trip having given trip Id and update TripBooking attributes accordingly
		Optional<TripBooking> optionalTripBooking=tripBookingRepository2.findById(tripId);
		if(!optionalTripBooking.isPresent()) return ;

		TripBooking tripBooking=optionalTripBooking.get();
		tripBooking.setStatus(TripStatus.COMPLETED);
		Driver driver=tripBooking.getDriver();
		driver.getCab().setAvailable(true);

		driverRepository2.save(driver);
		tripBookingRepository2.save(tripBooking);

	}
}
