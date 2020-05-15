# 引入 LOCAL_PATH
LOCAL_PATH :=	$(call my-dir)

# 清空所有变量（除 LOCAL_PATH 外）
include $(CLEAR_VARS)

#LOCAL_MODULE :=	su
#LOCAL_SRC_FILES :=	su.c

LOCAL_MODULE :=	dyserver
LOCAL_SRC_FILES	:=	dyserver.c
#LOCAL_MODULE :=	cdemo
#LOCAL_SRC_FILES	:=	cdemo.c
LOCAL_LDLIBS += -llog

#LOCAL_LDFLAGS := -llog
# 引用系统库
# LOCAL_LDLIBS	+=	-lz
# 编译类型为可执行文件
include $(BUILD_EXECUTABLE)