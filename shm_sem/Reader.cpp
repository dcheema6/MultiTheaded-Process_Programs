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
	Semaphore reader ("read");
	Shared<Point> main("main", false);

	while(true){
		reader.Wait();
		
		//if loop is used in verify if data is updated in non sem version
		//if(currThread != main->threadID || timesRepoted != main->timesRepoted){
		//	currThread = main->threadID;
		//	timesRepoted = main->timesRepoted;
			cout << main->threadID << ", " << main->timesRepoted << ", " << main->elapsedTime << endl;
		//}
	}
	return 0;
}
