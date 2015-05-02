# Canoe
A basic raft implementation. Intro to programming with akka actors.

Terms: Mark the beginning and end of an election and the reign of a leadership

## Membership

- Nodes will start up with a well known list of nodes.

## State Machine

### Follower

- Stay as leader so long as hearbeats received.
- onTimeout
  - Transition to coordinate state.

- onAppendEntries
  - If log does not contain logIndex and term, reject.

### Candidate
Timeout value should be chosen at random (eg. 10ms - 500ms) to prevent recurring
inconclusive elections.

- onEnter
  - Increment term
  - Add vote for oneself
  - Call election by sending RequestVote to all others in the group

- onReceiveVote
  - if majority achieved, transition to leader
  - if no majority yet, inc # votes received.

- onRequestVote
  - if current log more up to date than candidate, deny vote
    - logx > logy if x.lastIndex.term > y.lastIndex.term || if x.log.length > y.log.length

- onAppendEntries
  - if term in msg >= current term, transition to follower, set leader as sender of msg.

- onTimeout
  - Increment term and start a new election

### Leader

- State
  - lastCommittedIndex: highest index in log it knows to be committed.
  - nextIndex[]: list of nextIndex to write in each workers log

- onEnter
  - Init follower's nextIndex to local log index + 1.

- onClientRequest
  - Append to local log
  - send AppendEntries(currentTerm, lastCommittedIndex) to followers in parallel.

- onTimeout
  - Send heartbeats to followers.


## WAL

- logIndex : counter that increments per log entry

