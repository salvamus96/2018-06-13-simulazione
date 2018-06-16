package it.polito.tdp.flightdelays.model;

import java.time.LocalDateTime;

public class Event {

	private Passeggero passeggero;
	private Airport airport; // aeroporto in cui si trova il passegero in quella data
	private LocalDateTime data;
	
	public Event(Passeggero passeggero, Airport airport, LocalDateTime data) {
		super();
		this.passeggero = passeggero;
		this.airport = airport;
		this.data = data;
	}

	public Passeggero getPasseggero() {
		return passeggero;
	}

	public void setPasseggero(Passeggero passeggero) {
		this.passeggero = passeggero;
	}

	public Airport getAirport() {
		return airport;
	}

	public void setAirport(Airport airport) {
		this.airport = airport;
	}

	public LocalDateTime getData() {
		return data;
	}

	public void setData(LocalDateTime data) {
		this.data = data;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((data == null) ? 0 : data.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Event other = (Event) obj;
		if (data == null) {
			if (other.data != null)
				return false;
		} else if (!data.equals(other.data))
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return String.format("%s --- %s --- %s", this.passeggero, this.airport, this.data);
	}
	
	
}
