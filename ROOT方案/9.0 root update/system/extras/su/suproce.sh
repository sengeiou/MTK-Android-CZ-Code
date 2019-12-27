#!/system/bin/sh


mount -o rw,remount /system
chmod 06755 su
su --daemon

echo "su daemon done."
