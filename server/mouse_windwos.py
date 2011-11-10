import win32api, win32con
from win32api import GetSystemMetrics
import time
import SocketServer
def click(x,y):
    win32api.SetCursorPos((x,y))
    #win32api.mouse_event(win32con.MOUSEEVENTF_LEFTDOWN,x,y,0,0)
    #win32api.mouse_event(win32con.MOUSEEVENTF_LEFTUP,x,y,0,0)
    
def clickleftbutton(x,y):
    win32api.SetCursorPos((x,y))
    win32api.mouse_event(win32con.MOUSEEVENTF_LEFTDOWN,x,y,0,0)
    win32api.mouse_event(win32con.MOUSEEVENTF_LEFTUP,x,y,0,0)

#for i in range(0,1000) :
#    time.sleep(.005)
#    click(i,i)

#var = raw_input("Enter something: ")

class EchoRequestHandler(SocketServer.BaseRequestHandler ):
    def handle(self):
        #print self.client_address, 'connected!'
        self.max_packet_size=1
        #print "Resolution sent: " + str(xpixels) + " " + str(ypixels)
        line = self.request[0].rstrip()
        returnsock = self.request[1]
        #pos = line.rstrip().split(None)
        if (line == "inite"):
          print "init received"
          #d = display.Display()
          #s = d.screen()

          xpixels = GetSystemMetrics(0)
          print(xpixels) ,
          ypixels = GetSystemMetrics(1)
          print(ypixels)
          click(xpixels/2,ypixels/2)
          returnsock.sendto(str(xpixels)+"x"+str(ypixels) + '\n', self.client_address)
          print "Resolution sent: " + str(xpixels) + " " + str(ypixels)
        else :
          pos = line.rstrip().split(None)
          #d = display.Display()
          #s = d.screen()
          #root = s.root
          print pos[0] + " "  +pos[1] + " " +pos[2]
          if pos[2] == "1" :
              print("click")
              clickleftbutton(int(pos[0]),int(pos[1]))
     
          else :
              click(int(pos[0]),int(pos[1]))

        

if __name__ == '__main__':
    print("Mouse event Test")
    for i in range(0,100):
        time.sleep(.005)
        click(i,i)
    ans = raw_input("start server ?? <Y/N>")
    #port = raw_input("Port Number: ")
    #address = raw_input("IpAddress: ")                     
    if ans == "y" or ans == "Y":
        server = SocketServer.UDPServer(("128.61.126.74", 50154), EchoRequestHandler)
        server.serve_forever()
    
    #server.close_request()

       
