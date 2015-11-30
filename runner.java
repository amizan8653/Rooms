import java.util.*;



public class runner{ 
	
	public class runnerHandler implements Rooms.Handler {
		int roomNum; 
		public void onEmpty(){
			System.out.println("EMPTY: All threads have exited " + roomNum);
		}
		runnerHandler (int _roomNum){
			roomNum = _roomNum;
		}
	}
	public static void main (String [] args){
		runner root = new runner();
		int numRooms = 4;
		int numPatients = 9;
		impatientPatient[] patients = new impatientPatient[numPatients];
		Rooms <impatientPatient>rooms = new Rooms<impatientPatient>(numRooms);
		ArrayList <Integer> roomsPatientsNeedToVisit = new ArrayList<Integer>();
		//don't need to visit room index 2. 
		for (int i = 0; i < numRooms; i++){
			if (i == 2)
				continue;
			roomsPatientsNeedToVisit.add(i);
			roomsPatientsNeedToVisit.add(i);
		}
		//give each room the appropriate handler
		for (int i = 0; i < numRooms; i++){
			rooms.setExitHandler(i, root.new runnerHandler(i));
		}
		//setup the patients/threads
		for (int i = 0; i < numPatients; i++){
			patients[i] = new impatientPatient(roomsPatientsNeedToVisit, rooms);
		}
		//start the threads
		for (int i = 0; i < numPatients; i++){
			patients[i].start();
		}
		  
	  }
}