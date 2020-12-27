# DistributedSystemProject
<h2>Software Failure Tolerant and/or Highly Available Distributed Event Management System</h2>

  <h3>Overall Architecture</h3>
    <h4>Client</h4>
      <p>
        A client can be a customer or a manager. A customer may send requests to book or cancel an event, or view event schedule.A manager, in addition to those operations mentioned above, may add, delete events or view availability of events.Client communicates with Front End (FE) of the system to exchange messages. The communication is facilitated by CORBA standards.The client sends a request by invoking one of the services defined in Interface Repository of the system.
      </p>

<h4>Front End</h4>
      <p>

        FE is the only component that directly communicates with clients using CORBA standards. Concurrency is implemented to ensure efficient communication between the FE and clients.Moreover, it also communicates with a sequencer, which, in turn, communicates with replica managers (RMs) to exchange client requests and server responses. More importantly, FE is a gatekeeper that detects any software failure (software incorrectness) or server crash (server inavailability) based on server responses.In particular, if any of the replicas produces incorrect result, the FE informs all the RMs about that replica.If the same replica produces incorrect result for 3 consecutive client requests, then the RMs are informed to have the replica replaced with a correct one.If the FE does not receive the result from a replica within a reasonable time frame, it informs all RMs of a potential crash on that replica.The FE and the sequence communicate with each other via UDP protocol.

        FE is the only component that directly communicates with clients using CORBA standards. Concurrency is implemented to ensure efficient communication between the FE and clients.Moreover, it also communicates with a sequencer, which, in turn, communicates with replica managers (RMs) to exchange client requests and server responses</br>. More importantly, FE is a gatekeeper that detects any software failure (software incorrectness) or server crash (server inavailability) based on server responses.In particular, if any of the replicas produces incorrect result, the FE informs all the RMs about that replica.If the same replica produces incorrect result for 3 consecutive client requests, then the RMs are informed to have the replica replaced with a correct one.If the FE does not receive the result from a replica within a reasonable time frame, it informs all RMs of a potential crash on that replica.<br>The FE and the sequence communicate with each other via UDP protocol.

</p>


<h4>Sequencer</h4>
    <p>
    Sequencer acts as a middleware between front-end and replica managers of all the replicas and communicates via UDP protocol. It’s primary job is to generate sequence number based on a chronological order and assign it  to client’s request.The sequencer then multicast those requests to all RMs. It also forwards responses from RMs back to FE.</br>
    </p>
