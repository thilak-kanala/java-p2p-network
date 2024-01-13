# P2P File Sharing Software

## How To Use (Based on given configuration)

As specified in the project description, we choose to start the remote peers **manually**.
The machines SSHed into and starting of peers will vary based on configuration.
The compilation of the code and starting of peer is standard.

1. Establish remote connection

- Establish connection using SSH with `storm.cise.ufl.edu` server  

- VSCode Environment can be connected via SSH to the CISE servers

- Upload the code directory to the remote server via *Filezilla*

 `ssh gator_id@storm.cise.ufl.edu`

- Compile the code

 `javac PeerProcess.java`


- Connect to different machines (on different terminal windows) from within the remote CISE machine for different peers by SSHing into the appropriate machine mentioned in the `PeerInfo.cfg`

`ssh gator_id@lin114-01`  
`ssh gator_id@lin114-02`   
`ssh gator_id@lin114-03`   
`ssh gator_id@lin114-04`   
`ssh gator_id@lin114-05`   
`ssh gator_id@lin114-07`

- Start the remote peers on the different machine (terminals)

`java PeerProcess 1001`  
`java PeerProcess 1002`   
`java PeerProcess 1003`   
`java PeerProcess 1004`   
`java PeerProcess 1005`   
`java PeerProcess 1006`

The code will execute, generate logs, log files, and save the pieces of the trasnferred file in appropirate directories

---
> Project associated with University of Florida, Gainesville