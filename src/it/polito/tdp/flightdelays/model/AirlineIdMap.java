package it.polito.tdp.flightdelays.model;

import java.util.HashMap;
import java.util.Map;

public class AirlineIdMap {

private Map <Integer, Airline> map;
	
	
	public AirlineIdMap () {
		
		this.map = new HashMap <> ();
	}
	
	public Airline get (int airlineId) {
		return map.get(airlineId);
	}

	public Airline get (Airline airline) {
		Airline old = map.get(airline.getId());
		if (old == null) {
			map.put(airline.getId(), airline);
			return airline;
		}
		return old;
	}

	public void put (int airlineId, Airline airline) {
		map.put(airlineId, airline);
	}

}
