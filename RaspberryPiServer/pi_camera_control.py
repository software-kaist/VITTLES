#!/usr/bin/env python

from flask import Flask, request, render_template
from subprocess import Popen, PIPE
import os
import sys
import signal
import subprocess
import picamera
from picamera.camera import PiCamera
from socket import *

app = Flask(__name__, static_url_path='/static')
subproc = 0
flag = 0
#cmd = "raspivid -n -t 0 -h 200 -w 320 -fps 20 -hf -vf -b 2000000 -o - | gst-launch-1.0 -v fdsrc ! h264parse ! rtph264pay pt=96 config-interval=1 ! gdppay ! tcpserversink host=192.168.0.12 port=5000"
#youtube live streaming
#cmd = "raspivid -n -o - -t 0 -vf -hf -fps 30 -b 6000000 | ffmpeg -re -ar 44100 -ac 2 -acodec pcm_s16le -f s16le -ac 2 -i /dev/zero -f h264 -i - -vcodec copy -acodec aac -ab 128k -g 50 -strict experimental -f flv rtmp://a.rtmp.youtube.com/live2/hochan97.yucp-bpu9-yzwj-2aec"
#Pi RTMP Streaming
cmd = "raspivid -n -t 0 -w 640 -h 400 -fps 20 -b 6000000 -hf -vf -o - | ffmpeg -i - -vcodec copy -an -r 25 -f flv -metadata streamName=myStream tcp://0.0.0.0:6666"

#cmd = "raspivid -n -w 720 -h 405 -fps 25 -vf -t 86400000 -b 1800000 -ih -o - | ffmpeg -y -i - -c:v copy -map 0:0 -f ssegment -segment_time 4 -segment_format mpegts -segment_list /home/pi/python_test/static/live/stream.m3u8 -segment_list_size 720 -segment_list_flags live -segment_list_type m3u8 /home/pi/python_test/static/live/out%03d.ts"
#cmd = "raspivid -n -t 0 -h 200 -w 320 -fps 20 -hf -vf -b 2000000 -o - | ffmpeg -y -i - -c:v copy -map 0:0 -f ssegment -segment_time 8 -segment_format mpegts -segment_list /home/pi/python_test/static/live/stream.m3u8 -segment_list_size 320 -segment_list_flags live -segment_list_type m3u8 /home/pi/python_test/static/live/out%03d.ts"

@app.route("/")
def hello():
    result = subprocess.check_output ('ps -ef|grep "ffmpeg -re -ar 44100"' , shell=True)
    if result.find('ffmpeg -re -ar 44100') == -1:
        msg = 'closed'
    else:
        msg = ''
    return render_template('index.html')

@app.route("/cameraon", methods=['GET', 'POST'])
def camera_on():
	global subproc, flag
	if flag == 0:
		proc = subprocess.Popen(cmd, shell=True, preexec_fn=os.setsid)
		subproc = proc
		flag = 1;
		msg = "PI camera on! pid=" + str(subproc.pid)
		return render_template('result.html', message=msg, state='1')
	else:
		msg = 'PI camera already on'
		return render_template('result.html', message=msg, state='1')

@app.route("/cameraoff", methods=['GET', 'POST'])
def camera_off():
	global subproc, flag
	if flag == 1:
		os.killpg(subproc.pid, signal.SIGKILL)
		flag = 0
		msg = "PI camera off! pid=" + str(subproc.pid)
		return render_template('result.html', message=msg)
	else:
		msg = "PI camera already off"
		return render_template('result.html', message=msg)

@app.route("/result.html", methods=['GET', 'POST'])
def show_result():
	return render_template('result.html')

cmd1 = "raspivid -t 0 -h 200 -w 355 -fps 20 -hf -vf -b 2000000 -o - | gst-launch-1.0 -e -vvvv fdsrc ! h264parse ! rtph264pay pt=96 config-interval=1 ! udpsink host=192.168.0.6 port=5000"

@app.route("/camonoff")
def cam_onoff():
    global subproc, flag
    if flag == 1:
        os.killpg(subproc.pid, signal.SIGKILL)
        flag = 0
    elif flag == 0:
        proc = subprocess.Popen(cmd1, shell=True, preexec_fn=os.setsid)
        subproc = proc
        flag = 1
    return "test"


@app.route("/onair", methods=['GET', 'POST'])
def onair():
    msg = 'On-Air'
    return render_template('onair.html', message=msg)

@app.route('/input/<int:input_id>', methods=['GET'])
def show_post(input_id):
    s = socket(AF_INET, SOCK_STREAM)
    s.connect(('192.168.0.75', 4445))
    s.send(str(input_id) +'\n')
    s.close()
    pass

if __name__ == "__main__":
	app.run(host='0.0.0.0', port=8888, debug=True)
