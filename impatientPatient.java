import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

//this is an impatient patient who is stuck at a doctor's office, and has to go through a bunch of waiting rooms
//before finally getting his/her/its appointment. 
public class impatientPatient extends Thread{
	public ArrayList<Integer> roomSeq;
	Rooms<impatientPatient> rooms;
	String roomsToVisit;
	public impatientPatient(List<Integer> input, Rooms <impatientPatient> _rooms){
		roomSeq = new ArrayList<Integer>();
		for (int i = 0; i < input.size(); i++){
			roomSeq.add(input.get(i));
		}
		rooms = _rooms;
		Collections.shuffle(roomSeq);
		roomsToVisit = "";
		for (int i = 0; i < roomSeq.size(); i++){
			roomsToVisit = roomsToVisit + " " + roomSeq.get(i);
		}
		System.out.println("Patient " + getId() + " wants to visit room" + roomsToVisit  + "." );
	}
	
	public void run(){
		for (int i = 0; i < roomSeq.size(); i++){
			rooms.enter(roomSeq.get(i), this);
			for(int j = 0; j < 30000000; j++){
				//delay
			}
			rooms.exit(this);
		}
	}
	
}