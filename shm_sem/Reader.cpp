#include <iostream>
#include <string>
#include "SharedObject.h"
#include "Semaphore.h"

using namespace std;

class Point{
	public:
		int threadID;
		int timesRepoted;
		int elapsedTime;

		Point(){
			threadID = 0;
			timesRepoted = 0;
			elapsedTime = 0;
		}
};

int main(void)
{
	/*
	*Uncomment the code to run task 1
	int currThread = -1;
	int timesRepoted = -1;
	*/
	Semaphore reader ("read");

	Shared<Point> main("main", false);

	while(true){
		reader.Wait(); //comment out this line for task1
		
		//if loop is used in verify if data is updated in non sem version
		//Uncomment the lines below for task 1
		//if(currThread != main->threadID || timesRepoted != main->timesRepoted){
		//	currThread = main->threadID;
		//	timesRepoted = main->timesRepoted;
			cout << main->threadID << ", " << main->timesRepoted << ", " << main->elapsedTime << endl;
		//}
	}
	return 0;
}
