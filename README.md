[![Build Status](https://img.shields.io/travis/gsharma/leader-election/master.svg)](https://travis-ci.org/gsharma/leader-election)
[![Test Coverage](https://img.shields.io/codecov/c/github/gsharma/leader-election/master.svg)](https://codecov.io/github/gsharma/leader-election?branch=master)
[![Quality Gate](https://sonarcloud.io/api/project_badges/measure?project=com.github.leaderelection:leader-election&metric=alert_status)](https://sonarcloud.io/dashboard?id=com.github.leaderelection:leader-election)
[![Licence](https://img.shields.io/hexpm/l/plug.svg)](https://github.com/gsharma/leader-election/blob/master/LICENSE)

# Leader Election

## Bully Algorithm
Bully Algorithm is a type of monarchical leader election. 

### Assumptions
1. The system is synchronous.
2. Processes may fail at any time, including during execution of the algorithm.
3. There is a failure detector which detects failed processes.
4. A process fails by stopping and returns from failure by restarting.
5. Message delivery between processes is reliable.
6. Each process knows its own process id and address, and that of every other process.

### Algorithm
The algorithm uses the following message types:
1. Election Message: Sent to announce election
2. Answer (Alive) Message: Responds to the Election message
3. Coordinator (Victory) Message: Sent by winner of the election to announce victory

When a process P recovers from failure, or the failure detector indicates that the current coordinator has failed, P performs the following actions:
1. If P has the highest process id, it sends a Victory message to all other processes and becomes the new Coordinator. Otherwise, P broadcasts an Election message to all other processes with higher process IDs than itself.
2. If P receives no Answer after sending an Election message, then it broadcasts a Victory message to all other processes and becomes the Coordinator.
3. If P receives an Answer from a process with a higher ID, it sends no further messages for this election and waits for a Victory message. (If there is no Victory message after a period of time, it restarts the process at the beginning.)
4. If P receives an Election message from another process with a lower ID it sends an Answer message back and starts the election process at the beginning, by sending an Election message to higher-numbered processes.
5. If P receives a Coordinator message, it treats the sender as the coordinator.

### Implementation Notes
1. Let process ID be a Type 4 UUID. Note that UUIDs will need to be comparable.
2. As an invariant, the process ID does not change during the lifetime of a process.
3. The lifetime of a process will be denoted by an epoch stamped on all the members in a cluster.
4. From leader election to leader death, the cluster's epoch does not change. A new round bumps the cluster's epoch.
5. For improved reliability (a stretch goal), metadata of cluster members, leadership, epochs are/can be saved in persistent storage.
6. Satisfying assumption #1: TCP is used as the transport.
7. Satisfying assumption #2,6: addition/removal of cluster members will be broadcasted to all processes participating in leader election.
8. Satisfying assumption #3: a heartbeat-based failure detector is provided.
9. Satisfying assumption #4: processes obey fail-stop maxim.
10. Satisfying assumption #5: Blocking I/O is used in conjunction with TCP as the transport.

<this is a work in progress>

