#!/bin/sh
setprop vlan.dev eth0
vlandev=getprop vlan.dev
setprop vlan.id 40
vlanid=getprop vlan.id
setprop vlan.dhcp 0
vlandhcp=getprop vlan.dhcp
setprop vlan.ip 192.168.40.233
vlanip=getprop vlan.ip
setprop vlan.ctrl 1
vlanctrl=getprop vlan.ctrl
#setprop vlan.dev $vlandev

ifconfig $(getprop vlan.dev) up
toybox vconfig add $(getprop vlan.dev) $(getprop vlan.id)
ifconfig $(getprop vlan.dev).$(getprop vlan.id) $(getprop vlan.ip) up
