#include <iostream>
#include <fstream>
#include <string>
#include <sstream>
#include <vector>
#include "unistd.h"
#include "sys/wait.h"

using namespace std;

int main(){

    string userData;   
    vector <pid_t> childList;

    do{
        cout << "Enter some text:";
        getline(cin, userData);

        if(userData == "done") break;

        pid_t child = fork();

        if(child > 0){
            childList.push_back(child);
        }
        else if(child == 0){

            pid_t thisProcess = getpid();
           
            cout << "Child created: " << thisProcess << endl;

            ostringstream convert;
            convert << thisProcess;

            ofstream childFile((convert.str()).c_str());

            do{
                childFile << userData << endl;
                sleep(1);
            }while(userData != "done");

            childFile.close();
            break;
        }

        sleep(1);

    }while(true);

    vector<int>:: iterator i;

    for(i = childList.begin(); i != childList.end(); i++){
        kill(*i, SIGKILL);
        cout << "Child deleted: " << *i << endl;
    }

    return 0;
}