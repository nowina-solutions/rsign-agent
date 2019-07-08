# RSign Agent

This agent helps to perform RSign requests. 

Each time it detects a file to be signed (file matching pattern inbound.filename.regex in the folder agent.directory.in), the file is uploaded to RSign. 
Once completion of upload, a UPLOAD file is created. 

Once the file is signed on RSign, the signed file is fetched and stored next to the original file. 

## Install

You can download the last release : https://github.com/nowina-solutions/rsign-agent/releases

The archive must be unzipped in the folder of your choice. 

* The private key from RSign must be stored in the file private.key
* User information must be defined in application.properties
* Directory to monitor must be defined in application.properties 

	agent.directory.in=C:/temp

	inbound.filename.regex=tobesigned-([a-z0-9])+.pdf
 

To register the Windows service, use

	rsign-agent.exe install
	
The service is then registered as a Windows Local Service and can be started / stopped from the administrative tools.  

## License

The agent is licensed under EUPL license. 

