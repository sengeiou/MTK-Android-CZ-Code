#include <stdio.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <string.h>
#include <stdlib.h>
#include <sys/un.h>
#include <pthread.h>
#include <arpa/inet.h>

#include <android/log.h>
#define clogd(...) __android_log_print(ANDROID_LOG_INFO, "ccsBootAnimation", __VA_ARGS__)


void getProp(const char pname[], char* data){
    printf("%s", pname);
    FILE *fp = NULL;
    //char data[100] = {'0'};
    fp = popen(pname, "r");
    if (fp == NULL)
    {
        printf("popen error!\n");
        return ;
    }
    while (fgets(data, 100, fp) != NULL)
    {
        printf("=%s\n", data);
        clogd("%s = %s\n", pname, data);
    }
    pclose(fp);
    //return 1;
}

int main(int argc, char *argv[]) {
    char vlandev[30] = {0};
    char vlanid[30] = {0};
    char vlandhcp[30] = {0};
    char vlanip[30] = {0};
    char vlanctrl[30] = {0};
   getProp("getprop vlan.dev", vlandev);
   getProp("getprop vlan.id", vlanid);
   getProp("getprop vlan.dhcp", vlandhcp);
   getProp("getprop vlan.ip", vlanip);
   getProp("getprop vlan.ctrl", vlanctrl);
   //clogd("[libshowlogo: %s %d]show boot logo, index = 0 \n",__FUNCTION__,__LINE__);
   printf("=%s\n", vlandev);
   clogd("vlandev = %s,vlanid = %s,vlandhcp = %s,vlanip = %s,vlanctrl = %s\n",
   vlandev, vlanid, vlandhcp, vlanip, vlanctrl);
    //printf("Element= %d\n", index);
}



