#!/usr/bin/python
import sys, json, xmpp, random, string, MySQLdb, time
from threading import Timer

SERVER = 'gcm.googleapis.com'
PORT = 5235
USERNAME = "930098142781"
PASSWORD = "AIzaSyAbe1nI1AqwMleZGgFuLykmzjMV0mCInKw"
REGISTRATION_ID = "APA91bEy7U9VVDcD609PXjeJN4_TMyhz4aBO5dKaqNcDhBIAATG1-Hd73SQ-2xwZdbOpGKyuZAd0cZPOX6SOsI5z2fFNFdu21PRIJ1jB1uxtaeEz5WritIB0H7hHANcX8HrgZr3LQ07cBJ4A9QvBuMgwRAL0WxzNIrdUbQX_EFQXf9u7-3vU8Bg"

unacked_messages_quota = 100
send_queue = []

CONVERSATIONS = []

class Conversation:
  def __init__(self, people, chatHistory, cID):
    self.people = people
    self.messages = chatHistory
    self.id = cID
    self.synchronized = False
  def __str__(self):
    return "conv btwn " + self.people[0] + "," + self.people[1] + " messages: " + str(self.messages)


db = MySQLdb.connect(host="104.130.202.28",
  user="root",
  passwd="0vcg0nur193gp1s6", 
  db="chat")
cur = db.cursor()

def loadConversations():
  queryString = "SELECT * from conversations"
  cur.execute(queryString)
  for row in cur:
    cID = row[0]
    #print "cID is type ", type(cID), cID
    people = json.loads(row[1])
    #print "people is type ", type(people), people
    chatHistory = json.loads(row[2])
    #print "chatHistory is type ", type(chatHistory)
    conv = Conversation(people, chatHistory, cID)
    CONVERSATIONS.append(conv)

loadConversations()
print CONVERSATIONS[0].messages

def findOrCreate(username, contact):
  for c in CONVERSATIONS:
    if username in c.people and contact in c.people:
      print "found it! "
      return c
  return newConversation(username, contact)

def newConversation(username, contact):
  cur.execute("INSERT INTO conversations () VALUES () ")
  cID = db.insert_id()
  conv = Conversation([username, contact], [], cID)
  CONVERSATIONS.append(conv)
  print "new conversation"
  #print str(conv)
  return conv

def conversationID(conversation):
  return conversation.id

def syncData():
  #print 'syncing data'
  for conv in CONVERSATIONS:
    if not conv.synchronized:
      people = json.dumps(conv.people)
      #print "syncing for " + people
      history = json.dumps(conv.messages)
      cID = conv.id
      cur.execute("UPDATE conversations SET people=%s, messageLog=%s WHERE id=%s",(people, history, cID))
      # ^ probably not doing anything :(
      conv.synchronized = True

# Return a random alphanumerical id
def random_id():
  rid = ''
  for x in range(8): rid += random.choice(string.ascii_letters + string.digits)
  return rid

def message_callback(session, message):
  print 'message received.'
  global unacked_messages_quota
  gcm = message.getTags('gcm')
  if gcm:
    gcm_json = gcm[0].getData()
    msg = json.loads(gcm_json)
    print 'msg is ',
    print msg
    if not msg.has_key('message_type'):
      # Acknowledge the incoming message immediately.
      send({'to': msg['from'],
            'message_type': 'ack',
            'message_id': msg['message_id']})
      # Queue a response back to the server.
      if msg.has_key('from'):
        # Send a dummy echo response back to the app that sent the upstream message.
        username = msg['data']['username']
        contact = msg['data']['contact']
        print 'contact ' + contact
        print 'username ' + username
        conversation = findOrCreate(username, contact)
        if msg['data']['messageStatus'] == "new conversation":
          # opening conversation
          print 'new conversation'
          openConversation(conversation, username, contact, msg)
        elif msg['data']['messageStatus'] == "new message":
          # new message
          print 'new message'
          saveMessage(conversation, username, contact, msg)
          
    elif msg['message_type'] == 'ack' or msg['message_type'] == 'nack':
      unacked_messages_quota += 1

def openConversation(conversation, username, contact, msg):
  oldMessages = json.dumps(conversation.messages)
  #print oldMessages
  print type(oldMessages)
  send_queue.append({'to': msg['from'],
                     'message_id': random_id(),
                     'data': {'messageStatus':'new conversation', 
                     #'message': "start a convo with " + contact,
                     'username':username,
                     'contact':contact,
                     'message':oldMessages,
                     'conversationID': conversation.id
                     }})
  # send_queue.append({'to': msg['from'],
  #                    'message_id': random_id(),
  #                    'data': {'messageStatus':'new conversation', 
  #                    'conversationID':conversationID(conversation),
  #                    'username': username,
  #                    'contact': contact,
  #                    'prevMessages': oldMessages}})
                      # send all the previous messages so that they can reload

# called when username sends msg to contact. 
def saveMessage(conversation, username, contact, msg):
  message = msg['data']['message']
  curTime = time.time() * 1000 

  totalMessage = {'text':message, 'time':curTime, 'author':username,'conversation':conversation.id}
  conversation.messages.append(totalMessage)
  #print "chat history: " 
  #print conversation.messages
  conversation.synchronized = False
  # print conversation.messages
  # send_queue.append({'to': msg['from'],
  #                    'message_id': random_id(),
  #                    'data': {'messageStatus':'new message', 
  #                    'message': message,
  #                    'username':username,
  #                    'contact':contact}})

def send(json_dict):
  template = ("<message><gcm xmlns='google:mobile:data'>{1}</gcm></message>")
  client.send(xmpp.protocol.Message(
      node=template.format(client.Bind.bound[0], json.dumps(json_dict))))

def flush_queued_messages():
  global unacked_messages_quota
  while len(send_queue) and unacked_messages_quota > 0:
    senditem = send_queue.pop(0)
    print 'going to send', senditem
    send(senditem)
    unacked_messages_quota -= 1

client = xmpp.Client('gcm.googleapis.com', debug=['socket'])
client.connect(server=(SERVER,PORT), secure=1, use_srv=False)
auth = client.auth(USERNAME, PASSWORD)
if not auth:
  print 'Authentication failed!'
  sys.exit(1)

client.RegisterHandler('message', message_callback)

send_queue.append({'to': REGISTRATION_ID,
                   'message_id': 'reg_id',
                   'data': {'hello': 'world', "messageStatus":"starting up"}})

while True:
  client.Process(1)
  flush_queued_messages()
  syncData()
