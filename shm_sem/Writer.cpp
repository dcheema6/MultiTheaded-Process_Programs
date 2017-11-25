#include <iostream>
#include <string>
#include <thread>
#include <vector>
#include <sstream>
#include <ctime>
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


void threadFunc(int sleepTime, Point *main, int tID){
	int timesRepoted = 0;
	long elapsedTime = time(0);

	Semaphore guard ("main");
	Semaphore reader ("read");

	while(true){
		guard.Wait();

		stringstream ss;
		ss << this_thread::get_id();
		main->threadID = tID;
		main->timesRepoted = timesRepoted;
		main->elapsedTime = (time(0) - elapsedTime);
		elapsedTime = time(0);
		timesRepoted++;

		reader.Signal();
		guard.Signal();

		sleep(sleepTime);
	}
	return;
}

//function returns true if input is a +ve integer and false otherwise
bool isNumber(const string& s){
	return !s.empty() && s.find_first_not_of("0123456789") == string::npos;
}

int main(void)
{
	string input;
	int threadCount = 0;
	int sleepTime;

	vector<thread> threads;
	Shared<Point> main ("main", true);

	bool isInputNumber;

	Semaphore guard ("main", 1, true);
	Semaphore reader ("read", 0, true);

	do {
		//Input validaton loops
		do {
			cout << "\nWould you like to create a new thread? (y/n) : ";
			cin >> input;
			if (input != "y" && input != "n") {
				cout << "\nPlease enter a valid input\n";
			}
		}while (input != "y" && input != "n");
		if(input == "n"){
			break;
		}
		do {
			string intInput;
			cout << "\nEnter the deplay time for the new Thread (sec): ";
			cin >> intInput;
			isInputNumber = isNumber(intInput);
			if (!isInputNumber) {
				cout << "\nPlease enter a valid input\n";
			}
			else {
				sleepTime = stoi(intInput);
			}
		}while (!isInputNumber);
		if (input == "y"){
			//making a new thread and adding it to vector.
			//threads calls threadFunc
			threads.push_back(thread (threadFunc, sleepTime, main.get(), threadCount));
			threadCount++;
		}
	}while (true);
	//deleting all the threads
	threads.erase(threads.begin(), threads.end());
	return 0;
}