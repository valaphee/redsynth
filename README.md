```
 ______           __ _______               __   __
|   __ \.-----.--|  |     __|.--.--.-----.|  |_|  |--.
|      <|  -__|  _  |__     ||  |  |     ||   _|     |
|___|__||_____|_____|_______||___  |__|__||____|__|__|
                             |_____|
```
![license](https://img.shields.io/badge/License-Apache_2.0-blue.svg)
![version](https://img.shields.io/badge/Version-0.0.1-darkred.svg)

Generate redstone circuits out of Verilog.

## Building a Blackbox
To use RedSynth you first have to build a blackbox, this is like an interface between Verilog and Minecraft,<br>
and will be used for simulation or for bus generation when using synthesis.

The main block of the blackbox is black concrete, this has no special property, but glues everything together,<br>
for the pins, white concrete is used, in simulations this can change to redstone blocks, when it is an output which is currently active<br>
and when they have other white concrete neighbors, they will form one single pin. For bus spacing light gray concrete
can be used.

When the blackbox is built, it has to be described with signs. Every blackbox must have the main sign, which is later used<br>
to start the simulation by right clicking, the content has to be the following:<br>
```
[RedSynth]
<filename>

(status, can either be running or failed)
```

The ports also have to be described:<br>
```
^, °, v, < or > (location of the port, relative to the sign)
<Verilog name>
<index> (default is 0)
<type> (default is self, can be either self, neighbor, value)
```

And voilà, your blackbox is finished.<br>
Now you can start your simulation, by clicking on the main sign.

## References
* http://www.csit-sun.pub.ro/~cpop/VerilogHDL_Tools/synver.pdf
* https://yosyshq.net/yosys/files/yosys_manual.pdf
