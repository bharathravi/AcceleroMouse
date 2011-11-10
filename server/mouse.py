#!/usr/bin/python
import Xlib.ext.xtest
from Xlib import X, display
import Xlib.display
import Xlib.X
import Xlib.XK
import Xlib.error
import Xlib.ext.xtest


import SocketServer

from socket import *      #import the socket library
 
import SocketServer
from optparse import OptionParser

class MouseControl:
  def __init__(self):
    self.display = Xlib.display.Display()
    self.screen = self.display.screen()
    self.root = self.screen.root

  def mouse_click(self, button):
    self.mouse_down(button)
    self.mouse_up(button)

  def mouse_down(self, button): #button= 1 left, 2 middle, 3 right
    Xlib.ext.xtest.fake_input(self.display,Xlib.X.ButtonPress, button)
    self.display.sync()

  def mouse_up(self, button):
    Xlib.ext.xtest.fake_input(self.display,Xlib.X.ButtonRelease, button)
    self.display.sync() 

  def mouse_warp(self, x,y):
    self.root.warp_pointer(x,y)
    self.display.sync()

  def get_screen_resolution(self):
    return self.screen['width_in_pixels'], self.screen['height_in_pixels']

mouse = MouseControl()
class EchoRequestHandler(SocketServer.BaseRequestHandler ):
    def handle(self):
        #print self.client_address, 'connected!'
        self.max_packet_size=1
        #print "Resolution sent: " + str(xpixels) + " " + str(ypixels)
        line = self.request[0].rstrip()
        returnsock = self.request[1]
        if (line == "init"):
          print "init received"
          d = display.Display()
          s = d.screen()
          xpixels = s.width_in_pixels
          ypixels = s.height_in_pixels
          xmm = s.width_in_mms
          ymm = s.height_in_mms
          root = s.root
          root.warp_pointer(xpixels/2,ypixels/2)
          returnsock.sendto(str(xpixels)+ "x" + str(ypixels) + " " + str(xmm) + "X" + str(ymm) + '\n', self.client_address)
          print "Resolution sent: " + str(xpixels) + " " + str(ypixels)
        else:
          pos = line.rstrip().split(None)
          d = display.Display()
          s = d.screen()
          root = s.root
          # Handle shake first
          if (pos[2] == '2'):
            print("Shake event!!!!!");

          print pos[0] + " "  +pos[1] + " " +pos[2]
          root.warp_pointer(int(pos[0]),int(pos[1]))
          if (pos[2] == "1"):
            #Mouse button down
            mouse.mouse_down(1)
          else:
            #Mouse button up
            mouse.mouse_up(1)
            
        d.sync()

          #self.request.send(data)
          #if data.strip() == 'bye':
          #    return

    #def finish(self):
     #   print self.client_address, 'disconnected!'
        #self.request.send('bye ' + str(self.client_address) + '\n')

parser = OptionParser()
parser.add_option("--ip", "--ipaddress", dest="ipaddress",
                  help="IP Address to start server on", metavar="IP")
parser.add_option("--p", "--port", type="int",dest="port",
                  help="Port to start server on", metavar="PORT")

(options, args) = parser.parse_args()
server = SocketServer.UDPServer((options.ipaddress, options.port), EchoRequestHandler)
server.serve_forever()

