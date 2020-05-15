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

int sockfd, newfd;

int main(int argc, char *argv[]) {
    int ret;
    pthread_t read_tid, write_tid;
    struct sockaddr_in server_addr;
    struct sockaddr_in clientAddr;
    int addr_len = sizeof(clientAddr);

    char buffer[200];
    int iDataNum;

    server_addr.sin_family = AF_INET;
    server_addr.sin_addr.s_addr = INADDR_ANY;
    server_addr.sin_port = htons(5679);
    sockfd = socket(AF_INET, SOCK_STREAM, 0);
    if (sockfd < 0) {
        exit(1);
    }
    ret = bind(sockfd, (struct sockaddr *) (&server_addr), sizeof(server_addr));
    perror("server");
    if (ret < 0) {
        exit(2);
    }
    ret = listen(sockfd, 4);
    if (ret < 0) {
        exit(3);
    }
    printf("wait client connect...\n");
    while (1) {
        newfd = accept(sockfd, (struct sockaddr *) &clientAddr, (socklen_t * ) & addr_len);
        if (newfd < 0) {
            perror("accept");
            continue;
        }
        printf("server and client is success connected\n");
        printf("IP is %s\n", inet_ntoa(clientAddr.sin_addr));
        printf("Port is %d\n", htons(clientAddr.sin_port));

        char sendChar[50] = "socket connect success";
        int iret2 = send(newfd, sendChar, strlen(sendChar), 0);
        printf("iret2 = %d \n", iret2);

        while (1) {
            iDataNum = recv(newfd, buffer, 1024, 0);
            buffer[iDataNum] = '\0';

            printf("read:%s\n", buffer);
            if (strcmp(buffer, "close") == 0){
                memset(sendChar, 0, sizeof(sendChar));
                strcpy(sendChar, "socket closed!");
                iret2 = send(newfd, sendChar, strlen(sendChar), 0);
                printf("ret = %d \n", iret2);
                break;
            } else if (strcmp(buffer, "copy") == 0) {
                int ret = 0;
                ret = system("mv  ../../sdcard/bootanimation.zip  /data/local");
                printf("ret = %d \n", ret);
                clogd("mv result = %d \n", ret);
                ret = system("chmod  0755  ../../data/local/bootanimation.zip");
                printf("ret = %d \n", ret);
                clogd("chmod result = %d \n", ret);
                ret = system("ls");
                printf("ret = %d \n", ret);
                clogd("ls result = %d \n", ret);
            } else if (strcmp(buffer, "ls") == 0) {
                ret = system("ls");
                printf("ret = %d \n", ret);
                clogd("ls result = %d \n", ret);
            }
            sleep(1);//1 s
        }

    }
}

