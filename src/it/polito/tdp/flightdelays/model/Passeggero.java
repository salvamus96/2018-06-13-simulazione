package it.polito.tdp.flightdelays.model;

public class Passeggero {

	private int passeggeroId;
	private int voli;
	private int delay;
	
	public Passeggero (int passeggeroId, int voli) {
		
		this.passeggeroId = passeggeroId;
		this.voli = voli;
		this.delay = 0;
	}
	
	public int getPasseggeroId() {
		return passeggeroId;
	}
	
	public void setPasseggeroId(int passeggeroId) {
		this.passeggeroId = passeggeroId;
	}
	
	public int getVoli() {
		return voli;
	}
	
	public void setVoli(int voli) {
		this.voli = voli;
	}
	
	public int getDelay() {
		return delay;
	}
	
	public void setDelay(int delay) {
		this.delay = delay;
	}
	
	// decremento il numero di voli ogni volta che viene effettuato un viaggio
	public void voloEffettuato() {
		this.voli --;
	}

	public void accumuloRitardo (int ritardo) {
		this.delay += ritardo;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + passeggeroId;
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
		Passeggero other = (Passeggero) obj;
		if (passeggeroId != other.passeggeroId)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return String.format("Passeggero%d , voli rimasti: %2d ritado totale: %3d", this.passeggeroId, this.voli, this.delay);
	}
	
	
	
}
