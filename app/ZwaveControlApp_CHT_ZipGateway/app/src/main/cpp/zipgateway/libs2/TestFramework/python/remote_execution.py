import paramiko
import socket
import threading
import time
import re
import xunit

DEBUG = 0

class RemoteTestSession():
  def __init__(self, server, username='developer', password='1234', timeout=60, xmlfile=None):
    self.server = server
    self.xmlfile = xmlfile
    self.testResult = []
    self.testTime   = 0.0
    
    self.username = username
    self.password = password
    self.timeout  = timeout

    self.remotesession_stdin = None

    self.sshclient = paramiko.SSHClient()
    self.sshclient.set_missing_host_key_policy(paramiko.AutoAddPolicy())
    self.sshclient.connect(self.server, username=self.username, password=self.password)
    self.sftpsession = self.sshclient.open_sftp()
    
  def put(self, localFile, remoteFile):
    # Use SFTP module to upload file
    self.sftpsession.put(localFile, remoteFile)

  def executeTest(self, remoteFile):
    self.testResult = []
    consoleThread = RemoteTestConsoleThread(self, self.server, 11236)
    consoleThread.start()
    if DEBUG: print 'Running %s remotely' % remoteFile
    remote_cmd = './bin/start-700_platform.sh -k ~/%s -r 256' % (remoteFile)
    stdin, stdout, stderr = self.sshclient.exec_command(remote_cmd)
    consoleThread.join()
    
  def getTestResult(self):
    return self.testResult
       
  def getTestResultXml(self, testname):
    return xunit.TestSuite().fromUnity(self.testResult, name=testname, time=self.testTime, hostname=self.server)
       
  def setTestResult(self, testResult, time=0.0):
    self.testResult = testResult
    self.testTime   = time

  def endTest(self):
    remote_cmd = 'killall toplevel'
    stdin, stdout, stderr = self.sshclient.exec_command(remote_cmd)

  def getTimeout(self):
    return self.timeout
    
  def close(self):
    self.sftpsession.close()
    self.sshclient.close()
  
class TimeoutMonitorThread(threading.Thread):

  def __init__(self, sshsession):
      super(TimeoutMonitorThread, self).__init__()
      self.sshsession = sshsession
      self.timeout    = sshsession.getTimeout()
      self.starttime  = 0
      self.stoptime   = 0
      self.status     = 'stopped'

  def run(self):
    if DEBUG: print 'TimeoutMonitor started'
    self.starttime = time.time()
    self.timeouttime = self.starttime + self.timeout
    currentTime = time.time()
    while currentTime < self.timeouttime:
      time.sleep(1)
      currentTime = time.time()
    
    self.stoptime = time.time()
    if self.timeouttime and (currentTime >= self.timeouttime ):
      if DEBUG: print 'Timeout reached'
      self.status     = 'expired'
      self.sshsession.endTest()
    else:
      if DEBUG: print 'TimeoutMonitor stopped'
      self.status     = 'stopped'

  def resettimer(self):
    currentTime = time.time()
    self.timeouttime = currentTime + self.timeout

  def status(self):
      return self.status

  def timeElapsed(self):
      return self.stoptime - self.starttime

  def exit(self):
    self.timeouttime = 0

class RemoteTestConsoleThread(threading.Thread):

  def __init__(self, sshsession, host, port):
      super(RemoteTestConsoleThread, self).__init__()
      self.sshsession = sshsession
      self.host = host
      self.port = port

  def run(self):
    self.running = 5
    receive_buffer = ''
    timeoutThread = TimeoutMonitorThread(self.sshsession)
    timeoutThread.start()
    while self.running:
      try:
        self.socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self.socket.connect((self.host, self.port))
        if DEBUG: print 'Connected %s:%d' % (self.host, self.port)
        while self.running:
          try:
            if not self.running: break
            new_data = self.socket.recv(1024)
            if not new_data: break
            timeoutThread.resettimer()
            receive_buffer += new_data

            retval = re.search(r"\x04", new_data)
            if retval:
              if DEBUG: print 'Test completed'
              self.running = 0
              timeoutThread.exit()
              self.sshsession.endTest()

          except IOError as e:
            if DEBUG: print 'Connection has been closed / 2'
            self.running = 0

        self.socket.close()
      except IOError as e:
        if DEBUG: print 'Connection closed / 1'
      if self.running: self.running -= 1
      time.sleep(1)
    console_output = receive_buffer.splitlines()

    if timeoutThread.status == 'expired':
      summary = '%i Tests 1 Failures 0 Ignored' % ((len(console_output)+1), )
      console_output.append(':9999:Unknown:FAIL: Timeout')
      console_output.append('-----------------------')
      console_output.append(summary)
      console_output.append('FAIL')
    totaltime = timeoutThread.timeElapsed()
    self.sshsession.setTestResult(console_output, totaltime)

  def exit(self):
    if self.running == 1:
      self.running = 0
      try:
        eof_message = b''
        self.socket.shutdown(socket.SHUT_RDWR)
        self.socket.sendto(eof_message, (self.host, self.port))
      except IOError as e:
        if DEBUG: print 'Connection exit / 4'
      self.socket.close()
