# Tinker 学习





### MultiDex

1. multiDex，在art虚拟机上面默认支持。所以在android 21以上不需要额外进入multiDex依赖
2. 在android21以下时，MutliDex.Install的时候，会把除了主dex外其他dex也添加到dexPathList中，给classloader寻找类



