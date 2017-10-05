echo "check below: if you see tcp6   0    0 :::2181   :::*  LISTEN"
echo "that means that zookeeper is running and looks fine"
echo "============================================================"
netstat -ant | grep :2181
