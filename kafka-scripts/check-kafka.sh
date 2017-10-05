echo "check below: if you see tcp6   0    0 :::9092   :::*  LISTEN"
echo "that means that kafka is running and looks fine"
echo "============================================================"
netstat -ant | grep :9092
