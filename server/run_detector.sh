#!/bin/bash

/Users/yoadfekete/Documents/projects/personal/blindspot-main/demo/detection_app/DetectionDemo/build/distributions/DetectionDemo-1.0-SNAPSHOT/bin/DetectionDemo demo.Main -s /Users/yoadfekete/Documents/projects/personal/blindspot/sshj/src/main/java/net -a sshj_extracted -c net.schmizz.sshj.SocketClient -m 'void connect(java.lang.String)' -t sshj_malicious_extracted --output graph.json --diff -b 3891366983 --draw
