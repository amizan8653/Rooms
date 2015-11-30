import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;


public class Rooms<T extends Thread>  {
	private ArrayList<room> roomArray;
	private AtomicIntegerArray patientsWaitingOutside;
	private ReentrantLock lock;
	private Condition [] roomAvailable;
	private int currRoom;
	
	private class room{
		Handler handler;
		ConcurrentHashMap<Long, T> roomPatients;
		
		private room(){
			roomPatients = new ConcurrentHashMap<Long, T>();
		}
	}

	public interface Handler {
		void onEmpty();
	}
	
	public Rooms(int m) { 
		roomArray = new ArrayList<room>();
		lock = new ReentrantLock();
		roomAvailable = new Condition[m];
		patientsWaitingOutside = new AtomicIntegerArray(m);
		currRoom = -1;
		
		for (int i = 0; i < m; i++){
			roomArray.add(new room());
			roomAvailable[i] = lock.newCondition();
		}
	};
	
	private boolean areAllZero(AtomicIntegerArray input){
		int sum = 0;
		for(int i = 0; i < input.length(); i++){
			sum += input.get(i);
		}
		if (sum == 0)
			return true;
		return false;
	}
	
	void enter(int i, T patient) {  
		lock.lock(); //no other thread allowed to exit or enter here right now
		
		//take care of the case where you are the first patient to enter
		if(areAllZero(patientsWaitingOutside) && currRoom == -1){
			//you're the first patient, so you can go and set the room to whatever you want.
			currRoom = i;
			System.out.println("NEW_ROOM: set to " + currRoom);
			roomAvailable[currRoom].signalAll();
		} 
		
		boolean prevState = roomArray.get(currRoom).roomPatients.containsKey(patient.getId()); 
		
		//mark the fact that you're waiting outside
		patientsWaitingOutside.getAndIncrement(i); //indicates that there are patients that are waiting for this room

		//if the room you're assigned to isn't available, wait.
		while (currRoom != i){
			try { roomAvailable[i].await(); } 
			catch (InterruptedException e) {
				System.out.println("ERROR: Rooms class got interuppted in enter method, trying to enter room " + i);
				e.printStackTrace(); 
			}
		}
		
		//once the room is available, you can join the other people in the room set called "patientWaiting"
		roomArray.get(i).roomPatients.put(patient.getId(), patient);
		System.out.println("ENTRY: Patient " + patient.getId() + " has entered room " + currRoom);
		
		//decrment the line of people waiting outside this room
		
		patientsWaitingOutside.decrementAndGet(i);
		
		//allow other threads/patients to enter/exit
		lock.unlock();
		boolean afterState = roomArray.get(currRoom).roomPatients.containsKey(patient.getId());
		if(prevState == false && afterState == true){
			//item was found and deleted successfully
			//System.out.println("SUCCESS: insertion success");
		}
		else {
			System.out.println("ERROR: unsuccessful addition of patient "  + patient.getId());
			System.out.println("prevstate: " + prevState + " afterState: " + afterState);
		}
		
	};
	Boolean exit(T patientToLeave) { 
		lock.lock(); //no other thread allowed to exit or enter here right now
		int oldRoom = currRoom; //incase the room changes when you try to find afterState
		boolean prevState = roomArray.get(currRoom).roomPatients.containsKey(patientToLeave.getId());
		//remove the thread/patient from the room
		roomArray.get(currRoom).roomPatients.remove(patientToLeave.getId());
		System.out.println("EXIT: Patient " + patientToLeave.getId() + " has left room " + currRoom);
		
		//check to see if this is the last patient to leave
		if (roomArray.get(currRoom).roomPatients.isEmpty()){			
			//run the onEmpty() handler defined by main()
			roomArray.get(currRoom).handler.onEmpty();
			setNextRoom();

		}
		
		//release the lock for other threads to enter/exit
		boolean afterState = roomArray.get(oldRoom).roomPatients.containsKey(patientToLeave.getId());
		lock.unlock();
		if(prevState == true && afterState == false){
			return true; //item was found and deleted successfully
		}
		else {
			System.out.println("ERROR: unsuccessful removal of patient "  + patientToLeave.getId());
			System.out.println("prevstate: " + prevState + " afterState: " + afterState);
			return false; //item was NOT found in map
		}
	};
	
	//a function to allow main method to set an exit handler for the room.
    public void setExitHandler(int i, Rooms.Handler h) { 
    	roomArray.get(i).handler = h;
    };
    
    //either set the currRoom to the next room with patients outside, or signal firstPatientNeeded
    private boolean setNextRoom(){
    	int checkedAllRooms = 0;
    	int nextRoom = currRoom;
    	do{
    	nextRoom = (nextRoom + 1) % roomArray.size();
    	checkedAllRooms++;
    	//System.out.println("ITER: checkedAllRooms = " + checkedAllRooms +" nextRoom = " + nextRoom + " numPatients waiting = " + patientsWaitingOutside.get(nextRoom));
    	} while(patientsWaitingOutside.get(nextRoom) == 0 && checkedAllRooms < roomArray.size());
    	if(checkedAllRooms == roomArray.size() && patientsWaitingOutside.get(nextRoom) == 0){
    		//there are no patients waiting in any of the rooms
    		//System.out.println("Need to set currRoom upon first patient inserted in atomic integer array");
    		currRoom = -1;
    		return false;
    	}
    	//otherwise, the next room to allocate for waiting patients is this:
    	currRoom = nextRoom;
    	System.out.println("NEW_ROOM: set to " + currRoom);
    	roomAvailable[currRoom].signalAll();
    	return true;
    }
  
}