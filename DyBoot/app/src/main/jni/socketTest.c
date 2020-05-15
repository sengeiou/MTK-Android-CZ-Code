#include <stdio.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <string.h>
#include <stdlib.h>
#include <sys/un.h>
#include <pthread.h>
#include <arpa/inet.h>

#include <android/log.h>
#define clogd(...) __android_log_print(ANDROID_LOG_INFO, "cczBootAnimation", __VA_ARGS__)


int sockfd, newfd;

void *read_socket();

int main(int argc, char *argv[]) {
    int ret;
    pthread_t read_tid, write_tid;
    struct sockaddr_in server_addr;
    struct sockaddr_in clientAddr;
    int addr_len = sizeof(clientAddr);

    char buffer[200];
    int iDataNum;

    server_addr.sin_family = AF_INET;                // 设置域为IPV4
    server_addr.sin_addr.s_addr = INADDR_ANY;        // 绑定到 INADDR_ANY 地址
    server_addr.sin_port = htons(5679);            // 注意端口转换
    sockfd = socket(AF_INET, SOCK_STREAM, 0);
    if (sockfd < 0) {
        exit(1);                                // 调用socket函数建立socket描述符出错
    }
    ret = bind(sockfd, (struct sockaddr *) (&server_addr), sizeof(server_addr));
    perror("server");
    if (ret < 0) {
        exit(2);                                // 调用bind函数绑定套接字与地址出错
    }
    ret = listen(sockfd, 4);
    if (ret < 0) {
        exit(3);                                // 调用listen函数出错
    }
    printf("等待客户端连接！\n");
    //newfd = accept(sockfd, NULL, NULL);
    while (1) {
        newfd = accept(sockfd, (struct sockaddr *) &clientAddr, (socklen_t * ) & addr_len);
        if (newfd < 0) {
            perror("accept");
            continue;
        }
        printf("服务器与客户端建立连接成功！\n");
        printf("IP is %s\n", inet_ntoa(clientAddr.sin_addr));
        printf("Port is %d\n", htons(clientAddr.sin_port));

        // 建立一个线程，负责从socket读取数据
//    pthread_create(&read_tid, NULL, read_socket, NULL);

        while (1) {
            printf("读取消息:\n");
            iDataNum = recv(newfd, buffer, 1024, 0);
            buffer[iDataNum] = '\0';
            if (strcmp(buffer, "quit") == 0)
                break;
            printf("%s\n", buffer);

            if (strcmp(buffer, "ls") == 0) {
                int ret = 0;
                ret = system("ls");
                printf("ret = %d \n", ret);
                clogd("ls result = %d \n", ret);
                ret = system("pwd");
                //printf("ret = %d \n", ret);
                clogd("pwd result = %d \n", ret);

                printf("发送消息:\n");
                printf("pwd result\n");
                //memset(buffer,0,sizeof(buffer));
                //sprintf(buffer,"%d",ret);
                //snprintf(buffer, sizeof(buffer), "hello %s", "ret");

                //send(newfd, buffer, strlen(buffer), 0);
                int iret2 = send(newfd, "hello", 5, 0);
                printf("ret = %d \n", iret2);
                printf(":%d\n", iret2);

                if(strcmp(buffer, "quit") == 0)
                    break;
            }
            sleep(1);//10s
//        sleep(10000);
        }

    }
}

// 从socket读取数据
void *read_socket() {
    int recv_num, recv_num_total = 0;
    char recv_buf[200];
    printf("read_socket into\n");
    while (1) {
        memset(recv_buf, 0, sizeof(recv_buf));        // 清空一下recv_buf缓存区
        recv_num = recv(newfd, recv_buf, 1024, 0);
        if (recv_num < 0)
            printf("服务器端接收失败！\n");
        else if (recv_num > 0) {
            recv_num_total += recv_num;
            printf("服务器端：调用接收成功！内容为：%s\n共收到%d个字节的数据。\n", recv_buf, recv_num_total);
            sync();
        } else {
            printf("服务器端与客户端的连接已中断\n");
            printf("等待客户端连接！\n");
            newfd = accept(sockfd, NULL, NULL);
        }
        sleep(1);
    }
}

