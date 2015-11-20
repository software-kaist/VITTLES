#!/usr/bin/env python

from flask import Flask, request, render_template, jsonify
from subprocess import Popen, PIPE
import os
import sys
import signal
import subprocess
import picamera
from picamera.camera import PiCamera
import socket
from socket import *
import time
import RPi.GPIO as gpio
import thread
import threading

app = Flask(__name__, static_url_path='/static')
subproc = 0
flag = 0
onair_flag = 0
capture_flag = 0
mCamera = 0
mPlayer = 0

#Motor 1 GPIO Pin
IC1_A = 27
IC1_B = 22
 
#Motor 2 GPIO Pin
IC2_A = 17
IC2_B = 18
 
gpio.setmode(gpio.BCM)
 
#Motor Pin Setup
gpio.setwarnings(False)
gpio.setup(IC1_A, gpio.OUT)
gpio.setup(IC1_B, gpio.OUT)
gpio.setup(IC2_A, gpio.OUT)
gpio.setup(IC2_B, gpio.OUT)

pwm = gpio.PWM(IC2_B,1000)
pwm.ChangeDutyCycle(80)

#cmd = "raspivid -n -t 0 -h 200 -w 320 -fps 20 -hf -vf -b 2000000 -o - | gst-launch-1.0 -v fdsrc ! h264parse ! rtph264pay pt=96 config-interval=1 ! gdppay ! tcpserversink host=192.168.0.12 port=5000"
#youtube live streaming
#cmd = "raspivid -n -o - -t 0 -vf -hf -fps 30 -b 6000000 | ffmpeg -re -ar 44100 -ac 2 -acodec pcm_s16le -f s16le -ac 2 -i /dev/zero -f h264 -i - -vcodec copy -acodec aac -ab 128k -g 50 -strict experimental -f flv rtmp://a.rtmp.youtube.com/live2/hochan97.yucp-bpu9-yzwj-2aec"
#Pi RTMP Streaming
cmd = "raspivid -n -t 0 -w 640 -h 400 -fps 20 -b 6000000 -hf -vf -o - | ffmpeg -i - -vcodec copy -an -r 25 -f flv -metadata streamName=myStream tcp://0.0.0.0:6666"

#cmd = "raspivid -n -w 720 -h 405 -fps 25 -vf -t 86400000 -b 1800000 -ih -o - | ffmpeg -y -i - -c:v copy -map 0:0 -f ssegment -segment_time 4 -segment_format mpegts -segment_list /home/pi/python_test/static/live/stream.m3u8 -segment_list_size 720 -segment_list_flags live -segment_list_type m3u8 /home/pi/python_test/static/live/out%03d.ts"
#cmd = "raspivid -n -t 0 -h 200 -w 320 -fps 20 -hf -vf -b 2000000 -o - | ffmpeg -y -i - -c:v copy -map 0:0 -f ssegment -segment_time 8 -segment_format mpegts -segment_list /home/pi/python_test/static/live/stream.m3u8 -segment_list_size 320 -segment_list_flags live -segment_list_type m3u8 /home/pi/python_test/static/live/out%03d.ts"

cmd_src = "raspivid -n -t 0 -w 640 -h 400 -fps 20 -b 6000000 -hf -vf -o - | nc 0.0.0.0 8000"
cmd_python = "python test_streaming_capture.py"

@app.route("/")
def hello():
    return render_template('index.html')

@app.route("/cameraon", methods=['GET', 'POST'])
def camera_on():
    global flag, onair_flag
    if flag == 0:
        #subprocess.Popen(cmd_python, shell=True, preexec_fn=os.setsid)
        #msg = "PI camera on! pid=" + str(subproc.pid)
        thread.start_new_thread(rtmpStreamer, ('rtmpStreamerThread',))
        flag = 1;
        msg = "PI camera on!"
        return render_template('result.html', message=msg, state='1')
    else:
        msg = 'PI camera already on'
        return render_template('result.html', message=msg, state='1')

@app.route("/cameraoff", methods=['GET', 'POST'])
def camera_off():
    global subproc, flag, onair_flag
    if onair_flag == 1:
        #os.killpg(subproc.pid, signal.SIGKILL)
        mCamera.capture('hochan.jpg', use_video_port=True)
        mCamera.wait_recording(0)
        mCamera.stop_recording()
        flag = 0
        onair_flag = 0
        #msg = "PI camera off! pid=" + str(subproc.pid)
        msg = "PI camera off!"
        return render_template('result.html', message=msg)
    else:
        msg = "PI camera already off"
        return render_template('result.html', message=msg)

@app.route("/result.html", methods=['GET', 'POST'])
def show_result():
	return render_template('result.html')

cmd1 = "raspivid -t 0 -h 200 -w 355 -fps 20 -hf -vf -b 2000000 -o - | gst-launch-1.0 -e -vvvv fdsrc ! h264parse ! rtph264pay pt=96 config-interval=1 ! udpsink host=%s port=5000"

@app.route('/camonoff/<client_ip>')
def cam_onoff(client_ip):
    global subproc, flag
    #fwd = request.environ.get('HTTP_X_FORWARDED_FOR', None)
    #if fwd is None:
    #    clientIP = request.remote_addr
    #else:
    #    ip_adds = request.headers.getlist("X-Forwarded-For")
    #    clientIP = ip_adds[0]
        
    print("\n-------------------------------------------------------")
    print("Client IP : " + client_ip)
    #print("URL : " + request.url)
    #print("Headers \n " + str(request.headers))
    #print("Data : " + str(request.data))
    print("-------------------------------------------------------\n")
        
    if flag == 1:
        os.killpg(subproc.pid, signal.SIGKILL)
        flag = 0
    elif flag == 0:
        proc = subprocess.Popen(cmd1%client_ip, shell=True, preexec_fn=os.setsid)
        subproc = proc
        flag = 1
    return "test"


@app.route("/onair", methods=['GET', 'POST'])
def onair():
    global subproc, flag, onair_flag
    msg = 'On-Air'
    if onair_flag == 0:
        onair_flag = 1;
        #proc = subprocess.Popen(cmd_src, shell=True, preexec_fn=os.setsid)
        #subproc = proc
        thread.start_new_thread(networkRecording, ('networkRecordingThread',))
    return render_template('onair.html', message=msg)

@app.route('/inputTV/<int:input_id>', methods=['GET'])
def sendIRSignalQRemote(input_id):
    s = socket(AF_INET, SOCK_STREAM)
    s.connect(('192.168.0.75', 4445))
    s.send(str(input_id) +'\n')
    s.close()
    pass

#neutral    0
#movement forward|reverse (1|2)
#steering left|right (1|2)
#movement $ steering $ angle(-180~180) $ power(0~100%) $ weapon
@app.route('/inputBattleCar/<command>')
def inputBattleCar(command):
    data = command.split('$')
    movement = int(data[0])
    steering = int(data[1])
    angle = int(data[2])
    power = int(data[3])
    weapon = int(data[4])

    if movement == 1:
        forward()
    elif movement == 2:
        backward()
    else:
        stopFB()

    if steering == 1:
        turnLeft()
    elif steering == 2:
        turnRight()
    else:
        stopLR()
    return command

def forward():
    #LOG('info','GPIO Forward')
    gpio.output(IC2_A, gpio.LOW)
    #gpio.output(IC2_B, gpio.HIGH)
    pwm.start(80)
def backward():
    #LOG('info','GPIO Backward')
    gpio.output(IC2_A, gpio.HIGH)
    gpio.output(IC2_B, gpio.LOW)

def turnLeft():
    #LOG('info','GPIO Turn Left')
    gpio.output(IC1_A, gpio.HIGH)
    gpio.output(IC1_B, gpio.LOW)

def turnRight():
    #LOG('info','GPIO Turn Right')
    gpio.output(IC1_A, gpio.LOW)
    gpio.output(IC1_B, gpio.HIGH)

def stopFB():
    #LOG('info','GPIO Stop Back Wheel')
    pwm.stop()
    gpio.output(IC2_A, gpio.LOW)
    gpio.output(IC2_B, gpio.LOW)

def stopLR():
    #LOG('info','GPIO Front Wheel Zero')
    gpio.output(IC1_A, gpio.LOW)
    gpio.output(IC1_B, gpio.LOW)

def networkRecording(a):
    # Connect a client socket to my_server:8000 (change my_server to the
    # hostname of your server)
    client_socket = socket()
    client_socket.connect(('0.0.0.0', 8000))

    # Make a file-like object out of the connection
    connection = client_socket.makefile('wb')
    try:
        with picamera.PiCamera() as camera:
            global mCamera
            camera.resolution = (640, 480)
            camera.framerate = 20
            camera.vflip = True
            camera.hflip = True
            mCamera = camera
            # Start a preview and let the camera warm up for 2 seconds
            #camera.start_preview()
            #time.sleep(2)
            # Start recording, sending the output to the connection for 60
            # seconds, then stop
            camera.start_recording(connection, format='h264')
            print 'start_recording'
            camera.wait_recording(2400)
    finally:
        global mPlayer
        connection.close()
        client_socket.close()
        os.kill(mPlayer.pid, signal.SIGKILL);
        print '[check] networkRecording end'

def rtmpStreamer(a):
    global mPlayer
    server_socket = socket()
    server_socket.setsockopt(SOL_SOCKET, SO_REUSEADDR, 1)
    server_socket.bind(('0.0.0.0', 8000))
    server_socket.listen(0)

    connection = server_socket.accept()[0].makefile('rb')
    try:
        cmdline = 'ffmpeg -i - -vcodec copy -an -r 25 -f flv -metadata streamName=myStream tcp://0.0.0.0:6666'
        mPlayer = subprocess.Popen(cmdline.split(), stdin=subprocess.PIPE)
        while True:
            data = connection.read(1024)
            if not data:
                break
            mPlayer.stdin.write(data)
    finally:
        connection.close()
        server_socket.close()
        mPlayer.terminate()
        os.kill(mPlayer.pid, signal.SIGKILL);
        print '[check] rtmpStreamer end'

if __name__ == "__main__":
	app.run(host='0.0.0.0', port=8888, debug=True)

